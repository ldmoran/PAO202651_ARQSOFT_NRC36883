package ec.edu.monster.paises.movil.vista;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ec.edu.monster.paises.movil.R;
import ec.edu.monster.paises.movil.controlador.PaisController;
import ec.edu.monster.paises.movil.modelo.ClienteApi;
import ec.edu.monster.paises.movil.modelo.Pais;
import ec.edu.monster.paises.movil.modelo.Reporte;

/** Pantalla principal: cabecera, tres secciones (Paises, API, Reportes) y el formulario de detalle encima. */
public class MainActivity extends Activity implements Anfitrion {
    private static final PathInterpolator SALIDA = new PathInterpolator(.23f, 1f, .32f, 1f);

    private PaisController ctl;
    private TextView totalTxt, avisoTxt;
    private FrameLayout contenido;
    private ListaPaisesVista lista;
    private ConsultaApiVista consulta;
    private ReportesVista reportes;
    private UniversidadesVista universidades;
    private DetallePaisVista detalle;
    private LinearLayout nav, cabecera;
    private final View[] pestanas = new View[4];
    private int actual = 0;
    private boolean detalleAbierto = false;
    private boolean cambios = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ctl = new PaisController(new ClienteApi(this));

        FrameLayout raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Ui.FONDO);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        raiz.addView(col, new FrameLayout.LayoutParams(-1, -1));

        col.addView(crearCabecera());
        contenido = new FrameLayout(this);
        col.addView(contenido, new LinearLayout.LayoutParams(-1, 0, 1f));
        nav = crearNavegacion();
        col.addView(nav);

        lista = new ListaPaisesVista(this, ctl, this);
        consulta = new ConsultaApiVista(this, ctl, this);
        reportes = new ReportesVista(this, ctl);
        universidades = new UniversidadesVista(this, ctl, this);
        detalle = new DetallePaisVista(this, ctl, this);
        for (View v : new View[]{lista, consulta, reportes, universidades, detalle}) {
            v.setVisibility(View.GONE);
            contenido.addView(v, new FrameLayout.LayoutParams(-1, -1));
        }

        avisoTxt = Ui.texto(this, "", 15, Color.WHITE, true);
        avisoTxt.setPadding(Ui.dp(this, 18), Ui.dp(this, 14), Ui.dp(this, 18), Ui.dp(this, 14));
        avisoTxt.setBackground(Ui.caja(this, Ui.OCEANO, 14, 0));
        avisoTxt.setAlpha(0f);
        avisoTxt.setVisibility(View.INVISIBLE);
        FrameLayout.LayoutParams la = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        la.bottomMargin = Ui.dp(this, 92);
        la.leftMargin = Ui.dp(this, 20);
        la.rightMargin = Ui.dp(this, 20);
        raiz.addView(avisoTxt, la);

        setContentView(raiz);
        seleccionar(0);
        actualizarTotal();
    }

    // ------------------------------------------------------------------ cabecera y navegacion
    private LinearLayout crearCabecera() {
        cabecera = new LinearLayout(this);
        cabecera.setOrientation(LinearLayout.HORIZONTAL);
        cabecera.setGravity(Gravity.CENTER_VERTICAL);
        cabecera.setBackgroundColor(Ui.OCEANO);
        cabecera.setPadding(Ui.dp(this, 16), Ui.dp(this, 10), Ui.dp(this, 6), Ui.dp(this, 10));

        FrameLayout logo = new FrameLayout(this);
        logo.setBackground(Ui.caja(this, Ui.ARENA, 12, 0));
        ImageView g = Ui.icono(this, R.drawable.ic_mundo, Ui.OCEANO, 26);
        logo.addView(g, new FrameLayout.LayoutParams(Ui.dp(this, 26), Ui.dp(this, 26), Gravity.CENTER));
        cabecera.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42)));

        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setPadding(Ui.dp(this, 12), 0, 0, 0);
        tx.addView(Ui.titulo(this, "Atlas de Países", 20, Color.WHITE));
        totalTxt = Ui.texto(this, "", 12, 0xFFB5CCD2, false);
        tx.addView(totalTxt);
        cabecera.addView(tx, new LinearLayout.LayoutParams(0, -2, 1f));

        ImageButton importar = Ui.botonIcono(this, R.drawable.ic_nube, Ui.ARENA, "Importar países");
        importar.setOnClickListener(v -> lista.importar());
        ImageButton servidor = Ui.botonIcono(this, R.drawable.ic_servidor, Ui.ARENA, "Dirección del servidor");
        servidor.setOnClickListener(v -> pedirServidor());
        cabecera.addView(importar);
        cabecera.addView(servidor);
        return cabecera;
    }

    private LinearLayout crearNavegacion() {
        LinearLayout n = new LinearLayout(this);
        n.setOrientation(LinearLayout.HORIZONTAL);
        n.setBackgroundColor(Ui.PAPEL);
        n.setElevation(Ui.dp(this, 6));
        int[] iconos = {R.drawable.ic_mundo, R.drawable.ic_api, R.drawable.ic_barras, R.drawable.ic_uni};
        String[] nombres = {"Países", "API", "Reportes", "Universid."};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            LinearLayout it = new LinearLayout(this);
            it.setOrientation(LinearLayout.VERTICAL);
            it.setGravity(Gravity.CENTER);
            it.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 8));
            ImageView ic = Ui.icono(this, iconos[i], Ui.SUAVE, 24);
            TextView t = Ui.texto(this, nombres[i], 12, Ui.SUAVE, true);
            t.setPadding(0, Ui.dp(this, 2), 0, 0);
            t.setGravity(Gravity.CENTER);
            it.addView(ic);
            it.addView(t);
            it.setBackground(Ui.conOnda(this, new android.graphics.drawable.ColorDrawable(0)));
            it.setOnClickListener(v -> {
                if (detalleAbierto) cerrarDetalle(false);
                seleccionar(idx);
            });
            Ui.presionable(it);
            pestanas[i] = it;
            n.addView(it, new LinearLayout.LayoutParams(0, Ui.dp(this, 64), 1f));
        }
        return n;
    }

    private void seleccionar(int i) {
        actual = i;
        View[] v = {lista, consulta, reportes, universidades};
        for (int k = 0; k < 4; k++) {
            boolean on = k == i;
            if (on && v[k].getVisibility() != View.VISIBLE) {
                v[k].setAlpha(0f);
                v[k].setVisibility(View.VISIBLE);
                v[k].animate().alpha(1f).setDuration(200).setInterpolator(SALIDA).start();
            } else if (!on) {
                v[k].setVisibility(View.GONE);
            }
            LinearLayout it = (LinearLayout) pestanas[k];
            int color = on ? Ui.MAR_OSCURO : Ui.SUAVE;
            ((ImageView) it.getChildAt(0)).setColorFilter(color);
            ((TextView) it.getChildAt(1)).setTextColor(color);
        }
        if (i == 0) lista.recargar();
        if (i == 2) reportes.recargar();
        if (i == 3) universidades.recargar();
    }

    // ------------------------------------------------------------------ Anfitrion
    @Override
    public void aviso(String texto, boolean error) {
        avisoTxt.setText(texto);
        avisoTxt.setBackground(Ui.caja(this, error ? Ui.ERROR : Ui.OCEANO, 14, 0));
        avisoTxt.setVisibility(View.VISIBLE);
        avisoTxt.setTranslationY(Ui.dp(this, 10));
        avisoTxt.animate().cancel();
        avisoTxt.animate().alpha(1f).translationY(0).setDuration(240).setInterpolator(SALIDA).withEndAction(() ->
                avisoTxt.animate().alpha(0f).setStartDelay(error ? 3400 : 2400).setDuration(200).setInterpolator(SALIDA)
                        .withEndAction(() -> avisoTxt.setVisibility(View.INVISIBLE)).start()).start();
    }

    @Override
    public void abrirDetalle(Pais p) {
        detalleAbierto = true;
        cambios = false;
        detalle.mostrar(p);
        detalle.setAlpha(0f);
        detalle.setTranslationX(Ui.dp(this, 28));
        detalle.setVisibility(View.VISIBLE);
        detalle.animate().alpha(1f).translationX(0).setDuration(260).setInterpolator(SALIDA).start();
        nav.setVisibility(View.GONE);
    }

    @Override
    public void cerrarDetalle(boolean huboCambios) {
        if (!detalleAbierto) return;
        detalleAbierto = false;
        detalle.setVisibility(View.GONE);
        nav.setVisibility(View.VISIBLE);
        android.view.inputmethod.InputMethodManager im = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (im != null) im.hideSoftInputFromWindow(detalle.getWindowToken(), 0);
        if (huboCambios) {
            lista.recargar();
            actualizarTotal();
        }
    }

    @Override
    public void actualizarTotal() {
        ctl.ejecutar(() -> ctl.api().reportes(), new PaisController.Resultado<Reporte>() {
            public void ok(Reporte r) {
                totalTxt.setText(Ui.entero(r.total) + " países en la base de datos");
            }

            public void error(String m) {
                totalTxt.setText("Sin conexión con el servidor");
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (detalleAbierto) cerrarDetalle(false);
        else super.onBackPressed();
    }

    // ------------------------------------------------------------------ direccion del servidor
    private void pedirServidor() {
        final EditText e = Ui.campo(this, ClienteApi.BASE_POR_DEFECTO);
        e.setText(ctl.api().base());
        e.setSelectAllOnFocus(true);
        FrameLayout caja = new FrameLayout(this);
        caja.setPadding(Ui.dp(this, 20), Ui.dp(this, 8), Ui.dp(this, 20), 0);
        caja.addView(e, new FrameLayout.LayoutParams(-1, Ui.dp(this, 48)));
        new AlertDialog.Builder(this)
                .setTitle("Dirección del servidor")
                .setMessage("En el emulador usa http://10.0.2.2:8091. En un celular real, la IP del computador, por ejemplo http://192.168.1.10:8091.")
                .setView(caja)
                .setPositiveButton("Guardar", (d, w) -> {
                    ctl.api().guardarBase(e.getText().toString());
                    aviso("Servidor: " + ctl.api().base(), false);
                    actualizarTotal();
                    if (actual == 0) lista.recargar();
                    if (actual == 2) reportes.recargar();
                    if (actual == 3) universidades.recargar();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
