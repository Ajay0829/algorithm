"""
Main application entry point for the trading dashboard
"""
import dash
import dash_bootstrap_components as dbc
import logging

from kafka_consumer import trading_consumer
from ui_components import UIComponents
from callback_handlers import CallbackHandlers

# Configure logging
logger = logging.getLogger(__name__)

class TradingDashboard:
    """Main trading dashboard application"""

    def __init__(self):
        self.app = dash.Dash(__name__, external_stylesheets=[dbc.themes.CYBORG])
        self.app.title = "Algorithmic Trading Dashboard"
        self._setup_layout()
        self._register_callbacks()
        self._start_services()

    def _setup_layout(self):
        """Setup the dashboard layout"""
        self.app.layout = UIComponents.create_layout()

    def _register_callbacks(self):
        """Register all callback handlers"""
        CallbackHandlers.register_callbacks(self.app)

    def _start_services(self):
        """Start background services"""
        trading_consumer.start_consumer()
        logger.info("Trading dashboard services started")

    def run(self, debug=True, port=8050):
        """Run the dashboard application"""
        try:
            logger.info(f"Starting trading dashboard on port {port}")
            self.app.run(debug=debug, port=port)
        except KeyboardInterrupt:
            logger.info("Shutting down trading dashboard")
            self._cleanup()

    def _cleanup(self):
        """Cleanup resources on shutdown"""
        trading_consumer.stop_consumer()
        logger.info("Trading dashboard shut down complete")

# Create and run the application
if __name__ == '__main__':
    dashboard = TradingDashboard()
    dashboard.run()
