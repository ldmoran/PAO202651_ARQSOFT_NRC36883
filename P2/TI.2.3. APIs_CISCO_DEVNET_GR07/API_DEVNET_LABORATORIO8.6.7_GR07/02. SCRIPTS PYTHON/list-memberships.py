import requests
import json
import config

# Parte 6, paso 2: lista los miembros de la sala (GET /memberships?roomId=...)
url = config.BASE_URL + '/memberships'
params = {'roomId': config.room_id()}
res = requests.get(url, headers=config.headers(True), params=params)
print(json.dumps(res.json(), indent=4))
