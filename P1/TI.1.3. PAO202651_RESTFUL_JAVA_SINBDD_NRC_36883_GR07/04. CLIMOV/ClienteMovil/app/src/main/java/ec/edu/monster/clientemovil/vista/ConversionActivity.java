package ec.edu.monster.clientemovil.vista;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import ec.edu.monster.clientemovil.R;
import ec.edu.monster.clientemovil.controlador.ControladorConversiones;
import ec.edu.monster.clientemovil.controlador.SesionApp;
import ec.edu.monster.clientemovil.modelo.RespuestaServicio;
import java.util.List;

/**
 * Pantalla de conversion (CU-02, CU-03 y CU-04). Solo se muestra con sesion
 * activa; si el servidor rechaza el token, vuelve al login.
 */
public class ConversionActivity extends Activity {

    private static final int COLOR_ERROR = Color.parseColor("#B00020");
    private static final int COLOR_EXITO = Color.parseColor("#1B7F3B");
    private static final int COLOR_INFO = Color.parseColor("#1F3864");

    private ControladorConversiones controlador;
    private Spinner cmbCategoria;
    private Spinner cmbOrigen;
    private Spinner cmbDestino;
    private EditText txtValor;
    private Button btnConvertir;
    private Button btnIntercambiar;
    private TextView lblResultado;
    private TextView lblEstado;
    private boolean categoriaInicializada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controlador = SesionApp.actual();
        if (controlador == null || !controlador.haySesion()) {
            volverAlLogin(null);
            return;
        }
        setContentView(R.layout.activity_conversion);
        cmbCategoria = findViewById(R.id.cmbCategoria);
        cmbOrigen = findViewById(R.id.cmbOrigen);
        cmbDestino = findViewById(R.id.cmbDestino);
        txtValor = findViewById(R.id.txtValor);
        btnConvertir = findViewById(R.id.btnConvertir);
        btnIntercambiar = findViewById(R.id.btnIntercambiar);
        lblResultado = findViewById(R.id.lblResultado);
        lblEstado = findViewById(R.id.lblEstado);

        findViewById(R.id.btnCerrar).setOnClickListener(v -> volverAlLogin(null));
        btnConvertir.setOnClickListener(v -> convertir());
        btnIntercambiar.setOnClickListener(v -> intercambiar());
        cmbCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int posicion, long id) {
                if (categoriaInicializada) {
                    cargarUnidades();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        habilitar(false);
        cargarCategorias();
    }

    private void habilitar(boolean si) {
        cmbCategoria.setEnabled(si);
        cmbOrigen.setEnabled(si);
        cmbDestino.setEnabled(si);
        txtValor.setEnabled(si);
        btnConvertir.setEnabled(si);
        btnIntercambiar.setEnabled(si);
    }

    private void estado(String texto, int color) {
        lblEstado.setTextColor(color);
        lblEstado.setText(texto);
    }

    private void llenar(Spinner s, List<String> items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
    }

    /** Muestra el error; si el token ya no sirve, avisa y regresa al login. */
    private void manejarError(RespuestaServicio r) {
        estado("[" + r.getCodigo() + "] " + r.getMensaje(), COLOR_ERROR);
        if (r.requiereNuevoLogin()) {
            volverAlLogin("Su sesión terminó. Inicie sesión nuevamente.");
        }
    }

    private void volverAlLogin(String aviso) {
        SesionApp.cerrar();
        if (aviso != null) {
            Toast.makeText(this, aviso, Toast.LENGTH_LONG).show();
        }
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // ---------------- catalogo (CU-02 / CU-03) ----------------

    private void cargarCategorias() {
        estado("Cargando catálogo...", COLOR_INFO);
        new Thread(() -> {
            RespuestaServicio r = controlador.categorias();
            runOnUiThread(() -> {
                if (isFinishing()) {
                    return;
                }
                if (!r.esExitosa()) {
                    manejarError(r);
                    return;
                }
                categoriaInicializada = false;
                llenar(cmbCategoria, r.getDatos());
                categoriaInicializada = true;
                cargarUnidades();
            });
        }).start();
    }

    private void cargarUnidades() {
        final String categoria = (String) cmbCategoria.getSelectedItem();
        if (categoria == null) {
            return;
        }
        habilitar(false);
        estado("Cargando unidades de " + categoria + "...", COLOR_INFO);
        new Thread(() -> {
            RespuestaServicio r = controlador.unidades(categoria);
            runOnUiThread(() -> {
                if (isFinishing()) {
                    return;
                }
                if (!r.esExitosa()) {
                    manejarError(r);
                    cmbCategoria.setEnabled(true);
                    return;
                }
                llenar(cmbOrigen, r.getDatos());
                llenar(cmbDestino, r.getDatos());
                if (r.getDatos().size() > 1) {
                    cmbDestino.setSelection(1);
                }
                lblResultado.setText(R.string.resultado);
                estado("", COLOR_INFO);
                habilitar(true);
            });
        }).start();
    }

    private void intercambiar() {
        int o = cmbOrigen.getSelectedItemPosition();
        cmbOrigen.setSelection(cmbDestino.getSelectedItemPosition());
        cmbDestino.setSelection(o);
    }

    // ---------------- conversion (CU-04) ----------------

    private void convertir() {
        final String categoria = (String) cmbCategoria.getSelectedItem();
        final String origen = (String) cmbOrigen.getSelectedItem();
        final String destino = (String) cmbDestino.getSelectedItem();
        final double valor;
        try {
            valor = Double.parseDouble(txtValor.getText().toString().trim().replace(',', '.'));
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            estado("Ingrese un número válido (ejemplo: 12.5).", COLOR_ERROR);
            return;
        }
        if (categoria == null || origen == null || destino == null) {
            estado("Seleccione la categoría y las unidades.", COLOR_ERROR);
            return;
        }
        btnConvertir.setEnabled(false);
        estado("Convirtiendo...", COLOR_INFO);
        new Thread(() -> {
            RespuestaServicio r = controlador.convertir(categoria, valor, origen, destino);
            runOnUiThread(() -> {
                if (isFinishing()) {
                    return;
                }
                btnConvertir.setEnabled(true);
                if (r.esExitosa() && r.getResultado() != null) {
                    lblResultado.setText(ControladorConversiones.formatear(valor) + " " + r.getUnidadOrigen() + " = "
                            + ControladorConversiones.formatear(r.getResultado()) + " " + r.getUnidadDestino());
                    estado(r.getMensaje(), COLOR_EXITO);
                } else {
                    manejarError(r);
                }
            });
        }).start();
    }
}
