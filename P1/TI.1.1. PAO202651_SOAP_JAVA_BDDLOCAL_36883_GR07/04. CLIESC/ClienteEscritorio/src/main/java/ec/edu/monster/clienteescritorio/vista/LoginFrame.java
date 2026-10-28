package ec.edu.monster.clienteescritorio.vista;

import ec.edu.monster.clienteescritorio.controlador.ControladorConversiones;
import ec.edu.monster.clienteescritorio.modelo.RespuestaServicio;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/** Ventana de inicio de sesion (CU-01). */
public class LoginFrame extends JFrame {

    private final JTextField txtUsuario = new JTextField(26);
    private final JPasswordField txtClave = new JPasswordField(26);
    private final JTextField txtServidor = new JTextField(ControladorConversiones.urlPorDefecto(), 26);
    private final JLabel lblEstado = new JLabel(" ", SwingConstants.CENTER);
    private final javax.swing.JButton btnIngresar = Estilo.boton("Iniciar sesión");

    public LoginFrame() {
        super("Conversión de Unidades Monster - Iniciar sesión");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel fondo = new JPanel(new BorderLayout());
        fondo.setBackground(Estilo.FONDO);
        fondo.setBorder(new EmptyBorder(16, 20, 16, 20));
        JPanel raiz = Estilo.tarjeta(new BorderLayout(0, 10));
        fondo.add(raiz, BorderLayout.CENTER);

        // Encabezado: imagen y titulo
        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false);
        ImageIcon icono = Estilo.banner(400);
        if (icono != null) {
            cabecera.add(new JLabel(icono), BorderLayout.NORTH);
        }
        JPanel titulos = new JPanel(new BorderLayout(0, 2));
        titulos.setOpaque(false);
        titulos.add(Estilo.titulo("Conversión de Unidades Monster"), BorderLayout.NORTH);
        titulos.add(Estilo.subtitulo("Longitud · Masa · Temperatura"), BorderLayout.CENTER);
        cabecera.add(titulos, BorderLayout.CENTER);
        raiz.add(cabecera, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 4, 5, 4);
        c.anchor = GridBagConstraints.WEST;
        agregar(form, c, 0, "Usuario", txtUsuario);
        agregar(form, c, 1, "Clave", txtClave);
        agregar(form, c, 2, "Servidor", txtServidor);
        raiz.add(form, BorderLayout.CENTER);

        // Boton y estado
        JPanel sur = new JPanel(new BorderLayout(0, 6));
        sur.setOpaque(false);
        JPanel boton = new JPanel();
        boton.setOpaque(false);
        boton.add(btnIngresar);
        sur.add(boton, BorderLayout.NORTH);
        lblEstado.setFont(Estilo.fuente(Font.PLAIN, 13));
        sur.add(lblEstado, BorderLayout.CENTER);
        raiz.add(sur, BorderLayout.SOUTH);

        Estilo.estilizarCampo(txtUsuario);
        Estilo.estilizarCampo(txtClave);
        Estilo.estilizarCampo(txtServidor);
        setContentPane(fondo);
        btnIngresar.addActionListener(e -> ingresar());
        txtClave.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar);
        pack();
        setLocationRelativeTo(null);
    }

    private void agregar(JPanel p, GridBagConstraints c, int fila, String texto, java.awt.Component campo) {
        c.gridx = 0;
        c.gridy = fila;
        p.add(Estilo.etiqueta(texto), c);
        c.gridx = 1;
        p.add(campo, c);
    }

    private void mostrar(String texto, Color color) {
        lblEstado.setForeground(color);
        lblEstado.setText(texto);
    }

    private void ingresar() {
        final String usuario = txtUsuario.getText();
        final String clave = new String(txtClave.getPassword());
        final ControladorConversiones controlador = ControladorConversiones.conectar(txtServidor.getText());
        btnIngresar.setEnabled(false);
        mostrar("Conectando...", Estilo.AZUL);
        new SwingWorker<RespuestaServicio, Void>() {
            @Override
            protected RespuestaServicio doInBackground() {
                return controlador.iniciarSesion(usuario, clave);
            }

            @Override
            protected void done() {
                btnIngresar.setEnabled(true);
                try {
                    RespuestaServicio r = get();
                    if (r.esExitosa()) {
                        new ConversionFrame(controlador).setVisible(true);
                        dispose();
                    } else {
                        mostrar("[" + r.getCodigo() + "] " + r.getMensaje(), Estilo.ERROR);
                    }
                } catch (Exception ex) {
                    mostrar("Error inesperado: " + ex.getMessage(), Estilo.ERROR);
                }
            }
        }.execute();
    }

    // Acceso para pruebas
    JTextField campoUsuario() {
        return txtUsuario;
    }

    JPasswordField campoClave() {
        return txtClave;
    }

    JLabel etiquetaEstado() {
        return lblEstado;
    }
}
