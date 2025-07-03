package com.market.streamline.service;

import com.market.streamline.entity.structure.BreakOfStructure;
import com.market.streamline.entity.structure.CandleEntity;
import com.market.streamline.entity.structure.SwingPoint;
import com.market.streamline.entity.zone.Zone;
import com.market.streamline.repository.BreakOfStructureRepository;
import com.market.streamline.repository.CandleRepository;
import com.market.streamline.repository.ZoneRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ZoneService {

    private final BreakOfStructureRepository breakOfStructureRepository;
    private final ZoneRepository zoneRepository;
    private final CandleRepository candleRepository;

    public ZoneService(BreakOfStructureRepository breakOfStructureRepository, ZoneRepository zoneRepository, CandleRepository candleRepository) {
        this.breakOfStructureRepository = breakOfStructureRepository;
        this.zoneRepository = zoneRepository;
        this.candleRepository = candleRepository;
    }

    // Instead of using BOS, use impulse to identify zones
    // Use BOS on the lower timeframe to identify zones on the higher timeframe
    public void identifyZone(BreakOfStructure bos) {
        // Logic to identify a zone based on the break of structure.

        LocalDateTime to = bos.getCandleTimestamp();
        LocalDateTime from = bos.getStrongSwingPoint().getCandleTimestamp();

        String lowerTimeframe;
        if (Objects.equals(bos.getTimeframe(), "1d")) {
            lowerTimeframe = "1h";
        } else if (Objects.equals(bos.getTimeframe(), "1h")) {
            lowerTimeframe = "15m";
        } else {
            return;
        }

        List<BreakOfStructure> lowerTimeFrameBosList = breakOfStructureRepository.findByStockSymbolAndTimeframeAndCandleTimestampBetweenOrderByCandleTimestampDesc(
                bos.getStockSymbol(),
                lowerTimeframe,
                from.minusMinutes(60),
                to.plusMinutes(60)
        );

        if (lowerTimeFrameBosList.isEmpty()) {
            return;
        }

        BreakOfStructure lowerTimeframeBOSFinal = new BreakOfStructure();

        for (BreakOfStructure lowerTimeFrameBos : lowerTimeFrameBosList) {
            if (Objects.equals(bos.getDirection(), lowerTimeFrameBos.getDirection()) &&
                lowerTimeFrameBos.getWeakSwingPoint().getIsMajor() &&
                lowerTimeFrameBos.getStrongSwingPoint().getIsMajor()) {
                lowerTimeframeBOSFinal = lowerTimeFrameBos;
            } else if (Objects.equals(bos.getDirection(), lowerTimeFrameBos.getDirection())){
                lowerTimeframeBOSFinal = lowerTimeFrameBos;
                break;
            } else {
                lowerTimeframeBOSFinal = lowerTimeFrameBos;
                break;
            }
        }
        if (lowerTimeframeBOSFinal.getId() == null) {
            return;
        }
        Zone zone = getZone(bos, lowerTimeframeBOSFinal);
        zoneRepository.save(zone);
    }

    private Zone getZone(BreakOfStructure bos, BreakOfStructure bosLowerTimeframe) {
        double zoneStrength = 0.0, zoneVolume = 0.0;
        CandleEntity candleEntity = mapToHigherTimeframe(bosLowerTimeframe.getStrongSwingPoint(), bos);
        boolean isDemand = bos.getDirection().equals("BULLISH");
        return new Zone(
                bos.getStockSymbol(),
                bos.getTimeframe(),
                candleEntity.getCandleTimestamp(),
                isDemand ? "DEMAND" : "SUPPLY",
                isDemand ? candleEntity.getHigh() : candleEntity.getLow(),
                isDemand ? candleEntity.getLow() : candleEntity.getHigh(),
                "ACTIVE",
                zoneVolume,
                zoneStrength,
                null,
                0,
                candleEntity.getCandleTimestamp()

        );
    }

    // Helper to map a lower timeframe candle timestamp to the higher timeframe candle timestamp that contains it
    private CandleEntity mapToHigherTimeframe(SwingPoint ltfSwingPoint, BreakOfStructure bos) {
        LocalDateTime ltfCandleTimestamp = ltfSwingPoint.getCandleTimestamp();
        CandleEntity ltfCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampOrderByCandleTimestampDesc(
                bos.getStockSymbol(),
                ltfSwingPoint.getTimeframe(),
                ltfCandleTimestamp
        );
        String higherTimeframe = bos.getTimeframe();
        CandleEntity enclosingCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanEqualOrderByCandleTimestampDesc(
                bos.getStockSymbol(),
                higherTimeframe,
                ltfCandleTimestamp
        );

        if (enclosingCandle == null) {
            throw new IllegalArgumentException("No enclosing HTF candle found.");
        }

        CandleEntity previousCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanOrderByCandleTimestampDesc(
                bos.getStockSymbol(),
                higherTimeframe,
                ltfCandleTimestamp
        );

        if (previousCandle == null) {
            throw new IllegalArgumentException("No previous HTF candle found before impulse.");
        }

        // choose between previousCandle and enclosingCandle based on the logic

        if (contains(ltfCandle, enclosingCandle) && contains(ltfCandle, previousCandle)) {
            if (bos.getDirection().equals("BULLISH")) {
                // Find the candle with lowest high for demand zone
                if (enclosingCandle.getHigh() <= previousCandle.getHigh()) {
                    return enclosingCandle;
                } else {
                    return previousCandle;
                }
            } else {
                if (enclosingCandle.getLow() >= previousCandle.getLow()) {
                    return enclosingCandle;
                } else {
                    return previousCandle;
                }
            }
        } else if (contains(ltfCandle, enclosingCandle)) {
            return enclosingCandle;
        } else if (contains(ltfCandle, previousCandle)) {
            return previousCandle;
        } else {
            // Ideally this should not happen, but if it does, return the enclosingCandle
            return enclosingCandle;
        }
    }

    boolean contains(CandleEntity ltfCandle, CandleEntity htfCandle) {
        return (ltfCandle.getLow() >= htfCandle.getLow() && ltfCandle.getHigh() <= htfCandle.getHigh());
    }

    // Check for taps, if there is an active trade ignore, update no of taps, strength
    public Zone updateZoneStrength(CandleEntity candleEntity) {
        return null;
    }

    // New swing point is formed, check if this could increase the strength of the zone
    public Zone updateZoneStrength(SwingPoint swingPoint) {
        return null;
    }

    public Optional<Zone> fetchNearbyValidZone(CandleEntity candleEntity) {
        // Logic to fetch a nearby valid zone based on the candle entity.
        return Optional.empty();
    }
}
