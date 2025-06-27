"""
Data models and state management for the trading dashboard
"""
from collections import deque
from typing import List, Dict, Any
import time
from config import PROCESSING_DELAY, MAX_CANDLES_TO_DISPLAY

class DataState:
    """Manages all the trading data state"""

    def __init__(self):
        self.candles: List[Dict[str, Any]] = []
        self.rendered_candles: List[Dict[str, Any]] = []
        self.all_events_buffer: deque = deque()
        self.swings: List[Dict[str, Any]] = []
        self.zones: List[Dict[str, Any]] = []
        self.boss: List[Dict[str, Any]] = []

        # Performance tracking
        self.last_processed_time = 0
        self.chart_needs_full_rebuild = False
        self.last_candle_count = 0

    def should_process_next_event(self) -> bool:
        """Rate limiting for event processing to simulate real-time delays"""
        current_time = time.time()
        if current_time - self.last_processed_time >= PROCESSING_DELAY:
            self.last_processed_time = current_time
            return True
        return False

    def clear_all_data(self):
        """Clear all trading data"""
        self.candles.clear()
        self.rendered_candles.clear()
        self.swings.clear()
        self.zones.clear()
        self.boss.clear()
        self.all_events_buffer.clear()
        self.chart_needs_full_rebuild = True

    def add_candle(self, candle_data: Dict[str, Any]) -> bool:
        """Add a new candle to the data"""
        ts = candle_data.get('timestamp')

        # Add to rendered candles if not already present
        if not any(c.get('timestamp') == ts for c in self.rendered_candles):
            self.rendered_candles.append(candle_data)
            if len(self.rendered_candles) > MAX_CANDLES_TO_DISPLAY:
                self.rendered_candles.pop(0)

        # Add to all candles if not already present
        if not any(c.get('timestamp') == ts for c in self.candles):
            self.candles.append(candle_data)
            return True

        return False

    def update_candle(self, candle_data: Dict[str, Any]) -> bool:
        """Update an existing candle"""
        ts = candle_data.get('timestamp')
        updated = False

        # Update in rendered candles
        for idx, c in enumerate(self.rendered_candles):
            if c.get('timestamp') == ts:
                self.rendered_candles[idx] = candle_data
                updated = True
                break

        # Update in all candles
        for idx, c in enumerate(self.candles):
            if c.get('timestamp') == ts:
                self.candles[idx] = candle_data
                break

        return updated

    def delete_candle(self, timestamp) -> bool:
        """Delete a candle by timestamp"""
        old_count = len(self.rendered_candles)
        self.rendered_candles[:] = [c for c in self.rendered_candles if c.get('timestamp') != timestamp]
        self.candles[:] = [c for c in self.candles if c.get('timestamp') != timestamp]

        if len(self.rendered_candles) != old_count:
            self.chart_needs_full_rebuild = True
            return True
        return False

    def update_swing(self, swing_data: Dict[str, Any]) -> bool:
        """Update swing points"""
        ts = swing_data.get('timestamp')
        swing_type = swing_data.get('swingType')
        key = (ts, swing_type)

        # Remove existing swing with same key
        self.swings[:] = [s for s in self.swings if (s.get('timestamp'), s.get('swingType')) != key]
        self.swings.append(swing_data)
        self.chart_needs_full_rebuild = True
        return True

    def delete_swing(self, timestamp, swing_type) -> bool:
        """Delete a swing point"""
        key = (timestamp, swing_type)
        old_count = len(self.swings)
        self.swings[:] = [s for s in self.swings if (s.get('timestamp'), s.get('swingType')) != key]

        if len(self.swings) != old_count:
            self.chart_needs_full_rebuild = True
            return True
        return False

    def update_zone(self, zone_data: Dict[str, Any]) -> bool:
        """Update supply/demand zones"""
        key = (zone_data.get('startTs'), zone_data.get('endTs'), zone_data.get('zoneType'))

        # Remove existing zone with same key
        self.zones[:] = [z for z in self.zones if (z.get('startTs'), z.get('endTs'), z.get('zoneType')) != key]
        self.zones.append(zone_data)
        self.chart_needs_full_rebuild = True
        return True

    def delete_zone(self, start_ts, end_ts, zone_type) -> bool:
        """Delete a zone"""
        key = (start_ts, end_ts, zone_type)
        old_count = len(self.zones)
        self.zones[:] = [z for z in self.zones if (z.get('startTs'), z.get('endTs'), z.get('zoneType')) != key]

        if len(self.zones) != old_count:
            self.chart_needs_full_rebuild = True
            return True
        return False

    def update_bos(self, bos_data: Dict[str, Any]) -> bool:
        """Update break of structure"""
        key = (bos_data.get('start'), bos_data.get('end'), bos_data.get('bosType'))

        # Remove existing BOS with same key
        self.boss[:] = [b for b in self.boss if (b.get('start'), b.get('end'), b.get('bosType')) != key]
        self.boss.append(bos_data)
        self.chart_needs_full_rebuild = True
        return True

    def delete_bos(self, start, end, bos_type) -> bool:
        """Delete a break of structure"""
        key = (start, end, bos_type)
        old_count = len(self.boss)
        self.boss[:] = [b for b in self.boss if (b.get('start'), b.get('end'), b.get('bosType')) != key]

        if len(self.boss) != old_count:
            self.chart_needs_full_rebuild = True
            return True
        return False

# Global data state instance
data_state = DataState()
