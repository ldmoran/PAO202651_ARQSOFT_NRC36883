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
        // El servidor va oculto: solo se muestra al pulsar "Servidor" (por defecto se usa la URL configurada).
        final JLabel lblServidor = Estilo.etiqueta("");
        lblServidor.setToolTipText("Servidor");
        lblServidor.setIcon(new Flecha(false));
        lblServidor.setIconTextGap(6);
        lblServidor.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        txtServidor.setVisible(false);
        lblServidor.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                boolean mostrar = !txtServidor.isVisible();
                txtServidor.setVisible(mostrar);
                lblServidor.setIcon(new Flecha(mostrar));
                lblServidor.setText(mostrar ? "Servidor" : "");
                LoginFrame.this.pack();
            }
        });
        c.gridx = 0;
        c.gridy = 2;
        form.add(lblServidor, c);
        c.gridx = 1;
        form.add(txtServidor, c);
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

    /** Flechita del desplegable "Servidor": hacia la derecha si esta plegado, hacia abajo si esta desplegado. */
    private static final class Flecha implements javax.swing.Icon {

        private final boolean abajo;

        Flecha(boolean abajo) {
            this.abajo = abajo;
        }

        @Override
        public int getIconWidth() {
            return 12;
        }

        @Override
        public int getIconHeight() {
            return 12;
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilo.AZUL);
            int[] px = abajo ? new int[] {x + 1, x + 11, x + 6} : new int[] {x + 3, x + 3, x + 10};
            int[] py = abajo ? new int[] {y + 3, y + 3, y + 10} : new int[] {y + 1, y + 11, y + 6};
            g2.fillPolygon(px, py, 3);
            g2.dispose();
        }
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
