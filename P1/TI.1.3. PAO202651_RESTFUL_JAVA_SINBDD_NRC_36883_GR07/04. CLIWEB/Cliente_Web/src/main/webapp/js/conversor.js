const $ = (id) => document.getElementById(id);
const estado = $("estado");
const campos = ["categoria", "origen", "destino", "valor", "btnConvertir", "btnIntercambiar"].map($);

function habilitar(si) {
  campos.forEach((c) => (c.disabled = !si));
}

function llenar(select, items) {
  select.replaceChildren(
    ...items.map((t) => {
      const o = document.createElement("option");
      o.value = t;
      o.textContent = t;
      return o;
    })
  );
}

// Si el servidor dice que el token ya no sirve (o no hay sesion), se avisa y se vuelve al login.
function sesionTerminada(r) {
  if (r.http === 401) {
    alert("Su sesión terminó. Inicie sesión nuevamente.");
    location.replace("index.html");
    return true;
  }
  return false;
}

function errorDe(r) {
  const d = r.datos || {};
  return d.codigo ? "[" + d.codigo + "] " + d.mensaje : "Error inesperado (HTTP " + r.http + ")";
}

async function cargarCategorias() {
  mostrarEstado(estado, "Cargando catálogo...", "info");
  const r = await api("api/catalogo");
  if (sesionTerminada(r)) {
    return;
  }
  if (!r.datos || r.datos.estado !== "OK") {
    mostrarEstado(estado, errorDe(r), "error");
    return;
  }
  llenar($("categoria"), r.datos.datos);
  await cargarUnidades();
}

async function cargarUnidades() {
  habilitar(false);
  const cat = $("categoria").value;
  mostrarEstado(estado, "Cargando unidades de " + cat + "...", "info");
  const r = await api("api/catalogo?categoria=" + encodeURIComponent(cat));
  if (sesionTerminada(r)) {
    return;
  }
  if (!r.datos || r.datos.estado !== "OK") {
    mostrarEstado(estado, errorDe(r), "error");
    $("categoria").disabled = false;
    return;
  }
  llenar($("origen"), r.datos.datos);
  llenar($("destino"), r.datos.datos);
  if (r.datos.datos.length > 1) {
    $("destino").selectedIndex = 1;
  }
  $("resultado").textContent = "Resultado";
  mostrarEstado(estado, "", "");
  habilitar(true);
}

$("categoria").addEventListener("change", cargarUnidades);

$("btnIntercambiar").addEventListener("click", () => {
  const o = $("origen").value;
  $("origen").value = $("destino").value;
  $("destino").value = o;
});

$("formConversion").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const texto = $("valor").value.trim().replace(",", ".");
  if (texto === "" || isNaN(Number(texto))) {
    mostrarEstado(estado, "Ingrese un número válido (ejemplo: 12.5).", "error");
    return;
  }
  $("btnConvertir").disabled = true;
  mostrarEstado(estado, "Convirtiendo...", "info");
  try {
    const r = await api("api/convertir", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: formulario({
        categoria: $("categoria").value,
        valor: texto,
        origen: $("origen").value,
        destino: $("destino").value,
      }),
    });
    if (sesionTerminada(r)) {
      return;
    }
    const d = r.datos;
    if (d && d.estado === "OK" && d.resultadoTexto !== null) {
      $("resultado").textContent = Number(texto) + " " + d.unidadOrigen + " = " + d.resultadoTexto + " " + d.unidadDestino;
      mostrarEstado(estado, d.mensaje, "exito");
    } else {
      mostrarEstado(estado, errorDe(r), "error");
    }
  } catch (e) {
    mostrarEstado(estado, "No se pudo contactar con el cliente web.", "error");
  } finally {
    $("btnConvertir").disabled = false;
  }
});

$("btnCerrar").addEventListener("click", async () => {
  await api("api/sesion", { method: "POST" });
  location.replace("index.html");
});

cargarCategorias();
