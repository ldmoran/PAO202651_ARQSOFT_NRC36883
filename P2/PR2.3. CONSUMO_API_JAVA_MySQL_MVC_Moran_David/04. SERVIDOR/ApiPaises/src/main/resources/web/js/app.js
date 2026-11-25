// Atlas de Países: cliente web del taller. Solo presenta; las reglas viven en el servidor (patron MVC).
const API = (new URLSearchParams(location.search).get("api") || "") + "/api";
const REGIONES = ["América", "Europa", "Asia", "África", "Oceanía", "Antártida"];
const $ = (id) => document.getElementById(id);
const nf = new Intl.NumberFormat("es-EC");
const compacto = new Intl.NumberFormat("es-EC", { notation: "compact", maximumFractionDigits: 1 });

const estado = { paises: [], region: "", seleccionado: null, esNuevo: false, temporizador: null, vistaActual: "paises" };

// ------------------------------------------------------------------ utilidades
function esc(t) {
  return String(t ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

async function llamar(metodo, ruta, cuerpo) {
  let r;
  try {
    r = await fetch(API + ruta, { method: metodo, headers: { "Content-Type": "application/json" }, body: cuerpo ? JSON.stringify(cuerpo) : undefined });
  } catch (e) {
    throw new Error("No hay conexión con el servidor. ¿Está encendido?");
  }
  const datos = r.status === 204 ? null : await r.json().catch(() => ({}));
  if (!r.ok) throw new Error((datos && datos.error) || "Algo salió mal (" + r.status + ")");
  return datos;
}

function aviso(texto, tipo = "ok") {
  const nodo = document.createElement("div");
  nodo.className = "aviso" + (tipo === "error" ? " error-aviso" : "");
  nodo.innerHTML = `<svg class="ic"><use href="#i-${tipo === "error" ? "cerrar" : "check"}"/></svg><span>${esc(texto)}</span>`;
  $("avisos").appendChild(nodo);
  setTimeout(() => { nodo.classList.add("salida"); setTimeout(() => nodo.remove(), 200); }, tipo === "error" ? 5200 : 3200);
}

function bandera(url) {
  return url ? `<img class="bandera" src="${esc(url)}" alt="" loading="lazy" width="34" height="24">` : `<span class="bandera bandera-vacia"></span>`;
}

// ------------------------------------------------------------------ vistas
const VISTAS = ["paises", "api", "reportes"];
function mostrarVista(nombre) {
  if (!VISTAS.includes(nombre)) nombre = "paises";
  estado.vistaActual = nombre;
  VISTAS.forEach((v) => { $("vista-" + v).hidden = v !== nombre; });
  document.querySelectorAll("#menu a").forEach((a) => a.classList.toggle("activo", a.dataset.vista === nombre));
  if (nombre !== "paises") cerrarDetalle();
  if (nombre === "reportes") cargarReportes();
  if (nombre === "api") $("apiNombre").focus({ preventScroll: true });
}
window.addEventListener("hashchange", () => mostrarVista(location.hash.slice(1)));

// ------------------------------------------------------------------ lista de países
function filtros() {
  const p = new URLSearchParams();
  if ($("fNombre").value.trim()) p.set("nombre", $("fNombre").value.trim());
  if ($("fCapital").value.trim()) p.set("capital", $("fCapital").value.trim());
  if (estado.region) p.set("region", estado.region);
  return p.toString();
}

async function cargarPaises(animar = false) {
  try {
    estado.paises = await llamar("GET", "/paises?" + filtros());
    pintarLista(animar);
  } catch (e) {
    pintarError(e.message);
  }
}

function pintarChips() {
  const todas = ["", ...REGIONES];
  $("regiones").innerHTML = todas.map((r) =>
    `<button type="button" class="chip" data-region="${esc(r)}" aria-pressed="${estado.region === r}">${r || "Todas"}</button>`).join("");
}

function pintarLista(animar = false) {
  const lista = estado.paises;
  const hay = lista.length > 0;
  $("tabla").hidden = !hay;
  $("vacio").hidden = hay;
  const buscando = filtros() !== "";
  $("resumen").innerHTML = hay ? `Mostrando <b class="num">${nf.format(lista.length)}</b> ${lista.length === 1 ? "país" : "países"}` : "";
  if (!hay) {
    $("vacio").innerHTML = buscando
      ? `<h3>Sin resultados</h3><p>Ningún país coincide con esa búsqueda. Prueba con otro nombre o quita el filtro de región.</p>`
      : `<h3>Aún no hay países</h3><p>La base de datos está vacía. Importa los países desde la API pública y aparecerán aquí.</p><button class="boton boton-azul" type="button" id="vacioImportar"><span>Importar países</span></button>`;
    const b = $("vacioImportar");
    if (b) b.addEventListener("click", importar);
    return;
  }
  $("tabla").classList.toggle("anima", animar);
  $("filas").innerHTML = lista.map((p, i) => `
    <tr tabindex="0" data-id="${p.id}" style="--i:${i}" aria-selected="${estado.seleccionado === p.id}">
      <td><div class="pais">${bandera(p.bandera)}<span>${esc(p.nombre)}<span class="sub">${esc(p.subregion)}</span></span></div></td>
      <td class="c-capital">${esc(p.capital) || "—"}</td>
      <td class="c-region">${esc(p.region)}</td>
      <td class="der">${nf.format(p.poblacion)}</td>
      <td class="der c-area">${nf.format(Math.round(p.area))}</td>
      <td class="c-moneda">${esc(p.moneda) || "—"}</td>
    </tr>`).join("");
}

function pintarError(mensaje) {
  $("tabla").hidden = true;
  $("vacio").hidden = false;
  $("resumen").textContent = "";
  $("vacio").innerHTML = `<h3>No se pudo cargar</h3><p>${esc(mensaje)}</p><button class="boton boton-azul" type="button" id="reintentar"><span>Reintentar</span></button>`;
  $("reintentar").addEventListener("click", () => { cargarPaises(); actualizarTotal(); });
}

async function actualizarTotal() {
  try {
    const r = await llamar("GET", "/reportes");
    animarNumero($("totalBase"), r.total);
    $("textoTotal").textContent = r.total === 1 ? "país en la base de datos" : "países en la base de datos";
  } catch (e) { /* el error ya se muestra en la lista */ }
}

function animarNumero(nodo, destino) {
  const inicio = parseInt(nodo.dataset.v || "0", 10);
  nodo.dataset.v = destino;
  if (matchMedia("(prefers-reduced-motion: reduce)").matches || inicio === destino) { nodo.textContent = nf.format(destino); return; }
  const t0 = performance.now(), dur = 700;
  const paso = (t) => {
    const k = Math.min(1, (t - t0) / dur), e = 1 - Math.pow(1 - k, 3);
    nodo.textContent = nf.format(Math.round(inicio + (destino - inicio) * e));
    if (k < 1) requestAnimationFrame(paso);
  };
  requestAnimationFrame(paso);
}

// ------------------------------------------------------------------ importar
async function importar() {
  const botones = [$("btnImportar"), $("vacioImportar")].filter(Boolean);
  botones.forEach((b) => { b.disabled = true; b.classList.add("cargando"); });
  $("notaImportar").textContent = "Descargando países de la API y guardándolos...";
  try {
    const r = await llamar("POST", "/importar");
    aviso(`${nf.format(r.recibidos)} países recibidos: ${nf.format(r.nuevos)} nuevos y ${nf.format(r.actualizados)} actualizados.`);
    await Promise.all([cargarPaises(true), actualizarTotal()]);
  } catch (e) {
    aviso(e.message, "error");
  } finally {
    botones.forEach((b) => { b.disabled = false; b.classList.remove("cargando"); });
    $("notaImportar").textContent = "Descarga los países de la API pública y los guarda en la base sin duplicarlos.";
  }
}

// ------------------------------------------------------------------ panel de detalle (crear, editar, eliminar)
function abrirDetalle(pais) {
  estado.esNuevo = !pais;
  estado.seleccionado = pais ? pais.id : null;
  const p = pais || { nombre: "", codigo: "", capital: "", region: "América", subregion: "", poblacion: 0, area: 0, moneda: "", bandera: "" };
  $("t-detalle").textContent = pais ? pais.nombre : "Nuevo país";
  $("dNombre").value = p.nombre; $("dCodigo").value = p.codigo; $("dCapital").value = p.capital; $("dRegion").value = REGIONES.includes(p.region) ? p.region : "América";
  $("dSubregion").value = p.subregion; $("dPoblacion").value = p.poblacion ? nf.format(p.poblacion) : ""; $("dArea").value = p.area ? String(p.area) : "";
  $("dMoneda").value = p.moneda; $("dBanderaUrl").value = p.bandera;
  const img = $("dBandera");
  img.hidden = !p.bandera; if (p.bandera) img.src = p.bandera;
  $("dError").hidden = true;
  $("btnEliminar").hidden = !pais;
  resetearEliminar();
  $("detalle").hidden = false;
  document.querySelector(".app").classList.add("con-detalle");
  document.querySelectorAll("#filas tr").forEach((tr) => tr.setAttribute("aria-selected", String(Number(tr.dataset.id) === estado.seleccionado)));
  setTimeout(() => $("dNombre").focus({ preventScroll: true }), 60);
}

function cerrarDetalle() {
  if ($("detalle").hidden) return;
  $("detalle").hidden = true;
  document.querySelector(".app").classList.remove("con-detalle");
  estado.seleccionado = null;
  document.querySelectorAll("#filas tr").forEach((tr) => tr.setAttribute("aria-selected", "false"));
}

function leerFormulario() {
  const entero = (t) => parseInt(String(t).replace(/[^\d]/g, ""), 10) || 0;
  return {
    nombre: $("dNombre").value, codigo: $("dCodigo").value, capital: $("dCapital").value, region: $("dRegion").value, subregion: $("dSubregion").value,
    poblacion: entero($("dPoblacion").value), area: parseFloat(String($("dArea").value).replace(",", ".")) || 0, moneda: $("dMoneda").value, bandera: $("dBanderaUrl").value,
  };
}

async function guardar(ev) {
  ev.preventDefault();
  const boton = $("btnGuardar");
  boton.disabled = true;
  $("dError").hidden = true;
  try {
    const datos = leerFormulario();
    if (estado.esNuevo) await llamar("POST", "/paises", datos);
    else await llamar("PUT", "/paises/" + estado.seleccionado, datos);
    aviso(estado.esNuevo ? "País creado" : "Cambios guardados");
    cerrarDetalle();
    await Promise.all([cargarPaises(), actualizarTotal()]);
  } catch (e) {
    $("dError").textContent = e.message;
    $("dError").hidden = false;
  } finally {
    boton.disabled = false;
  }
}

let pasoEliminar = 0;
function resetearEliminar() {
  pasoEliminar = 0;
  clearTimeout(estado.temporizador);
  $("btnEliminar").classList.remove("confirmar");
  $("btnEliminar").querySelector("span").textContent = "Eliminar";
}
async function eliminar() {
  if (pasoEliminar === 0) {
    pasoEliminar = 1;
    $("btnEliminar").classList.add("confirmar");
    $("btnEliminar").querySelector("span").textContent = "Toca otra vez para confirmar";
    estado.temporizador = setTimeout(resetearEliminar, 3500);
    return;
  }
  try {
    await llamar("DELETE", "/paises/" + estado.seleccionado);
    aviso("País eliminado");
    cerrarDetalle();
    await Promise.all([cargarPaises(), actualizarTotal()]);
  } catch (e) {
    $("dError").textContent = e.message;
    $("dError").hidden = false;
    resetearEliminar();
  }
}

// ------------------------------------------------------------------ consultar la API (actividad 1)
async function consultarApi(ev) {
  ev.preventDefault();
  const nombre = $("apiNombre").value.trim();
  const lista = $("listaApi");
  if (!nombre) { aviso("Escribe el nombre de un país", "error"); return; }
  lista.innerHTML = `<li><div class="dato"><span>Consultando la API...</span></div></li>`;
  try {
    const r = await llamar("GET", "/externo?nombre=" + encodeURIComponent(nombre));
    if (!r.length) { lista.innerHTML = `<li><div class="dato"><b>Sin resultados</b><span>La API no tiene ningún país llamado «${esc(nombre)}».</span></div></li>`; return; }
    lista.innerHTML = r.map((p, i) => `
      <li style="--i:${i}">
        ${bandera(p.bandera)}
        <div class="dato"><b>${esc(p.nombre)}</b><span>${esc(p.capital) || "Sin capital"} · ${esc(p.region)} · ${nf.format(p.poblacion)} habitantes</span></div>
        <button class="boton boton-azul" type="button" data-guardar="${esc(p.codigo)}" data-nombre="${esc(p.nombre)}"><span>Guardar en la base</span></button>
      </li>`).join("");
  } catch (e) {
    lista.innerHTML = `<li><div class="dato"><b>No se pudo consultar</b><span>${esc(e.message)}</span></div></li>`;
  }
}

async function guardarDesdeApi(boton) {
  boton.disabled = true;
  try {
    await llamar("POST", "/externo/guardar", { nombre: boton.dataset.nombre, codigo: boton.dataset.guardar });
    boton.outerHTML = `<span class="guardado"><svg class="ic"><use href="#i-check"/></svg>Guardado</span>`;
    actualizarTotal();
  } catch (e) {
    boton.disabled = false;
    aviso(e.message, "error");
  }
}

// ------------------------------------------------------------------ reportes (actividad 5)
async function cargarReportes() {
  try {
    const r = await llamar("GET", "/reportes");
    const mas = r.masPoblado;
    $("repPrincipal").innerHTML = mas
      ? `<p class="frase">El país más poblado es <em>${esc(mas.nombre)}</em>, con ${compacto.format(mas.poblacion)} de habitantes.</p>
         <p class="detalle-frase">${bandera(mas.bandera)}<span>${esc(mas.capital) || "Sin capital"} · ${esc(mas.region)} · ${nf.format(mas.poblacion)} personas</span></p>
         ${r.porRegion.length ? `<p class="dato-region">La región con más países es <b>${esc(r.porRegion[0].region)}</b>, con ${nf.format(r.porRegion[0].paises)}.</p>` : ""}`
      : `<p class="frase">Todavía no hay países para reportar.</p><p class="detalle-frase">Importa los países desde la API y vuelve aquí.</p>`;
    const max = Math.max(1, ...r.porRegion.map((x) => x.paises));
    $("repBarras").innerHTML = r.porRegion.map((x, i) => `
      <div class="barra" style="--i:${i}"><span class="nombre">${esc(x.region)}</span>
        <div class="pista"><div class="relleno" data-ancho="${(x.paises / max) * 100}"></div></div><span class="valor">${nf.format(x.paises)}</span></div>`).join("");
    requestAnimationFrame(() => requestAnimationFrame(() => document.querySelectorAll(".relleno").forEach((n) => { n.style.width = n.dataset.ancho + "%"; })));
    $("repTabla").innerHTML = r.porRegion.map((x) =>
      `<tr><td><b>${esc(x.region)}</b></td><td class="der">${nf.format(x.paises)}</td><td class="der">${nf.format(x.poblacionTotal)}</td><td class="der">${nf.format(x.promedio)}</td></tr>`).join("");
  } catch (e) {
    $("repPrincipal").innerHTML = `<p class="frase">No se pudieron cargar los reportes.</p><p class="detalle-frase">${esc(e.message)}</p>`;
  }
}

// ------------------------------------------------------------------ eventos
function buscarConRetraso() {
  clearTimeout(buscarConRetraso.t);
  buscarConRetraso.t = setTimeout(cargarPaises, 220);
}

function iniciar() {
  document.querySelectorAll("#tabla thead th").forEach((th, i) => { th.classList.add(["c-pais", "c-capital", "c-region", "c-poblacion", "c-area", "c-moneda"][i]); });
  pintarChips();
  $("busqueda").addEventListener("submit", (e) => { e.preventDefault(); cargarPaises(); });
  $("fNombre").addEventListener("input", buscarConRetraso);
  $("fCapital").addEventListener("input", buscarConRetraso);
  $("regiones").addEventListener("click", (e) => {
    const b = e.target.closest(".chip"); if (!b) return;
    estado.region = b.dataset.region; pintarChips(); cargarPaises();
  });
  $("filas").addEventListener("click", (e) => {
    const tr = e.target.closest("tr"); if (!tr) return;
    abrirDetalle(estado.paises.find((p) => p.id === Number(tr.dataset.id)));
  });
  $("filas").addEventListener("keydown", (e) => {
    const tr = e.target.closest("tr"); if (!tr) return;
    if (e.key === "Enter" || e.key === " ") { e.preventDefault(); tr.click(); }
    if (e.key === "ArrowDown") { e.preventDefault(); (tr.nextElementSibling || tr).focus(); }
    if (e.key === "ArrowUp") { e.preventDefault(); (tr.previousElementSibling || tr).focus(); }
  });
  $("btnNuevo").addEventListener("click", () => abrirDetalle(null));
  $("btnCerrar").addEventListener("click", cerrarDetalle);
  $("formPais").addEventListener("submit", guardar);
  $("btnEliminar").addEventListener("click", eliminar);
  $("btnImportar").addEventListener("click", importar);
  $("formApi").addEventListener("submit", consultarApi);
  $("listaApi").addEventListener("click", (e) => { const b = e.target.closest("[data-guardar]"); if (b) guardarDesdeApi(b); });
  document.addEventListener("keydown", (e) => { if (e.key === "Escape") cerrarDetalle(); });
  mostrarVista(location.hash.slice(1));
  cargarPaises(true);
  actualizarTotal();
}
iniciar();
