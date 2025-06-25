import requests
import subprocess

# API endpoint URL
url = "http://localhost:8080/api/batch/run"

# Define the stock symbol
stockSymbol = "NVDA"  # Set your desired stock symbol here

# JSON payload with parameters
payload = {
    "stockSymbol": stockSymbol,
    "timeframe": "1h",   # or "15m", "1d"
    "from": "2024-09-01",
    "to": "2024-12-01"
}

# Make the POST request
response = requests.post(url, json=payload)

# Print the response
print("Status Code:", response.status_code)
print("Response:", response.text)
