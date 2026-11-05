const estado = document.getElementById("estado");
const boton = document.getElementById("btnIngresar");

function mostrar(texto, clase) {
  estado.textContent = texto;
  estado.className = "estado " + (clase || "");
}

if (!NATIVO) {
  document.getElementById("servidor").placeholder = "http://localhost:8080/REST_EurekaBank_Java_GR07/api/";
}

async function ingresar() {
  if (boton.disabled) return;
  const usuario = document.getElementById("usuario").value;
  const clave = document.getElementById("clave").value;
  if (!usuario.trim() || !clave.trim()) {
    mostrar("Ingrese usuario y clave.", "error");
    return;
  }
  boton.disabled = true;
  mostrar("Verificando...", "info");
  const r = await api("login", enviarJson({ usuario, contrasena: clave, servidor: document.getElementById("servidor").value }));
  boton.disabled = false;
  if (r.estado === 200) {
    irA("panel.html");
  } else {
    mostrar(r.estado === 401 ? "Usuario o clave incorrectos." : (r.datos.mensaje || "No se pudo iniciar sesión."), "error");
  }
}

boton.addEventListener("click", ingresar);
document.getElementById("formLogin").addEventListener("keydown", (e) => { if (e.key === "Enter") ingresar(); });

(async () => {
  await esperarPuente();
  if (NATIVO) document.getElementById("servidor").value = window.EurekaNative.urlPorDefecto();
  const s = await api("sesion");
  if (s.datos && s.datos.activa) irA("panel.html");
})();
