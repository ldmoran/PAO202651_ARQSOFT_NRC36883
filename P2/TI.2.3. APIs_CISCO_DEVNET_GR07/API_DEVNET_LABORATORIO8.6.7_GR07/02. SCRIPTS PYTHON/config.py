import os

# Datos que NO se escriben en el codigo (el token es secreto y caduca a las 12 h).
# Definirlos en PowerShell antes de ejecutar los scripts:
#   $env:WEBEX_TOKEN  = "<token personal de developer.webex.com>"
#   $env:WEBEX_EMAIL  = "<correo de un usuario Webex a agregar a la sala>"
# El ID de la sala lo guarda create-rooms.py en room_id.txt (o $env:WEBEX_ROOM_ID).

BASE_URL = "https://webexapis.com/v1"


def token():
    t = os.environ.get("WEBEX_TOKEN", "").strip()
    if not t:
        raise SystemExit("Falta la variable de entorno WEBEX_TOKEN (token personal de Webex).")
    return t


def headers(json_body=False):
    h = {"Authorization": "Bearer {}".format(token())}
    if json_body:
        h["Content-Type"] = "application/json"
    return h


def email():
    e = os.environ.get("WEBEX_EMAIL", "").strip()
    if not e:
        raise SystemExit("Falta la variable de entorno WEBEX_EMAIL (correo de un usuario Webex).")
    return e


def room_id():
    r = os.environ.get("WEBEX_ROOM_ID", "").strip()
    if r:
        return r
    try:
        with open(os.path.join(os.path.dirname(__file__), "room_id.txt")) as f:
            return f.read().strip()
    except FileNotFoundError:
        raise SystemExit("No hay ID de sala: ejecute primero create-rooms.py o defina WEBEX_ROOM_ID.")
