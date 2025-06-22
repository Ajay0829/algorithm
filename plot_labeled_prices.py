import sys
import pandas as pd
import mplfinance as mpf
import matplotlib.pyplot as plt

if len(sys.argv) > 1:
    stock_symbol = sys.argv[1]
    print(f"Plotting for stock symbol: {stock_symbol}")
else:
    print("No stock symbol provided.")
    sys.exit(1)

# Read the CSV
# Ensure your CSV has columns: ['timestamp', 'open', 'high', 'low', 'close', ...]
df = pd.read_csv(f'{stock_symbol}_swing_points.csv', parse_dates=['timestamp'])
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

# Prepare additional plots for major/minor swing highs/lows if present
apds = []
if 'swing_high' in df.columns:
    if 'is_major' in df.columns:
        major_highs = df[(df['is_major'] == True) & df['swing_high'].notnull()]['swing_high']
        minor_highs = df[(df['is_major'] == False) & df['swing_high'].notnull()]['swing_high']
        # Align to main df index for mplfinance
        major_highs_aligned = pd.Series(index=df.index, dtype=float)
        for idx, val in zip(major_highs.index, major_highs.values):
            major_highs_aligned.loc[idx] = val
        minor_highs_aligned = pd.Series(index=df.index, dtype=float)
        for idx, val in zip(minor_highs.index, minor_highs.values):
            minor_highs_aligned.loc[idx] = val
        if not minor_highs_aligned.dropna().empty:
            apds.append(mpf.make_addplot(minor_highs_aligned, type='scatter', markersize=100, marker='*', color='lightcoral', panel=0, label='Minor Swing High'))
        if not major_highs_aligned.dropna().empty:
            apds.append(mpf.make_addplot(major_highs_aligned, type='scatter', markersize=100, marker='^', color='darkred', panel=0, label='Major Swing High'))
    else:
        swing_highs = df['swing_high'].dropna()
        if not swing_highs.empty:
            apds.append(mpf.make_addplot(swing_highs, type='scatter', markersize=100, marker='^', color='darkred', panel=0, label='Swing High'))

if 'swing_low' in df.columns:
    if 'is_major' in df.columns:
        major_lows = df[(df['is_major'] == True) & df['swing_low'].notnull()]['swing_low']
        minor_lows = df[(df['is_major'] == False) & df['swing_low'].notnull()]['swing_low']
        major_lows_aligned = pd.Series(index=df.index, dtype=float)
        for idx, val in zip(major_lows.index, major_lows.values):
            major_lows_aligned.loc[idx] = val
        minor_lows_aligned = pd.Series(index=df.index, dtype=float)
        for idx, val in zip(minor_lows.index, minor_lows.values):
            minor_lows_aligned.loc[idx] = val
        if not minor_lows_aligned.dropna().empty:
            apds.append(mpf.make_addplot(minor_lows_aligned, type='scatter', markersize=100, marker='*', color='lightgreen', panel=0, label='Minor Swing Low'))
        if not major_lows_aligned.dropna().empty:
            apds.append(mpf.make_addplot(major_lows_aligned, type='scatter', markersize=100, marker='v', color='darkgreen', panel=0, label='Major Swing Low'))
    else:
        swing_lows = df['swing_low'].dropna()
        if not swing_lows.empty:
            apds.append(mpf.make_addplot(swing_lows, type='scatter', markersize=100, marker='v', color='darkgreen', panel=0, label='Swing Low'))

# Ensure index is DatetimeIndex before plotting
if not isinstance(df.index, pd.DatetimeIndex):
    df.index = pd.to_datetime(df.index)

# Plot candlestick chart with swing points and prepare for BOS lines
fig, axes = mpf.plot(
    df,
    type='candle',
    style=s,
    addplot=apds if apds else [],  # Pass an empty list instead of None
    title=f'{stock_symbol}',
    ylabel='Price',
    ylabel_lower='',
    volume=False,
    figratio=(16,6),
    figscale=1.2,
    tight_layout=True,
    xrotation=45,
    show_nontrading=True,
    datetime_format='%Y-%m-%d',
    warn_too_much_data=10000,
    returnfig=True
)
ax = axes[0]

# Draw horizontal lines for BOS events from swing point to BOS candle
if 'break_of_structure' in df.columns:
    for idx, row in df.iterrows():
        if pd.notnull(row.get('break_of_structure')) and row.get('break_of_structure') != '':
            try:
                swing_idx = df.index.get_loc(row['break_of_structure'])
            except KeyError:
                swing_idx = None
            try:
                bos_idx = df.index.get_loc(row.name)
            except KeyError:
                bos_idx = None
            if swing_idx is not None and bos_idx is not None and bos_idx > swing_idx:
                # Determine y based on swing type
                swing_type = None
                if 'type' in df.columns:
                    swing_type = df.iloc[bos_idx]['type']
                if swing_type == 'HIGH' and 'type' in df.columns:
                    y = df.iloc[swing_idx]['high']
                    ax.hlines(y, df.index[swing_idx], df.index[bos_idx], colors='green', linewidth=2, linestyles='dashed', label='BOS')
                elif swing_type == 'LOW' and 'type' in df.columns:
                    y = df.iloc[swing_idx]['low']
                    ax.hlines(y, df.index[swing_idx], df.index[bos_idx], colors='red', linewidth=2, linestyles='dashed', label='BOS')
                else:
                    y = row['close']  # fallback
                    ax.hlines(y, df.index[swing_idx], df.index[bos_idx], colors='black', linewidth=2, linestyles='dashed', label='BOS')
plt.show()
