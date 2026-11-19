package ec.edu.monster.comercializadora.movil.vista;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ec.edu.monster.comercializadora.movil.R;
import ec.edu.monster.comercializadora.movil.controlador.ControladorMovil;
import ec.edu.monster.comercializadora.movil.modelo.CuotaAmortizacion;
import ec.edu.monster.comercializadora.movil.modelo.DetalleMontoMaximo;
import ec.edu.monster.comercializadora.movil.modelo.EvaluacionCredito;
import ec.edu.monster.comercializadora.movil.modelo.Factura;
import ec.edu.monster.comercializadora.movil.modelo.LineaFactura;
import ec.edu.monster.comercializadora.movil.modelo.Formato;
import ec.edu.monster.comercializadora.movil.modelo.ResultadoFactura;
import ec.edu.monster.comercializadora.movil.modelo.Telefono;
import java.util.ArrayList;
import java.util.List;

/**
 * Unica pantalla de la aplicacion: una barra inferior con cuatro secciones (catalogo, venta, credito y
 * facturas). La interfaz se arma por codigo para no depender de librerias externas.
 */
public class MainActivity extends Activity {

    private static final int NAVY = Color.parseColor("#0B2A4A");
    private static final int AZUL = Color.parseColor("#0F5C99");
    private static final int ORO = Color.parseColor("#F2B01E");
    private static final int FONDO = Color.parseColor("#F3F6FA");
    private static final int BORDE = Color.parseColor("#D5DDE8");
    private static final int TEXTO = Color.parseColor("#1F2937");
    private static final int GRIS = Color.parseColor("#6B7A8D");
    private static final int ERROR = Color.parseColor("#C0392B");
    private static final int EXITO = Color.parseColor("#1B7F3B");

    private ControladorMovil ctl;
    private LinearLayout contenido;
    private TextView lblEstado;
    private LinearLayout[] itemsNav;
    private LinearLayout panelServidor;
    private EditText txtServidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctl = new ControladorMovil(this);
        setContentView(armarPantalla());
        mostrarSeccion(0);
    }

    // ---------------- Estructura general ----------------

    private View armarPantalla() {
        LinearLayout raiz = new LinearLayout(this);
        raiz.setOrientation(LinearLayout.VERTICAL);
        raiz.setBackgroundColor(FONDO);
        raiz.setFitsSystemWindows(true);

        LinearLayout cabecera = new LinearLayout(this);
        cabecera.setOrientation(LinearLayout.VERTICAL);
        cabecera.setBackgroundColor(NAVY);
        LinearLayout fila = new LinearLayout(this);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setPadding(dp(16), dp(12), dp(8), dp(12));
        fila.addView(new Icono(this, 0, ORO, NAVY, true), new LinearLayout.LayoutParams(dp(40), dp(40)));
        LinearLayout nombres = new LinearLayout(this);
        nombres.setOrientation(LinearLayout.VERTICAL);
        nombres.setPadding(dp(12), 0, 0, 0);
        TextView titulo = new TextView(this);
        titulo.setText("Comercializadora");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(19);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        nombres.addView(titulo);
        TextView sub = new TextView(this);
        sub.setText("Venta de celulares · BanQuito");
        sub.setTextColor(Color.parseColor("#9DB4CC"));
        sub.setTextSize(12);
        nombres.addView(sub);
        fila.addView(nombres, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        FrameLayout ajustes = new FrameLayout(this);
        ajustes.addView(new Icono(this, 5, Color.WHITE, 0, false), new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER));
        ajustes.setOnClickListener(v -> panelServidor.setVisibility(panelServidor.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));
        fila.addView(ajustes, new LinearLayout.LayoutParams(dp(48), dp(48)));
        cabecera.addView(fila);

        panelServidor = new LinearLayout(this);
        panelServidor.setOrientation(LinearLayout.VERTICAL);
        panelServidor.setBackgroundColor(Color.parseColor("#123C66"));
        panelServidor.setPadding(dp(16), dp(10), dp(16), dp(12));
        panelServidor.setVisibility(View.GONE);
        TextView ayuda = new TextView(this);
        ayuda.setText("Servidor: en el emulador use 10.0.2.2; en un celular real, la IP de la PC.");
        ayuda.setTextColor(Color.parseColor("#9DB4CC"));
        ayuda.setTextSize(12);
        panelServidor.addView(ayuda);
        txtServidor = campo("http://IP:8080/Comercializadora/WSComercializadora");
        txtServidor.setText(ctl.url());
        txtServidor.setTextSize(13);
        panelServidor.addView(txtServidor);
        Button conectar = boton("Conectar", v -> {
            ctl.cambiarUrl(txtServidor.getText().toString().trim());
            panelServidor.setVisibility(View.GONE);
            mostrarSeccion(0);
        });
        conectar.setBackground(fondo(ORO, 10, 0));
        conectar.setTextColor(NAVY);
        panelServidor.addView(conectar);
        cabecera.addView(panelServidor);
        raiz.addView(cabecera);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(dp(16), dp(8), dp(16), dp(20));
        scroll.addView(contenido);
        raiz.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        lblEstado = new TextView(this);
        lblEstado.setPadding(dp(16), dp(6), dp(16), dp(6));
        lblEstado.setTextSize(13);
        lblEstado.setTypeface(Typeface.DEFAULT_BOLD);
        raiz.addView(lblEstado);

        LinearLayout nav = new LinearLayout(this);
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(dp(8));
        nav.setPadding(dp(6), dp(4), dp(6), dp(4));
        String[] nombresNav = {"Catálogo", "Venta", "Crédito", "Facturas"};
        itemsNav = new LinearLayout[nombresNav.length];
        for (int i = 0; i < nombresNav.length; i++) {
            final int seccion = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(0, dp(8), 0, dp(8));
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(24), dp(24));
            ilp.gravity = Gravity.CENTER_HORIZONTAL;
            item.addView(new Icono(this, i + 1, GRIS, 0, false), ilp);
            TextView t = new TextView(this);
            t.setText(nombresNav[i]);
            t.setTextSize(11);
            t.setGravity(Gravity.CENTER);
            item.addView(t);
            item.setOnClickListener(v -> mostrarSeccion(seccion));
            itemsNav[i] = item;
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            nlp.setMargins(dp(3), 0, dp(3), 0);
            nav.addView(item, nlp);
        }
        raiz.addView(nav);
        return raiz;
    }

    private void mostrarSeccion(int i) {
        for (int k = 0; k < itemsNav.length; k++) {
            boolean activo = k == i;
            Icono ic = (Icono) itemsNav[k].getChildAt(0);
            ic.color = activo ? AZUL : GRIS;
            ic.invalidate();
            TextView t = (TextView) itemsNav[k].getChildAt(1);
            t.setTextColor(activo ? AZUL : GRIS);
            t.setTypeface(activo ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            itemsNav[k].setBackground(activo ? fondo(Color.parseColor("#E6F0FA"), 12, 0) : null);
        }
        contenido.removeAllViews();
        estado("", false);
        if (i == 0) {
            seccionCatalogo();
        } else if (i == 1) {
            seccionVenta();
        } else if (i == 2) {
            seccionCredito();
        } else {
            seccionFacturas();
        }
    }

    // ---------------- Catalogo ----------------

    private void seccionCatalogo() {
        contenido.addView(titulo("Catálogo de celulares"));
        contenido.addView(boton("+  Agregar celular", v -> dialogoTelefono(null)));
        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        contenido.addView(lista);
        ctl.ejecutar(() -> ctl.servicio().listarTelefonos(), telefonos -> {
            lista.removeAllViews();
            for (Telefono t : telefonos) {
                LinearLayout tarjeta = tarjeta();
                LinearLayout cab = new LinearLayout(this);
                cab.setGravity(Gravity.CENTER_VERTICAL);
                cab.addView(new Icono(this, 1, AZUL, Color.parseColor("#E6F0FA"), true), new LinearLayout.LayoutParams(dp(40), dp(40)));
                LinearLayout datos = new LinearLayout(this);
                datos.setOrientation(LinearLayout.VERTICAL);
                datos.setPadding(dp(12), 0, dp(8), 0);
                datos.addView(texto(t.getMarca() + " " + t.getModelo(), true));
                TextView pr = texto(Formato.dinero(t.getPrecio()), true);
                pr.setTextColor(AZUL);
                datos.addView(pr);
                cab.addView(datos, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                cab.addView(chip(t.isDisponible() ? "Disponible" : "Agotado", t.isDisponible() ? EXITO : ERROR));
                tarjeta.addView(cab);
                LinearLayout fila = new LinearLayout(this);
                Button ed = botonSec("Editar", v -> dialogoTelefono(t));
                Button el = botonSec("Eliminar", v -> confirmarEliminar(t));
                el.setTextColor(ERROR);
                fila.addView(ed, peso());
                fila.addView(el, peso());
                tarjeta.addView(fila);
                lista.addView(tarjeta);
            }
            estado(telefonos.size() + " celulares.", false);
        }, m -> estado(m, true));
    }

    private void dialogoTelefono(Telefono actual) {
        LinearLayout f = new LinearLayout(this);
        f.setOrientation(LinearLayout.VERTICAL);
        f.setPadding(dp(20), dp(8), dp(20), 0);
        EditText marca = campo("Marca");
        EditText modelo = campo("Modelo");
        EditText precio = campo("Precio");
        precio.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        CheckBox disponible = new CheckBox(this);
        disponible.setText("Disponible");
        disponible.setChecked(true);
        if (actual != null) {
            marca.setText(actual.getMarca());
            modelo.setText(actual.getModelo());
            precio.setText(String.valueOf(actual.getPrecio()));
            disponible.setChecked(actual.isDisponible());
        }
        f.addView(marca);
        f.addView(modelo);
        f.addView(precio);
        f.addView(disponible);
        new AlertDialog.Builder(this).setTitle(actual == null ? "Agregar celular" : "Editar celular").setView(f)
                .setPositiveButton("Guardar", (d, w) -> {
                    Double p = numero(precio.getText().toString());
                    if (p == null) {
                        estado("El precio debe ser un numero, por ejemplo 199.99.", true);
                        return;
                    }
                    String ma = marca.getText().toString();
                    String mo = modelo.getText().toString();
                    if (actual == null) {
                        ctl.ejecutar(() -> ctl.servicio().crearTelefono(ma, mo, p), cod -> {
                            if (cod > 0) {
                                mostrarSeccion(0);
                                estado("Celular agregado con codigo " + cod + ".", false);
                            } else {
                                estado("Complete marca, modelo y un precio mayor a cero.", true);
                            }
                        }, m -> estado(m, true));
                    } else {
                        ctl.ejecutar(() -> ctl.servicio().actualizarTelefono(actual.getCodTelefono(), ma, mo, p,
                                disponible.isChecked()), ok -> {
                                    mostrarSeccion(0);
                                    estado(ok ? "Celular actualizado." : "No se pudo actualizar.", !ok);
                                }, m -> estado(m, true));
                    }
                }).setNegativeButton("Cancelar", null).show();
    }

    private void confirmarEliminar(Telefono t) {
        new AlertDialog.Builder(this).setTitle("Eliminar").setMessage(t.getMarca() + " " + t.getModelo() + "?")
                .setPositiveButton("Eliminar", (d, w) -> ctl.ejecutar(() -> ctl.servicio().eliminarTelefono(t.getCodTelefono()),
                        ok -> {
                            mostrarSeccion(0);
                            estado(ok ? "Celular eliminado (si ya tenia facturas queda como no disponible)." : "No existe.", !ok);
                        }, m -> estado(m, true)))
                .setNegativeButton("Cancelar", null).show();
    }

    // ---------------- Venta ----------------

    private void seccionVenta() {
        contenido.addView(titulo("Venta de celulares"));
        EditText cedula = campo("Cédula (10 dígitos)");
        cedula.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText nombre = campo("Nombre (solo si es cliente nuevo)");
        RadioGroup forma = new RadioGroup(this);
        RadioButton efectivo = new RadioButton(this);
        efectivo.setText("Efectivo (42 % de descuento)");
        efectivo.setId(View.generateViewId());
        RadioButton credito = new RadioButton(this);
        credito.setText("Credito");
        credito.setId(View.generateViewId());
        forma.addView(efectivo);
        forma.addView(credito);
        efectivo.setChecked(true);
        EditText plazo = campo("Plazo en meses (3 a 18)");
        plazo.setInputType(InputType.TYPE_CLASS_NUMBER);
        plazo.setText("6");
        LinearLayout resultado = new LinearLayout(this);
        resultado.setOrientation(LinearLayout.VERTICAL);

        contenido.addView(cedula);
        contenido.addView(nombre);
        TextView ayuda = texto("Celulares de esta compra (una sola factura): escriba la cantidad de cada uno", true);
        ayuda.setTextSize(13);
        ayuda.setTextColor(GRIS);
        ayuda.setPadding(0, dp(14), 0, dp(4));
        contenido.addView(ayuda);
        LinearLayout listaCompra = tarjeta();
        contenido.addView(listaCompra);
        contenido.addView(forma);
        contenido.addView(plazo);
        Button vender = boton("Vender", null);
        vender.setBackground(fondo(ORO, 10, 0));
        vender.setTextColor(NAVY);
        contenido.addView(vender);
        contenido.addView(resultado);

        List<Telefono> disponibles = new ArrayList<>();
        List<EditText> cantidades = new ArrayList<>();
        ctl.ejecutar(() -> ctl.servicio().listarTelefonos(), lista -> {
            for (Telefono t : lista) {
                if (!t.isDisponible()) {
                    continue;
                }
                disponibles.add(t);
                LinearLayout fila = new LinearLayout(this);
                fila.setGravity(Gravity.CENTER_VERTICAL);
                fila.setPadding(0, dp(4), 0, dp(4));
                LinearLayout datos = new LinearLayout(this);
                datos.setOrientation(LinearLayout.VERTICAL);
                datos.addView(texto(t.getMarca() + " " + t.getModelo(), true));
                TextView pr = texto(Formato.dinero(t.getPrecio()), false);
                pr.setTextColor(AZUL);
                datos.addView(pr);
                fila.addView(datos, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                EditText n = new EditText(this);
                n.setInputType(InputType.TYPE_CLASS_NUMBER);
                n.setText("0");
                n.setGravity(Gravity.CENTER);
                n.setSelectAllOnFocus(true);
                n.setBackground(fondo(Color.WHITE, 10, BORDE));
                cantidades.add(n);
                fila.addView(n, new LinearLayout.LayoutParams(dp(70), dp(44)));
                listaCompra.addView(fila);
            }
        }, m -> estado(m, true));

        vender.setOnClickListener(v -> {
            List<Integer> codigos = new ArrayList<>();
            for (int i = 0; i < disponibles.size(); i++) {
                Integer n = entero(cantidades.get(i).getText().toString());
                for (int k = 0; n != null && k < Math.min(n, 20); k++) {
                    codigos.add(disponibles.get(i).getCodTelefono());
                }
            }
            if (codigos.isEmpty()) {
                estado("Escriba la cantidad de al menos un celular.", true);
                return;
            }
            String ced = cedula.getText().toString().trim();
            String nom = nombre.getText().toString().trim();
            boolean aCredito = credito.isChecked();
            Integer meses = entero(plazo.getText().toString());
            if (aCredito && meses == null) {
                estado("Ingrese el plazo en meses.", true);
                return;
            }
            estado("Procesando...", false);
            ctl.ejecutar(() -> {
                ResultadoFactura r = aCredito ? ctl.servicio().facturarCreditoVarios(ced, nom, codigos, meses)
                        : ctl.servicio().facturarEfectivoVarios(ced, nom, codigos);
                List<CuotaAmortizacion> cuotas = r.isExito() && aCredito
                        ? ctl.servicio().obtenerAmortizacion(r.getFactura().getCodCredito()) : new ArrayList<>();
                return new Object[] {r, cuotas};
            }, par -> {
                ResultadoFactura r = (ResultadoFactura) par[0];
                @SuppressWarnings("unchecked")
                List<CuotaAmortizacion> cuotas = (List<CuotaAmortizacion>) par[1];
                resultado.removeAllViews();
                estado(r.getMensaje(), !r.isExito());
                if (r.isExito() && r.getFactura() != null) {
                    for (EditText n : cantidades) {
                        n.setText("0");
                    }
                    resultado.addView(tarjetaFactura(r.getFactura()));
                    if (!cuotas.isEmpty()) {
                        resultado.addView(tablaAmortizacion(cuotas));
                    }
                }
            }, m -> estado(m, true));
        });
    }

    // ---------------- Credito ----------------

    private void seccionCredito() {
        contenido.addView(titulo("Sujeto de crédito"));
        EditText cedula = campo("Cédula (10 dígitos)");
        cedula.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout resultado = new LinearLayout(this);
        resultado.setOrientation(LinearLayout.VERTICAL);
        contenido.addView(cedula);
        contenido.addView(boton("Verificar sujeto de crédito", v -> {
            String ced = cedula.getText().toString().trim();
            estado("Consultando al banco...", false);
            ctl.ejecutar(() -> {
                EvaluacionCredito ev = ctl.servicio().verificarSujetoCredito(ced);
                DetalleMontoMaximo d = ev.isSujetoCredito() ? ctl.servicio().calcularMontoMaximo(ced) : null;
                return new Object[] {ev, d};
            }, par -> {
                EvaluacionCredito ev = (EvaluacionCredito) par[0];
                DetalleMontoMaximo d = (DetalleMontoMaximo) par[1];
                resultado.removeAllViews();
                estado("", false);
                LinearLayout t = tarjeta();
                TextView veredicto = texto(ev.isSujetoCredito() ? "Sí es sujeto de crédito" : "No es sujeto de crédito", true);
                veredicto.setTextColor(ev.isSujetoCredito() ? EXITO : ERROR);
                veredicto.setTextSize(18);
                t.addView(veredicto);
                t.addView(texto(ev.getMotivo(), false));
                if (d != null) {
                    t.addView(texto("Cliente: " + d.getNombre(), false));
                    t.addView(texto("Promedio de depositos (3 meses): " + Formato.dinero(d.getPromedioDepositos()), false));
                    t.addView(texto("Promedio de retiros (3 meses): " + Formato.dinero(d.getPromedioRetiros()), false));
                    TextView mm = texto("Monto máximo: " + Formato.dinero(d.getMontoMaximo()), true);
                    mm.setTextColor(AZUL);
                    mm.setTextSize(17);
                    t.addView(mm);
                }
                resultado.addView(t);
            }, m -> estado(m, true));
        }));
        contenido.addView(resultado);
    }

    // ---------------- Facturas ----------------

    private void seccionFacturas() {
        contenido.addView(titulo("Facturas de un cliente"));
        EditText cedula = campo("Cédula (10 dígitos)");
        cedula.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        LinearLayout cuotas = new LinearLayout(this);
        cuotas.setOrientation(LinearLayout.VERTICAL);
        contenido.addView(cedula);
        contenido.addView(boton("Buscar", v -> ctl.ejecutar(() -> ctl.servicio().consultarFacturas(cedula.getText().toString().trim()),
                facturas -> {
                    lista.removeAllViews();
                    cuotas.removeAllViews();
                    estado(facturas.isEmpty() ? "El cliente no tiene facturas." : facturas.size() + " facturas.", false);
                    for (Factura f : facturas) {
                        LinearLayout t = tarjetaFactura(f);
                        if (f.getCodCredito() > 0) {
                            t.addView(boton("Ver cuotas del credito " + f.getCodCredito(), b -> ctl.ejecutar(
                                    () -> ctl.servicio().obtenerAmortizacion(f.getCodCredito()), c -> {
                                        cuotas.removeAllViews();
                                        cuotas.addView(tablaAmortizacion(c));
                                    }, m -> estado(m, true))));
                        }
                        lista.addView(t);
                    }
                }, m -> estado(m, true))));
        contenido.addView(lista);
        contenido.addView(cuotas);
    }

    // ---------------- Piezas de interfaz ----------------

    private LinearLayout tarjetaFactura(Factura f) {
        LinearLayout t = tarjeta();
        LinearLayout cab = new LinearLayout(this);
        cab.setGravity(Gravity.CENTER_VERTICAL);
        TextView num = texto("Factura N.º " + f.getNumFactura(), true);
        num.setTextSize(16);
        cab.addView(num, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        boolean cred = "CREDITO".equalsIgnoreCase(f.getFormaPago());
        cab.addView(chip(cred ? "Crédito" : "Efectivo", cred ? AZUL : EXITO));
        t.addView(cab);
        TextView fecha = texto(f.getFecha(), false);
        fecha.setTextColor(GRIS);
        fecha.setTextSize(12);
        t.addView(fecha);
        t.addView(separador());
        t.addView(texto(f.getNombreCliente() + " (" + f.getCedula() + ")", false));
        if (f.getLineas().isEmpty()) {
            t.addView(texto(f.getTelefono(), false));
        } else {
            for (LineaFactura l : f.getLineas()) {
                t.addView(fila2(l.getCantidad() + " × " + l.getDescripcion(), Formato.dinero(l.getSubtotal())));
            }
        }
        if (f.getNumCuotas() > 0) {
            t.addView(texto(f.getNumCuotas() + " cuotas", false));
        }
        t.addView(fila2("Precio de lista", Formato.dinero(f.getPrecioLista())));
        t.addView(fila2("Descuento", "- " + Formato.dinero(f.getDescuento())));
        LinearLayout total = fila2("TOTAL", Formato.dinero(f.getTotal()));
        for (int i = 0; i < 2; i++) {
            TextView x = (TextView) total.getChildAt(i);
            x.setTypeface(Typeface.DEFAULT_BOLD);
            x.setTextColor(AZUL);
            x.setTextSize(17);
        }
        t.addView(total);
        return t;
    }

    private LinearLayout fila2(String izq, String der) {
        LinearLayout f = new LinearLayout(this);
        f.setPadding(0, dp(3), 0, 0);
        TextView a = texto(izq, false);
        a.setTextColor(GRIS);
        TextView b = texto(der, false);
        b.setGravity(Gravity.END);
        f.addView(a, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        f.addView(b, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return f;
    }

    private View separador() {
        View v = new View(this);
        v.setBackgroundColor(BORDE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        lp.setMargins(0, dp(8), 0, dp(8));
        v.setLayoutParams(lp);
        return v;
    }

    private View tablaAmortizacion(List<CuotaAmortizacion> filas) {
        LinearLayout caja = tarjeta();
        caja.setPadding(0, 0, 0, 0);
        TextView tit = texto("Tabla de amortización", true);
        tit.setPadding(dp(14), dp(12), dp(14), dp(8));
        caja.addView(tit);
        HorizontalScrollView h = new HorizontalScrollView(this);
        LinearLayout tabla = new LinearLayout(this);
        tabla.setOrientation(LinearLayout.VERTICAL);
        String[] cab = {"N.º", "Fecha", "Cuota", "Interés", "Capital", "Saldo"};
        int[] ancho = {44, 96, 74, 70, 74, 80};
        tabla.addView(filaTabla(cab, ancho, true, false));
        int n = 0;
        for (CuotaAmortizacion c : filas) {
            String[] v = {String.valueOf(c.getNumCuota()), c.getFechaPago(), dos(c.getValorCuota()), dos(c.getInteresPagado()),
                dos(c.getCapitalPagado()), dos(c.getSaldo())};
            tabla.addView(filaTabla(v, ancho, false, n++ % 2 == 1));
        }
        h.addView(tabla);
        caja.addView(h);
        return caja;
    }

    private LinearLayout filaTabla(String[] celdas, int[] ancho, boolean cabecera, boolean zebra) {
        LinearLayout f = new LinearLayout(this);
        f.setBackgroundColor(cabecera ? NAVY : zebra ? FONDO : Color.WHITE);
        for (int i = 0; i < celdas.length; i++) {
            TextView t = new TextView(this);
            t.setText(celdas[i]);
            t.setTextSize(12);
            t.setTextColor(cabecera ? Color.WHITE : TEXTO);
            t.setTypeface(cabecera ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            t.setGravity(i < 2 ? Gravity.CENTER : Gravity.END);
            t.setPadding(dp(6), dp(8), dp(8), dp(8));
            f.addView(t, new LinearLayout.LayoutParams(dp(ancho[i]), ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        return f;
    }

    private static String dos(double v) {
        return String.format(java.util.Locale.US, "%,.2f", v);
    }

    private TextView chip(String s, int color) {
        TextView c = new TextView(this);
        c.setText(s);
        c.setTextSize(11);
        c.setTypeface(Typeface.DEFAULT_BOLD);
        c.setTextColor(color);
        c.setPadding(dp(10), dp(3), dp(10), dp(3));
        c.setBackground(fondo((color & 0x00FFFFFF) | 0x22000000, 20, 0));
        return c;
    }

    private GradientDrawable fondo(int color, int radio, int borde) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radio));
        if (borde != 0) {
            g.setStroke(dp(1), borde);
        }
        return g;
    }

    /** Iconos dibujados con vectores: 0 logo, 1 celular, 2 carrito, 3 tarjeta, 4 factura, 5 engranaje. */
    static class Icono extends View {
        final int tipo;
        int color;
        final int fondoColor;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        Icono(android.content.Context c, int tipo, int color, int fondoColor, boolean conFondo) {
            super(c);
            this.tipo = tipo;
            this.color = color;
            this.fondoColor = conFondo ? fondoColor : 0;
        }

        @Override
        protected void onDraw(Canvas c) {
            float w = getWidth(), h = getHeight();
            int trazo = color;
            if (fondoColor != 0) {
                p.setStyle(Paint.Style.FILL);
                if (tipo == 0) {
                    p.setColor(color);
                    trazo = fondoColor;
                } else {
                    p.setColor(fondoColor);
                }
                c.drawRoundRect(0, 0, w, h, w * 0.3f, h * 0.3f, p);
            }
            p.setColor(trazo);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(w * 0.08f);
            p.setStrokeCap(Paint.Cap.ROUND);
            float x = w * 0.5f, y = h * 0.5f, u = w * 0.22f;
            switch (tipo) {
                case 0:
                case 1:
                    c.drawRoundRect(x - u * 0.9f, y - u * 1.5f, x + u * 0.9f, y + u * 1.5f, u * 0.4f, u * 0.4f, p);
                    c.drawLine(x - u * 0.3f, y + u * 1.0f, x + u * 0.3f, y + u * 1.0f, p);
                    break;
                case 2:
                    c.drawLine(x - u * 1.5f, y - u * 1.3f, x - u * 0.9f, y - u * 1.3f, p);
                    c.drawLine(x - u * 0.9f, y - u * 1.3f, x - u * 0.5f, y + u * 0.7f, p);
                    c.drawLine(x - u * 0.5f, y + u * 0.7f, x + u * 1.3f, y + u * 0.7f, p);
                    c.drawLine(x - u * 0.8f, y - u * 0.5f, x + u * 1.5f, y - u * 0.5f, p);
                    p.setStyle(Paint.Style.FILL);
                    c.drawCircle(x - u * 0.2f, y + u * 1.4f, u * 0.25f, p);
                    c.drawCircle(x + u * 0.9f, y + u * 1.4f, u * 0.25f, p);
                    break;
                case 3:
                    c.drawRoundRect(x - u * 1.5f, y - u * 1.0f, x + u * 1.5f, y + u * 1.0f, u * 0.3f, u * 0.3f, p);
                    c.drawLine(x - u * 1.5f, y - u * 0.3f, x + u * 1.5f, y - u * 0.3f, p);
                    c.drawLine(x - u * 1.1f, y + u * 0.5f, x - u * 0.2f, y + u * 0.5f, p);
                    break;
                case 4:
                    c.drawRoundRect(x - u * 1.1f, y - u * 1.5f, x + u * 1.1f, y + u * 1.5f, u * 0.3f, u * 0.3f, p);
                    c.drawLine(x - u * 0.6f, y - u * 0.6f, x + u * 0.6f, y - u * 0.6f, p);
                    c.drawLine(x - u * 0.6f, y, x + u * 0.6f, y, p);
                    c.drawLine(x - u * 0.6f, y + u * 0.6f, x + u * 0.2f, y + u * 0.6f, p);
                    break;
                default:
                    c.drawCircle(x, y, u * 0.6f, p);
                    for (int i = 0; i < 8; i++) {
                        double a = Math.PI / 4 * i;
                        c.drawLine((float) (x + Math.cos(a) * u), (float) (y + Math.sin(a) * u),
                                (float) (x + Math.cos(a) * u * 1.5f), (float) (y + Math.sin(a) * u * 1.5f), p);
                    }
            }
        }
    }

    private void estado(String texto, boolean esError) {
        lblEstado.setText(texto);
        lblEstado.setVisibility(texto.isEmpty() ? View.GONE : View.VISIBLE);
        lblEstado.setTextColor(esError ? ERROR : EXITO);
        if (esError && !texto.isEmpty()) {
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
        }
    }

    private TextView titulo(String s) {
        TextView t = texto(s, true);
        t.setTextSize(20);
        t.setTextColor(NAVY);
        t.setPadding(0, dp(14), 0, dp(4));
        return t;
    }

    private TextView texto(String s, boolean negrita) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(TEXTO);
        t.setTextSize(15);
        if (negrita) {
            t.setTypeface(Typeface.DEFAULT_BOLD);
        }
        return t;
    }

    private EditText campo(String pista) {
        EditText e = new EditText(this);
        e.setHint(pista);
        e.setSingleLine(true);
        e.setTextColor(TEXTO);
        e.setHintTextColor(Color.parseColor("#9AA7B6"));
        e.setBackground(fondo(Color.WHITE, 10, BORDE));
        e.setPadding(dp(14), 0, dp(14), 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        lp.topMargin = dp(10);
        e.setLayoutParams(lp);
        return e;
    }

    private Button boton(String s, View.OnClickListener al) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setStateListAnimator(null);
        b.setBackground(fondo(AZUL, 10, 0));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        lp.topMargin = dp(12);
        b.setLayoutParams(lp);
        if (al != null) {
            b.setOnClickListener(al);
        }
        return b;
    }

    private Button botonSec(String s, View.OnClickListener al) {
        Button b = boton(s, al);
        b.setTextColor(AZUL);
        b.setBackground(fondo(Color.WHITE, 10, AZUL));
        return b;
    }

    private LinearLayout.LayoutParams peso() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
        lp.setMargins(dp(4), dp(12), dp(4), 0);
        return lp;
    }

    private LinearLayout tarjeta() {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setBackground(fondo(Color.WHITE, 14, BORDE));
        t.setElevation(dp(2));
        t.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        t.setLayoutParams(lp);
        return t;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private static Double numero(String s) {
        try {
            return Double.valueOf(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer entero(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
