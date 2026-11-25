package ec.edu.monster.paises.movil.vista;

import android.content.Context;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

import ec.edu.monster.paises.movil.R;
import ec.edu.monster.paises.movil.controlador.PaisController;
import ec.edu.monster.paises.movil.modelo.Pais;

/** Actividad 1: consultar un pais por nombre en la API publica y, si sirve, guardarlo en la base. */
class ConsultaApiVista extends ScrollView {
    private final PaisController ctl;
    private final Anfitrion host;
    private final EditText nombre;
    private final LinearLayout resultados;

    ConsultaApiVista(Context c, PaisController ctl, Anfitrion host) {
        super(c);
        this.ctl = ctl;
        this.host = host;
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(Ui.dp(c, 20), Ui.dp(c, 18), Ui.dp(c, 20), Ui.dp(c, 28));
        addView(col);

        col.addView(Ui.titulo(c, "Consultar la API", 30, Ui.OCEANO));
        TextView intro = Ui.texto(c, "Busca un país directamente en la API pública, sin pasar por la base de datos. Si te sirve, guárdalo.", 15, Ui.SUAVE, false);
        intro.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 16));
        col.addView(intro);

        nombre = Ui.campo(c, "Nombre del país, por ejemplo ecuador");
        nombre.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        nombre.setOnEditorActionListener((v, accion, ev) -> {
            consultar();
            return true;
        });
        col.addView(nombre, new LinearLayout.LayoutParams(-1, Ui.dp(c, 48)));
        Button b = Ui.boton(c, "Consultar", Ui.MAR, 0xFFFFFFFF, 0);
        b.setOnClickListener(v -> consultar());
        LinearLayout.LayoutParams lb = new LinearLayout.LayoutParams(-1, Ui.dp(c, 48));
        lb.topMargin = Ui.dp(c, 10);
        col.addView(b, lb);

        resultados = new LinearLayout(c);
        resultados.setOrientation(LinearLayout.VERTICAL);
        resultados.setPadding(0, Ui.dp(c, 16), 0, 0);
        col.addView(resultados);
    }

    private void mensaje(String titulo, String detalle) {
        resultados.removeAllViews();
        Context c = getContext();
        TextView t = Ui.texto(c, titulo, 16, Ui.TINTA, true);
        TextView d = Ui.texto(c, detalle, 14, Ui.SUAVE, false);
        resultados.addView(t);
        resultados.addView(d);
    }

    private void consultar() {
        final String n = nombre.getText().toString().trim();
        if (n.isEmpty()) {
            host.aviso("Escribe el nombre de un país", true);
            return;
        }
        InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null) im.hideSoftInputFromWindow(nombre.getWindowToken(), 0);
        mensaje("Consultando la API…", "");
        ctl.ejecutar(() -> ctl.api().consultarEnApi(n), new PaisController.Resultado<List<Pais>>() {
            public void ok(List<Pais> lista) {
                resultados.removeAllViews();
                if (lista.isEmpty()) {
                    mensaje("Sin resultados", "La API no tiene ningún país llamado «" + n + "».");
                    return;
                }
                for (Pais p : lista) resultados.addView(tarjeta(p));
            }

            public void error(String m) {
                mensaje("No se pudo consultar", m);
            }
        });
    }

    private LinearLayout tarjeta(final Pais p) {
        final Context c = getContext();
        LinearLayout t = new LinearLayout(c);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setPadding(Ui.dp(c, 16), Ui.dp(c, 14), Ui.dp(c, 16), Ui.dp(c, 14));
        t.setBackground(Ui.caja(c, Ui.PAPEL, 14, Ui.LINEA));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = Ui.dp(c, 10);
        t.setLayoutParams(lp);

        LinearLayout fila = new LinearLayout(c);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        ImageView bandera = Ui.bandera(c, 44, 30);
        Ui.cargarBandera(bandera, p.bandera);
        fila.addView(bandera);
        LinearLayout tx = new LinearLayout(c);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setPadding(Ui.dp(c, 14), 0, 0, 0);
        tx.addView(Ui.texto(c, p.nombre, 17, Ui.TINTA, true));
        tx.addView(Ui.texto(c, (p.capital.isEmpty() ? "Sin capital" : p.capital) + " · " + p.region + " · " + Ui.entero(p.poblacion) + " habitantes", 13, Ui.SUAVE, false));
        fila.addView(tx, new LinearLayout.LayoutParams(0, -2, 1f));
        t.addView(fila);

        final Button guardar = Ui.boton(c, "Guardar en la base", Ui.MAR, 0xFFFFFFFF, 0);
        LinearLayout.LayoutParams lg = new LinearLayout.LayoutParams(-1, Ui.dp(c, 48));
        lg.topMargin = Ui.dp(c, 12);
        t.addView(guardar, lg);
        guardar.setOnClickListener(v -> {
            guardar.setEnabled(false);
            ctl.ejecutar(() -> {
                ctl.api().guardarDesdeApi(p.nombre, p.codigo);
                return Boolean.TRUE;
            }, new PaisController.Resultado<Boolean>() {
                public void ok(Boolean r) {
                    guardar.setText("Guardado");
                    guardar.setBackground(Ui.caja(c, Ui.MAR_CLARO, 12, 0));
                    guardar.setTextColor(Ui.MAR_OSCURO);
                    guardar.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0);
                    guardar.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(Ui.MAR_OSCURO));
                    host.actualizarTotal();
                }

                public void error(String m) {
                    guardar.setEnabled(true);
                    host.aviso(m, true);
                }
            });
        });
        return t;
    }
}
