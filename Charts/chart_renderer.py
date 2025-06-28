"""
Chart rendering and visualization logic
"""
import plotly.graph_objs as go
from plotly.subplots import make_subplots
import pandas as pd
from datetime import datetime, timedelta
from typing import List, Tuple
from config import COLORS, SWING_MARKERS, INITIAL_CANDLES_TO_SHOW
from data_models import data_state

class ChartRenderer:
    """Handles all chart rendering and visualization"""

    @staticmethod
    def create_optimized_figure(x: List, open_: List, high: List, low: List, close: List, volume: List) -> go.Figure:
        """Create figure with candlestick chart and volume subplot"""
        # Create subplots: main chart (90%) and volume chart (10%)
        fig = make_subplots(
            rows=2, cols=1,
            shared_xaxes=True,
            vertical_spacing=0.02,
            row_heights=[0.9, 0.1]
        )

        # Add candlestick trace to main chart
        fig.add_trace(go.Candlestick(
            x=x, open=open_, high=high, low=low, close=close,
            increasing_line_color=COLORS['bullish_candle'],
            decreasing_line_color=COLORS['bearish_candle'],
            name='Price',
            hoverinfo='skip',
            showlegend=False
        ), row=1, col=1)

        # Add volume bars with color coding
        volume_colors = ChartRenderer._get_volume_colors(close)
        volume_in_millions = [v / 1000000 for v in volume]

        fig.add_trace(go.Bar(
            x=x,
            y=volume_in_millions,
            name='Volume',
            marker_color=volume_colors,
            hoverinfo='skip',
            showlegend=False
        ), row=2, col=1)

        return fig

    @staticmethod
    def _get_volume_colors(close: List) -> List[str]:
        """Generate volume bar colors based on price movement"""
        volume_colors = []
        for i in range(len(close)):
            if i == 0:
                volume_colors.append(COLORS['bullish_candle'])
            else:
                if close[i] >= close[i-1]:
                    volume_colors.append(COLORS['bullish_candle'])
                else:
                    volume_colors.append(COLORS['bearish_candle'])
        return volume_colors

    @staticmethod
    def add_annotations(fig: go.Figure, x: List, high: List, low: List):
        """Add zones, swings, BOS, and trade annotations to the figure"""
        ChartRenderer._add_zones(fig)
        ChartRenderer._add_swing_points(fig, x, high, low)
        ChartRenderer._add_bos_lines(fig)
        ChartRenderer._add_trades(fig, x)

    @staticmethod
    def _add_zones(fig: go.Figure):
        """Add supply/demand zones to the chart"""
        for zone in data_state.zones:
            color = COLORS['supply_zone'] if zone['zoneType'].upper() == 'SUPPLY' else COLORS['demand_zone']
            x0, x1 = zone['startTs'], zone['endTs']
            fig.add_shape(
                type='rect', xref='x', yref='y',
                x0=x0, x1=x1, y0=zone['y0'], y1=zone['y1'],
                fillcolor=color, opacity=0.15,
                line=dict(color=color, width=1, dash='dot'),
                row=1, col=1
            )

    @staticmethod
    def _add_swing_points(fig: go.Figure, x: List, high: List, low: List):
        """Add swing points to the chart"""
        for swing in data_state.swings:
            ts = swing['timestamp']
            swing_type = swing.get('swingType')
            marker = SWING_MARKERS.get(swing_type)

            if marker and ts in x:
                idx = x.index(ts)
                y_val = high[idx] if swing.get('useHigh', False) else low[idx]

                # Add offset for better visibility
                offset = (high[idx] - low[idx]) * 0.05
                y_display = y_val + offset if swing.get('useHigh', False) else y_val - offset

                fig.add_trace(go.Scatter(
                    x=[ts],
                    y=[y_display],
                    mode='markers',
                    marker=dict(
                        size=marker['size'],
                        color=marker['color'],
                        symbol=marker['symbol'],
                        line=dict(color='white', width=1)
                    ),
                    name=swing_type.replace('_', ' ').title(),
                    showlegend=False,
                    hovertemplate=f"<b>{swing_type.replace('_', ' ').title()}</b><br>" +
                                 f"Price: ${y_val:.2f}<br>" +
                                 f"Date: %{{x}}<extra></extra>"
                ), row=1, col=1)

    @staticmethod
    def _add_bos_lines(fig: go.Figure):
        """Add break of structure lines to the chart"""
        for bos in data_state.boss:
            start_ts = bos.get('start')
            end_ts = bos.get('end')
            bos_type = bos.get('bosType', 'BULLISH')
            y_value = bos.get('y')

            line_color = COLORS['bullish_bos'] if bos_type == 'BULLISH' else COLORS['bearish_bos']

            if start_ts and end_ts and y_value is not None:
                # Add the line
                fig.add_shape(
                    type='line',
                    xref='x', yref='y',
                    x0=start_ts, x1=end_ts,
                    y0=y_value, y1=y_value,
                    line=dict(color=line_color, width=2, dash='dot'),
                    opacity=0.8,
                    row=1, col=1
                )

                # Add annotation
                fig.add_annotation(
                    x=end_ts, y=y_value,
                    text=f"BOS-{bos_type}",
                    showarrow=True,
                    arrowhead=2, arrowsize=1, arrowwidth=2,
                    arrowcolor=line_color,
                    ax=20, ay=-30,
                    font=dict(size=10, color=line_color),
                    bgcolor='rgba(0,0,0,0.5)',
                    bordercolor=line_color, borderwidth=1,
                    row=1, col=1
                )

    @staticmethod
    def _add_trades(fig: go.Figure, x: List):
        """Add trade annotations that don't overlap with swing points"""
        for trade in data_state.trades:
            trade_id = trade.get('id')
            entry_timestamp = trade.get('entryTimestamp')
            entry_price = trade.get('entryPrice')
            trade_type = trade.get('tradeType', 'BUY')
            result = trade.get('result', 'PENDING')
            is_active = trade.get('isActive', True)
            exit_price = trade.get('exitPrice')
            exit_timestamp = trade.get('exitTimestamp')

            if not entry_timestamp or not entry_price:
                continue

            # Entry point markers - designed to not overlap with swing points
            if trade_type == 'BUY':
                # BUY: Green square positioned below price to avoid swing highs
                entry_symbol = 'square'
                entry_color = '#32CD32'  # Lime green
                y_offset = -0.3  # Position below the price
            else:
                # SELL: Red circle positioned above price to avoid swing lows
                entry_symbol = 'circle'
                entry_color = '#DC143C'  # Crimson red
                y_offset = 0.3  # Position above the price

            fig.add_trace(go.Scatter(
                x=[entry_timestamp],
                y=[entry_price],
                mode='markers',
                marker=dict(
                    size=14,
                    color=entry_color,
                    symbol=entry_symbol,
                    line=dict(color='white', width=2)
                ),
                name=f'Entry',
                showlegend=False,
                hoverinfo='skip'
            ), row=1, col=1)

            # Entry price annotation positioned to avoid overlap
            fig.add_annotation(
                x=entry_timestamp,
                y=entry_price + y_offset,
                text=f"${entry_price:.2f}",
                showarrow=False,
                font=dict(size=9, color='white', family='Arial Black'),
                bgcolor=entry_color,
                bordercolor='white',
                borderwidth=1,
                opacity=0.9,
                xanchor='center',
                yanchor='middle',
                row=1, col=1
            )

            # Exit marker - show for any trade with a result (WIN/LOSS), regardless of active status
            if result in ['WIN', 'LOSS'] and exit_price:
                # Use entry timestamp if exit timestamp is not available
                display_exit_timestamp = exit_timestamp if exit_timestamp else entry_timestamp

                # Exit markers - using distinct symbols that don't overlap
                if result == 'WIN':
                    outcome_color = '#00FF00'  # Bright green
                    exit_symbol = 'diamond'    # Diamond for wins
                elif result == 'LOSS':
                    outcome_color = '#FF4444'  # Red
                    exit_symbol = 'x'          # X for losses
                else:
                    outcome_color = '#FFA500'  # Orange
                    exit_symbol = 'diamond'

                fig.add_trace(go.Scatter(
                    x=[display_exit_timestamp],
                    y=[exit_price],
                    mode='markers',
                    marker=dict(
                        size=12,
                        color=outcome_color,
                        symbol=exit_symbol,
                        line=dict(color='white', width=2)
                    ),
                    name=f'Exit',
                    showlegend=False,
                    hoverinfo='skip'
                ), row=1, col=1)

                # Exit price with P&L calculation
                profit_loss = exit_price - entry_price if trade_type == 'BUY' else entry_price - exit_price
                profit_loss_text = f"+${profit_loss:.2f}" if profit_loss >= 0 else f"-${abs(profit_loss):.2f}"

                fig.add_annotation(
                    x=display_exit_timestamp,
                    y=exit_price + (0.2 if result == 'WIN' else -0.2),
                    text=f"${exit_price:.2f}<br>{profit_loss_text}",
                    showarrow=False,
                    font=dict(size=8, color='white', family='Arial Black'),
                    bgcolor=outcome_color,
                    bordercolor='white',
                    borderwidth=1,
                    opacity=0.9,
                    xanchor='center',
                    yanchor='middle',
                    row=1, col=1
                )

    @staticmethod
    def apply_chart_layout(fig: go.Figure, is_paused: bool = False):
        """Apply professional chart layout and styling"""
        title_suffix = " (PAUSED)" if is_paused else ""

        fig.update_layout(
            title={
                'text': f"📊 Live Market Analysis with Volume{title_suffix}",
                'x': 0.5,
                'xanchor': 'center',
                'font': {'size': 24, 'color': COLORS['accent'], 'family': 'Arial Black'}
            },
            template='plotly_dark',
            plot_bgcolor=COLORS['background'],
            paper_bgcolor=COLORS['background'],
            font=dict(color='#ffffff', family='Arial'),
            margin=dict(l=50, r=80, t=80, b=50),
            hovermode=False,
            uirevision='constant',
            dragmode='zoom'
        )

        # Update main chart (price) axes
        fig.update_xaxes(
            showgrid=True, gridcolor='#2d3748', gridwidth=0.5,
            showspikes=True, spikemode='across+toaxis', spikecolor=COLORS['accent'],
            spikethickness=1, spikedash='dot',
            spikesnap='cursor',
            type='date', rangeslider=dict(visible=False),
            rangeselector=dict(
                buttons=[
                    dict(count=1, label='1D', step='day', stepmode='backward'),
                    dict(count=7, label='7D', step='day', stepmode='backward'),
                    dict(count=30, label='1M', step='day', stepmode='backward'),
                    dict(count=90, label='3M', step='day', stepmode='backward'),
                    dict(step='all', label='All')
                ],
                bgcolor='#2d3748', activecolor=COLORS['accent'],
                bordercolor='#404040', borderwidth=1,
                font=dict(color='#ffffff', size=10)
            ),
            uirevision='constant', row=1, col=1
        )

        fig.update_yaxes(
            title=dict(text="Price ($)", font=dict(size=14, color='#B0BEC5')),
            showgrid=True, gridcolor='#2d3748', gridwidth=0.5,
            showspikes=True, spikemode='across+toaxis', spikecolor=COLORS['accent'],
            spikethickness=1, spikedash='dot',
            spikesnap='cursor',
            tickformat='$.2f', side='right',
            uirevision='constant', row=1, col=1
        )

        # Update volume chart axes
        fig.update_xaxes(
            title=dict(text="Time", font=dict(size=14, color='#B0BEC5')),
            showgrid=True, gridcolor='#2d3748', gridwidth=0.5,
            showspikes=True, spikemode='across+toaxis', spikecolor=COLORS['accent'],
            spikethickness=1, spikedash='dot',
            spikesnap='cursor',
            type='date', rangeslider=dict(visible=False),
            uirevision='constant', row=2, col=1
        )

        fig.update_yaxes(
            title=dict(text="Volume (M)", font=dict(size=14, color='#B0BEC5')),
            showgrid=True, gridcolor='#2d3748', gridwidth=0.5,
            showspikes=True, spikemode='across+toaxis', spikecolor=COLORS['accent'],
            spikethickness=1, spikedash='dot',
            spikesnap='cursor',
            tickformat=',', side='right',
            uirevision='constant', row=2, col=1
        )

        fig.update_layout(xaxis_rangeslider_visible=False)

    @staticmethod
    def set_chart_range(fig: go.Figure, x: List):
        """Set the x-axis range to focus on recent candles"""
        if not x:
            return

        # Convert timestamps to datetime if needed
        if isinstance(x[0], str):
            x_dates = pd.to_datetime(x)
        else:
            x_dates = x

        # Focus on the most recent candles
        num_candles_to_show = min(INITIAL_CANDLES_TO_SHOW, len(x_dates))

        if len(x_dates) >= num_candles_to_show:
            start_index = len(x_dates) - num_candles_to_show
            x_range_start = x_dates[start_index]
            x_range_end = x_dates[-1]

            # Add padding
            time_span = x_range_end - x_range_start
            if time_span.total_seconds() > 0:
                padding = time_span * 0.05
                x_range_start = x_range_start - padding
                x_range_end = x_range_end + padding
        else:
            # Show all with padding
            time_span = x_dates[-1] - x_dates[0]
            padding = time_span * 0.1 if time_span.total_seconds() > 0 else timedelta(hours=1)
            x_range_start = x_dates[0] - padding
            x_range_end = x_dates[-1] + padding

        # Set range for both subplots
        fig.update_xaxes(range=[x_range_start, x_range_end], row=1, col=1)
        fig.update_xaxes(range=[x_range_start, x_range_end], row=2, col=1)

    @staticmethod
    def create_status_figure(message: str, is_error: bool = False) -> go.Figure:
        """Create a status figure for loading/error states"""
        return go.Figure(layout=go.Layout(
            title=message,
            template='plotly_dark',
            plot_bgcolor=COLORS['background'],
            paper_bgcolor=COLORS['background'],
            font=dict(color='#fff'),
            margin=dict(l=10, r=10, t=50, b=10),
            uirevision='constant'
        ))

# Global chart renderer instance
chart_renderer = ChartRenderer()
