const estado = document.getElementById("estado");
const boton = document.getElementById("btnIngresar");

// Si ya hay sesion activa, se salta el login.
api("api/sesion").then((r) => {
  if (r.datos && r.datos.activa) {
    location.replace("conversor.html");
  }
});

document.getElementById("formLogin").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  boton.disabled = true;
  mostrarEstado(estado, "Conectando...", "info");
  try {
    const r = await api("api/login", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: formulario({
        usuario: document.getElementById("usuario").value,
        clave: document.getElementById("clave").value,
        servidor: document.getElementById("servidor").value,
      }),
    });
    if (r.datos && r.datos.estado === "OK") {
      location.replace("conversor.html");
    } else {
      const d = r.datos || {};
      mostrarEstado(estado, d.codigo ? "[" + d.codigo + "] " + d.mensaje : "No se pudo iniciar sesión", "error");
    }
  } catch (e) {
    mostrarEstado(estado, "No se pudo contactar con el cliente web.", "error");
  } finally {
    boton.disabled = false;
  }
});
