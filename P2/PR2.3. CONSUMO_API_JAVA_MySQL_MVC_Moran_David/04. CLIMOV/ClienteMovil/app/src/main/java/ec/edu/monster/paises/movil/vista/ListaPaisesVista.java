package ec.edu.monster.paises.movil.vista;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ec.edu.monster.paises.movil.R;
import ec.edu.monster.paises.movil.controlador.PaisController;
import ec.edu.monster.paises.movil.modelo.Pais;

/** Actividades 2 a 4: listado de paises, busqueda por nombre y capital, filtro por region. */
class ListaPaisesVista extends FrameLayout {
    private static final String[] REGIONES = {"", "América", "Europa", "Asia", "África", "Oceanía", "Antártida"};

    private final PaisController ctl;
    private final Anfitrion host;
    private final EditText fNombre, fCapital;
    private final LinearLayout chips;
    private final TextView resumen, vacioTitulo, vacioTexto;
    private final LinearLayout vacio;
    private final Button vacioImportar;
    private final ListView lista;
    private final AdaptadorPaises adaptador = new AdaptadorPaises();
    private final Handler retraso = new Handler(Looper.getMainLooper());
    private String region = "";
    private int consulta = 0;

    ListaPaisesVista(Context c, PaisController ctl, Anfitrion host) {
        super(c);
        this.ctl = ctl;
        this.host = host;

        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        addView(col, new LayoutParams(-1, -1));

        TextView titulo = Ui.titulo(c, "Países", 30, Ui.OCEANO);
        titulo.setPadding(Ui.dp(c, 20), Ui.dp(c, 18), Ui.dp(c, 20), Ui.dp(c, 10));
        col.addView(titulo);

        LinearLayout campos = new LinearLayout(c);
        campos.setOrientation(LinearLayout.HORIZONTAL);
        campos.setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0);
        fNombre = Ui.campo(c, "Buscar por nombre");
        fCapital = Ui.campo(c, "Capital");
        LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(0, Ui.dp(c, 48), 1.25f);
        a.rightMargin = Ui.dp(c, 8);
        campos.addView(fNombre, a);
        campos.addView(fCapital, new LinearLayout.LayoutParams(0, Ui.dp(c, 48), 1f));
        col.addView(campos);

        HorizontalScrollView hs = new HorizontalScrollView(c);
        hs.setHorizontalScrollBarEnabled(false);
        chips = new LinearLayout(c);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(Ui.dp(c, 16), Ui.dp(c, 12), Ui.dp(c, 16), Ui.dp(c, 4));
        hs.addView(chips);
        col.addView(hs);
        pintarChips();

        resumen = Ui.texto(c, "", 13, Ui.SUAVE, false);
        resumen.setPadding(Ui.dp(c, 20), Ui.dp(c, 8), Ui.dp(c, 20), Ui.dp(c, 6));
        col.addView(resumen);

        FrameLayout zona = new FrameLayout(c);
        col.addView(zona, new LinearLayout.LayoutParams(-1, 0, 1f));

        lista = new ListView(c);
        lista.setAdapter(adaptador);
        lista.setDivider(new android.graphics.drawable.ColorDrawable(Ui.LINEA));
        lista.setDividerHeight(1);
        lista.setClipToPadding(false);
        lista.setPadding(0, 0, 0, Ui.dp(c, 88));
        lista.setBackgroundColor(Ui.PAPEL);
        lista.setOnItemClickListener((p, v, pos, id) -> host.abrirDetalle(adaptador.datos.get(pos)));
        zona.addView(lista, new LayoutParams(-1, -1));

        vacio = new LinearLayout(c);
        vacio.setOrientation(LinearLayout.VERTICAL);
        vacio.setGravity(Gravity.CENTER_HORIZONTAL);
        vacio.setPadding(Ui.dp(c, 32), Ui.dp(c, 56), Ui.dp(c, 32), 0);
        vacioTitulo = Ui.titulo(c, "", 22, Ui.OCEANO);
        vacioTitulo.setGravity(Gravity.CENTER);
        vacioTexto = Ui.texto(c, "", 15, Ui.SUAVE, false);
        vacioTexto.setGravity(Gravity.CENTER);
        vacioTexto.setPadding(0, Ui.dp(c, 8), 0, Ui.dp(c, 18));
        vacioImportar = Ui.boton(c, "Importar países", Ui.MAR, 0xFFFFFFFF, 0);
        vacioImportar.setOnClickListener(v -> importar());
        vacio.addView(vacioTitulo);
        vacio.addView(vacioTexto);
        vacio.addView(vacioImportar);
        vacio.setVisibility(GONE);
        zona.addView(vacio, new LayoutParams(-1, -2));

        Button nuevo = Ui.boton(c, "Nuevo país", Ui.MAR, 0xFFFFFFFF, 0);
        nuevo.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_mas, 0, 0, 0);
        nuevo.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
        nuevo.setCompoundDrawablePadding(Ui.dp(c, 8));
        nuevo.setOnClickListener(v -> host.abrirDetalle(null));
        LayoutParams fab = new LayoutParams(-2, Ui.dp(c, 52), Gravity.BOTTOM | Gravity.END);
        fab.setMargins(0, 0, Ui.dp(c, 18), Ui.dp(c, 18));
        addView(nuevo, fab);

        TextWatcher espera = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int i, int j, int k) { }
            public void onTextChanged(CharSequence s, int i, int j, int k) { }
            public void afterTextChanged(Editable e) {
                retraso.removeCallbacksAndMessages(null);
                retraso.postDelayed(ListaPaisesVista.this::recargar, 250);
            }
        };
        fNombre.addTextChangedListener(espera);
        fCapital.addTextChangedListener(espera);
    }

    private void pintarChips() {
        chips.removeAllViews();
        for (final String r : REGIONES) {
            TextView ch = Ui.chip(getContext(), r.isEmpty() ? "Todas" : r, r.equals(region));
            ch.setOnClickListener(v -> {
                region = r;
                pintarChips();
                recargar();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, Ui.dp(getContext(), 40));
            lp.rightMargin = Ui.dp(getContext(), 8);
            chips.addView(ch, lp);
        }
    }

    void recargar() {
        final int mia = ++consulta;
        final boolean filtrando = !fNombre.getText().toString().trim().isEmpty() || !fCapital.getText().toString().trim().isEmpty() || !region.isEmpty();
        final String n = fNombre.getText().toString(), cap = fCapital.getText().toString(), reg = region;
        ctl.ejecutar(() -> ctl.api().listar(n, cap, reg), new PaisController.Resultado<List<Pais>>() {
            public void ok(List<Pais> v) {
                if (mia != consulta) return;
                adaptador.datos = v;
                adaptador.notifyDataSetChanged();
                boolean hay = !v.isEmpty();
                lista.setVisibility(hay ? VISIBLE : GONE);
                vacio.setVisibility(hay ? GONE : VISIBLE);
                resumen.setText(hay ? "Mostrando " + Ui.entero(v.size()) + (v.size() == 1 ? " país" : " países") : "");
                if (!hay) {
                    vacioTitulo.setText(filtrando ? "Sin resultados" : "Aún no hay países");
                    vacioTexto.setText(filtrando ? "Ningún país coincide con esa búsqueda. Prueba con otro nombre o quita el filtro de región."
                            : "La base de datos está vacía. Importa los países desde la API pública y aparecerán aquí.");
                    vacioImportar.setVisibility(filtrando ? GONE : VISIBLE);
                }
            }

            public void error(String m) {
                if (mia != consulta) return;
                adaptador.datos = new ArrayList<>();
                adaptador.notifyDataSetChanged();
                lista.setVisibility(GONE);
                vacio.setVisibility(VISIBLE);
                vacioTitulo.setText("No se pudo cargar");
                vacioTexto.setText(m);
                vacioImportar.setVisibility(GONE);
                resumen.setText("");
            }
        });
    }

    void importar() {
        host.aviso("Importando países, un momento…", false);
        ctl.ejecutar(() -> ctl.api().importar(), new PaisController.Resultado<String>() {
            public void ok(String r) {
                host.aviso(r, false);
                host.actualizarTotal();
                recargar();
            }

            public void error(String m) {
                host.aviso(m, true);
            }
        });
    }

    // ------------------------------------------------------------------ adaptador
    private class AdaptadorPaises extends BaseAdapter {
        List<Pais> datos = new ArrayList<>();

        public int getCount() { return datos.size(); }
        public Object getItem(int i) { return datos.get(i); }
        public long getItemId(int i) { return datos.get(i).id; }

        public View getView(int i, View reuso, ViewGroup padre) {
            Context c = padre.getContext();
            Fila f;
            if (reuso == null) {
                f = new Fila(c);
            } else {
                f = (Fila) reuso;
            }
            f.pintar(datos.get(i));
            return f;
        }
    }

    private static class Fila extends LinearLayout {
        final android.widget.ImageView bandera;
        final TextView nombre, sub, poblacion;

        Fila(Context c) {
            super(c);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(Ui.dp(c, 16), Ui.dp(c, 10), Ui.dp(c, 16), Ui.dp(c, 10));
            setMinimumHeight(Ui.dp(c, 64));
            setBackground(Ui.conOnda(c, new android.graphics.drawable.ColorDrawable(Ui.PAPEL)));
            bandera = Ui.bandera(c, 40, 28);
            addView(bandera);
            LinearLayout tx = new LinearLayout(c);
            tx.setOrientation(VERTICAL);
            tx.setPadding(Ui.dp(c, 14), 0, Ui.dp(c, 8), 0);
            nombre = Ui.texto(c, "", 16, Ui.TINTA, true);
            sub = Ui.texto(c, "", 13, Ui.SUAVE, false);
            tx.addView(nombre);
            tx.addView(sub);
            addView(tx, new LayoutParams(0, -2, 1f));
            poblacion = Ui.texto(c, "", 14, Ui.TINTA, true);
            addView(poblacion);
        }

        void pintar(Pais p) {
            nombre.setText(p.nombre);
            sub.setText((p.capital.isEmpty() ? "Sin capital" : p.capital) + " · " + p.region);
            poblacion.setText(Ui.compacto(p.poblacion));
            Ui.cargarBandera(bandera, p.bandera);
        }
    }
}
