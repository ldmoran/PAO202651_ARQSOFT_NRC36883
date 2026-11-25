package ec.edu.monster.paises.movil.vista;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import ec.edu.monster.paises.movil.R;
import ec.edu.monster.paises.movil.controlador.PaisController;
import ec.edu.monster.paises.movil.modelo.Pais;

/** Actividad 2: crear, editar y eliminar un pais. Las reglas de validacion viven en el servidor. */
class DetallePaisVista extends LinearLayout {
    private static final String[] REGIONES = {"América", "Europa", "Asia", "África", "Oceanía", "Antártida"};

    private final PaisController ctl;
    private final Anfitrion host;
    private final TextView titulo, error;
    private final ImageView bandera;
    private final EditText nombre, codigo, capital, subregion, poblacion, area, moneda, urlBandera;
    private final Spinner region;
    private final Button guardar, eliminar;
    private final Handler reloj = new Handler(Looper.getMainLooper());
    private Pais actual;
    private boolean confirmando;

    DetallePaisVista(Context c, PaisController ctl, Anfitrion host) {
        super(c);
        this.ctl = ctl;
        this.host = host;
        setOrientation(VERTICAL);
        setBackgroundColor(Ui.FONDO);

        LinearLayout barra = new LinearLayout(c);
        barra.setOrientation(HORIZONTAL);
        barra.setGravity(Gravity.CENTER_VERTICAL);
        barra.setPadding(Ui.dp(c, 6), Ui.dp(c, 8), Ui.dp(c, 16), 0);
        View atras = Ui.botonIcono(c, R.drawable.ic_atras, Ui.OCEANO, "Volver");
        atras.setOnClickListener(v -> host.cerrarDetalle(false));
        titulo = Ui.titulo(c, "País", 26, Ui.OCEANO);
        barra.addView(atras);
        barra.addView(titulo);
        addView(barra);

        ScrollView sv = new ScrollView(c);
        sv.setFillViewport(false);
        addView(sv, new LayoutParams(-1, 0, 1f));
        LinearLayout f = new LinearLayout(c);
        f.setOrientation(VERTICAL);
        f.setPadding(Ui.dp(c, 20), Ui.dp(c, 8), Ui.dp(c, 20), Ui.dp(c, 28));
        sv.addView(f);

        bandera = new ImageView(c);
        bandera.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bandera.setBackground(Ui.caja(c, 0xFFF1ECE0, 12, 0));
        bandera.setPadding(Ui.dp(c, 14), Ui.dp(c, 14), Ui.dp(c, 14), Ui.dp(c, 14));
        f.addView(bandera, new LayoutParams(-1, Ui.dp(c, 130)));

        nombre = Ui.campo(c, "Nombre");
        codigo = Ui.campo(c, "ECU");
        codigo.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(3), new android.text.InputFilter.AllCaps()});
        capital = Ui.campo(c, "Capital");
        subregion = Ui.campo(c, "Subregión");
        poblacion = Ui.campoNumero(c, "0", false);
        area = Ui.campoNumero(c, "0", true);
        moneda = Ui.campo(c, "Moneda");
        urlBandera = Ui.campo(c, "https://…");
        region = new Spinner(c);
        ArrayAdapter<String> ad = new ArrayAdapter<>(c, android.R.layout.simple_spinner_item, REGIONES);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        region.setAdapter(ad);
        region.setBackground(Ui.caja(c, Ui.PAPEL, 12, Ui.LINEA));
        region.setPadding(Ui.dp(c, 8), 0, Ui.dp(c, 8), 0);

        f.addView(etiquetado(c, "Nombre", nombre));
        f.addView(par(c, etiquetado(c, "Código (3 letras)", codigo), etiquetado(c, "Capital", capital)));
        f.addView(par(c, etiquetado(c, "Región", region), etiquetado(c, "Subregión", subregion)));
        f.addView(par(c, etiquetado(c, "Población", poblacion), etiquetado(c, "Área (km²)", area)));
        f.addView(etiquetado(c, "Moneda", moneda));
        f.addView(etiquetado(c, "Bandera (dirección de la imagen)", urlBandera));

        error = Ui.texto(c, "", 14, Ui.ERROR, true);
        error.setVisibility(GONE);
        error.setPadding(0, Ui.dp(c, 12), 0, 0);
        f.addView(error);

        LinearLayout pie = new LinearLayout(c);
        pie.setOrientation(HORIZONTAL);
        pie.setPadding(0, Ui.dp(c, 18), 0, 0);
        guardar = Ui.boton(c, "Guardar", Ui.MAR, 0xFFFFFFFF, 0);
        guardar.setOnClickListener(v -> guardar());
        eliminar = Ui.boton(c, "Eliminar", 0, Ui.ERROR, 0xFFE2B7A8);
        eliminar.setBackground(Ui.conOnda(c, Ui.caja(c, 0, 12, 0xFFE2B7A8)));
        eliminar.setOnClickListener(v -> eliminar());
        LayoutParams g = new LayoutParams(0, Ui.dp(c, 48), 1f);
        g.rightMargin = Ui.dp(c, 10);
        pie.addView(guardar, g);
        pie.addView(eliminar);
        f.addView(pie);
    }

    private static View etiquetado(Context c, String etiqueta, View campo) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(VERTICAL);
        l.setPadding(0, Ui.dp(c, 14), 0, 0);
        TextView t = Ui.texto(c, etiqueta, 12, Ui.SUAVE, true);
        t.setPadding(Ui.dp(c, 2), 0, 0, Ui.dp(c, 6));
        l.addView(t);
        l.addView(campo, new LayoutParams(-1, Ui.dp(c, 48)));
        return l;
    }

    private static View par(Context c, View a, View b) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(HORIZONTAL);
        LayoutParams pa = new LayoutParams(0, -2, 1f);
        pa.rightMargin = Ui.dp(c, 6);
        LayoutParams pb = new LayoutParams(0, -2, 1f);
        pb.leftMargin = Ui.dp(c, 6);
        l.addView(a, pa);
        l.addView(b, pb);
        return l;
    }

    /** Muestra un pais para editarlo, o un formulario vacio si es null. */
    void mostrar(Pais p) {
        actual = p;
        confirmando = false;
        reloj.removeCallbacksAndMessages(null);
        error.setVisibility(GONE);
        boolean nuevo = p == null;
        titulo.setText(nuevo ? "Nuevo país" : "Editar país");
        eliminar.setVisibility(nuevo ? GONE : VISIBLE);
        eliminar.setText("Eliminar");
        eliminar.setTextColor(Ui.ERROR);
        eliminar.setBackground(Ui.conOnda(getContext(), Ui.caja(getContext(), 0, 12, 0xFFE2B7A8)));
        guardar.setEnabled(true);
        Pais x = nuevo ? new Pais() : p;
        nombre.setText(x.nombre);
        codigo.setText(x.codigo);
        capital.setText(x.capital);
        subregion.setText(x.subregion);
        poblacion.setText(x.poblacion == 0 ? "" : String.valueOf(x.poblacion));
        area.setText(x.area == 0 ? "" : (x.area == Math.floor(x.area) ? String.valueOf((long) x.area) : String.valueOf(x.area)));
        moneda.setText(x.moneda);
        urlBandera.setText(x.bandera);
        int pos = 0;
        for (int i = 0; i < REGIONES.length; i++) if (REGIONES[i].equals(x.region)) pos = i;
        region.setSelection(pos);
        bandera.setVisibility(x.bandera.isEmpty() ? GONE : VISIBLE);
        Ui.cargarBandera(bandera, x.bandera);
    }

    private void guardar() {
        Pais p = new Pais();
        p.id = actual == null ? 0 : actual.id;
        p.nombre = nombre.getText().toString().trim();
        p.codigo = codigo.getText().toString().trim().toUpperCase();
        p.capital = capital.getText().toString().trim();
        p.region = (String) region.getSelectedItem();
        p.subregion = subregion.getText().toString().trim();
        p.moneda = moneda.getText().toString().trim();
        p.bandera = urlBandera.getText().toString().trim();
        try {
            String pob = poblacion.getText().toString().trim();
            String ar = area.getText().toString().trim().replace(',', '.');
            p.poblacion = pob.isEmpty() ? 0 : Long.parseLong(pob);
            p.area = ar.isEmpty() ? 0 : Double.parseDouble(ar);
        } catch (NumberFormatException e) {
            mostrarError("Población y área deben ser números");
            return;
        }
        error.setVisibility(GONE);
        guardar.setEnabled(false);
        final boolean nuevo = p.id == 0;
        ctl.ejecutar(() -> nuevo ? ctl.api().crear(p) : ctl.api().actualizar(p), new PaisController.Resultado<Pais>() {
            public void ok(Pais r) {
                host.aviso(nuevo ? "País creado" : "País guardado", false);
                host.cerrarDetalle(true);
            }

            public void error(String m) {
                guardar.setEnabled(true);
                mostrarError(m);
            }
        });
    }

    /** Eliminar pide confirmar con un segundo toque, para no borrar por accidente. */
    private void eliminar() {
        if (actual == null) return;
        if (!confirmando) {
            confirmando = true;
            eliminar.setText("Toca otra vez para eliminar");
            eliminar.setTextColor(0xFFFFFFFF);
            eliminar.setBackground(Ui.conOnda(getContext(), Ui.caja(getContext(), Ui.ERROR, 12, 0)));
            reloj.postDelayed(() -> {
                confirmando = false;
                eliminar.setText("Eliminar");
                eliminar.setTextColor(Ui.ERROR);
                eliminar.setBackground(Ui.conOnda(getContext(), Ui.caja(getContext(), 0, 12, 0xFFE2B7A8)));
            }, 3500);
            return;
        }
        final int id = actual.id;
        ctl.ejecutar(() -> {
            ctl.api().eliminar(id);
            return Boolean.TRUE;
        }, new PaisController.Resultado<Boolean>() {
            public void ok(Boolean v) {
                host.aviso("País eliminado", false);
                host.cerrarDetalle(true);
            }

            public void error(String m) {
                mostrarError(m);
            }
        });
    }

    private void mostrarError(String m) {
        error.setText(m);
        error.setVisibility(VISIBLE);
    }
}
