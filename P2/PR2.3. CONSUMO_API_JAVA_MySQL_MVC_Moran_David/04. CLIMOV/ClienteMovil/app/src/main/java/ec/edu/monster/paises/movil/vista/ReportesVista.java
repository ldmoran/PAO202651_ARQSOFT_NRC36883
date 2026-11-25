package ec.edu.monster.paises.movil.vista;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import ec.edu.monster.paises.movil.controlador.PaisController;
import ec.edu.monster.paises.movil.modelo.Pais;
import ec.edu.monster.paises.movil.modelo.Reporte;

/** Actividad 5: paises por region, pais mas poblado, region con mas paises y promedio de poblacion. */
class ReportesVista extends ScrollView {
    private final PaisController ctl;
    private final LinearLayout col;

    ReportesVista(Context c, PaisController ctl) {
        super(c);
        this.ctl = ctl;
        col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(Ui.dp(c, 20), Ui.dp(c, 18), Ui.dp(c, 20), Ui.dp(c, 28));
        addView(col);
    }

    void recargar() {
        ctl.ejecutar(() -> ctl.api().reportes(), new PaisController.Resultado<Reporte>() {
            public void ok(Reporte r) {
                pintar(r);
            }

            public void error(String m) {
                col.removeAllViews();
                col.addView(Ui.titulo(getContext(), "Reportes", 30, Ui.OCEANO));
                TextView t = Ui.texto(getContext(), "No se pudieron cargar los reportes.\n" + m, 15, Ui.SUAVE, false);
                t.setPadding(0, Ui.dp(getContext(), 12), 0, 0);
                col.addView(t);
            }
        });
    }

    private static CharSequence resaltado(String antes, String dato, String despues, int color) {
        SpannableStringBuilder s = new SpannableStringBuilder(antes);
        int ini = s.length();
        s.append(dato);
        s.setSpan(new ForegroundColorSpan(color), ini, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(Typeface.BOLD), ini, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.append(despues);
        return s;
    }

    private void pintar(Reporte r) {
        Context c = getContext();
        col.removeAllViews();
        col.addView(Ui.titulo(c, "Reportes", 30, Ui.OCEANO));

        Pais mas = r.masPoblado;
        TextView frase = Ui.titulo(c, "", 26, Ui.OCEANO);
        frase.setPadding(0, Ui.dp(c, 18), 0, 0);
        frase.setLineSpacing(0, 1.05f);
        if (mas == null) {
            frase.setText("Todavía no hay países para reportar.");
            col.addView(frase);
        } else {
            frase.setText(resaltado("El país más poblado es ", mas.nombre, ", con " + Ui.compacto(mas.poblacion) + " de habitantes.", Ui.TERRACOTA));
            col.addView(frase);
            TextView det = Ui.texto(c, (mas.capital.isEmpty() ? "Sin capital" : mas.capital) + " · " + mas.region + " · " + Ui.entero(mas.poblacion) + " personas", 14, Ui.SUAVE, false);
            det.setPadding(0, Ui.dp(c, 8), 0, 0);
            col.addView(det);
        }
        if (!r.porRegion.isEmpty()) {
            Reporte.FilaRegion top = r.porRegion.get(0);
            TextView reg = Ui.texto(c, "", 16, Ui.TINTA, false);
            reg.setText(resaltado("La región con más países es ", top.region, ", con " + Ui.entero(top.paises) + ".", Ui.TERRACOTA));
            reg.setPadding(0, Ui.dp(c, 14), 0, 0);
            col.addView(reg);
        }

        TextView h1 = Ui.texto(c, "Países por región", 16, Ui.OCEANO, true);
        h1.setPadding(0, Ui.dp(c, 28), 0, Ui.dp(c, 12));
        col.addView(h1);
        int max = 1;
        for (Reporte.FilaRegion f : r.porRegion) max = Math.max(max, f.paises);
        int i = 0;
        for (Reporte.FilaRegion f : r.porRegion) col.addView(filaBarra(c, f, max, i++));

        TextView h2 = Ui.texto(c, "Promedio poblacional por región", 16, Ui.OCEANO, true);
        h2.setPadding(0, Ui.dp(c, 28), 0, Ui.dp(c, 8));
        col.addView(h2);
        LinearLayout tabla = new LinearLayout(c);
        tabla.setOrientation(LinearLayout.VERTICAL);
        tabla.setBackground(Ui.caja(c, Ui.PAPEL, 14, Ui.LINEA));
        tabla.setPadding(Ui.dp(c, 14), Ui.dp(c, 6), Ui.dp(c, 14), Ui.dp(c, 6));
        tabla.addView(filaTabla(c, "Región", "Países", "Promedio", true));
        for (Reporte.FilaRegion f : r.porRegion) tabla.addView(filaTabla(c, f.region, Ui.entero(f.paises), Ui.compacto(f.promedio), false));
        col.addView(tabla);
    }

    private static View filaTabla(Context c, String a, String b, String d, boolean cabecera) {
        LinearLayout l = new LinearLayout(c);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setMinimumHeight(Ui.dp(c, 44));
        int color = cabecera ? Ui.SUAVE : Ui.TINTA;
        float sp = cabecera ? 12 : 15;
        TextView x = Ui.texto(c, a, sp, color, !cabecera);
        TextView y = Ui.texto(c, b, sp, color, cabecera);
        TextView z = Ui.texto(c, d, sp, color, cabecera);
        y.setGravity(Gravity.END);
        z.setGravity(Gravity.END);
        l.addView(x, new LinearLayout.LayoutParams(0, -2, 1.4f));
        l.addView(y, new LinearLayout.LayoutParams(0, -2, 0.8f));
        l.addView(z, new LinearLayout.LayoutParams(0, -2, 1.2f));
        return l;
    }

    private static View filaBarra(Context c, Reporte.FilaRegion f, int max, int indice) {
        LinearLayout l = new LinearLayout(c);
        l.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.dp(c, 36));
        l.setLayoutParams(lp);
        TextView n = Ui.texto(c, f.region, 14, Ui.TINTA, true);
        l.addView(n, new LinearLayout.LayoutParams(Ui.dp(c, 84), -2));
        Barra b = new Barra(c, f.paises / (float) max, indice % 2 == 0 ? Ui.TERRACOTA : Ui.MAR, indice);
        l.addView(b, new LinearLayout.LayoutParams(0, Ui.dp(c, 24), 1f));
        TextView v = Ui.texto(c, Ui.entero(f.paises), 14, Ui.TINTA, true);
        v.setGravity(Gravity.END);
        l.addView(v, new LinearLayout.LayoutParams(Ui.dp(c, 44), -2));
        return l;
    }

    /** Barra que crece desde la izquierda al aparecer (curva de salida rapida, 500 ms). */
    private static class Barra extends View {
        private final Paint pista = new Paint(Paint.ANTI_ALIAS_FLAG), relleno = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF caja = new RectF();
        private final float meta;
        private float avance;

        Barra(Context c, float meta, int color, int indice) {
            super(c);
            this.meta = meta;
            pista.setColor(0xFFE7E1D0);
            relleno.setColor(color);
            ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
            a.setDuration(520);
            a.setStartDelay(indice * 60L);
            a.setInterpolator(new PathInterpolator(.23f, 1f, .32f, 1f));
            a.addUpdateListener(x -> {
                avance = (float) x.getAnimatedValue();
                invalidate();
            });
            a.start();
        }

        @Override
        protected void onDraw(Canvas g) {
            float r = Ui.dp(getContext(), 7);
            caja.set(0, 0, getWidth(), getHeight());
            g.drawRoundRect(caja, r, r, pista);
            caja.set(0, 0, Math.max(getHeight(), getWidth() * meta * avance), getHeight());
            g.drawRoundRect(caja, r, r, relleno);
        }
    }
}
