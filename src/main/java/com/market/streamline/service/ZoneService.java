package com.market.streamline.service;

import com.market.external.polygon.dto.Candle;
import com.market.streamline.entity.BreakOfStructure;
import com.market.streamline.entity.CandleEntity;
import com.market.streamline.entity.SwingPoint;
import com.market.streamline.entity.Zone;
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
        double zoneNearPoint, zoneFarPoint, zoneStrength, zoneVolume;
        String zoneType;

        if (bos.getDirection().equals("BULLISH")) {
            zoneNearPoint = bosLowerTimeframe.getWeakSwingPoint().getPrice();
            zoneFarPoint = bosLowerTimeframe.getStrongSwingPoint().getPrice();
            zoneType = "DEMAND";
            zoneStrength = 0.0;
            zoneVolume = 0.0;
        } else {
            zoneNearPoint = bosLowerTimeframe.getWeakSwingPoint().getPrice();
            zoneFarPoint = bosLowerTimeframe.getStrongSwingPoint().getPrice();
            zoneType = "SUPPLY";
            zoneStrength = 0.0;
            zoneVolume = 0.0;
        }

        Zone zone = new Zone(
                bos.getStockSymbol(),
                bos.getTimeframe(),
                mapToHigherTimeframe(bosLowerTimeframe.getStrongSwingPoint().getCandleTimestamp(), bos),
                zoneType,
                zoneNearPoint,
                zoneFarPoint,
                "ACTIVE",
                zoneVolume,
                zoneStrength
        );
        return zone;
    }

    // Helper to map a lower timeframe candle timestamp to the higher timeframe candle timestamp that contains it
    private LocalDateTime mapToHigherTimeframe(LocalDateTime lowerTimestamp, BreakOfStructure bos) {
        String higherTimeframe = bos.getTimeframe();
        CandleEntity beforeCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampLessThanEqualOrderByCandleTimestampDesc(
                bos.getStockSymbol(),
                higherTimeframe,
                lowerTimestamp
        );

        CandleEntity afterCandle = candleRepository.findTopByStockSymbolAndTimeframeAndCandleTimestampGreaterThanOrderByCandleTimestampAsc(
                bos.getStockSymbol(),
                higherTimeframe,
                lowerTimestamp
        );

        if (encloses(lowerTimestamp, beforeCandle.getCandleTimestamp(), higherTimeframe)) {
            return beforeCandle.getCandleTimestamp();
        } else if (encloses(lowerTimestamp, afterCandle.getCandleTimestamp(), higherTimeframe)) {
            return afterCandle.getCandleTimestamp();
        } else {
            throw new IllegalArgumentException("No higher timeframe candle found for the given lower timeframe timestamp.");
        }

    }

    boolean encloses(LocalDateTime lowerTimestamp, LocalDateTime higherTimestamp, String higherTimeframe) {
        // Assuming higherTimestamp is the start of the higher timeframe candle
        long durationInMinutes;
        if (higherTimeframe.equals("1h")) {
            durationInMinutes = 60;
        } else if (higherTimeframe.equals("15m")) {
            durationInMinutes = 15;
        } else if (higherTimeframe.equals("1d")) {
            durationInMinutes = 1440; // 24 hours
        } else {
            throw new IllegalArgumentException("Unsupported higher timeframe: " + higherTimeframe);
        }

        return lowerTimestamp.isAfter(higherTimestamp.minusMinutes(1)) && lowerTimestamp.isBefore(higherTimestamp.plusMinutes(durationInMinutes));
    }

    public boolean invalidateZones(CandleEntity candleEntity) {
        return true;
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
