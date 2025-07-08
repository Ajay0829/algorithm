import requests
import subprocess

# API endpoint URL
url = "http://localhost:8081/api/data/process-all-stocks"

# Define the stock symbol
# stockSymbol = "UBER"  # Set your desired stock symbol here
#
# # JSON payload with parameters
# payload = {
#     "stockSymbol": stockSymbol,
#     "timeframe": "1d",   # or "15m", "1d"
#     "from": "2024-08-01",
#     "to": "2025-06-01"
# }

# Make the POST request
response = requests.post(url)

# Print the response
print("Status Code:", response.status_code)
print("Response:", response.text)
