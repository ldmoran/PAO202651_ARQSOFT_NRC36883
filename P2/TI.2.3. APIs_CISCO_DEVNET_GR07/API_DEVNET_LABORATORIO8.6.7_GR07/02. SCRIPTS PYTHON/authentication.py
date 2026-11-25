import requests
import json
import config

# Parte 3: prueba del token de acceso (GET /people/me)
url = config.BASE_URL + '/people/me'
res = requests.get(url, headers=config.headers())
print(json.dumps(res.json(), indent=4))
