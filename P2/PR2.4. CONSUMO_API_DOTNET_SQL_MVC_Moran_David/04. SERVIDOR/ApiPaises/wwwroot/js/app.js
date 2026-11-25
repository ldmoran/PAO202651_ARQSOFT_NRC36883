// Detalles de comportamiento de la web. El resto lo hace el servidor (MVC).

// Las filas de las tablas abren el formulario del pais.
document.querySelectorAll("tr[data-href]").forEach((fila) => {
  fila.tabIndex = 0;
  fila.addEventListener("click", () => { location.href = fila.dataset.href; });
  fila.addEventListener("keydown", (e) => { if (e.key === "Enter") location.href = fila.dataset.href; });
});

// Eliminar pide un segundo toque para confirmar.
document.querySelectorAll("[data-confirmar]").forEach((boton) => {
  boton.addEventListener("click", (e) => {
    if (boton.classList.contains("confirmar")) return;
    e.preventDefault();
    const texto = boton.querySelector("span");
    boton.classList.add("confirmar");
    boton.dataset.texto = texto.textContent;
    texto.textContent = "Toca otra vez para eliminar";
    setTimeout(() => { boton.classList.remove("confirmar"); texto.textContent = boton.dataset.texto; }, 3500);
  });
});

// Botones que tardan (importar, consultar): se bloquean y giran el icono.
document.querySelectorAll("form[data-ocupado]").forEach((form) => {
  form.addEventListener("submit", () => {
    const boton = form.querySelector("button[type=submit]");
    boton.classList.add("cargando");
    setTimeout(() => { boton.disabled = true; }, 0);
  });
});

// Los avisos se van solos.
document.querySelectorAll(".aviso").forEach((aviso) => {
  const espera = aviso.classList.contains("error-aviso") ? 5000 : 3200;
  setTimeout(() => { aviso.classList.add("salida"); setTimeout(() => aviso.remove(), 220); }, espera);
});

// Buscar mientras se escribe (con una pequena espera para no recargar en cada letra).
document.querySelectorAll("form[data-busqueda]").forEach((form) => {
  let t;
  form.querySelectorAll("input[type=search]").forEach((campo) => {
    campo.addEventListener("input", () => { clearTimeout(t); t = setTimeout(() => form.submit(), 450); });
  });
  const ultimo = form.querySelector("input[data-foco]");
  if (ultimo && ultimo.value) { ultimo.focus(); ultimo.setSelectionRange(ultimo.value.length, ultimo.value.length); }
});

// Borrar con icono: primer toque arma (se pone rojo), segundo toque confirma.
document.querySelectorAll('[data-confirmar-icono]').forEach((boton) => {
  boton.addEventListener('click', (e) => {
    if (boton.classList.contains('confirmar')) return;
    e.preventDefault();
    boton.classList.add('confirmar');
    setTimeout(() => boton.classList.remove('confirmar'), 3000);
  });
});
