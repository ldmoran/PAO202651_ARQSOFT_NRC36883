import requests
import json
import config

# Parte 5, paso 2: lista las salas del usuario autenticado (GET /rooms)
url = config.BASE_URL + '/rooms'
params = {'max': '100'}
res = requests.get(url, headers=config.headers(True), params=params)
print(json.dumps(res.json(), indent=4))
