import pandas as pd
import mplfinance as mpf

# Read the CSV
# Ensure your CSV has columns: ['timestamp', 'open', 'high', 'low', 'close', ...]
df = pd.read_csv('aac_swing_points.csv', parse_dates=['timestamp'])
df = df.sort_values('timestamp')
df = df.set_index('timestamp')

# Ensure 'open', 'close', 'high', and 'low' columns are floats for correct candle coloring in mplfinance
df['open'] = pd.to_numeric(df['open'], errors='coerce')
df['close'] = pd.to_numeric(df['close'], errors='coerce')
df['high'] = pd.to_numeric(df['high'], errors='coerce')
df['low'] = pd.to_numeric(df['low'], errors='coerce')

# Set up custom colors: green if close > open, red otherwise (force up=green, down=red, and force mplfinance to use close>open for up)
mc = mpf.make_marketcolors(up='green', down='red', edge='inherit', wick='black', volume='inherit')
s = mpf.make_mpf_style(marketcolors=mc, base_mpf_style='charles')

# Workaround: If all closes are equal to or greater than open, force some candles to be red for test
# print(df[['open', 'close']].head(20))

# Check for any close < open (should be red)
if (df['close'] < df['open']).any():
    print("There are candles where close < open (should be red).")
else:
    print("WARNING: All candles have close >= open. All will be green.")

# Prepare additional plots for swing highs/lows if present
apds = []
if 'swing_high' in df.columns:
    apds.append(mpf.make_addplot(df['swing_high'], type='scatter', markersize=100, marker='^', color='darkred', panel=0, label='Swing High'))
if 'swing_low' in df.columns:
    apds.append(mpf.make_addplot(df['swing_low'], type='scatter', markersize=100, marker='v', color='darkgreen', panel=0, label='Swing Low'))

# Plot candlestick chart with swing points
mpf.plot(
    df,
    type='candle',
    style=s,
    addplot=apds if apds else None,
    title='AAC Candle Highs and Lows with Swing Points',
    ylabel='Price',
    ylabel_lower='',
    volume=False,
    figratio=(16,6),
    figscale=1.2,
    tight_layout=True,
    xrotation=45,
    show_nontrading=True,
    datetime_format='%Y-%m-%d',
    warn_too_much_data=10000
)
