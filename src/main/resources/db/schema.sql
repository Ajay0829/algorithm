-- Table: stock_features

CREATE TABLE IF NOT EXISTS stock_features (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    high DOUBLE PRECISION,
    low DOUBLE PRECISION,
    open DOUBLE PRECISION,
    close DOUBLE PRECISION,
    volume DOUBLE PRECISION,
    current_swing_high DOUBLE PRECISION,
    current_swing_low DOUBLE PRECISION,
    current_price_relative_value DOUBLE PRECISION,
    current_candle_distance_from_latest_swing DOUBLE PRECISION,
    prev_n_candle_high DOUBLE PRECISION,
    prev_n_candle_low DOUBLE PRECISION,
    prev_n_candle_volume DOUBLE PRECISION,
    nearest_relevant_zone_near_point DOUBLE PRECISION,
    nearest_relevant_zone_far_point DOUBLE PRECISION,
    nearest_relevant_zone_type VARCHAR(16),
    nearest_relevant_zone_strength DOUBLE PRECISION,
    nearest_relevant_zone_bos_volume DOUBLE PRECISION,
    nearby_sell_liquidity_zone DOUBLE PRECISION,
    nearby_buy_liquidity_zone DOUBLE PRECISION,
    rsi_14 DOUBLE PRECISION,
    ma_14 DOUBLE PRECISION,
    is_earnings_day BOOLEAN,
    earnings_release_session VARCHAR(32),
    forward_pe DOUBLE PRECISION,
    atr DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE (stock_symbol, timeframe, candle_timestamp)
);

-- Index for fast lookup
CREATE INDEX IF NOT EXISTS idx_stock_features_symbol_timeframe_ts
    ON stock_features (stock_symbol, timeframe, candle_timestamp);

-- Table: swing_points
CREATE TABLE IF NOT EXISTS swing_points (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    price DOUBLE PRECISION,
    swing_type VARCHAR(8), -- HIGH or LOW
    confirmed BOOLEAN DEFAULT FALSE,
    UNIQUE (stock_symbol, timeframe, candle_timestamp, swing_type)
);
CREATE INDEX IF NOT EXISTS idx_swing_points_symbol_timeframe_ts
    ON swing_points (stock_symbol, timeframe, candle_timestamp);

-- Table: trends
CREATE TABLE IF NOT EXISTS trends (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    type VARCHAR(16), -- e.g., UP, DOWN, SIDEWAYS
    strength DOUBLE PRECISION,
    UNIQUE (stock_symbol, timeframe, candle_timestamp)
);
CREATE INDEX IF NOT EXISTS idx_trends_symbol_timeframe_ts
    ON trends (stock_symbol, timeframe, candle_timestamp);

-- Table: zones
CREATE TABLE IF NOT EXISTS zones (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    near_point DOUBLE PRECISION,
    far_point DOUBLE PRECISION,
    type VARCHAR(16), -- SUPPLY, DEMAND
    volume DOUBLE PRECISION,
    strength DOUBLE PRECISION,
    UNIQUE (stock_symbol, timeframe, candle_timestamp, type)
);
CREATE INDEX IF NOT EXISTS idx_zones_symbol_timeframe_ts
    ON zones (stock_symbol, timeframe, candle_timestamp);

-- Table: sweeps
CREATE TABLE IF NOT EXISTS sweeps (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    type VARCHAR(16), -- BUY, SELL
    swing_point_id INTEGER REFERENCES swing_points(id) ON DELETE CASCADE,
    price DOUBLE PRECISION,
    UNIQUE (stock_symbol, timeframe, candle_timestamp, type)
);
CREATE INDEX IF NOT EXISTS idx_sweeps_symbol_timeframe_ts
    ON sweeps (stock_symbol, timeframe, candle_timestamp);

-- Table: bos (Break of Structure)
CREATE TABLE IF NOT EXISTS bos (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    timeframe VARCHAR(16) NOT NULL,
    candle_timestamp TIMESTAMP NOT NULL,
    type VARCHAR(16), -- e.g., UP, DOWN
    weak_swing_point INTEGER REFERENCES swing_points(id) ON DELETE CASCADE,
    strong_swing_point INTEGER REFERENCES swing_points(id) ON DELETE CASCADE,
    UNIQUE (stock_symbol, timeframe, candle_timestamp, type)
);
CREATE INDEX IF NOT EXISTS idx_bos_symbol_timeframe_ts
    ON bos (stock_symbol, timeframe, candle_timestamp);

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

-- Table: fundamentals
CREATE TABLE IF NOT EXISTS fundamentals (
    id SERIAL PRIMARY KEY,
    stock_symbol VARCHAR(16) NOT NULL,
    pe_forward DOUBLE PRECISION,
    atr DOUBLE PRECISION,
    earnings_release_session VARCHAR(32),
    next_earnings_date TIMESTAMP,
    UNIQUE (stock_symbol, earnings_release_session, next_earnings_date)
);
CREATE INDEX IF NOT EXISTS idx_fundamentals_stock_earnings
    ON fundamentals (stock_symbol, earnings_release_session, next_earnings_date);

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

