import requests
import json
import config

# Parte 6, paso 4: agrega a una persona a la sala (POST /memberships)
url = config.BASE_URL + '/memberships'
params = {'roomId': config.room_id(), 'personEmail': config.email()}
res = requests.post(url, headers=config.headers(True), json=params)
print(json.dumps(res.json(), indent=4))
