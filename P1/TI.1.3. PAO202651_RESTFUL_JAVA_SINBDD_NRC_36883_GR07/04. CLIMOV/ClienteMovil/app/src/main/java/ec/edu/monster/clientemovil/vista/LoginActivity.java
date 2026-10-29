package ec.edu.monster.clientemovil.vista;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import ec.edu.monster.clientemovil.R;
import ec.edu.monster.clientemovil.controlador.ControladorConversiones;
import ec.edu.monster.clientemovil.controlador.SesionApp;
import ec.edu.monster.clientemovil.modelo.RespuestaServicio;

/** Pantalla de inicio de sesion (CU-01). */
public class LoginActivity extends Activity {

    private static final String PREFS = "conuni";
    private static final String CLAVE_SERVIDOR = "servidor";

    private EditText txtServidor;
    private EditText txtUsuario;
    private EditText txtClave;
    private Button btnIngresar;
    private TextView lblEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txtServidor = findViewById(R.id.txtServidor);
        txtUsuario = findViewById(R.id.txtUsuario);
        txtClave = findViewById(R.id.txtClave);
        btnIngresar = findViewById(R.id.btnIngresar);
        lblEstado = findViewById(R.id.lblEstado);

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        txtServidor.setText(prefs.getString(CLAVE_SERVIDOR, ControladorConversiones.urlPorDefecto()));

        TextView lblServidor = findViewById(R.id.lblServidor);
        lblServidor.setOnClickListener(v -> {
            boolean mostrar = txtServidor.getVisibility() != View.VISIBLE;
            txtServidor.setVisibility(mostrar ? View.VISIBLE : View.GONE);
            lblServidor.setText(mostrar ? R.string.servidor : null);
            lblServidor.setCompoundDrawablesWithIntrinsicBounds(
                    mostrar ? R.drawable.ic_flecha_abajo : R.drawable.ic_flecha_der, 0, 0, 0);
        });

        btnIngresar.setOnClickListener(v -> ingresar());
        txtClave.setOnEditorActionListener((v, accion, ev) -> {
            if (accion == EditorInfo.IME_ACTION_DONE || accion == EditorInfo.IME_ACTION_GO) {
                ingresar();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (SesionApp.haySesion()) {
            irAConversion();
        }
    }

    private void mostrar(String texto, int color) {
        lblEstado.setTextColor(color);
        lblEstado.setText(texto);
    }

    private void ingresar() {
        final String url = txtServidor.getText().toString();
        final String usuario = txtUsuario.getText().toString();
        final String clave = txtClave.getText().toString();
        btnIngresar.setEnabled(false);
        mostrar("Conectando...", getResources().getColor(R.color.azul, getTheme()));
        new Thread(() -> {
            ControladorConversiones c = SesionApp.iniciar(url);
            RespuestaServicio r = c.iniciarSesion(usuario, clave);
            runOnUiThread(() -> {
                if (isFinishing()) {
                    return;
                }
                btnIngresar.setEnabled(true);
                if (r.esExitosa()) {
                    getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                            .putString(CLAVE_SERVIDOR, c.getUrl()).apply();
                    txtClave.setText("");
                    irAConversion();
                } else {
                    SesionApp.cerrar();
                    mostrar("[" + r.getCodigo() + "] " + r.getMensaje(), Color.parseColor("#B00020"));
                }
            });
        }).start();
    }

    private void irAConversion() {
        startActivity(new Intent(this, ConversionActivity.class));
        finish();
    }
}
