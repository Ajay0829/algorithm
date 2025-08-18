package com.market.streamline.service;

import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ZoneMetricsService {
    private final ZoneRepository zoneRepository;
    private final CandleRepository candleRepository;

    public ZoneMetricsService(ZoneRepository zoneRepository,
                              CandleRepository candleRepository) {
        this.zoneRepository = zoneRepository;
        this.candleRepository = candleRepository;
    }

    public void updateZoneMetrics(CandleEntity candleEntity) {
        List<Zone> zones = zoneRepository.findZonesWithMissingMetrics(
                candleEntity.getStockSymbol(), candleEntity.getTimeframe());

        for (Zone zone : zones) {
            List<CandleEntity> futureCandles = candleRepository.getCandlesBetweenTimestamps(
                    zone.getStockSymbol(),
                    zone.getTimeframe(),
                    zone.getIdentifiedAt(),
                    candleEntity.getCandleTimestamp()
            );
            if (futureCandles == null || futureCandles.isEmpty()) continue;
            futureCandles.sort(Comparator.comparing(CandleEntity::getCandleTimestamp));

            MetricsResult r = computeMetrics(futureCandles, zone);
            apply(zone, r, false);
        }
    }

    public void updateZoneMetricsWithProvisionalTouch(Zone zone, CandleEntity lastCandle, double entryPrice) {
        List<CandleEntity> window = candleRepository.getCandlesBetweenTimestamps(
                zone.getStockSymbol(), zone.getTimeframe(), zone.getIdentifiedAt(), lastCandle.getCandleTimestamp().minusHours(1));
        if (window == null || window.isEmpty()) return;
        window.sort(Comparator.comparing(CandleEntity::getCandleTimestamp));

        // Build a synthetic touch candle using only what we truly know: the entry price
        CandleEntity touch = new CandleEntity(
                zone.getStockSymbol(),
                zone.getTimeframe(),
                lastCandle.getCandleTimestamp(),
                entryPrice, entryPrice, entryPrice, entryPrice,
                averageVolume(window)
        );
        window.add(touch);

        MetricsResult r = computeMetrics(window, zone);
        r.finalized = false; // provisional call should not close the impulse
        apply(zone, r, true);
    }

    private void apply(Zone zone, MetricsResult r, boolean provisional) {
        zone.setHalfLife(r.halfLifeBars);                 // bars (ceil of fractional)
        zone.setResilience(r.resilience);                 // 0..1 (max retrace in 30 bars)
        zone.setSameDirectionExtremeMovement(r.sameDirectionExtremeMovement); // >=0 (can exceed 1 if extension)
        // Only toggle impulseExtending on non-provisional updates
        if (!provisional) {
            zone.setImpulseExtending(!r.finalized); // true while open, false when finalized
        }
        zoneRepository.save(zone);
    }

    private MetricsResult computeMetrics(List<CandleEntity> candles, Zone zone) {
        final double EPS = 1e-9;

        final boolean isDemand = "DEMAND".equalsIgnoreCase(zone.getZoneType());
        final double farPoint = zone.getFarPoint();
        final double impulseStrengthPct = zone.getStrength(); // % move recorded at identification

        // Theoretical impulse end from the zone record (used for half-level & normalization)
        final double impulseEnd = isDemand
                ? farPoint + (impulseStrengthPct * farPoint / 100.0)
                : farPoint - (impulseStrengthPct * farPoint / 100.0);
        final double deltaImp = Math.max(Math.abs(impulseEnd - farPoint), EPS);

        final int n = candles.size();
        double[] close = new double[n];
        for (int i = 0; i < n; i++) {
            CandleEntity c = candles.get(i);
            close[i] = nz(c.getClose());
        }

        // ---------------- Half-life (CLOSE-based, no continuation): ----------------
        // Scan from bar 1 up to bar 30 for the first close that crosses the 50% retrace level.
        final double halfLevel = farPoint + 0.5 * (impulseEnd - farPoint);
        int halfLifeBars = 30; // cap if never crosses within first 30 bars
        for (int i = 1; i < n; i++) {
            boolean crossed = isDemand ? (close[i] <= halfLevel)   // up-impulse -> retrace down
                    : (close[i] >= halfLevel);  // down-impulse -> retrace up
            if (crossed) { halfLifeBars = i + 1; break; }
        }

        // ---------------- Resilience (0..1): max retracement in 30 bars post impulse ----------------
        double maxRetrace = 0.0;
        for (int i = 1; i < n; i++) {
            double retrace = isDemand
                    ? (impulseEnd - close[i]) / deltaImp   // move back toward base
                    : (close[i] - impulseEnd) / deltaImp;
            if (!Double.isFinite(retrace)) retrace = 0.0;
            if (retrace < 0.0) retrace = 0.0; // still extending
            if (retrace > 1.0) retrace = 1.0; // over-retraced
            if (retrace > maxRetrace) maxRetrace = retrace;
        }
        double resilience = maxRetrace; // 0..1

        // ---------------- Same-direction extreme movement (CLOSE-based): ----------------
        double extremeClose = close[0];
        for (int i = 1; i < n; i++) {
            extremeClose = isDemand ? Math.max(extremeClose, close[i])
                    : Math.min(extremeClose, close[i]);
        }
        // Unbounded above (tracks max same-direction extension); always >= 0
        double sameDirectionExtremeMovement = Math.max(1.0, Math.abs(extremeClose - farPoint) / deltaImp);

        // ---------------- Finalization: near-point taken out or 30 bars elapsed ----------------
        boolean baseTouched = isDemand ? (close[n - 1] <= nz(zone.getFarPoint()))
                : (close[n - 1] >= nz(zone.getFarPoint()));
        boolean finalized = baseTouched || n >= 30;

        MetricsResult r = new MetricsResult(finalized);
        r.halfLifeBars = halfLifeBars;
        r.resilience = resilience;
        r.sameDirectionExtremeMovement = sameDirectionExtremeMovement;
        return r;
    }

    private static double nz(Double v) { return v == null ? 0.0 : v; }

    private static double averageVolume(List<CandleEntity> candles) {
        if (candles == null || candles.isEmpty()) return 0.0;
        double s = 0.0; for (CandleEntity c : candles) s += nz(c.getVolume());
        return s / candles.size();
    }

    private static class MetricsResult {
        boolean finalized;
        int halfLifeBars;
        double resilience;               // 0..1 (max retrace within 30 bars)
        double sameDirectionExtremeMovement; // >=0 (can exceed 1 if extension)
        MetricsResult(boolean finalized) { this.finalized = finalized; }
    }
}
