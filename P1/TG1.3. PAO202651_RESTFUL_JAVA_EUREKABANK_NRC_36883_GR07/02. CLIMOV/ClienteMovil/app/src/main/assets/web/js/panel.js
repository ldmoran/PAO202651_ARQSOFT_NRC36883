// Panel de EurekaBank: Cuentas, Movimientos y Operar (mismo diseno de TG1.2).
const cont = document.getElementById("contenido");
let seccion = 0;
let cuentas = [];
let movimientos = [];
let tipos = ["DEPOSITO", "RETIRO", "TRANSFERENCIA"];
let cuentaMov = "";
let tipo = "DEPOSITO";
let origen = "";
let destino = "";
let importe = "";
let aviso = { texto: "", clase: "" };
let ocupado = false;

function titulo(t) {
  const s = t.toLowerCase().replace("deposito", "depósito");
  return s ? s[0].toUpperCase() + s.slice(1) : s;
}

function sesionTermino(r) {
  if (r.estado === 401) { irA("index.html"); return true; }
  return false;
}

function avisar(texto, clase) { aviso = { texto, clase: clase || "" }; }

// ---------------------------------------------------------------- datos
async function cargarCuentas() {
  const r = await api("cuentas");
  if (sesionTermino(r)) return;
  if (r.estado !== 200) { avisar(r.datos.mensaje, "eb-error"); return; }
  cuentas = r.datos;
  if (cuentas.length > 0) {
    if (!cuentas.some((c) => c.codigo === origen)) origen = cuentas[0].codigo;
    if (!cuentas.some((c) => c.codigo === destino)) destino = cuentas.length > 1 ? cuentas[1].codigo : cuentas[0].codigo;
  }
}

async function cargarMovimientos() {
  const r = await api("movimientos" + (cuentaMov ? "?cuenta=" + encodeURIComponent(cuentaMov) : ""));
  if (sesionTermino(r)) return;
  if (r.estado !== 200) { avisar(r.datos.mensaje, "eb-error"); return; }
  movimientos = r.datos;
  avisar(movimientos.length + " movimientos.", "eb-info");
}

async function ir(i) {
  seccion = i;
  avisar("", "");
  if (i === 1) await cargarMovimientos(); else await cargarCuentas();
  pintar();
}

async function registrar() {
  if (ocupado) return;
  const valor = parseFloat(String(importe).replace(",", "."));
  if (Number.isNaN(valor) || valor <= 0) { avisar("Ingrese un importe mayor a 0 (por ejemplo 150.50).", "eb-error"); pintar(); return; }
  ocupado = true; pintar();
  const r = await api("operar", enviarJson({ cuentaOrigen: origen, cuentaDestino: tipo === "TRANSFERENCIA" ? destino : "", tipo, importe: valor }));
  ocupado = false;
  if (sesionTermino(r)) return;
  if (r.estado === 200) {
    importe = "";
    await cargarCuentas();
    avisar(r.datos.mensaje, "eb-exito");
  } else {
    avisar(r.datos.mensaje, "eb-error");
  }
  pintar();
}

// ---------------------------------------------------------------- vistas
function opcion(valor, texto, elegido) {
  const o = el("option", null, texto);
  o.value = valor;
  if (valor === elegido) o.selected = true;
  return o;
}

function vistaCuentas() {
  const h = el("h2", null, "Cuentas");
  const caja = el("div", "eb-tarjetas");
  for (const c of cuentas) {
    const a = el("article", "eb-cuenta");
    const cab = el("header");
    cab.append(el("span", "eb-codigo", c.codigo), el("span", "eb-chip eb-ok", c.estado));
    a.append(cab, el("h3", null, c.nombreCliente), el("div", "eb-saldo", c.moneda + " " + dinero(c.saldo)),
      el("small", null, (c.emailCliente || "") + " · " + (c.telefonoCliente || "")));
    const acc = el("div", "eb-acciones");
    const b = el("button", "eb-sec", "Ver movimientos");
    b.type = "button";
    b.addEventListener("click", async () => { cuentaMov = c.codigo; seccion = 1; await cargarMovimientos(); pintar(); });
    acc.append(b);
    a.append(acc);
    caja.append(a);
  }
  return [h, caja];
}

function vistaMovimientos() {
  const h = el("h2", null, "Movimientos");
  const barra = el("div", "eb-barra");
  const sel = el("select");
  sel.append(opcion("", "Todas las cuentas", cuentaMov));
  for (const c of cuentas) sel.append(opcion(c.codigo, c.codigo + " — " + c.nombreCliente, cuentaMov));
  sel.addEventListener("change", async () => { cuentaMov = sel.value; await cargarMovimientos(); pintar(); });
  const act = el("button", null, "Actualizar");
  act.type = "button";
  act.addEventListener("click", async () => { await cargarMovimientos(); pintar(); });
  barra.append(sel, act);

  const caja = el("div", "eb-tabla-caja");
  const tabla = el("table", "eb-tabla");
  const cab = el("thead");
  const fila = el("tr");
  [["Cuenta"], ["N.º"], ["Fecha"], ["Tipo"], ["Importe", "der"], ["Saldo", "der"], ["Referencia"]].forEach(([t, c]) => fila.append(el("th", c, t)));
  cab.append(fila);
  const cuerpo = el("tbody");
  for (const m of movimientos) {
    const entrada = String(m.tipo).endsWith("Entrada");
    const tr = el("tr");
    const tdTipo = el("td");
    tdTipo.append(el("span", "eb-chip " + (entrada ? "eb-ok" : "eb-mal"), m.tipo));
    tr.append(el("td", null, m.codigoCuenta), el("td", null, m.numero), el("td", null, m.fecha), tdTipo,
      el("td", "der " + (entrada ? "eb-pos" : "eb-neg"), (entrada ? "+" : "−") + dinero(m.importe)),
      el("td", "der", dinero(m.saldoActual)), el("td", null, m.referencia || ""));
    cuerpo.append(tr);
  }
  tabla.append(cab, cuerpo);
  caja.append(tabla);
  return [h, barra, caja];
}

function vistaOperar() {
  const h = el("h2", null, "Operar");
  const f = el("form", "eb-form");
  f.addEventListener("submit", (e) => { e.preventDefault(); registrar(); });

  f.append(Object.assign(el("label", null, "Tipo de operación"), { htmlFor: "tipo" }));
  const sTipo = el("select"); sTipo.id = "tipo";
  for (const t of tipos) sTipo.append(opcion(t, titulo(t), tipo));
  sTipo.addEventListener("change", () => { tipo = sTipo.value; pintar(); });
  f.append(sTipo);

  f.append(Object.assign(el("label", null, "Cuenta " + (tipo === "TRANSFERENCIA" ? "origen" : "")), { htmlFor: "origen" }));
  const sOrigen = el("select"); sOrigen.id = "origen";
  for (const c of cuentas) sOrigen.append(opcion(c.codigo, c.codigo + " — " + c.nombreCliente + " (" + dinero(c.saldo) + ")", origen));
  sOrigen.addEventListener("change", () => { origen = sOrigen.value; });
  f.append(sOrigen);

  if (tipo === "TRANSFERENCIA") {
    f.append(Object.assign(el("label", null, "Cuenta destino"), { htmlFor: "destino" }));
    const sDestino = el("select"); sDestino.id = "destino";
    for (const c of cuentas) sDestino.append(opcion(c.codigo, c.codigo + " — " + c.nombreCliente, destino));
    sDestino.addEventListener("change", () => { destino = sDestino.value; });
    f.append(sDestino);
  }

  f.append(Object.assign(el("label", null, "Importe"), { htmlFor: "importe" }));
  const inp = el("input"); inp.id = "importe"; inp.inputMode = "decimal"; inp.placeholder = "0.00"; inp.value = importe;
  inp.addEventListener("input", () => { importe = inp.value; });
  f.append(inp);

  const b = el("button", "eb-principal", "Registrar " + titulo(tipo).toLowerCase());
  b.type = "submit"; b.disabled = ocupado;
  f.append(b);
  return [h, f];
}

function pintar() {
  document.querySelectorAll(".eb-nav").forEach((b) => b.classList.toggle("activo", Number(b.dataset.seccion) === seccion));
  cont.replaceChildren();
  if (aviso.texto) {
    const a = el("div", "eb-aviso " + aviso.clase, aviso.texto);
    a.setAttribute("role", "status");
    cont.append(a);
  }
  cont.append(...(seccion === 0 ? vistaCuentas() : seccion === 1 ? vistaMovimientos() : vistaOperar()));
}

document.querySelectorAll(".eb-nav").forEach((b) => b.addEventListener("click", () => ir(Number(b.dataset.seccion))));
document.getElementById("btnSalir").addEventListener("click", async () => { await api("salir", enviarJson({})); irA("index.html"); });

(async () => {
  await esperarPuente();
  const s = await api("sesion");
  if (!s.datos.activa) { irA("index.html"); return; }
  document.getElementById("usuarioActual").textContent = s.datos.usuario;
  await cargarCuentas();
  const t = await api("tipos");
  if (t.estado === 200 && t.datos.length > 0) { tipos = t.datos; tipo = tipos[0]; }
  pintar();
})();
