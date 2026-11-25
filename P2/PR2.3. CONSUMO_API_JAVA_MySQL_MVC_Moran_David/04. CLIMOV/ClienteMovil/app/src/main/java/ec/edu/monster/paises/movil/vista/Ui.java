package ec.edu.monster.paises.movil.vista;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.InputType;
import android.util.LruCache;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Colores, tamanos y piezas sueltas del tema "atlas". Todo se arma en codigo, sin XML de pantallas. */
final class Ui {
    static final int OCEANO = 0xFF10303F, MAR = 0xFF0F6F80, MAR_OSCURO = 0xFF0B5967, MAR_CLARO = 0xFFDCEDF0,
            TERRACOTA = 0xFFB5471F, ARENA = 0xFFF1E3C4, FONDO = 0xFFF5F1E8, PAPEL = 0xFFFFFDF8,
            TINTA = 0xFF1D2A30, SUAVE = 0xFF51616A, LINEA = 0xFFDCD5C2, ERROR = 0xFFB3261E;

    static final Typeface SERIFA = Typeface.create("serif", Typeface.BOLD);
    private static final NumberFormat ENTERO = NumberFormat.getIntegerInstance(new Locale("es", "EC"));
    private static final NumberFormat UN_DECIMAL;

    static {
        UN_DECIMAL = NumberFormat.getNumberInstance(new Locale("es", "EC"));
        UN_DECIMAL.setMaximumFractionDigits(1);
        UN_DECIMAL.setMinimumFractionDigits(1);
    }

    private Ui() {
    }

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static String entero(long n) {
        return ENTERO.format(n);
    }

    /** 1402112000 -> "1402,1 M". */
    static String compacto(long n) {
        return n >= 1_000_000 ? UN_DECIMAL.format(n / 1_000_000.0).replace(".", "") + " M" : ENTERO.format(n);
    }

    // ------------------------------------------------------------------ fondos
    static GradientDrawable caja(Context c, int relleno, float radioDp, int borde) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(relleno);
        g.setCornerRadius(dp(c, radioDp));
        if (borde != 0) g.setStroke(dp(c, 1.5f), borde);
        return g;
    }

    static Drawable conOnda(Context c, Drawable base) {
        return new RippleDrawable(ColorStateList.valueOf(0x1F000000), base, null);
    }

    /** Efecto de pulsar: la pieza se encoge un 3 % mientras se mantiene el dedo (se nota que la app escucha). */
    static void presionable(final View v) {
        v.setOnTouchListener((vista, e) -> {
            int a = e.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) {
                vista.animate().scaleX(.97f).scaleY(.97f).setDuration(120).start();
            } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                vista.animate().scaleX(1f).scaleY(1f).setDuration(160).start();
            }
            return false;
        });
    }

    // ------------------------------------------------------------------ texto
    static TextView texto(Context c, String t, float sp, int color, boolean negrita) {
        TextView v = new TextView(c);
        v.setText(t);
        v.setTextSize(sp);
        v.setTextColor(color);
        if (negrita) v.setTypeface(Typeface.DEFAULT_BOLD);
        return v;
    }

    static TextView titulo(Context c, String t, float sp, int color) {
        TextView v = texto(c, t, sp, color, false);
        v.setTypeface(SERIFA);
        return v;
    }

    // ------------------------------------------------------------------ controles
    static Button boton(Context c, String t, int relleno, int colorTexto, int borde) {
        Button b = new Button(c);
        b.setText(t);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(colorTexto);
        b.setStateListAnimator(null);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(dp(c, 20), 0, dp(c, 20), 0);
        b.setBackground(conOnda(c, caja(c, relleno, 12, borde)));
        b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(c, 48)));
        presionable(b);
        return b;
    }

    static ImageButton botonIcono(Context c, int recurso, int color, String descripcion) {
        ImageButton b = new ImageButton(c);
        b.setImageResource(recurso);
        b.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        b.setBackground(conOnda(c, new ColorDrawable(0)));
        b.setContentDescription(descripcion);
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(c, 48), dp(c, 48)));
        return b;
    }

    static ImageView icono(Context c, int recurso, int color, int tamDp) {
        ImageView i = new ImageView(c);
        i.setImageResource(recurso);
        i.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        i.setLayoutParams(new LinearLayout.LayoutParams(dp(c, tamDp), dp(c, tamDp)));
        return i;
    }

    static EditText campo(Context c, String pista) {
        EditText e = new EditText(c);
        e.setHint(pista);
        e.setSingleLine(true);
        e.setTextSize(16);
        e.setTextColor(TINTA);
        e.setHintTextColor(0xFF5E6B6F);
        e.setPadding(dp(c, 14), 0, dp(c, 14), 0);
        e.setMinHeight(dp(c, 48));
        e.setGravity(Gravity.CENTER_VERTICAL);
        StateListDrawable s = new StateListDrawable();
        GradientDrawable enfocado = caja(c, PAPEL, 12, MAR);
        enfocado.setStroke(dp(c, 2), MAR);
        s.addState(new int[]{android.R.attr.state_focused}, enfocado);
        s.addState(new int[]{}, caja(c, PAPEL, 12, LINEA));
        e.setBackground(s);
        return e;
    }

    static EditText campoNumero(Context c, String pista, boolean decimal) {
        EditText e = campo(c, pista);
        e.setInputType(decimal ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL : InputType.TYPE_CLASS_NUMBER);
        return e;
    }

    static TextView chip(Context c, String t, boolean activo) {
        TextView v = texto(c, t, 14, activo ? ARENA : TINTA, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(c, 16), 0, dp(c, 16), 0);
        v.setMinHeight(dp(c, 40));
        v.setBackground(conOnda(c, caja(c, activo ? OCEANO : PAPEL, 20, activo ? 0 : LINEA)));
        presionable(v);
        return v;
    }

    // ------------------------------------------------------------------ banderas (se descargan una vez y se guardan en memoria)
    private static final ExecutorService DESCARGAS = Executors.newFixedThreadPool(3);
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(80);

    static ImageView bandera(Context c, int anchoDp, int altoDp) {
        ImageView i = new ImageView(c);
        i.setScaleType(ImageView.ScaleType.CENTER_CROP);
        i.setBackground(caja(c, 0xFFE9E4D4, 4, 0));
        i.setLayoutParams(new LinearLayout.LayoutParams(dp(c, anchoDp), dp(c, altoDp)));
        return i;
    }

    static void cargarBandera(final ImageView vista, final String url) {
        vista.setTag(url);
        vista.setImageDrawable(null);
        if (url == null || url.isEmpty()) return;
        Bitmap en = CACHE.get(url);
        if (en != null) {
            vista.setImageBitmap(en);
            return;
        }
        DESCARGAS.execute(() -> {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setConnectTimeout(6000);
                con.setReadTimeout(8000);
                try (InputStream in = con.getInputStream()) {
                    final Bitmap b = BitmapFactory.decodeStream(in);
                    if (b != null) {
                        CACHE.put(url, b);
                        vista.post(() -> {
                            if (url.equals(vista.getTag())) vista.setImageBitmap(b);
                        });
                    }
                }
            } catch (Exception ignorada) {
                // sin internet o sin bandera: queda el cuadro de color
            }
        });
    }
}
