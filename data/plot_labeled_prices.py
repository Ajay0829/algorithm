import matplotlib
import matplotlib.pyplot as plt
matplotlib.use('Qt5Agg')
import pandas as pd
from matplotlib.patches import Rectangle
from matplotlib.backends.backend_qt5agg import NavigationToolbar2QT
plt.ion()  # Enable interactive mode
plt.style.use('default')  # Reset to default style for consistent behavior

# Load the data
df = pd.read_csv('labeled_amd.us.csv')

# Convert timestamp to datetime if needed
df['date'] = pd.to_datetime(df['date'], unit='ms')

# Reset index to ensure integer indexing
print('Resetting DataFrame index...')
df = df.reset_index(drop=True)

# Only plot the first 1/8th of the data for performance
n = len(df)
if n > 0:
    print(f"Plotting only the first 1/8th of the data: {n} rows.")
    df = df.iloc[:n].copy()

# Plot high and low prices
fig, ax = plt.subplots(figsize=(16, 8))
ax.plot(df['date'], df['high'], label='High', color='magenta', linestyle='--', linewidth=1)
ax.plot(df['date'], df['low'], label='Low', color='cyan', linestyle='--', linewidth=1)

# Plot close price
ax.plot(df['date'], df['close'], label='Close Price', color='black', linewidth=1)

# Define label styles
label_styles = {
    'swing_high': {'marker': 'o', 'color': 'green', 'label': 'Swing High', 'edgecolor': 'black', 'linewidths': 2},
    'swing_low': {'marker': 'o', 'color': 'red', 'label': 'Swing Low', 'edgecolor': 'black', 'linewidths': 2},
    'none': {'marker': '.', 'color': 'gray', 'label': 'None', 'edgecolor': None, 'linewidths': 1}
}

# Use the correct label column name
label_col = 'labels' if 'labels' in df.columns else 'label'

# For each label, plot markers with a small y-offset to avoid overlap
label_offsets = {
    'swing_high': 0.01,
    'swing_low': -0.01,
    'none': 0.0
}

# For connecting swing points
prev_swing_point = None
prev_date = None

legend_labels = set()
for idx, row in df.iterrows():
    if idx % 1000 == 0:
        print(f"Scatter plotting row {idx}/{len(df)}...")
    if pd.isna(row[label_col]):
        continue
    label_list = [lbl.strip() for lbl in str(row[label_col]).split(';') if lbl.strip()]
    for label in label_list:
        if label in label_styles:
            style = label_styles[label]
            # Place swing_high on high, swing_low on low, others on close
            if label == 'swing_high':
                y_val = float(row['high'])
                # Connect with previous point if exists
                if prev_swing_point is not None:
                    ax.plot([prev_date, row['date']], [prev_swing_point, y_val],
                           color='gray', linestyle=':', linewidth=2, alpha=0.8)
                prev_swing_point = y_val
                prev_date = row['date']
            elif label == 'swing_low':
                y_val = float(row['low'])
                # Connect with previous point if exists
                if prev_swing_point is not None:
                    ax.plot([prev_date, row['date']], [prev_swing_point, y_val],
                           color='gray', linestyle=':', linewidth=2, alpha=0.8)
                prev_swing_point = y_val
                prev_date = row['date']
            else:
                y_offset = label_offsets.get(label, 0.0)
                y_val = float(row['close']) * (1 + y_offset)

            try:
                legend_label = style['label'] if style['label'] not in legend_labels else "_nolegend_"
                ax.scatter(row['date'], y_val,
                           marker=style['marker'], color=style['color'],
                           label=legend_label,
                           s=64 if label in ['swing_high', 'swing_low'] else 16, alpha=0.85,
                           edgecolors='face', linewidths=0)
                legend_labels.add(style['label'])
            except Exception as e:
                print(f"Scatter plot error at idx {idx}: {e}")

# Draw rectangles for demand and supply zones (thicker alpha)
for idx, row in df.iterrows():
    if pd.isna(row[label_col]):
        continue
    label_list = [lbl.strip() for lbl in str(row[label_col]).split(';') if lbl.strip()]
    # Demand zone: draw a rectangle from low to high, from this candle to 10 candles after
    if 'demand_zone' in label_list:
        try:
            x0 = row['date']
            idx1 = min(len(df) - 1, idx + 10)
            x1 = df.loc[idx1, 'date']
            y0 = float(row['low'])
            y1 = float(row['high'])
            ax.add_patch(Rectangle((x0, y0), x1 - x0, y1 - y0, color='green', alpha=0.35, linewidth=0))
        except Exception as e:
            print(f"Demand zone rectangle error at idx {idx}: {e}")
    # Supply zone: draw a rectangle from low to high, from this candle to 10 candles after (red color)
    if 'supply_zone' in label_list:
        try:
            x0 = row['date']
            idx1 = min(len(df) - 1, idx + 10)
            x1 = df.loc[idx1, 'date']
            y0 = float(row['low'])
            y1 = float(row['high'])
            ax.add_patch(Rectangle((x0, y0), x1 - x0, y1 - y0, color='red', alpha=0.35, linewidth=0))
        except Exception as e:
            print(f"Supply zone rectangle error at idx {idx}: {e}")

# Draw horizontal lines for liquidity sweep and BOS
for idx, row in df.iterrows():
    if idx % 1000 == 0:
        print(f"Line plotting row {idx}/{len(df)}...")
    if pd.isna(row[label_col]):
        continue
    label_list = [lbl.strip() for lbl in str(row[label_col]).split(';') if lbl.strip()]
    # Liquidity sweep: draw a less thick horizontal line just above high (sell_sweep) or just below low (buy_sweep)
    for sweep_label, sweep_color, sweep_style in [
        ('buy_sweep', 'green', 'dotted'),
        ('sell_sweep', 'red', 'dotted')
    ]:
        if sweep_label in label_list:
            try:
                if sweep_label == 'buy_sweep':
                    y = float(row['low']) - 0.01 * float(row['low'])  # just below low
                else:  # sell_sweep
                    y = float(row['high']) + 0.01 * float(row['high'])  # just above high
                idx0 = max(0, idx - 5)
                idx1 = min(len(df) - 1, idx + 5)
                x0 = df.loc[idx0, 'date']
                x1 = df.loc[idx1, 'date']
                ax.hlines(y, xmin=x0, xmax=x1, colors=sweep_color, linestyles=sweep_style, linewidth=2, label=sweep_label.replace('_', ' ').title() + ' Line' if sweep_label not in legend_labels else "")
                legend_labels.add(sweep_label)
            except Exception as e:
                print(f"Liquidity sweep line error at idx {idx}: {e}")
    # BOS: draw a horizontal line from swing_point0 idx to current idx at swing_point0_price
    if 'bos' in label_list:
        sp0_idx = row.get('swing_point0_index', None)
        sp0_type = str(row.get('swing_point0_type', '')).lower()
        try:
            sp0_idx = int(float(sp0_idx))
            x0 = df.loc[sp0_idx, 'date']
            x1 = df.loc[idx, 'date']
            # Use high for swing high, low for swing low
            if sp0_type == 'high':
                y = float(df.loc[sp0_idx, 'high'])
            elif sp0_type == 'low':
                y = float(df.loc[sp0_idx, 'low'])
            else:
                print(f"BOS line error: unknown sp0_type at idx {idx}, sp0_idx {sp0_idx}")
                continue
            ax.hlines(y, xmin=x0, xmax=x1, colors='purple', linestyles='solid', linewidth=2, label='BOS Line' if 'BOS Line' not in legend_labels else "")
            legend_labels.add('BOS Line')
        except Exception as e:
            print(f"BOS line error at idx {idx}: {e}")

plt.tight_layout()
plt.show()
input("Press Enter to close the plot...")  # Keep the plot window open
plt.close()
