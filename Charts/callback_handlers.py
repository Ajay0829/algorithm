"""
Callback handlers for the trading dashboard
"""
import dash
from dash import callback
from dash.dependencies import Output, Input, State
import logging
import pandas as pd
from datetime import datetime, timedelta

from config import BATCH_SIZE
from data_models import data_state
from event_processor import EventProcessor
from chart_renderer import ChartRenderer

logger = logging.getLogger(__name__)

class CallbackHandlers:
    """Manages all Dash callback functions"""

    @staticmethod
    def register_callbacks(app):
        """Register all callbacks with the Dash app"""

        @app.callback(
            Output('simulation-state', 'data'),
            [Input('pause-btn', 'n_clicks'),
             Input('resume-btn', 'n_clicks'),
             Input('clear-btn', 'n_clicks')],
            State('simulation-state', 'data'),
            prevent_initial_call=True
        )
        def handle_controls(pause_clicks, resume_clicks, clear_clicks, current_state):
            """Handle control button clicks (pause, resume, clear)"""
            ctx = dash.callback_context
            if not ctx.triggered:
                return current_state

            trigger_id = ctx.triggered[0]['prop_id'].split('.')[0]

            if trigger_id == 'pause-btn':
                current_state['paused'] = True
                logger.info("Simulation paused")
            elif trigger_id == 'resume-btn':
                current_state['paused'] = False
                logger.info("Simulation resumed")
            elif trigger_id == 'clear-btn':
                data_state.clear_all_data()
                logger.info("All data cleared")

            return current_state

        @app.callback(
            Output('candlestick-chart', 'figure'),
            Output('chart-figure-store', 'data'),
            [Input('interval-component', 'n_intervals')],
            [State('chart-figure-store', 'data'),
             State('simulation-state', 'data')],
            prevent_initial_call=False
        )
        def update_chart_with_controls(n, stored_fig, sim_state):
            """Main chart update callback with pause/resume support"""
            try:
                # Check if simulation is paused
                is_paused = sim_state and sim_state.get('paused', False)

                # Process events with rate limiting (only if not paused)
                event_processed = False
                if not is_paused and data_state.all_events_buffer and data_state.should_process_next_event():
                    # Process a batch of events for better performance
                    for _ in range(BATCH_SIZE):
                        if data_state.all_events_buffer:
                            event_processed = EventProcessor.process_event(
                                data_state.all_events_buffer.popleft()
                            )
                        else:
                            break

                # Check if we have data to display
                if not data_state.rendered_candles:
                    status_title = 'Simulation Paused - Waiting for Data...' if is_paused else 'Waiting for Market Data...'
                    waiting_fig = ChartRenderer.create_status_figure(status_title)
                    return waiting_fig, waiting_fig.to_dict()

                # Extract and validate candle data
                candle_data = CallbackHandlers._extract_candle_data()
                if not candle_data:
                    status_title = 'Data Validation Error' if not is_paused else 'Simulation Paused - Data Error'
                    error_fig = ChartRenderer.create_status_figure(status_title, is_error=True)
                    return error_fig, error_fig.to_dict()

                x, open_, high, low, close, volume = candle_data

                # Create the chart figure
                fig = ChartRenderer.create_optimized_figure(x, open_, high, low, close, volume)
                ChartRenderer.add_annotations(fig, x, high, low)
                ChartRenderer.apply_chart_layout(fig, is_paused)
                ChartRenderer.set_chart_range(fig, x)

                # Reset rebuild flag
                data_state.chart_needs_full_rebuild = False
                data_state.last_candle_count = len(x)

                if event_processed or data_state.chart_needs_full_rebuild:
                    logger.info(f"Chart update with {len(x)} candles")

                return fig, fig.to_dict()

            except Exception as e:
                logger.error(f"Error in update_chart_with_controls: {str(e)}")
                error_fig = ChartRenderer.create_status_figure(f'Chart Error: {str(e)}', is_error=True)
                return error_fig, error_fig.to_dict()

    @staticmethod
    def _extract_candle_data():
        """Extract and validate candle data from rendered_candles"""
        try:
            x, open_, high, low, close, volume = [], [], [], [], [], []

            for candle in data_state.rendered_candles:
                if all(key in candle for key in ['timestamp', 'open', 'high', 'low', 'close']):
                    x.append(candle['timestamp'])
                    open_.append(candle['open'])
                    high.append(candle['high'])
                    low.append(candle['low'])
                    close.append(candle['close'])
                    volume.append(candle.get('volume', 0))

            # Validate data consistency
            lengths = [len(x), len(open_), len(high), len(low), len(close), len(volume)]
            if not x or len(set(lengths)) != 1:
                logger.warning(f"Data length mismatch: x={len(x)}, o={len(open_)}, h={len(high)}, l={len(low)}, c={len(close)}, v={len(volume)}")
                return None

            return x, open_, high, low, close, volume

        except Exception as e:
            logger.error(f"Error extracting candle data: {str(e)}")
            return None

# Global callback handlers instance
callback_handlers = CallbackHandlers()
