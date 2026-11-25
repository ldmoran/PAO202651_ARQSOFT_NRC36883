import requests
import json
import os
import config

# Parte 5, paso 4: crea una sala (POST /rooms) y guarda su ID en room_id.txt
url = config.BASE_URL + '/rooms'
params = {'title': 'DevNet Associate Training! '}
res = requests.post(url, headers=config.headers(True), json=params)
data = res.json()
print(json.dumps(data, indent=4))

if 'id' in data:
    with open(os.path.join(os.path.dirname(__file__), 'room_id.txt'), 'w') as f:
        f.write(data['id'])
    print('ID de sala guardado en room_id.txt')
