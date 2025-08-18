import requests

url = "http://localhost:8081/api/data/process-all-stocks"

response = requests.post(url)

# Print the response
print("Status Code:", response.status_code)
print("Response:", response.text)
