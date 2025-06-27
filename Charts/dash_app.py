"""
Main Dash application - now using modular architecture
This file imports and runs the trading dashboard using the new modular structure.
"""
from app import TradingDashboard

# Create and run the dashboard
if __name__ == '__main__':
    dashboard = TradingDashboard()
    dashboard.run(debug=True, port=8050)
