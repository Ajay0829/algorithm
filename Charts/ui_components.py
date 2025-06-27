"""
UI components and layout for the trading dashboard
"""
import dash_bootstrap_components as dbc
from dash import dcc, html
from config import COLORS, CHART_HEIGHT, MIN_CHART_HEIGHT

class UIComponents:
    """Handles all UI component creation and layout"""

    @staticmethod
    def create_control_panel():
        """Create the control panel with pause/resume/clear buttons"""
        return dbc.Row([
            dbc.Col([
                dbc.Card([
                    dbc.CardBody([
                        dbc.Row([
                            dbc.Col([
                                dbc.ButtonGroup([
                                    dbc.Button("⏸️ Pause", id="pause-btn", color="warning",
                                             size="sm", className="me-1"),
                                    dbc.Button("▶️ Resume", id="resume-btn", color="success",
                                             size="sm", className="me-1"),
                                    dbc.Button("🗑️ Clear", id="clear-btn", color="danger", size="sm"),
                                ], className="d-flex justify-content-center")
                            ], width=12)
                        ])
                    ])
                ], className="shadow-sm border-0",
                   style={'backgroundColor': COLORS['card_background']})
            ], width=12)
        ], className="mb-2")

    @staticmethod
    def create_main_chart():
        """Create the main chart component"""
        return dbc.Row([
            dbc.Col([
                dbc.Card([
                    dbc.CardBody([
                        dcc.Graph(
                            id='candlestick-chart',
                            config={
                                'displayModeBar': True,
                                'displaylogo': False,
                                'modeBarButtonsToRemove': ['pan2d', 'lasso2d', 'select2d'],
                                'toImageButtonOptions': {
                                    'format': 'png',
                                    'filename': 'trading_chart',
                                    'height': 800,
                                    'width': 1400,
                                    'scale': 2
                                }
                            },
                            style={'height': CHART_HEIGHT, 'minHeight': MIN_CHART_HEIGHT}
                        )
                    ], className="p-1")
                ], className="shadow border-0",
                   style={'backgroundColor': COLORS['card_background']})
            ], width=12)
        ])

    @staticmethod
    def create_hidden_stores():
        """Create hidden stores for state management"""
        return [
            dcc.Store(id='chart-figure-store'),
            dcc.Store(id='simulation-state', data={'paused': False}),
            dcc.Interval(id='interval-component', interval=100, n_intervals=0),
        ]

    @staticmethod
    def create_layout():
        """Create the complete dashboard layout"""
        return dbc.Container([
            # Control Panel
            UIComponents.create_control_panel(),

            # Main Chart
            UIComponents.create_main_chart(),

            # Hidden stores
            *UIComponents.create_hidden_stores()

        ], fluid=True, className="bg-dark min-vh-100 py-2",
           style={'backgroundColor': '#0a0a0a'})

# Global UI components instance
ui_components = UIComponents()
