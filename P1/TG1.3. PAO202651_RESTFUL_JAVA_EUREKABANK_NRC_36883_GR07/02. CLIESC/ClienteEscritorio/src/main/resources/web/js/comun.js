// Utilidades comunes. La misma interfaz corre en el navegador (cliente web, que llama al servlet /api/*),
// en el cliente de escritorio y en el movil: ahi existe window.EurekaNative, un puente que llama a la API REST.
// El escritorio y el movil abren las paginas con ?nativo=1 y entregan el puente window.EurekaNative.
const NATIVO = location.search.indexOf("nativo=1") >= 0;
if (!NATIVO) {
  document.body.classList.add("con-fondo"); // solo el cliente web usa el fondo de login con panel curvo
}

/** Espera (hasta 5 s) a que el cliente nativo entregue el puente. En el navegador no espera. */
async function esperarPuente() {
  if (!NATIVO) return;
  for (let i = 0; i < 250 && !window.EurekaNative; i++) await new Promise((r) => setTimeout(r, 20));
}

/** Cambia de pagina conservando ?nativo=1. */
function irA(pagina) {
  location.replace(pagina + location.search);
}

const pendientes = {};
let contador = 0;
window.__eurekaResp = function (id, estado, texto) {
  const r = pendientes[id];
  delete pendientes[id];
  let datos = {};
  try { datos = JSON.parse(texto); } catch (e) { /* cuerpo vacio */ }
  if (r) r({ estado, datos });
};

async function api(ruta, opciones) {
  if (NATIVO) {
    return new Promise((resolve) => {
      const id = String(++contador);
      pendientes[id] = resolve;
      window.EurekaNative.llamar(id, (opciones && opciones.method) || "GET", ruta, (opciones && opciones.body) || "");
    });
  }
  try {
    const r = await fetch("api/" + ruta, opciones);
    const datos = await r.json().catch(() => ({}));
    return { estado: r.status, datos };
  } catch (e) {
    return { estado: 0, datos: { mensaje: "No se pudo conectar con el servidor" } };
  }
}

function enviarJson(cuerpo) {
  return { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(cuerpo) };
}

function dinero(v) {
  return Number(v).toLocaleString("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function el(etiqueta, clase, texto) {
  const e = document.createElement(etiqueta);
  if (clase) e.className = clase;
  if (texto != null) e.textContent = texto;
  return e;
}
