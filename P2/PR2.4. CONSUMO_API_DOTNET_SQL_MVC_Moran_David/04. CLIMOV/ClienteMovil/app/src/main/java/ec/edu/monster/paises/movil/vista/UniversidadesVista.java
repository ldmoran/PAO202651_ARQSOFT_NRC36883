package ec.edu.monster.paises.movil.vista;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import ec.edu.monster.paises.movil.R;
import ec.edu.monster.paises.movil.controlador.PaisController;
import ec.edu.monster.paises.movil.modelo.Universidad;

/** Universidades: ver las guardadas en la base o traerlas de la API publica por pais (en ingles) y guardarlas. */
class UniversidadesVista extends ScrollView {
    private static final int MAXIMO = 100;

    private final PaisController ctl;
    private final Anfitrion host;
    private final LinearLayout col, resultados;
    private final EditText campo, campoUni;
    private final Button accion;
    private final LinearLayout.LayoutParams segLp;
    private final TextView chipGuardadas, chipApi;
    private boolean modoApi = false;
    private String paisConsultado = "";
    private String universidadConsultada = "";
    private final Handler retraso = new Handler(Looper.getMainLooper());
    private int consulta = 0;

    UniversidadesVista(Context c, PaisController ctl, Anfitrion host) {
        super(c);
        this.ctl = ctl;
        this.host = host;
        setFillViewport(true);
        col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(Ui.dp(c, 20), Ui.dp(c, 18), Ui.dp(c, 20), Ui.dp(c, 28));
        addView(col);

        col.addView(Ui.titulo(c, "Universidades", 30, Ui.OCEANO));

        LinearLayout seg = new LinearLayout(c);
        seg.setPadding(0, Ui.dp(c, 14), 0, Ui.dp(c, 12));
        segLp = new LinearLayout.LayoutParams(-2, Ui.dp(c, 40));
        segLp.rightMargin = Ui.dp(c, 8);
        chipGuardadas = Ui.chip(c, "Guardadas", true);
        chipApi = Ui.chip(c, "Traer de la API", false);
        chipGuardadas.setOnClickListener(v -> cambiarModo(false));
        chipApi.setOnClickListener(v -> cambiarModo(true));
        seg.addView(chipGuardadas, segLp);
        seg.addView(chipApi, segLp);
        col.addView(seg);

        campo = Ui.campo(c, "Buscar por nombre de la universidad");
        campo.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        campo.setOnEditorActionListener((v, a, e) -> {
            if (modoApi) consultarApi();
            return true;
        });
        campo.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int i, int j, int k) { }
            public void onTextChanged(CharSequence s, int i, int j, int k) { }
            public void afterTextChanged(Editable e) {
                if (modoApi) return;
                retraso.removeCallbacksAndMessages(null);
                retraso.postDelayed(UniversidadesVista.this::cargarGuardadas, 250);
            }
        });
        col.addView(campo, new LinearLayout.LayoutParams(-1, Ui.dp(c, 48)));

        campoUni = Ui.campo(c, "Universidad: Politécnica, Central…");
        campoUni.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        campoUni.setOnEditorActionListener((v, a, e) -> {
            consultarApi();
            return true;
        });
        LinearLayout.LayoutParams lu = new LinearLayout.LayoutParams(-1, Ui.dp(c, 48));
        lu.topMargin = Ui.dp(c, 10);
        campoUni.setVisibility(GONE);
        col.addView(campoUni, lu);

        accion = Ui.boton(c, "Consultar", Ui.MAR, 0xFFFFFFFF, 0);
        accion.setOnClickListener(v -> consultarApi());
        LinearLayout.LayoutParams la = new LinearLayout.LayoutParams(-1, Ui.dp(c, 48));
        la.topMargin = Ui.dp(c, 10);
        accion.setVisibility(GONE);
        col.addView(accion, la);

        resultados = new LinearLayout(c);
        resultados.setOrientation(LinearLayout.VERTICAL);
        resultados.setPadding(0, Ui.dp(c, 14), 0, 0);
        col.addView(resultados);
    }

    private void cambiarModo(boolean api) {
        modoApi = api;
        Context c = getContext();
        reemplazarChip(chipGuardadas, "Guardadas", !api);
        reemplazarChip(chipApi, "Traer de la API", api);
        campo.setText("");
        campoUni.setText("");
        campo.setHint(api ? "País: Ecuador, España, Perú…" : "Buscar por nombre de la universidad");
        campoUni.setVisibility(api ? VISIBLE : GONE);
        accion.setText("Buscar en la API");
        accion.setVisibility(api ? VISIBLE : GONE);
        resultados.removeAllViews();
        if (api) {
            texto("Escribe un país, una universidad o las dos cosas, y toca «Buscar en la API».", "");
        } else {
            cargarGuardadas();
        }
    }

    private void reemplazarChip(TextView v, String t, boolean activo) {
        Context c = getContext();
        v.setTextColor(activo ? Ui.ARENA : Ui.TINTA);
        v.setBackground(Ui.conOnda(c, Ui.caja(c, activo ? Ui.OCEANO : Ui.PAPEL, 20, activo ? 0 : Ui.LINEA)));
    }

    void recargar() {
        if (!modoApi) cargarGuardadas();
    }

    private void texto(String titulo, String detalle) {
        resultados.removeAllViews();
        Context c = getContext();
        resultados.addView(Ui.texto(c, titulo, 16, Ui.TINTA, true));
        if (!detalle.isEmpty()) resultados.addView(Ui.texto(c, detalle, 14, Ui.SUAVE, false));
    }

    // ------------------------------------------------------------------ guardadas
    private void cargarGuardadas() {
        final int mia = ++consulta;
        final String n = campo.getText().toString();
        ctl.ejecutar(() -> ctl.api().universidades(n, ""), new PaisController.Resultado<List<Universidad>>() {
            public void ok(List<Universidad> lista) {
                if (mia != consulta || modoApi) return;
                resultados.removeAllViews();
                if (lista.isEmpty()) {
                    texto(n.trim().isEmpty() ? "Aún no hay universidades" : "Sin resultados",
                            n.trim().isEmpty() ? "Toca «Traer de la API» para guardar las de un país." : "Ninguna universidad guardada coincide con esa búsqueda.");
                    return;
                }
                resumen(lista.size());
                for (int i = 0; i < Math.min(MAXIMO, lista.size()); i++) resultados.addView(fila(lista.get(i), true));
            }

            public void error(String m) {
                if (mia != consulta || modoApi) return;
                texto("No se pudo cargar", m);
            }
        });
    }

    private void resumen(int total) {
        Context c = getContext();
        String t = "Mostrando " + Ui.entero(Math.min(total, MAXIMO)) + (total > MAXIMO ? " de " + Ui.entero(total) + ". Afina la búsqueda para ver el resto." : (total == 1 ? " universidad" : " universidades"));
        TextView r = Ui.texto(c, t, 13, Ui.SUAVE, false);
        r.setPadding(0, 0, 0, Ui.dp(c, 8));
        resultados.addView(r);
    }

    // ------------------------------------------------------------------ API publica
    private void consultarApi() {
        final String pais = campo.getText().toString().trim();
        final String uni = campoUni.getText().toString().trim();
        if (pais.isEmpty() && uni.isEmpty()) {
            host.aviso("Escribe un país, una universidad o las dos cosas", true);
            return;
        }
        InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null) im.hideSoftInputFromWindow(campo.getWindowToken(), 0);
        texto("Consultando la API…", "");
        ctl.ejecutar(() -> ctl.api().universidadesEnApi(pais, uni), new PaisController.Resultado<List<Universidad>>() {
            public void ok(List<Universidad> lista) {
                if (!modoApi) return;
                resultados.removeAllViews();
                if (lista.isEmpty()) {
                    texto("Sin resultados", "La API no encontró universidades con esos datos. Prueba con otro país o con otra parte del nombre.");
                    return;
                }
                paisConsultado = pais;
                universidadConsultada = uni;
                Context c = getContext();
                Button todas = Ui.boton(c, "Guardar estas " + Ui.entero(lista.size()) + " en la base", Ui.MAR, 0xFFFFFFFF, 0);
                todas.setOnClickListener(v -> guardarTodas(todas));
                resultados.addView(todas, new LinearLayout.LayoutParams(-1, Ui.dp(c, 48)));
                TextView espacio = Ui.texto(c, "", 4, Ui.SUAVE, false);
                resultados.addView(espacio);
                resumen(lista.size());
                for (int i = 0; i < Math.min(MAXIMO, lista.size()); i++) resultados.addView(fila(lista.get(i), false));
            }

            public void error(String m) {
                if (modoApi) texto("No se pudo consultar", m);
            }
        });
    }

    private void guardarTodas(final Button b) {
        b.setEnabled(false);
        ctl.ejecutar(() -> ctl.api().importarUniversidades(paisConsultado, universidadConsultada), new PaisController.Resultado<String>() {
            public void ok(String r) {
                host.aviso(r, false);
                b.setText("Guardadas");
                b.setBackground(Ui.caja(getContext(), Ui.MAR_CLARO, 12, 0));
                b.setTextColor(Ui.MAR_OSCURO);
            }

            public void error(String m) {
                b.setEnabled(true);
                host.aviso(m, true);
            }
        });
    }

    // ------------------------------------------------------------------ fila
    private View fila(final Universidad u, boolean conBorrar) {
        final Context c = getContext();
        LinearLayout f = new LinearLayout(c);
        f.setGravity(Gravity.CENTER_VERTICAL);
        f.setPadding(Ui.dp(c, 16), Ui.dp(c, 10), Ui.dp(c, 8), Ui.dp(c, 10));
        f.setMinimumHeight(Ui.dp(c, 64));
        f.setBackground(Ui.caja(c, Ui.PAPEL, 14, Ui.LINEA));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = Ui.dp(c, 8);
        f.setLayoutParams(lp);
        LinearLayout tx = new LinearLayout(c);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.addView(Ui.texto(c, u.nombre, 15, Ui.TINTA, true));
        String sub = u.pais + (u.dominio.isEmpty() ? "" : " · " + u.dominio);
        tx.addView(Ui.texto(c, sub, 13, Ui.SUAVE, false));
        f.addView(tx, new LinearLayout.LayoutParams(0, -2, 1f));
        if (conBorrar) {
            final ImageButton b = Ui.botonIcono(c, R.drawable.ic_papelera, Ui.SUAVE, "Eliminar " + u.nombre);
            final boolean[] armado = {false};
            b.setOnClickListener(v -> {
                if (!armado[0]) {
                    armado[0] = true;
                    b.setColorFilter(Ui.ERROR, PorterDuff.Mode.SRC_IN);
                    host.aviso("Toca otra vez para eliminar", false);
                    b.postDelayed(() -> {
                        armado[0] = false;
                        b.setColorFilter(Ui.SUAVE, PorterDuff.Mode.SRC_IN);
                    }, 3000);
                    return;
                }
                ctl.ejecutar(() -> {
                    ctl.api().eliminarUniversidad(u.id);
                    return Boolean.TRUE;
                }, new PaisController.Resultado<Boolean>() {
                    public void ok(Boolean r) {
                        host.aviso("Universidad eliminada", false);
                        cargarGuardadas();
                    }

                    public void error(String m) {
                        host.aviso(m, true);
                    }
                });
            });
            f.addView(b);
        }
        return f;
    }
}
