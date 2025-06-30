"""
Event processing logic for trading data
"""
import logging
from typing import Dict, Any
from data_models import data_state

logger = logging.getLogger(__name__)

class EventProcessor:
    """Processes different types of trading events"""

    @staticmethod
    def process_event(event: Dict[str, Any]) -> bool:
        """
        Process a single trading event

        Args:
            event: Event data containing type, action, and data

        Returns:
            bool: True if event was processed and chart needs update
        """
        etype = event['type']
        action = event['action']
        data = event['data']

        if data.get('timeframe') != '1d':
            return False

        try:
            if etype == 'candle':
                return EventProcessor._process_candle_event(action, data)
            elif etype == 'swing':
                return EventProcessor._process_swing_event(action, data)
            elif etype == 'zone':
                return EventProcessor._process_zone_event(action, data)
            elif etype == 'bos':
                return EventProcessor._process_bos_event(action, data)
            elif etype == 'trade':
                return EventProcessor._process_trade_event(action, data)
            elif etype == 'liquidity':
                return EventProcessor._process_liquidity_event(action, data)
            else:
                logger.warning(f"Unknown event type: {etype}")
                return False

        except Exception as e:
            logger.error(f"Error processing {etype} event: {str(e)}")
            return False

    @staticmethod
    def _process_candle_event(action: str, data: Dict[str, Any]) -> bool:
        """Process candle events"""
        if action == 'created':
            return data_state.add_candle(data)
        elif action == 'updated':
            return data_state.update_candle(data)
        elif action == 'deleted':
            return data_state.delete_candle(data.get('timestamp'))
        else:
            logger.warning(f"Unknown candle action: {action}")
            return False

    @staticmethod
    def _process_swing_event(action: str, data: Dict[str, Any]) -> bool:
        """Process swing point events"""
        if action == 'deleted':
            return data_state.delete_swing(
                data.get('timestamp'),
                data.get('swingType')
            )
        else:  # created or updated
            return data_state.update_swing(data)

    @staticmethod
    def _process_zone_event(action: str, data: Dict[str, Any]) -> bool:
        """Process supply/demand zone events"""
        if action == 'deleted':
            return data_state.delete_zone(
                data.get('startTs'),
                data.get('endTs'),
                data.get('zoneType')
            )
        else:  # created or updated
            return data_state.update_zone(data)

    @staticmethod
    def _process_bos_event(action: str, data: Dict[str, Any]) -> bool:
        """Process break of structure events"""
        if action == 'deleted':
            return data_state.delete_bos(
                data.get('start'),
                data.get('end'),
                data.get('bosType')
            )
        else:  # created or updated
            return data_state.update_bos(data)

    @staticmethod
    def _process_trade_event(action: str, data: Dict[str, Any]) -> bool:
        """Process trade events"""
        if action == 'executed':
            return data_state.add_trade(data)
        elif action == 'updated':
            return data_state.update_trade(data)
        elif action == 'canceled':
            return data_state.cancel_trade(data.get('id'))
        else:
            logger.warning(f"Unknown trade action: {action}")
            return False

    @staticmethod
    def _process_liquidity_event(action: str, data: Dict[str, Any]) -> bool:
        """Process liquidity events (created, updated, swept, deleted)"""
        try:
            if action == 'created':
                # Add creation timestamp and action for visualization
                liquidity_data = data.copy()
                liquidity_data['action'] = 'created'
                return data_state.update_liquidity(liquidity_data)

            elif action == 'updated':
                # Update existing liquidity zone
                liquidity_data = data.copy()
                liquidity_data['action'] = 'updated'
                return data_state.update_liquidity(liquidity_data)

            elif action == 'swept':
                # Mark as swept before deletion (different visual treatment)
                liquidity_data = data.copy()
                liquidity_data['action'] = 'swept'
                # Keep it briefly for swept visualization, then remove
                data_state.update_liquidity(liquidity_data)
                return True

            elif action == 'deleted':
                # Remove from chart (market invalidation)
                return data_state.delete_liquidity(data)

            else:
                logger.warning(f"Unknown liquidity action: {action}")
                return False

        except Exception as e:
            logger.error(f"Error processing liquidity {action} event: {str(e)}")
            return False

# Global event processor instance
event_processor = EventProcessor()
