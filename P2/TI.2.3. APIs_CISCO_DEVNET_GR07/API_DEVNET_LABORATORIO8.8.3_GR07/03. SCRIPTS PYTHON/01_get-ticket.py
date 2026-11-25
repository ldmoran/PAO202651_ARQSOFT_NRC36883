import json
import requests

# Solicita un ticket de servicio al controlador PT-Controller0 (Packet Tracer).
# El ticket se guarda en ticket.txt para que lo reutilicen 02 y 03.
api_url = "http://localhost:58000/api/v1/ticket"

headers = {
    "content-type": "application/json"
}

body_json = {
    "username": "cisco",
    "password": "cisco123!"
}

resp = requests.post(api_url, json.dumps(body_json), headers=headers, verify=False)

print("Ticket request status: ", resp.status_code)
response_json = resp.json()

serviceTicket = response_json["response"]["serviceTicket"]
print("The service ticket number is: ", serviceTicket)

with open("ticket.txt", "w") as f:
    f.write(serviceTicket)
