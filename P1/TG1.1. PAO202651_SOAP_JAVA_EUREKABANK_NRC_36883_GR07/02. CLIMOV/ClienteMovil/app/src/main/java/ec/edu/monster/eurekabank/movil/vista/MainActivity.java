package ec.edu.monster.eurekabank.movil.vista;

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
import ec.edu.monster.eurekabank.movil.R;
import ec.edu.monster.eurekabank.movil.controlador.ControladorMovil;
import ec.edu.monster.eurekabank.movil.modelo.EurekaSoap;
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
    private LinearLayout nav;
    private String usuarioActual;
    private String cuentaElegida = "";
    private java.util.List<EurekaSoap.Cuenta> cuentasCache = new ArrayList<>();
    private LinearLayout panelServidor;
    private EditText txtServidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctl = new ControladorMovil(this);
        setContentView(armarPantalla());
        mostrarSeccion(-1);
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
        titulo.setText("EurekaBank");
        titulo.setTextColor(Color.WHITE);
        titulo.setTextSize(19);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        nombres.addView(titulo);
        TextView sub = new TextView(this);
        sub.setText("Banca en línea");
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
        txtServidor = campo("http://IP:8080/WS_EurekaBank_SOAPJAVA_GR07/WSEurekaBank");
        txtServidor.setText(ctl.url());
        txtServidor.setTextSize(13);
        panelServidor.addView(txtServidor);
        Button conectar = boton("Conectar", v -> {
            ctl.cambiarUrl(txtServidor.getText().toString().trim());
            panelServidor.setVisibility(View.GONE);
            usuarioActual = null;
            mostrarSeccion(-1);
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

        nav = new LinearLayout(this);
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(dp(8));
        nav.setPadding(dp(6), dp(4), dp(6), dp(4));
        String[] nombresNav = {"Cuentas", "Movimientos", "Operar"};
        int[] iconosNav = {3, 4, 2};
        itemsNav = new LinearLayout[nombresNav.length];
        for (int i = 0; i < nombresNav.length; i++) {
            final int seccion = i;
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(0, dp(8), 0, dp(8));
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(24), dp(24));
            ilp.gravity = Gravity.CENTER_HORIZONTAL;
            item.addView(new Icono(this, iconosNav[i], GRIS, 0, false), ilp);
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
        if (usuarioActual == null) {
            i = -1;
        }
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
        if (i < 0) {
            seccionLogin();
        } else if (i == 0) {
            seccionCuentas();
        } else if (i == 1) {
            seccionMovimientos();
        } else {
            seccionOperar();
        }
    }

    // ---------------- Inicio de sesion ----------------

    private void seccionLogin() {
        nav.setVisibility(View.GONE);
        contenido.addView(titulo("Iniciar sesión"));
        TextView ayuda = texto("Ingrese sus credenciales de EurekaBank.", false);
        ayuda.setTextColor(GRIS);
        contenido.addView(ayuda);
        EditText usuario = campo("Usuario");
        EditText clave = campo("Clave");
        clave.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        contenido.addView(usuario);
        contenido.addView(clave);
        contenido.addView(boton("Ingresar", v -> {
            String u = usuario.getText().toString().trim();
            String c = clave.getText().toString();
            if (u.isEmpty() || c.isEmpty()) {
                estado("Ingrese usuario y clave.", true);
                return;
            }
            estado("Verificando...", false);
            ctl.ejecutar(() -> ctl.servicio().iniciarSesion(u, c), ok -> {
                if (ok) {
                    usuarioActual = u;
                    nav.setVisibility(View.VISIBLE);
                    mostrarSeccion(0);
                } else {
                    estado("Usuario o clave incorrectos.", true);
                }
            }, m -> estado(m, true));
        }));
    }

    // ---------------- Cuentas ----------------

    private void seccionCuentas() {
        contenido.addView(titulo("Cuentas"));
        LinearLayout lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        contenido.addView(lista);
        ctl.ejecutar(() -> ctl.servicio().cuentas(), cuentas -> {
            cuentasCache = cuentas;
            lista.removeAllViews();
            for (EurekaSoap.Cuenta c : cuentas) {
                LinearLayout t = tarjeta();
                LinearLayout cab = new LinearLayout(this);
                cab.setGravity(Gravity.CENTER_VERTICAL);
                TextView cod = texto(c.codigo, true);
                cod.setTextColor(AZUL);
                cab.addView(cod, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                cab.addView(chip(c.estado, EXITO));
                t.addView(cab);
                t.addView(texto(c.nombreCliente, true));
                TextView saldo = texto(c.moneda + " " + EurekaSoap.dinero(c.saldo), true);
                saldo.setTextSize(22);
                saldo.setTextColor(AZUL);
                t.addView(saldo);
                TextView contacto = texto(c.email + " · " + c.telefono, false);
                contacto.setTextColor(GRIS);
                contacto.setTextSize(12);
                t.addView(contacto);
                Button ver = botonSec("Ver movimientos", v -> {
                    cuentaElegida = c.codigo;
                    mostrarSeccion(1);
                });
                t.addView(ver);
                lista.addView(t);
            }
            estado(cuentas.size() + " cuentas activas.", false);
        }, m -> estado(m, true));
    }

    // ---------------- Movimientos ----------------

    private void seccionMovimientos() {
        contenido.addView(titulo("Movimientos"));
        Spinner combo = new Spinner(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        lp.topMargin = dp(8);
        combo.setLayoutParams(lp);
        contenido.addView(combo);
        LinearLayout resultado = new LinearLayout(this);
        resultado.setOrientation(LinearLayout.VERTICAL);
        contenido.addView(resultado);
        List<String> codigos = new ArrayList<>();
        codigos.add("");
        List<String> etiquetas = new ArrayList<>();
        etiquetas.add("Todas las cuentas");
        ctl.ejecutar(() -> ctl.servicio().cuentas(), cuentas -> {
            int elegido = 0;
            for (EurekaSoap.Cuenta c : cuentas) {
                codigos.add(c.codigo);
                etiquetas.add(c.codigo + " — " + c.nombreCliente);
                if (c.codigo.equals(cuentaElegida)) {
                    elegido = codigos.size() - 1;
                }
            }
            combo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etiquetas));
            combo.setSelection(elegido);
            combo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> padre, View vista, int pos, long id) {
                    cuentaElegida = codigos.get(pos);
                    cargarMovimientos(cuentaElegida, resultado);
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> padre) {
                }
            });
        }, m -> estado(m, true));
    }

    private void cargarMovimientos(String cuenta, LinearLayout resultado) {
        ctl.ejecutar(() -> ctl.servicio().movimientos(cuenta), lista -> {
            resultado.removeAllViews();
            for (EurekaSoap.Movimiento m : lista) {
                LinearLayout t = tarjeta();
                LinearLayout cab = new LinearLayout(this);
                cab.setGravity(Gravity.CENTER_VERTICAL);
                cab.addView(texto(m.tipo, true), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                TextView imp = texto((m.esEntrada() ? "+" : "-") + EurekaSoap.dinero(m.importe), true);
                imp.setTextColor(m.esEntrada() ? EXITO : ERROR);
                cab.addView(imp);
                t.addView(cab);
                TextView det = texto(m.codigoCuenta + " · N.º " + m.numero + " · " + m.fecha, false);
                det.setTextColor(GRIS);
                det.setTextSize(12);
                t.addView(det);
                t.addView(fila2("Saldo después", EurekaSoap.dinero(m.saldoActual)));
                if (m.referencia != null && !m.referencia.isEmpty()) {
                    t.addView(fila2("Cuenta de referencia", m.referencia));
                }
                resultado.addView(t);
            }
            estado(lista.size() + " movimientos.", false);
        }, m -> estado(m, true));
    }

    // ---------------- Operar ----------------

    private void seccionOperar() {
        contenido.addView(titulo("Operar"));
        RadioGroup tipos = new RadioGroup(this);
        RadioButton dep = new RadioButton(this);
        dep.setText("Depósito");
        dep.setId(View.generateViewId());
        RadioButton ret = new RadioButton(this);
        ret.setText("Retiro");
        ret.setId(View.generateViewId());
        RadioButton tra = new RadioButton(this);
        tra.setText("Transferencia");
        tra.setId(View.generateViewId());
        tipos.addView(dep);
        tipos.addView(ret);
        tipos.addView(tra);
        dep.setChecked(true);
        Spinner origen = new Spinner(this);
        Spinner destino = new Spinner(this);
        TextView lblDestino = texto("Cuenta destino", true);
        lblDestino.setVisibility(View.GONE);
        destino.setVisibility(View.GONE);
        tra.setOnCheckedChangeListener((b, marcado) -> {
            lblDestino.setVisibility(marcado ? View.VISIBLE : View.GONE);
            destino.setVisibility(marcado ? View.VISIBLE : View.GONE);
        });
        EditText importe = campo("Importe (por ejemplo 150.50)");
        importe.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        contenido.addView(tipos);
        contenido.addView(texto("Cuenta (origen)", true));
        contenido.addView(origen);
        contenido.addView(lblDestino);
        contenido.addView(destino);
        contenido.addView(importe);
        Button registrar = boton("Registrar operación", null);
        registrar.setBackground(fondo(ORO, 10, 0));
        registrar.setTextColor(NAVY);
        contenido.addView(registrar);

        List<String> codigos = new ArrayList<>();
        List<String> etiquetas = new ArrayList<>();
        ctl.ejecutar(() -> ctl.servicio().cuentas(), cuentas -> {
            for (EurekaSoap.Cuenta c : cuentas) {
                codigos.add(c.codigo);
                etiquetas.add(c.codigo + " — " + c.nombreCliente);
            }
            ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etiquetas);
            origen.setAdapter(a);
            destino.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, etiquetas));
        }, m -> estado(m, true));

        registrar.setOnClickListener(v -> {
            Double monto = numero(importe.getText().toString());
            if (monto == null || monto <= 0) {
                estado("Ingrese un importe mayor a 0.", true);
                return;
            }
            if (origen.getSelectedItemPosition() < 0 || origen.getSelectedItemPosition() >= codigos.size()) {
                estado("Elija una cuenta.", true);
                return;
            }
            String tipo = tra.isChecked() ? "TRANSFERENCIA" : ret.isChecked() ? "RETIRO" : "DEPOSITO";
            String o = codigos.get(origen.getSelectedItemPosition());
            String d = tra.isChecked() && destino.getSelectedItemPosition() >= 0 ? codigos.get(destino.getSelectedItemPosition()) : "";
            estado("Procesando...", false);
            ctl.ejecutar(() -> ctl.servicio().registrar(tipo, o, d, monto), r -> {
                estado(r.ok ? r.mensaje : r.mensaje, !r.ok);
                if (r.ok) {
                    importe.setText("");
                }
            }, m -> estado(m, true));
        });
    }

    // ---------------- Piezas de interfaz ----------------

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
