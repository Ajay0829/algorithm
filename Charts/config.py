"""
Configuration settings for the trading dashboard
"""
import logging

# Logging configuration
logging.basicConfig(level=logging.WARNING)

# Performance optimization variables
PROCESSING_DELAY = 0.5  # seconds between candles
MAX_CANDLES_TO_DISPLAY = 1000
UPDATE_FREQUENCY = 100  # milliseconds
BATCH_SIZE = 1  # events to process per batch

# Kafka configuration
KAFKA_BOOTSTRAP_SERVERS = 'localhost:9092'
KAFKA_TOPICS = ['chart-annotations']

# Chart configuration
CHART_HEIGHT = '85vh'
MIN_CHART_HEIGHT = '600px'
INITIAL_CANDLES_TO_SHOW = 30

# Color scheme
COLORS = {
    'bullish_candle': '#26A69A',
    'bearish_candle': '#EF5350',
    'supply_zone': '#FF6B6B',
    'demand_zone': '#4ECDC4',
    'major_high': '#8B0000',  # Dark red for major highs
    'major_low': '#006400',   # Dark green for major lows
    'minor_high': '#FF6B6B',  # Light red for minor highs
    'minor_low': '#90EE90',   # Light green for minor lows
    'bullish_bos': '#00FF00',
    'bearish_bos': '#FF0000',
    'accent': '#00D4FF',
    'background': '#0f1419',
    'card_background': '#1e2130',
    # Trade colors
    'trade_entry': '#FFD700',  # Gold for entry point
    'trade_stop_loss': '#FF4444',  # Red for stop loss
    'trade_take_profit': '#44FF44',  # Green for take profit
    'trade_win': '#00FF88',  # Bright green for winning trades
    'trade_loss': '#FF4488',  # Red for losing trades
    'trade_pending': '#FFA500'  # Orange for pending trades
}

# Chart styling
SWING_MARKERS = {
    'major_high': {'color': COLORS['major_high'], 'symbol': 'triangle-up', 'size': 20},      # (^) dark red
    'major_low': {'color': COLORS['major_low'], 'symbol': 'triangle-down', 'size': 20},    # (v) dark green
    'minor_high': {'color': COLORS['minor_high'], 'symbol': 'star', 'size': 12},          # (*) light red
    'minor_low': {'color': COLORS['minor_low'], 'symbol': 'star', 'size': 12}             # (*) light green
}
