import requests
import json
import config

# Parte 4: detalles de un usuario registrado de Webex (GET /people?email=...)
url = config.BASE_URL + '/people'
params = {'email': config.email()}
res = requests.get(url, headers=config.headers(True), params=params)
print(json.dumps(res.json(), indent=4))
