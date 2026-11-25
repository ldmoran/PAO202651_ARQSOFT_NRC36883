import requests
import json
import config

# Parte 7, paso 2: envia un mensaje con formato Markdown a la sala (POST /messages)
message = 'Hello **DevNet Associates**!!'
url = config.BASE_URL + '/messages'
params = {'roomId': config.room_id(), 'markdown': message}
res = requests.post(url, headers=config.headers(True), json=params)
print(json.dumps(res.json(), indent=4))
