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
    'major_high': '#FF1493',
    'major_low': '#00FA9A',
    'minor_high': '#FFA500',
    'minor_low': '#1E90FF',
    'bullish_bos': '#00FF00',
    'bearish_bos': '#FF0000',
    'accent': '#00D4FF',
    'background': '#0f1419',
    'card_background': '#1e2130'
}

# Chart styling
SWING_MARKERS = {
    'major_high': {'color': COLORS['major_high'], 'symbol': 'triangle-up', 'size': 20},
    'major_low': {'color': COLORS['major_low'], 'symbol': 'triangle-down', 'size': 20},
    'minor_high': {'color': COLORS['minor_high'], 'symbol': 'circle', 'size': 12},
    'minor_low': {'color': COLORS['minor_low'], 'symbol': 'circle', 'size': 12}
}

