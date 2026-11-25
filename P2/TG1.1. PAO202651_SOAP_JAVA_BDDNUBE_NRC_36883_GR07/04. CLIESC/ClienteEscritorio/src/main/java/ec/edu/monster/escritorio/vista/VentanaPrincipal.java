package ec.edu.monster.escritorio.vista;

import ec.edu.monster.cliente.EurekaSoap;
import ec.edu.monster.cliente.EurekaSoap.Cuenta;
import ec.edu.monster.cliente.EurekaSoap.Movimiento;
import ec.edu.monster.escritorio.controlador.ControladorEscritorio;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Ventana unica de EurekaBank: inicio de sesion y, despues, menu lateral con tres paginas (cuentas, movimientos y operar). */
public class VentanaPrincipal extends JFrame {

    private final ControladorEscritorio ctl;
    private final String direccion;
    private final JLabel estado = new JLabel(" ");
    private final EstadoPunto punto = new EstadoPunto();
    private final JLabel tituloPagina = Estilo.titulo("Cuentas");
    private final JLabel subtituloPagina = Estilo.subtitulo("Cuentas activas con los datos de su cliente");
    private final CardLayout cartas = new CardLayout();
    private final JPanel paginas = new JPanel(cartas);
    private final CardLayout cartasRaiz = new CardLayout();
    private final JPanel raiz = new JPanel(cartasRaiz);

    // inicio de sesion
    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtClave = new JPasswordField(18);
    private final JTextField txtServidorLogin = new JTextField(18);
    private final JLabel lblLogin = new JLabel(" ");
    private final JLabel lblUsuario = new JLabel(" ");

    // cuentas
    private final DefaultTableModel modeloCuentas = soloLectura("Cuenta", "Cliente", "Moneda", "Saldo", "Estado", "Correo", "Teléfono");
    private final JTable tablaCuentas = new JTable(modeloCuentas);
    private List<Cuenta> cuentas = new ArrayList<>();

    // movimientos
    private final JComboBox<String> cmbCuentaMov = new JComboBox<>();
    private final DefaultTableModel modeloMov = soloLectura("Cuenta", "N.º", "Fecha", "Tipo", "Importe", "Saldo", "Referencia");
    private final JTable tablaMov = new JTable(modeloMov);

    // operar
    private final JToggleButton btnDeposito = new JToggleButton("Depósito", true);
    private final JToggleButton btnRetiro = new JToggleButton("Retiro");
    private final JToggleButton btnTransf = new JToggleButton("Transferencia");
    private final JComboBox<String> cmbOrigen = new JComboBox<>();
    private final JComboBox<String> cmbDestino = new JComboBox<>();
    private final JTextField txtImporte = new JTextField(10);
    private final JLabel lblDestino = new JLabel("Cuenta destino");
    private final JLabel lblResultado = new JLabel(" ");

    public VentanaPrincipal(ControladorEscritorio ctl) {
        this(ctl, "");
    }

    public VentanaPrincipal(ControladorEscritorio ctl, String direccion) {
        super("EurekaBank · Banca en línea");
        this.ctl = ctl;
        this.direccion = direccion == null ? "" : direccion;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel app = new JPanel(new BorderLayout());
        app.add(menuLateral(), BorderLayout.WEST);
        app.add(contenido(), BorderLayout.CENTER);
        raiz.add(panelLogin(), "login");
        raiz.add(app, "app");
        setContentPane(raiz);
        setMinimumSize(new Dimension(1040, 660));
        setSize(1200, 760);
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ estructura
    private JPanel menuLateral() {
        JPanel m = new JPanel(new BorderLayout());
        m.setBackground(Estilo.NAVY);
        m.setPreferredSize(new Dimension(236, 100));

        JPanel marca = new JPanel(new GridBagLayout());
        marca.setOpaque(false);
        marca.setBorder(BorderFactory.createEmptyBorder(26, 22, 22, 22));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        c.insets = new Insets(0, 0, 0, 12);
        marca.add(new Estilo.Logo(46), c);
        JLabel nombre = new JLabel("EurekaBank");
        nombre.setFont(Estilo.fuente(Font.BOLD, 19f));
        nombre.setForeground(Color.WHITE);
        JLabel lema = new JLabel("Banca en línea");
        lema.setFont(Estilo.fuente(Font.PLAIN, 12f));
        lema.setForeground(new Color(0xA9BCD0));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTHWEST;
        marca.add(nombre, c);
        c.gridy = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        marca.add(lema, c);
        m.add(marca, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(0, 1, 0, 4));
        botones.setOpaque(false);
        botones.setBorder(BorderFactory.createEmptyBorder(6, 12, 0, 12));
        ButtonGroup grupo = new ButtonGroup();
        BotonMenu cuentasBtn = new BotonMenu("Cuentas", Estilo.Forma.BANCO);
        BotonMenu movBtn = new BotonMenu("Movimientos", Estilo.Forma.RECIBO);
        BotonMenu operarBtn = new BotonMenu("Operar", Estilo.Forma.CARRITO);
        for (BotonMenu b : new BotonMenu[] {cuentasBtn, movBtn, operarBtn}) {
            grupo.add(b);
            botones.add(b);
        }
        cuentasBtn.setSelected(true);
        cuentasBtn.addActionListener(e -> {
            mostrar("cuentas", "Cuentas", "Cuentas activas con los datos de su cliente");
            recargar();
        });
        movBtn.addActionListener(e -> {
            mostrar("movimientos", "Movimientos", "Historial de una cuenta, con el saldo después de cada movimiento");
            cargarMovimientos();
        });
        operarBtn.addActionListener(e -> mostrar("operar", "Operar", "Registre un depósito, un retiro o una transferencia"));
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(botones, BorderLayout.NORTH);
        m.add(envoltorio, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        lblUsuario.setForeground(new Color(0xA9BCD0));
        lblUsuario.setFont(Estilo.fuente(Font.PLAIN, 12.5f));
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(0, 22, 6, 22));
        JButton salir = Estilo.botonSecundario("Cerrar sesión", null);
        JPanel zonaSalir = new JPanel(new BorderLayout());
        zonaSalir.setOpaque(false);
        zonaSalir.setBorder(BorderFactory.createEmptyBorder(0, 22, 4, 22));
        zonaSalir.add(salir, BorderLayout.CENTER);
        salir.addActionListener(e -> {
            txtClave.setText("");
            lblLogin.setText(" ");
            cartasRaiz.show(raiz, "login");
        });
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(lblUsuario, BorderLayout.NORTH);
        arriba.add(zonaSalir, BorderLayout.CENTER);
        sur.add(arriba, BorderLayout.NORTH);
        sur.add(panelServidor(), BorderLayout.SOUTH);
        m.add(sur, BorderLayout.SOUTH);
        return m;
    }

    /** Inicio de sesion (mismo diseno que el resto de proyectos): foto de Monster, campos con icono y boton azul. */
    private JPanel panelLogin() {
        JPanel fondo = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(0xEAF1FB), getWidth(), getHeight(), new Color(0xC9DAF2)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createLineBorder(new Color(0xC4D1E6)));
        tarjeta.setPreferredSize(new Dimension(500, 640));
        tarjeta.add(new Banner(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(4, 40, 22, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridy = 0;
        c.insets = new Insets(14, 0, 4, 0);
        JLabel t = Estilo.titulo("EurekaBank");
        t.setHorizontalAlignment(SwingConstants.CENTER);
        cuerpo.add(t, c);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 18, 0);
        JLabel st = Estilo.subtitulo("Cuentas · Movimientos · Operaciones. Ingrese sus credenciales.");
        st.setHorizontalAlignment(SwingConstants.CENTER);
        cuerpo.add(st, c);
        c.gridy = 2;
        c.insets = new Insets(0, 0, 12, 0);
        cuerpo.add(campoConIcono(false, "Usuario", txtUsuario), c);
        c.gridy = 3;
        cuerpo.add(campoConIcono(true, "Clave", txtClave), c);

        txtServidorLogin.setText(direccion.isBlank() ? EurekaSoap.URL_POR_DEFECTO : direccion);
        txtServidorLogin.setFont(Estilo.fuente(Font.PLAIN, 12f));
        txtServidorLogin.setVisible(false);
        JLabel flecha = new JLabel("", new Triangulo(false), SwingConstants.LEFT);
        flecha.setFont(Estilo.fuente(Font.BOLD, 12f));
        flecha.setForeground(Estilo.GRIS);
        flecha.setIconTextGap(8);
        flecha.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        flecha.setToolTipText("Servidor");
        flecha.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                boolean abrir = !txtServidorLogin.isVisible();
                txtServidorLogin.setVisible(abrir);
                flecha.setText(abrir ? "Servidor" : "");
                flecha.setIcon(new Triangulo(abrir));
                cuerpo.revalidate();
            }
        });
        c.gridy = 4;
        c.insets = new Insets(0, 0, 4, 0);
        cuerpo.add(flecha, c);
        c.gridy = 5;
        cuerpo.add(txtServidorLogin, c);

        JButton entrar = Estilo.botonPrimario("Ingresar", null);
        JPanel zonaBoton = new JPanel();
        zonaBoton.setOpaque(false);
        entrar.setPreferredSize(new Dimension(200, 42));
        zonaBoton.add(entrar);
        c.gridy = 6;
        c.insets = new Insets(12, 0, 4, 0);
        cuerpo.add(zonaBoton, c);
        lblLogin.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogin.setForeground(Estilo.ROJO);
        lblLogin.setFont(Estilo.fuente(Font.PLAIN, 12.5f));
        c.gridy = 7;
        c.insets = new Insets(4, 0, 0, 0);
        cuerpo.add(lblLogin, c);
        JLabel pie = new JLabel("Arquitectura de Software · ESPE · GR07", SwingConstants.CENTER);
        pie.setFont(Estilo.fuente(Font.PLAIN, 11.5f));
        pie.setForeground(Estilo.GRIS);
        c.gridy = 8;
        c.insets = new Insets(10, 0, 0, 0);
        cuerpo.add(pie, c);
        tarjeta.add(cuerpo, BorderLayout.CENTER);
        fondo.add(tarjeta);
        entrar.addActionListener(e -> iniciarSesion());
        txtClave.addActionListener(e -> iniciarSesion());
        txtUsuario.addActionListener(e -> txtClave.requestFocusInWindow());
        return fondo;
    }

    /** Campo con un icono a la izquierda (usuario o llave), como en el login de los demas proyectos. */
    private static JPanel campoConIcono(boolean llave, String pista, JTextField campo) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0xF8FBFF));
        p.setBorder(BorderFactory.createLineBorder(new Color(0xC4D1E6), 1, true));
        JComponent ico = new JComponent() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xF1F4F9));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Estilo.NAVY);
                g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                if (llave) {
                    g2.drawOval(cx - 9, cy - 1, 9, 9);
                    g2.drawLine(cx - 2, cy + 2, cx + 8, cy - 8);
                    g2.drawLine(cx + 4, cy - 4, cx + 7, cy - 1);
                } else {
                    g2.drawOval(cx - 4, cy - 9, 8, 8);
                    g2.drawArc(cx - 9, cy + 1, 18, 16, 0, 180);
                }
                g2.dispose();
            }
        };
        ico.setPreferredSize(new Dimension(42, 42));
        campo.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        campo.setBackground(new Color(0xF8FBFF));
        campo.putClientProperty("JTextField.placeholderText", pista);
        p.add(ico, BorderLayout.WEST);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    /** Foto de Monster en la parte superior de la tarjeta de inicio de sesion. */
    private static final class Banner extends JComponent {
        private final java.awt.image.BufferedImage img = cargar();

        Banner() {
            setPreferredSize(new Dimension(500, 250));
        }

        private static java.awt.image.BufferedImage cargar() {
            try (java.io.InputStream in = VentanaPrincipal.class.getResourceAsStream("/monster.jpg")) {
                return in == null ? null : javax.imageio.ImageIO.read(in);
            } catch (java.io.IOException e) {
                return null;
            }
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            if (img == null) {
                g2.setColor(Estilo.NAVY);
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                double esc = Math.max(getWidth() / (double) img.getWidth(), getHeight() / (double) img.getHeight());
                int w = (int) (img.getWidth() * esc), h = (int) (img.getHeight() * esc);
                g2.drawImage(img, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            }
            g2.dispose();
        }
    }

    private void iniciarSesion() {
        String u = txtUsuario.getText().trim();
        String c = new String(txtClave.getPassword());
        if (u.isEmpty() || c.isEmpty()) {
            lblLogin.setText("Ingrese usuario y clave.");
            return;
        }
        String url = txtServidorLogin.getText().trim();
        if (!ControladorEscritorio.urlValida(url)) {
            lblLogin.setText("La dirección del servidor debe empezar con http:// o https://");
            return;
        }
        ctl.conectarA(url);
        lblLogin.setForeground(Estilo.GRIS);
        lblLogin.setText("Verificando...");
        ctl.ejecutar(() -> ctl.servicio().iniciarSesion(u, c), ok -> {
            if (ok) {
                lblUsuario.setText("Usuario: " + u);
                cartasRaiz.show(raiz, "app");
                recargar();
            } else {
                lblLogin.setForeground(Estilo.ROJO);
                lblLogin.setText("Usuario o clave incorrectos.");
            }
        }, m -> {
            lblLogin.setForeground(Estilo.ROJO);
            lblLogin.setText(m);
        });
    }

    private JPanel contenido() {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(Estilo.FONDO);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(16, 28, 14, 28)));
        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(tituloPagina);
        textos.add(subtituloPagina);
        barra.add(textos, BorderLayout.CENTER);
        c.add(barra, BorderLayout.NORTH);

        paginas.setOpaque(false);
        paginas.setBorder(BorderFactory.createEmptyBorder(20, 28, 14, 28));
        paginas.add(panelCuentas(), "cuentas");
        paginas.add(panelMovimientos(), "movimientos");
        paginas.add(panelOperar(), "operar");
        c.add(paginas, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(8, 0));
        pie.setBackground(Color.WHITE);
        pie.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Estilo.BORDE),
                BorderFactory.createEmptyBorder(8, 28, 8, 28)));
        estado.setFont(Estilo.fuente(Font.PLAIN, 12.5f));
        estado.setForeground(Estilo.GRIS);
        pie.add(punto, BorderLayout.WEST);
        pie.add(estado, BorderLayout.CENTER);
        c.add(pie, BorderLayout.SOUTH);
        return c;
    }

    private void mostrar(String pagina, String titulo, String subtitulo) {
        cartas.show(paginas, pagina);
        tituloPagina.setText(titulo);
        subtituloPagina.setText(subtitulo);
    }

    /** Servidor oculto detras de una flecha: se puede escribir la direccion de otro equipo y conectarse a ese. */
    private JPanel panelServidor() {
        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        pie.setBorder(BorderFactory.createEmptyBorder(14, 22, 18, 22));

        JLabel flecha = new JLabel("", new Triangulo(false), SwingConstants.LEFT);
        flecha.setFont(Estilo.fuente(Font.BOLD, 12f));
        flecha.setForeground(new Color(0xA9BCD0));
        flecha.setIconTextGap(8);
        flecha.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        flecha.setToolTipText("Servidor");

        JTextField txtServidor = new JTextField(direccion.isBlank() ? EurekaSoap.URL_POR_DEFECTO : direccion);
        txtServidor.putClientProperty("JTextField.placeholderText", "http://IP-DEL-SERVIDOR:8080/WS_EurekaBank_SOAPJAVA_GR07/WSEurekaBank");
        txtServidor.setFont(Estilo.fuente(Font.PLAIN, 12f));
        JButton conectar = Estilo.botonDorado("Conectar", null);
        JPanel detalle = new JPanel(new BorderLayout(0, 8));
        detalle.setOpaque(false);
        detalle.add(txtServidor, BorderLayout.CENTER);
        detalle.add(conectar, BorderLayout.SOUTH);
        detalle.setVisible(false);

        flecha.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                boolean abrir = !detalle.isVisible();
                detalle.setVisible(abrir);
                flecha.setText(abrir ? "Servidor" : "");
                flecha.setIcon(new Triangulo(abrir));
                pie.revalidate();
            }
        });
        conectar.addActionListener(e -> {
            String url = txtServidor.getText().trim();
            if (!ControladorEscritorio.urlValida(url)) {
                error("La dirección del servidor debe empezar con http:// o https://");
                return;
            }
            ctl.conectarA(url);
            estado.setText("Conectando con " + url + " ...");
            punto.poner(Estilo.ORO);
            recargar();
        });
        pie.add(flecha, BorderLayout.NORTH);
        pie.add(detalle, BorderLayout.CENTER);
        return pie;
    }

    /** Flecha que apunta a la derecha (cerrado) o hacia abajo (abierto). */
    private static final class Triangulo implements javax.swing.Icon {
        private final boolean abajo;

        Triangulo(boolean abajo) {
            this.abajo = abajo;
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }

        @Override
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xA9BCD0));
            java.awt.geom.Path2D p = new java.awt.geom.Path2D.Float();
            if (abajo) {
                p.moveTo(x + 2, y + 4);
                p.lineTo(x + 12, y + 4);
                p.lineTo(x + 7, y + 11);
            } else {
                p.moveTo(x + 4, y + 2);
                p.lineTo(x + 11, y + 7);
                p.lineTo(x + 4, y + 12);
            }
            p.closePath();
            g2.fill(p);
            g2.dispose();
        }
    }

    private static DefaultTableModel soloLectura(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
    }

    private void aviso(String texto) {
        estado.setText(texto);
        punto.poner(Estilo.VERDE);
    }

    private void error(String texto) {
        estado.setText("Error: " + texto);
        punto.poner(Estilo.ROJO);
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static GridBagConstraints celda(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(5, 0, 5, 12);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private static JPanel campo(String etiqueta, java.awt.Component componente) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(Estilo.etiqueta(etiqueta), BorderLayout.NORTH);
        p.add(componente, BorderLayout.CENTER);
        return p;
    }

    private static JScrollPane desplazable(java.awt.Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createLineBorder(new Color(0xE8EEF4)));
        s.getViewport().setBackground(Color.WHITE);
        return s;
    }

    // ------------------------------------------------------------------ datos
    private void recargar() {
        ctl.ejecutar(() -> ctl.servicio().cuentas(), lista -> {
            cuentas = lista;
            modeloCuentas.setRowCount(0);
            cmbOrigen.removeAllItems();
            cmbDestino.removeAllItems();
            cmbCuentaMov.removeAllItems();
            cmbCuentaMov.addItem("Todas las cuentas");
            for (Cuenta c : lista) {
                modeloCuentas.addRow(new Object[] {c.codigo, c.nombreCliente, c.moneda, EurekaSoap.dinero(c.saldo), c.estado, c.email, c.telefono});
                String etiqueta = c.codigo + " — " + c.nombreCliente;
                cmbOrigen.addItem(etiqueta);
                cmbDestino.addItem(etiqueta);
                cmbCuentaMov.addItem(etiqueta);
            }
            punto.poner(Estilo.VERDE);
            aviso(lista.size() + " cuentas activas.");
        }, this::error);
    }

    private static String codigo(JComboBox<String> combo) {
        Object o = combo.getSelectedItem();
        if (o == null || o.toString().startsWith("Todas")) {
            return "";
        }
        return o.toString().split(" ")[0];
    }

    private void cargarMovimientos() {
        String cuenta = codigo(cmbCuentaMov);
        ctl.ejecutar(() -> ctl.servicio().movimientos(cuenta), lista -> {
            modeloMov.setRowCount(0);
            for (Movimiento m : lista) {
                modeloMov.addRow(new Object[] {m.codigoCuenta, m.numero, m.fecha, m.tipo, (m.esEntrada() ? "+" : "−") + EurekaSoap.dinero(m.importe),
                        EurekaSoap.dinero(m.saldoActual), m.referencia});
            }
            aviso(lista.size() + " movimientos.");
        }, this::error);
    }

    // ------------------------------------------------------------------ paginas
    private JPanel panelCuentas() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        Estilo.tabla(tablaCuentas);
        tablaCuentas.getColumnModel().getColumn(0).setCellRenderer(Estilo.celda(SwingConstants.CENTER));
        tablaCuentas.getColumnModel().getColumn(1).setCellRenderer(Estilo.celda(SwingConstants.LEFT));
        tablaCuentas.getColumnModel().getColumn(3).setCellRenderer(Estilo.celda(SwingConstants.RIGHT));
        tablaCuentas.getColumnModel().getColumn(4).setCellRenderer(Estilo.chips(Map.of("ACTIVO", Estilo.VERDE), Estilo.GRIS));
        Estilo.Tarjeta t = Estilo.tarjeta("Cuentas");
        t.add(desplazable(tablaCuentas), BorderLayout.CENTER);
        p.add(t, BorderLayout.CENTER);
        JButton ver = Estilo.botonPrimario("Ver movimientos de la cuenta elegida", Estilo.icono(Estilo.Forma.BUSCAR, 16, Color.WHITE));
        ver.addActionListener(e -> {
            int i = tablaCuentas.getSelectedRow();
            if (i < 0) {
                error("Elija una cuenta de la tabla.");
                return;
            }
            cmbCuentaMov.setSelectedIndex(i + 1);
            mostrar("movimientos", "Movimientos", "Historial de una cuenta, con el saldo después de cada movimiento");
            cargarMovimientos();
        });
        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.add(ver, BorderLayout.WEST);
        p.add(sur, BorderLayout.SOUTH);
        return p;
    }

    private JPanel panelMovimientos() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        Estilo.Tarjeta filtro = Estilo.tarjeta(null);
        JPanel fila = new JPanel(new BorderLayout(12, 0));
        fila.setOpaque(false);
        fila.add(campo("Cuenta", cmbCuentaMov), BorderLayout.CENTER);
        JButton actualizar = Estilo.botonPrimario("Actualizar", Estilo.icono(Estilo.Forma.RECARGAR, 16, Color.WHITE));
        JPanel b = new JPanel(new BorderLayout());
        b.setOpaque(false);
        b.add(actualizar, BorderLayout.SOUTH);
        fila.add(b, BorderLayout.EAST);
        filtro.add(fila, BorderLayout.CENTER);
        p.add(filtro, BorderLayout.NORTH);
        actualizar.addActionListener(e -> cargarMovimientos());
        cmbCuentaMov.addActionListener(e -> {
            if (cmbCuentaMov.isShowing()) {
                cargarMovimientos();
            }
        });
        Estilo.tabla(tablaMov);
        tablaMov.getColumnModel().getColumn(0).setCellRenderer(Estilo.celda(SwingConstants.CENTER));
        tablaMov.getColumnModel().getColumn(1).setCellRenderer(Estilo.celda(SwingConstants.CENTER));
        tablaMov.getColumnModel().getColumn(1).setMaxWidth(70);
        tablaMov.getColumnModel().getColumn(3).setCellRenderer(Estilo.chips(Map.of("Depósito Entrada", Estilo.VERDE,
                "Transferencia Entrada", Estilo.VERDE, "Retiro Salida", Estilo.ROJO, "Transferencia Salida", Estilo.ROJO), Estilo.GRIS));
        tablaMov.getColumnModel().getColumn(3).setMinWidth(190);
        tablaMov.getColumnModel().getColumn(4).setCellRenderer(Estilo.celda(SwingConstants.RIGHT));
        tablaMov.getColumnModel().getColumn(5).setCellRenderer(Estilo.celda(SwingConstants.RIGHT));
        Estilo.Tarjeta t = Estilo.tarjeta("Movimientos");
        t.add(desplazable(tablaMov), BorderLayout.CENTER);
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    private JPanel panelOperar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        Estilo.Tarjeta t = Estilo.tarjeta("Nueva operación");
        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        JPanel tipos = new JPanel(new GridLayout(1, 3, 8, 0));
        tipos.setOpaque(false);
        for (JToggleButton b : new JToggleButton[] {btnDeposito, btnRetiro, btnTransf}) {
            g.add(b);
            tipos.add(b);
            b.addActionListener(e -> {
                boolean transf = btnTransf.isSelected();
                lblDestino.setVisible(transf);
                cmbDestino.setVisible(transf);
            });
        }
        lblDestino.setFont(Estilo.fuente(Font.BOLD, 12.5f));
        lblDestino.setVisible(false);
        cmbDestino.setVisible(false);
        GridBagConstraints c = celda(0, 0);
        f.add(campo("Tipo de operación", tipos), c);
        c = celda(0, 1);
        f.add(campo("Cuenta (origen)", cmbOrigen), c);
        c = celda(0, 2);
        JPanel dest = campo("Cuenta destino", cmbDestino);
        dest.setVisible(true);
        f.add(dest, c);
        cmbDestino.setVisible(false);
        dest.setVisible(false);
        btnTransf.addActionListener(e -> dest.setVisible(btnTransf.isSelected()));
        btnDeposito.addActionListener(e -> dest.setVisible(false));
        btnRetiro.addActionListener(e -> dest.setVisible(false));
        c = celda(0, 3);
        f.add(campo("Importe", txtImporte), c);
        JButton registrar = Estilo.botonDorado("Registrar operación", Estilo.icono(Estilo.Forma.CHECK, 16, Estilo.NAVY));
        c = celda(0, 4);
        c.insets = new Insets(14, 0, 4, 0);
        f.add(registrar, c);
        lblResultado.setFont(Estilo.fuente(Font.BOLD, 13f));
        c = celda(0, 5);
        f.add(lblResultado, c);
        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setOpaque(false);
        envoltura.add(f, BorderLayout.NORTH);
        t.add(envoltura, BorderLayout.CENTER);
        t.setPreferredSize(new Dimension(520, 420));
        JPanel izq = new JPanel(new BorderLayout());
        izq.setOpaque(false);
        izq.add(t, BorderLayout.NORTH);
        p.add(izq, BorderLayout.WEST);
        registrar.addActionListener(e -> registrar());
        txtImporte.addActionListener(e -> registrar());
        return p;
    }

    private void registrar() {
        String tipo = btnTransf.isSelected() ? "TRANSFERENCIA" : btnRetiro.isSelected() ? "RETIRO" : "DEPOSITO";
        String origen = codigo(cmbOrigen);
        String destino = btnTransf.isSelected() ? codigo(cmbDestino) : "";
        double importe;
        try {
            importe = Double.parseDouble(txtImporte.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            importe = 0;
        }
        if (importe <= 0) {
            lblResultado.setForeground(Estilo.ROJO);
            lblResultado.setText("Ingrese un importe mayor a 0 (por ejemplo 150.50).");
            return;
        }
        double monto = importe;
        ctl.ejecutar(() -> ctl.servicio().registrar(tipo, origen, destino, monto), r -> {
            lblResultado.setForeground(r.ok ? Estilo.VERDE : Estilo.ROJO);
            lblResultado.setText(r.ok ? "✔ " + r.mensaje : r.mensaje);
            if (r.ok) {
                txtImporte.setText("");
                recargar();
            } else {
                punto.poner(Estilo.ROJO);
            }
        }, this::error);
    }

    // ------------------------------------------------------------------ piezas del menu y del estado
    /** Boton del menu lateral: icono, texto y una barra dorada cuando esta elegido. */
    private static final class BotonMenu extends JToggleButton {
        private final Estilo.Forma forma;

        BotonMenu(String texto, Estilo.Forma forma) {
            super(texto);
            this.forma = forma;
            setFont(Estilo.fuente(Font.BOLD, 14f));
            setForeground(new Color(0xC9D7E6));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(13, 52, 13, 12));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean activo = isSelected();
            if (activo || getModel().isRollover()) {
                g2.setColor(activo ? new Color(255, 255, 255, 30) : new Color(255, 255, 255, 14));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            }
            if (activo) {
                g2.setColor(Estilo.ORO);
                g2.fill(new RoundRectangle2D.Float(0, 10, 4, getHeight() - 20, 4, 4));
            }
            Estilo.icono(forma, 22, activo ? Estilo.ORO : new Color(0xC9D7E6)).paintIcon(this, g2, 18, (getHeight() - 22) / 2);
            g2.dispose();
            setForeground(activo ? Color.WHITE : new Color(0xC9D7E6));
            super.paintComponent(g);
        }
    }

    /** Punto de color que acompana al mensaje de estado. */
    private static final class EstadoPunto extends JComponent {
        private Color color = Estilo.VERDE;

        EstadoPunto() {
            setPreferredSize(new Dimension(14, 14));
        }

        void poner(Color c) {
            color = c;
            repaint();
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(2, 2, 10, 10);
            g2.dispose();
        }
    }
}
