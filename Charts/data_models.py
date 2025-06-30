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
        self.trades: List[Dict[str, Any]] = []
        self.liquidities: List[Dict[str, Any]] = []  # New liquidity state

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
        self.trades.clear()
        self.liquidities.clear()  # Clear liquidities too
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
        """Update break of structure safely"""
        key = (bos_data.get('start'), bos_data.get('end'), bos_data.get('bosType'))

        # Check if BOS already exists and update it
        for idx, existing_bos in enumerate(self.boss):
            existing_key = (existing_bos.get('start'), existing_bos.get('end'), existing_bos.get('bosType'))
            if existing_key == key:
                # Update existing BOS in-place
                self.boss[idx].update(bos_data)
                self.chart_needs_full_rebuild = True
                return True

        # If BOS not found, add it as new
        self.boss.append(bos_data)
        self.chart_needs_full_rebuild = True
        return True

    def add_bos(self, bos_data: Dict[str, Any]) -> bool:
        """Add a new BOS only if it doesn't exist"""
        key = (bos_data.get('start'), bos_data.get('end'), bos_data.get('bosType'))

        # Check if BOS already exists
        for existing_bos in self.boss:
            existing_key = (existing_bos.get('start'), existing_bos.get('end'), existing_bos.get('bosType'))
            if existing_key == key:
                # BOS already exists, don't add duplicate
                return False

        # Add new BOS
        self.boss.append(bos_data)
        self.chart_needs_full_rebuild = True
        return True

    def add_trade(self, trade_data: Dict[str, Any]) -> bool:
        """Add a new trade only if it doesn't exist"""
        trade_id = trade_data.get('id')

        # Check if trade already exists
        for existing_trade in self.trades:
            if existing_trade.get('id') == trade_id:
                # Trade already exists, don't add duplicate
                return False

        # Add new trade
        self.trades.append(trade_data)
        self.chart_needs_full_rebuild = True
        return True

    def update_trade(self, trade_data: Dict[str, Any]) -> bool:
        """Update an existing trade (e.g., when trade result is known)"""
        trade_id = trade_data.get('id')

        # Find and update existing trade
        for idx, trade in enumerate(self.trades):
            if trade.get('id') == trade_id:
                # Update the existing trade with new data
                self.trades[idx].update(trade_data)
                self.chart_needs_full_rebuild = True
                return True

        # If trade not found, add it as new trade
        self.trades.append(trade_data)
        self.chart_needs_full_rebuild = True
        return True

    def update_liquidity(self, liquidity_data: Dict[str, Any]) -> bool:
        """Update liquidity zones"""
        timestamp = liquidity_data.get('candleTimestamp')
        liquidity_type = liquidity_data.get('liquidityType')
        price = liquidity_data.get('price')
        stock_symbol = liquidity_data.get('stockSymbol')

        # Create unique key for liquidity identification
        key = (timestamp, liquidity_type, stock_symbol, price)

        # Remove existing liquidity with same key
        self.liquidities[:] = [l for l in self.liquidities if
                              (l.get('candleTimestamp'), l.get('liquidityType'),
                               l.get('stockSymbol'), l.get('price')) != key]

        # Add new liquidity
        self.liquidities.append(liquidity_data)
        return True

    def delete_liquidity(self, liquidity_data: Dict[str, Any]) -> bool:
        """Delete a specific liquidity zone"""
        timestamp = liquidity_data.get('candleTimestamp')
        liquidity_type = liquidity_data.get('liquidityType')
        price = liquidity_data.get('price')
        stock_symbol = liquidity_data.get('stockSymbol')

        key = (timestamp, liquidity_type, stock_symbol, price)
        old_count = len(self.liquidities)

        self.liquidities[:] = [l for l in self.liquidities if
                              (l.get('candleTimestamp'), l.get('liquidityType'),
                               l.get('stockSymbol'), l.get('price')) != key]

        return len(self.liquidities) != old_count
# Global data state instance
data_state = DataState()
