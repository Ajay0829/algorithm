-- Table: candles
CREATE TABLE IF NOT EXISTS candles (
    id SERIAL PRIMARY KEY,
    candle_timestamp TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    close DOUBLE PRECISION,
    high DOUBLE PRECISION,
    low DOUBLE PRECISION,
    open DOUBLE PRECISION,
    stock_symbol VARCHAR(255) NOT NULL,
    timeframe VARCHAR(255) NOT NULL,
    volume DOUBLE PRECISION,
    UNIQUE (stock_symbol, timeframe, candle_timestamp)
);

CREATE INDEX IF NOT EXISTS idx_candles_symbol_timeframe_ts
    ON candles (stock_symbol, timeframe, candle_timestamp);


-- Table: swing_points
CREATE TABLE IF NOT EXISTS swing_points (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    price DOUBLE PRECISION,
    swing_type VARCHAR(8), -- HIGH or LOW
    confirmed BOOLEAN DEFAULT FALSE,
    is_major BOOLEAN DEFAULT FALSE,
    UNIQUE (stock_symbol, timeframe, candle_timestamp, swing_type)
);

CREATE INDEX IF NOT EXISTS idx_swing_points_symbol_timeframe_ts
    ON swing_points (stock_symbol, timeframe, candle_timestamp);


-- Table: bos (Break of Structure)
CREATE TABLE IF NOT EXISTS bos (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    direction VARCHAR(16), -- BULLISH, BEARISH
    type VARCHAR(16),
    weak_swing_point INTEGER REFERENCES swing_points(id) ON DELETE CASCADE,
    strong_swing_point INTEGER REFERENCES swing_points(id) ON DELETE CASCADE,
    bos_volume DOUBLE PRECISION,
    UNIQUE (stock_symbol, timeframe, candle_timestamp, type)
);

CREATE INDEX IF NOT EXISTS idx_bos_symbol_timeframe_ts
    ON bos (stock_symbol, timeframe, candle_timestamp);


-- Table: trends
CREATE TABLE IF NOT EXISTS trends (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    type VARCHAR(16), -- e.g., UP, DOWN, SIDEWAYS
    strength DOUBLE PRECISION,
    strong_swing_point_id INTEGER REFERENCES swing_points(id) ON DELETE CASCADE,
    UNIQUE (stock_symbol, timeframe, candle_timestamp)
);

CREATE INDEX IF NOT EXISTS idx_trends_symbol_timeframe_ts
    ON trends (stock_symbol, timeframe, candle_timestamp);


-- Table: market indicators
CREATE TABLE IF NOT EXISTS market_indicators (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    average_volatility DOUBLE PRECISION,
    average_volume DOUBLE PRECISION NOT NULL,
    rsi_14 DOUBLE PRECISION,
    UNIQUE (stock_symbol, timeframe)
);

CREATE INDEX IF NOT EXISTS idx_market_indicators_symbol_timeframe
    ON market_indicators (stock_symbol, timeframe);


-- Table: zones
CREATE TABLE IF NOT EXISTS zones (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    near_point DOUBLE PRECISION,
    far_point DOUBLE PRECISION,
    type VARCHAR(16),
    volume DOUBLE PRECISION,
    strength DOUBLE PRECISION,
    no_of_taps INTEGER,
    strong_swing_point_id INTEGER REFERENCES swing_points(id) ON DELETE CASCADE,
    zone_type VARCHAR(16), -- SUPPLY, DEMAND (if you use this instead of type)
    identified_at TIMESTAMP,
    UNIQUE (stock_symbol, timeframe, candle_timestamp, zone_type)
);
CREATE INDEX IF NOT EXISTS idx_zones_symbol_timeframe_ts
    ON zones (stock_symbol, timeframe, candle_timestamp);


-- Table: liquidity
CREATE TABLE IF NOT EXISTS liquidity (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    type VARCHAR(16), -- BUY, SELL
    price DOUBLE PRECISION,
    no_of_equals INTEGER,
    UNIQUE (stock_symbol, timeframe, candle_timestamp, type)
);
CREATE INDEX IF NOT EXISTS idx_liquidity_symbol_timeframe_ts
    ON liquidity (stock_symbol, timeframe, candle_timestamp);

-- Table: liquidity_sweeps
CREATE TABLE IF NOT EXISTS liquidity_sweeps (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    sweep_type VARCHAR(16), -- BUY, SELL
    volume DOUBLE PRECISION,
    price DOUBLE PRECISION,
    no_of_equals INTEGER,
    UNIQUE (stock_symbol, timeframe, candle_timestamp)
);
CREATE INDEX IF NOT EXISTS idx_liquidity_sweeps_symbol_timeframe_ts
    ON liquidity_sweeps (stock_symbol, timeframe, candle_timestamp);


-- Table: trades
CREATE TABLE IF NOT EXISTS trades (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    entry_price DOUBLE PRECISION NOT NULL,
    stop_loss DOUBLE PRECISION NOT NULL,
    take_profit DOUBLE PRECISION NOT NULL,
    trade_type VARCHAR(255) NOT NULL,
    result VARCHAR(255),
    zone_id INTEGER REFERENCES zones(id) ON DELETE CASCADE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_trades_symbol_timeframe_ts
    ON trades (stock_symbol, timeframe, timestamp);
CREATE INDEX IF NOT EXISTS idx_trades_zone_id
    ON trades (zone_id);


-- Table: candle_aggregated_data
CREATE TABLE IF NOT EXISTS candle_aggregated_data (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(255),
    timeframe VARCHAR(255),
    candle_timestamp TIMESTAMP,
    open_price DOUBLE PRECISION,
    close_price DOUBLE PRECISION,
    high_price DOUBLE PRECISION,
    low_price DOUBLE PRECISION,
    volume DOUBLE PRECISION,
    last_swing_high DOUBLE PRECISION,
    last_swing_low DOUBLE PRECISION,
    last_liquidity_sweep_type INTEGER,
    supply_price DOUBLE PRECISION,
    supply_volume DOUBLE PRECISION,
    demand_price DOUBLE PRECISION,
    demand_volume DOUBLE PRECISION,
    bos_direction VARCHAR(255),
    bos_volume DOUBLE PRECISION,
    buy_liquidity DOUBLE PRECISION,
    buy_liquidity_strength INTEGER,
    sell_liquidity DOUBLE PRECISION,
    sell_liquidity_strength INTEGER,
    volatility DOUBLE PRECISION,
    average_volume DOUBLE PRECISION,
    rsi14 DOUBLE PRECISION,
    trade VARCHAR(255),
    entry_price DOUBLE PRECISION,
    trade_result VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_candle_aggregated_data_symbol_timeframe_ts
    ON candle_aggregated_data (stock_symbol, timeframe, candle_timestamp);