import json
import requests

# Lista los dispositivos de red que conoce el controlador.
# Usa el ticket generado por 01_get-ticket.py (archivo ticket.txt).
api_url = "http://localhost:58000/api/v1/network-device"

with open("ticket.txt") as f:
    serviceTicket = f.read().strip()

headers = {"X-Auth-Token": serviceTicket}

resp = requests.get(api_url, headers=headers, verify=False)

print("Request status: ", resp.status_code)

response_json = resp.json()
networkDevices = response_json["response"]

for networkDevice in networkDevices:
    print(networkDevice["hostname"], "\t", networkDevice["platformId"], "\t", networkDevice["managementIpAddress"])
