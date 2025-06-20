import pandas as pd
import mplfinance as mpf
import matplotlib.pyplot as plt

# Read the CSV
# Ensure your CSV has columns: ['timestamp', 'open', 'high', 'low', 'close', ...]
df = pd.read_csv('ko_swing_points.csv', parse_dates=['timestamp'])
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

# Prepare additional plots for swing highs/lows if present
apds = []
if 'swing_high' in df.columns:
    apds.append(mpf.make_addplot(df['swing_high'], type='scatter', markersize=100, marker='^', color='darkred', panel=0, label='Swing High'))
if 'swing_low' in df.columns:
    apds.append(mpf.make_addplot(df['swing_low'], type='scatter', markersize=100, marker='v', color='darkgreen', panel=0, label='Swing Low'))

# Plot candlestick chart with swing points and prepare for BOS lines
fig, axes = mpf.plot(
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
