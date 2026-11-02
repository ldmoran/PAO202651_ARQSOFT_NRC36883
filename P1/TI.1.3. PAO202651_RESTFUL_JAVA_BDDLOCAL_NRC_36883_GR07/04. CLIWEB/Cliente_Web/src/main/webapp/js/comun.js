// Utilidades comunes de las paginas del cliente web.
async function api(url, opciones) {
  const resp = await fetch(url, Object.assign({ credentials: "same-origin" }, opciones));
  let datos = null;
  try {
    datos = await resp.json();
  } catch (e) {
    // respuesta sin JSON
  }
  return { http: resp.status, datos: datos };
}

function formulario(objeto) {
  return new URLSearchParams(objeto).toString();
}

function mostrarEstado(elemento, texto, tipo) {
  elemento.textContent = texto || "";
  elemento.className = "estado " + (tipo || "");
}
