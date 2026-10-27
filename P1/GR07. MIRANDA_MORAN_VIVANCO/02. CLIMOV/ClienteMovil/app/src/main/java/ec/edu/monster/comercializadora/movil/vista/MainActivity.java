package ec.edu.monster.comercializadora.movil.vista;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

    private static final int AZUL = Color.parseColor("#1F3864");
    private static final int AZUL_MEDIO = Color.parseColor("#2D5090");
    private static final int ERROR = Color.parseColor("#B00020");
    private static final int EXITO = Color.parseColor("#1B7F3B");

    private ControladorMovil ctl;
    private LinearLayout contenido;
    private TextView lblEstado;
    private Button[] botonesNav;

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
        raiz.setFitsSystemWindows(true);

        LinearLayout cabecera = new LinearLayout(this);
        cabecera.setBackgroundResource(R.drawable.bg_cabecera);
        cabecera.setGravity(Gravity.CENTER_VERTICAL);
        cabecera.setPadding(dp(16), dp(12), dp(8), dp(12));
        TextView titulo = new TextView(this);
        titulo.setText("Comercializadora");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(20);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        cabecera.addView(titulo, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button servidor = new Button(this);
        servidor.setText("Servidor");
        servidor.setAllCaps(false);
        servidor.setOnClickListener(v -> dialogoServidor());
        cabecera.addView(servidor);
        raiz.addView(cabecera);

        ScrollView scroll = new ScrollView(this);
        contenido = new LinearLayout(this);
        contenido.setOrientation(LinearLayout.VERTICAL);
        contenido.setPadding(dp(16), dp(8), dp(16), dp(16));
        scroll.addView(contenido);
        raiz.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        lblEstado = new TextView(this);
        lblEstado.setPadding(dp(16), dp(4), dp(16), dp(4));
        lblEstado.setTextSize(13);
        raiz.addView(lblEstado);

        LinearLayout nav = new LinearLayout(this);
        nav.setBackgroundColor(AZUL);
        String[] nombres = {"Catalogo", "Venta", "Credito", "Facturas"};
        botonesNav = new Button[nombres.length];
        for (int i = 0; i < nombres.length; i++) {
            final int seccion = i;
            Button b = new Button(this);
            b.setText(nombres[i]);
            b.setAllCaps(false);
            b.setTextColor(Color.WHITE);
            b.setBackgroundColor(Color.TRANSPARENT);
            b.setOnClickListener(v -> mostrarSeccion(seccion));
            botonesNav[i] = b;
            nav.addView(b, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }
        raiz.addView(nav);
        return raiz;
    }

    private void mostrarSeccion(int i) {
        for (int k = 0; k < botonesNav.length; k++) {
            botonesNav[k].setBackgroundColor(k == i ? AZUL_MEDIO : Color.TRANSPARENT);
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

    private void dialogoServidor() {
        EditText txt = campo("URL del servicio");
        txt.setText(ctl.url());
        new AlertDialog.Builder(this).setTitle("Direccion del servicio")
                .setMessage("En el emulador use 10.0.2.2; en un celular real, la IP de la PC.")
                .setView(txt).setPositiveButton("Guardar", (d, w) -> {
                    ctl.cambiarUrl(txt.getText().toString());
                    mostrarSeccion(0);
                }).setNegativeButton("Cancelar", null).show();
    }

    // ---------------- Catalogo ----------------

    private void seccionCatalogo() {
        contenido.addView(titulo("Catalogo de celulares"));
        contenido.addView(boton("Agregar celular", v -> dialogoTelefono(null)));
        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        contenido.addView(lista);
        ctl.ejecutar(() -> ctl.servicio().listarTelefonos(), telefonos -> {
            lista.removeAllViews();
            for (Telefono t : telefonos) {
                LinearLayout tarjeta = tarjeta();
                tarjeta.addView(texto(t.getMarca() + " " + t.getModelo(), true));
                tarjeta.addView(texto(Formato.dinero(t.getPrecio()) + (t.isDisponible() ? "  (disponible)" : "  (no disponible)"), false));
                LinearLayout fila = new LinearLayout(this);
                fila.addView(boton("Editar", v -> dialogoTelefono(t)), peso());
                fila.addView(boton("Eliminar", v -> confirmarEliminar(t)), peso());
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
        EditText cedula = campo("Cedula (10 digitos)");
        cedula.setInputType(InputType.TYPE_CLASS_NUMBER);
        EditText nombre = campo("Nombre (solo si es cliente nuevo)");
        Spinner combo = new Spinner(this);
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
        contenido.addView(combo);
        contenido.addView(forma);
        contenido.addView(plazo);
        Button vender = boton("Vender", null);
        contenido.addView(vender);
        contenido.addView(resultado);

        List<Telefono> disponibles = new ArrayList<>();
        ctl.ejecutar(() -> ctl.servicio().listarTelefonos(), lista -> {
            List<String> etiquetas = new ArrayList<>();
            for (Telefono t : lista) {
                if (t.isDisponible()) {
                    disponibles.add(t);
                    etiquetas.add(t.getMarca() + " " + t.getModelo() + " - " + Formato.dinero(t.getPrecio()));
                }
            }
            combo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etiquetas));
        }, m -> estado(m, true));

        vender.setOnClickListener(v -> {
            int i = combo.getSelectedItemPosition();
            if (i < 0 || i >= disponibles.size()) {
                estado("Elija un celular disponible.", true);
                return;
            }
            int cod = disponibles.get(i).getCodTelefono();
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
                ResultadoFactura r = aCredito ? ctl.servicio().facturarCredito(ced, nom, cod, meses)
                        : ctl.servicio().facturarEfectivo(ced, nom, cod);
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
        contenido.addView(titulo("Sujeto de credito"));
        EditText cedula = campo("Cedula (10 digitos)");
        cedula.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout resultado = new LinearLayout(this);
        resultado.setOrientation(LinearLayout.VERTICAL);
        contenido.addView(cedula);
        contenido.addView(boton("Verificar", v -> {
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
                TextView veredicto = texto(ev.isSujetoCredito() ? "SI es sujeto de credito" : "NO es sujeto de credito", true);
                veredicto.setTextColor(ev.isSujetoCredito() ? EXITO : ERROR);
                t.addView(veredicto);
                t.addView(texto(ev.getMotivo(), false));
                if (d != null) {
                    t.addView(texto("Cliente: " + d.getNombre(), false));
                    t.addView(texto("Promedio de depositos (3 meses): " + Formato.dinero(d.getPromedioDepositos()), false));
                    t.addView(texto("Promedio de retiros (3 meses): " + Formato.dinero(d.getPromedioRetiros()), false));
                    t.addView(texto("Monto maximo: " + Formato.dinero(d.getMontoMaximo()), true));
                }
                resultado.addView(t);
            }, m -> estado(m, true));
        }));
        contenido.addView(resultado);
    }

    // ---------------- Facturas ----------------

    private void seccionFacturas() {
        contenido.addView(titulo("Facturas de un cliente"));
        EditText cedula = campo("Cedula (10 digitos)");
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
        t.addView(texto("Factura N. " + f.getNumFactura() + "  -  " + f.getFecha(), true));
        t.addView(texto(f.getNombreCliente() + " (" + f.getCedula() + ")", false));
        t.addView(texto(f.getTelefono(), false));
        t.addView(texto("Pago: " + f.getFormaPago()
                + (f.getNumCuotas() > 0 ? " a " + f.getNumCuotas() + " cuotas" : ""), false));
        t.addView(texto("Precio " + Formato.dinero(f.getPrecioLista()) + "   Descuento " + Formato.dinero(f.getDescuento()), false));
        t.addView(texto("TOTAL " + Formato.dinero(f.getTotal()), true));
        return t;
    }

    private View tablaAmortizacion(List<CuotaAmortizacion> filas) {
        HorizontalScrollView h = new HorizontalScrollView(this);
        TextView t = new TextView(this);
        t.setTypeface(Typeface.MONOSPACE);
        t.setTextSize(11);
        t.setTextColor(AZUL);
        t.setText(Formato.tablaTexto(filas));
        t.setPadding(dp(8), dp(8), dp(8), dp(8));
        h.addView(t);
        return h;
    }

    private void estado(String texto, boolean esError) {
        lblEstado.setText(texto);
        lblEstado.setTextColor(esError ? ERROR : EXITO);
        if (esError && !texto.isEmpty()) {
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
        }
    }

    private TextView titulo(String s) {
        TextView t = texto(s, true);
        t.setTextSize(18);
        t.setTextColor(AZUL);
        t.setPadding(0, dp(8), 0, dp(4));
        return t;
    }

    private TextView texto(String s, boolean negrita) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.parseColor("#1F2937"));
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
        e.setBackgroundResource(R.drawable.bg_input);
        e.setPadding(dp(12), 0, dp(12), 0);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(8);
        e.setLayoutParams(lp);
        return e;
    }

    private Button boton(String s, View.OnClickListener al) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundResource(R.drawable.bg_boton);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        lp.topMargin = dp(8);
        b.setLayoutParams(lp);
        if (al != null) {
            b.setOnClickListener(al);
        }
        return b;
    }

    private LinearLayout.LayoutParams peso() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(44), 1);
        lp.setMargins(dp(4), dp(8), dp(4), 0);
        return lp;
    }

    private LinearLayout tarjeta() {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setBackgroundResource(R.drawable.bg_tarjeta);
        t.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
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
