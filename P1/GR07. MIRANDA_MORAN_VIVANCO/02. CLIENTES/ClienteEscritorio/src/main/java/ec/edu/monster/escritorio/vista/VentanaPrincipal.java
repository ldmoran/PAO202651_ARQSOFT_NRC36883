package ec.edu.monster.escritorio.vista;

import ec.edu.monster.cliente.Formato;
import ec.edu.monster.cliente.modelos.CuotaAmortizacion;
import ec.edu.monster.cliente.modelos.DetalleMontoMaximo;
import ec.edu.monster.cliente.modelos.EvaluacionCredito;
import ec.edu.monster.cliente.modelos.Factura;
import ec.edu.monster.cliente.modelos.ResultadoFactura;
import ec.edu.monster.cliente.modelos.Telefono;
import ec.edu.monster.escritorio.controlador.ControladorEscritorio;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Ventana unica de la Comercializadora: menu lateral y cuatro paginas (catalogo, venta, credito y facturas). */
public class VentanaPrincipal extends JFrame {

    private static final double DESCUENTO_EFECTIVO = 0.42;

    private final ControladorEscritorio ctl;
    private final String direccion;
    private final JLabel estado = new JLabel(" ");
    private final EstadoPunto punto = new EstadoPunto();
    private final JLabel tituloPagina = Estilo.titulo("Catálogo de celulares");
    private final JLabel subtituloPagina = Estilo.subtitulo("Administre los teléfonos que se venden");
    private final CardLayout cartas = new CardLayout();
    private final JPanel paginas = new JPanel(cartas);

    // Catalogo
    private final DefaultTableModel modeloCatalogo = soloLectura("Cód.", "Marca", "Modelo", "Precio", "Estado");
    private final JTable tablaCatalogo = new JTable(modeloCatalogo);
    private final TableRowSorter<DefaultTableModel> filtroCatalogo = new TableRowSorter<>(modeloCatalogo);
    private final JTextField txtBuscar = new JTextField(16);
    private final JTextField txtMarca = new JTextField(12);
    private final JTextField txtModelo = new JTextField(14);
    private final JTextField txtPrecio = new JTextField(8);
    private final JCheckBox chkDisponible = new JCheckBox("Disponible para la venta", true);
    private List<Telefono> catalogo = new ArrayList<>();

    // Venta
    private final JTextField txtCedulaVenta = new JTextField(12);
    private final JTextField txtNombreVenta = new JTextField(18);
    private final JComboBox<String> cmbTelefono = new JComboBox<>();
    private final JToggleButton rbEfectivo = new JToggleButton("Efectivo  ·  -42 %", true);
    private final JToggleButton rbCredito = new JToggleButton("Crédito");
    private final JSpinner spPlazo = new JSpinner(new SpinnerNumberModel(6, 3, 18, 1));
    private final JLabel lblResumen = new JLabel(" ");
    private final JTextArea txtFactura = new JTextArea(8, 34);
    private final TablaAmortizacion tablaVenta = new TablaAmortizacion();
    private List<Telefono> disponibles = new ArrayList<>();
    private final JSpinner spCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final DefaultTableModel modeloCarrito = soloLectura("Celular", "Cant.", "Subtotal");
    private final JTable tablaCarrito = new JTable(modeloCarrito);
    /** Carrito de la compra: codigo de celular -> cantidad. Todo lo que hay aqui sale en una sola factura. */
    private final java.util.LinkedHashMap<Integer, Integer> carrito = new java.util.LinkedHashMap<>();

    // Credito
    private final JTextField txtCedulaCredito = new JTextField(14);
    private final JPanel resultadoCredito = new JPanel(new BorderLayout());

    // Facturas
    private final JTextField txtCedulaFacturas = new JTextField(14);
    private final DefaultTableModel modeloFacturas = soloLectura("N.", "Fecha", "Celular", "Pago", "Total", "Crédito");
    private final JTable tablaFacturas = new JTable(modeloFacturas);
    private final TablaAmortizacion tablaAmortFacturas = new TablaAmortizacion();
    private List<Factura> facturas = new ArrayList<>();

    public VentanaPrincipal(ControladorEscritorio ctl) {
        this(ctl, "");
    }

    public VentanaPrincipal(ControladorEscritorio ctl, String direccion) {
        super("BanQuito · Comercializadora de celulares");
        this.ctl = ctl;
        this.direccion = direccion == null ? "" : direccion;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(menuLateral(), BorderLayout.WEST);
        raiz.add(contenido(), BorderLayout.CENTER);
        setContentPane(raiz);
        setMinimumSize(new Dimension(1040, 660));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        recargarCatalogo();
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
        JLabel nombre = new JLabel("BanQuito");
        nombre.setFont(Estilo.fuente(Font.BOLD, 19f));
        nombre.setForeground(Color.WHITE);
        JLabel lema = new JLabel("Comercializadora");
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
        BotonMenu catalogoBtn = new BotonMenu("Catálogo", Estilo.Forma.TELEFONO);
        BotonMenu ventaBtn = new BotonMenu("Venta", Estilo.Forma.CARRITO);
        BotonMenu creditoBtn = new BotonMenu("Crédito", Estilo.Forma.BANCO);
        BotonMenu facturasBtn = new BotonMenu("Facturas", Estilo.Forma.RECIBO);
        for (BotonMenu b : new BotonMenu[] {catalogoBtn, ventaBtn, creditoBtn, facturasBtn}) {
            grupo.add(b);
            botones.add(b);
        }
        catalogoBtn.setSelected(true);
        catalogoBtn.addActionListener(e -> mostrar("catalogo", "Catálogo de celulares", "Administre los teléfonos que se venden"));
        ventaBtn.addActionListener(e -> mostrar("venta", "Nueva venta", "Venda de contado con 42 % de descuento o a crédito con el BanQuito"));
        creditoBtn.addActionListener(e -> mostrar("credito", "Consulta de crédito", "Compruebe si un cliente es sujeto de crédito y hasta cuánto puede pedir"));
        facturasBtn.addActionListener(e -> mostrar("facturas", "Facturas del cliente", "Consulte las compras y la tabla de amortización de cada crédito"));
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(botones, BorderLayout.NORTH);
        m.add(envoltorio, BorderLayout.CENTER);

        m.add(panelServidor(), BorderLayout.SOUTH);
        return m;
    }

    /**
     * El servidor va oculto detras de una flecha: por defecto se usa la direccion configurada (localhost o la variable
     * COMERCIALIZADORA_URL) y, al desplegarla, se puede escribir la direccion de otro equipo y conectarse a ese.
     */
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

        JTextField txtServidor = new JTextField(direccion.isBlank() ? ec.edu.monster.cliente.ComercializadoraSoap.URL_POR_DEFECTO : direccion);
        txtServidor.putClientProperty("JTextField.placeholderText", "http://IP-DEL-SERVIDOR:8080/Comercializadora/WSComercializadora");
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
            recargarCatalogo();
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
        paginas.add(panelCatalogo(), "catalogo");
        paginas.add(panelVenta(), "venta");
        paginas.add(panelCredito(), "credito");
        paginas.add(panelFacturas(), "facturas");
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

    // ------------------------------------------------------------------ catalogo
    private JPanel panelCatalogo() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        Estilo.Tarjeta lista = Estilo.tarjeta("Teléfonos registrados");
        Estilo.tabla(tablaCatalogo);
        tablaCatalogo.setRowSorter(filtroCatalogo);
        tablaCatalogo.getColumnModel().getColumn(0).setMaxWidth(70);
        tablaCatalogo.getColumnModel().getColumn(0).setCellRenderer(Estilo.celda(SwingConstants.CENTER));
        tablaCatalogo.getColumnModel().getColumn(1).setCellRenderer(Estilo.celda(SwingConstants.LEFT));
        tablaCatalogo.getColumnModel().getColumn(2).setCellRenderer(Estilo.celda(SwingConstants.LEFT));
        tablaCatalogo.getColumnModel().getColumn(3).setCellRenderer(Estilo.celda(SwingConstants.RIGHT));
        tablaCatalogo.getColumnModel().getColumn(4).setCellRenderer(Estilo.chips(
                Map.of("Disponible", Estilo.VERDE, "Agotado", Estilo.GRIS), Estilo.GRIS));
        tablaCatalogo.getColumnModel().getColumn(4).setMinWidth(130);
        tablaCatalogo.getColumnModel().getColumn(4).setMaxWidth(150);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar marca o modelo");
        txtBuscar.putClientProperty("JTextField.leadingIcon", Estilo.icono(Estilo.Forma.BUSCAR, 16, Estilo.GRIS));
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void aplicar() {
                String t = txtBuscar.getText().trim();
                filtroCatalogo.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(t), 1, 2));
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                aplicar();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                aplicar();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                aplicar();
            }
        });
        JPanel buscar = new JPanel(new BorderLayout());
        buscar.setOpaque(false);
        buscar.add(txtBuscar, BorderLayout.EAST);
        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(buscar, BorderLayout.NORTH);
        centro.add(desplazable(tablaCatalogo), BorderLayout.CENTER);
        lista.add(centro, BorderLayout.CENTER);
        p.add(lista, BorderLayout.CENTER);

        Estilo.Tarjeta ficha = Estilo.tarjeta("Datos del celular");
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints g = celda(0, 0);
        g.weightx = 1;
        campos.add(campo("Marca", txtMarca), g);
        g = celda(1, 0);
        g.weightx = 1;
        campos.add(campo("Modelo", txtModelo), g);
        g = celda(2, 0);
        g.weightx = 0.6;
        campos.add(campo("Precio (USD)", txtPrecio), g);
        g = celda(3, 0);
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.SOUTHWEST;
        campos.add(chkDisponible, g);
        JButton agregar = Estilo.botonVerde("Agregar", Estilo.icono(Estilo.Forma.MAS, 16, Color.WHITE));
        JButton guardar = Estilo.botonPrimario("Guardar cambios", Estilo.icono(Estilo.Forma.GUARDAR, 16, Color.WHITE));
        JButton eliminar = Estilo.botonPeligro("Eliminar", Estilo.icono(Estilo.Forma.BASURA, 16, Estilo.ROJO));
        JButton recargar = Estilo.botonSecundario("Recargar", Estilo.icono(Estilo.Forma.RECARGAR, 16, Estilo.AZUL));
        JPanel acciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        acciones.setOpaque(false);
        for (JButton b : new JButton[] {agregar, guardar, eliminar, recargar}) {
            acciones.add(b);
            acciones.add(javax.swing.Box.createHorizontalStrut(10));
        }
        JPanel formulario = new JPanel(new BorderLayout(0, 8));
        formulario.setOpaque(false);
        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(acciones, BorderLayout.SOUTH);
        ficha.add(formulario, BorderLayout.CENTER);
        p.add(ficha, BorderLayout.SOUTH);

        tablaCatalogo.getSelectionModel().addListSelectionListener(e -> {
            int vista = tablaCatalogo.getSelectedRow();
            int f = vista < 0 ? -1 : tablaCatalogo.convertRowIndexToModel(vista);
            if (!e.getValueIsAdjusting() && f >= 0 && f < catalogo.size()) {
                Telefono t = catalogo.get(f);
                txtMarca.setText(t.getMarca());
                txtModelo.setText(t.getModelo());
                txtPrecio.setText(String.valueOf(t.getPrecio()));
                chkDisponible.setSelected(t.isDisponible());
            }
        });
        agregar.addActionListener(e -> {
            Double precio = numero(txtPrecio.getText());
            if (precio == null) {
                error("El precio debe ser un número, por ejemplo 199.99.");
                return;
            }
            ctl.ejecutar(() -> ctl.servicio().crearTelefono(txtMarca.getText(), txtModelo.getText(), precio), cod -> {
                if (cod > 0) {
                    aviso("Celular agregado con código " + cod + ".");
                    recargarCatalogo();
                } else {
                    error("Complete marca, modelo y un precio mayor a cero.");
                }
            }, this::error);
        });
        guardar.addActionListener(e -> {
            int vista = tablaCatalogo.getSelectedRow();
            Double precio = numero(txtPrecio.getText());
            if (vista < 0) {
                error("Seleccione un celular de la tabla.");
                return;
            }
            if (precio == null) {
                error("El precio debe ser un número, por ejemplo 199.99.");
                return;
            }
            int cod = catalogo.get(tablaCatalogo.convertRowIndexToModel(vista)).getCodTelefono();
            ctl.ejecutar(() -> ctl.servicio().actualizarTelefono(cod, txtMarca.getText(), txtModelo.getText(), precio,
                    chkDisponible.isSelected()), ok -> {
                        if (ok) {
                            aviso("Celular actualizado.");
                            recargarCatalogo();
                        } else {
                            error("No se pudo actualizar: revise los datos.");
                        }
                    }, this::error);
        });
        eliminar.addActionListener(e -> {
            int vista = tablaCatalogo.getSelectedRow();
            if (vista < 0) {
                error("Seleccione un celular de la tabla.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "¿Eliminar este celular?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            int cod = catalogo.get(tablaCatalogo.convertRowIndexToModel(vista)).getCodTelefono();
            ctl.ejecutar(() -> ctl.servicio().eliminarTelefono(cod), ok -> {
                aviso(ok ? "Celular eliminado (si ya tenía facturas queda como no disponible)." : "No existe ese celular.");
                recargarCatalogo();
            }, this::error);
        });
        recargar.addActionListener(e -> recargarCatalogo());
        return p;
    }

    private static Double numero(String s) {
        try {
            return Double.valueOf(s.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void recargarCatalogo() {
        ctl.ejecutar(() -> ctl.servicio().listarTelefonos(), lista -> {
            catalogo = lista;
            modeloCatalogo.setRowCount(0);
            disponibles = new ArrayList<>();
            cmbTelefono.removeAllItems();
            for (Telefono t : lista) {
                modeloCatalogo.addRow(new Object[] {t.getCodTelefono(), t.getMarca(), t.getModelo(),
                        Formato.dinero(t.getPrecio()), t.isDisponible() ? "Disponible" : "Agotado"});
                if (t.isDisponible()) {
                    disponibles.add(t);
                    cmbTelefono.addItem(t.getMarca() + " " + t.getModelo() + "  ·  " + Formato.dinero(t.getPrecio()));
                }
            }
            actualizarResumen();
            aviso("Catálogo cargado: " + lista.size() + " celulares.");
        }, this::error);
    }

    // ------------------------------------------------------------------ venta
    private JPanel panelVenta() {
        JPanel p = new JPanel(new BorderLayout(18, 0));
        p.setOpaque(false);

        Estilo.Tarjeta formulario = Estilo.tarjeta("Datos de la venta");
        formulario.setPreferredSize(new Dimension(410, 100));
        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(false);
        GridBagConstraints g = celda(0, 0);
        g.weightx = 1;
        g.insets = new Insets(2, 0, 8, 0);
        f.add(campo("Cédula del cliente", txtCedulaVenta), g);
        g = celda(0, 1);
        g.insets = new Insets(2, 0, 8, 0);
        f.add(campo("Nombre (solo si es cliente nuevo)", txtNombreVenta), g);
        g = celda(0, 2);
        g.insets = new Insets(2, 0, 8, 0);
        f.add(campo("Celular", cmbTelefono), g);
        JPanel agregarFila = new JPanel(new BorderLayout(8, 0));
        agregarFila.setOpaque(false);
        JPanel cant = new JPanel(new BorderLayout());
        cant.setOpaque(false);
        spCantidad.setPreferredSize(new Dimension(74, 38));
        cant.add(spCantidad, BorderLayout.CENTER);
        agregarFila.add(campo("Cantidad", cant), BorderLayout.WEST);
        JButton agregar = Estilo.botonPrimario("Agregar a la compra", Estilo.icono(Estilo.Forma.MAS, 16, Color.WHITE));
        JPanel zonaAgregar = new JPanel(new BorderLayout());
        zonaAgregar.setOpaque(false);
        zonaAgregar.add(agregar, BorderLayout.SOUTH);
        agregarFila.add(zonaAgregar, BorderLayout.CENTER);
        g = celda(0, 3);
        g.insets = new Insets(2, 0, 8, 0);
        f.add(agregarFila, g);

        Estilo.tabla(tablaCarrito);
        tablaCarrito.getColumnModel().getColumn(1).setCellRenderer(Estilo.celda(SwingConstants.CENTER));
        tablaCarrito.getColumnModel().getColumn(1).setMaxWidth(60);
        tablaCarrito.getColumnModel().getColumn(2).setCellRenderer(Estilo.celda(SwingConstants.RIGHT));
        JScrollPane scrollCarrito = desplazable(tablaCarrito);
        scrollCarrito.setPreferredSize(new Dimension(300, 120));
        JButton quitar = Estilo.botonSecundario("Quitar de la compra", null);
        JPanel carritoPanel = new JPanel(new BorderLayout(0, 6));
        carritoPanel.setOpaque(false);
        carritoPanel.add(Estilo.etiqueta("Celulares de esta compra (una sola factura)"), BorderLayout.NORTH);
        carritoPanel.add(scrollCarrito, BorderLayout.CENTER);
        carritoPanel.add(quitar, BorderLayout.SOUTH);
        g = celda(0, 4);
        g.insets = new Insets(2, 0, 8, 0);
        f.add(carritoPanel, g);
        agregar.addActionListener(e -> agregarAlCarrito());
        quitar.addActionListener(e -> quitarDelCarrito());

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbEfectivo);
        grupo.add(rbCredito);
        for (JToggleButton b : new JToggleButton[] {rbEfectivo, rbCredito}) {
            b.setFont(Estilo.fuente(Font.BOLD, 13f));
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.putClientProperty("FlatLaf.style", "arc:12; margin:9,12,9,12; borderWidth:1; borderColor:#B9CBDD; background:#ffffff; "
                    + "selectedBackground:#0F5C99; focusWidth:0; hoverBackground:#E3EEF9");
            b.setForeground(b.isSelected() ? Color.WHITE : Estilo.AZUL);
            b.addItemListener(e -> {
                rbEfectivo.setForeground(rbEfectivo.isSelected() ? Color.WHITE : Estilo.AZUL);
                rbCredito.setForeground(rbCredito.isSelected() ? Color.WHITE : Estilo.AZUL);
            });
        }
        JPanel pago = new JPanel(new GridLayout(1, 2, 8, 0));
        pago.setOpaque(false);
        pago.add(rbEfectivo);
        pago.add(rbCredito);
        g = celda(0, 5);
        g.insets = new Insets(2, 0, 8, 0);
        f.add(campo("Forma de pago", pago), g);
        g = celda(0, 6);
        g.insets = new Insets(2, 0, 8, 0);
        f.add(campo("Plazo del crédito (meses)", spPlazo), g);
        spPlazo.setEnabled(false);
        rbCredito.addActionListener(e -> {
            spPlazo.setEnabled(true);
            actualizarResumen();
        });
        rbEfectivo.addActionListener(e -> {
            spPlazo.setEnabled(false);
            actualizarResumen();
        });
        cmbTelefono.addActionListener(e -> actualizarResumen());
        lblResumen.setFont(Estilo.fuente(Font.BOLD, 13f));
        lblResumen.setForeground(Estilo.AZUL);
        lblResumen.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        lblResumen.setOpaque(true);
        lblResumen.setBackground(Estilo.CELESTE);
        g = celda(0, 7);
        g.insets = new Insets(2, 0, 12, 0);
        f.add(lblResumen, g);
        JButton vender = Estilo.botonDorado("Vender", Estilo.icono(Estilo.Forma.CARRITO, 18, Estilo.NAVY));
        vender.setFont(Estilo.fuente(Font.BOLD, 15f));
        g = celda(0, 8);
        g.insets = new Insets(2, 0, 0, 0);
        f.add(vender, g);
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(f, BorderLayout.NORTH);
        JScrollPane scrollForm = new JScrollPane(arriba, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollForm.setBorder(BorderFactory.createEmptyBorder());
        scrollForm.setOpaque(false);
        scrollForm.getViewport().setOpaque(false);
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);
        formulario.add(scrollForm, BorderLayout.CENTER);
        p.add(formulario, BorderLayout.WEST);

        JPanel derecha = new JPanel(new BorderLayout(0, 16));
        derecha.setOpaque(false);
        Estilo.Tarjeta recibo = Estilo.tarjeta("Factura");
        txtFactura.setEditable(false);
        txtFactura.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        txtFactura.setForeground(Estilo.TEXTO);
        txtFactura.setBackground(new Color(0xFFFEF7));
        txtFactura.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        txtFactura.setText("Aquí aparecerá la factura cuando realice la venta.");
        recibo.add(desplazable(txtFactura), BorderLayout.CENTER);
        derecha.add(recibo, BorderLayout.NORTH);
        Estilo.Tarjeta amort = Estilo.tarjeta("Tabla de amortización");
        amort.add(desplazable(tablaVenta), BorderLayout.CENTER);
        derecha.add(amort, BorderLayout.CENTER);
        p.add(derecha, BorderLayout.CENTER);

        vender.addActionListener(e -> {
            java.util.Map<Integer, Integer> compra = new java.util.LinkedHashMap<>(carrito);
            if (compra.isEmpty()) {
                int i = cmbTelefono.getSelectedIndex();
                if (i < 0 || i >= disponibles.size()) {
                    error("Elija un celular disponible y presione Agregar a la compra.");
                    return;
                }
                compra.put(disponibles.get(i).getCodTelefono(), 1);
            }
            List<Integer> codigos = new ArrayList<>();
            compra.forEach((cod, n) -> {
                for (int k = 0; k < n; k++) {
                    codigos.add(cod);
                }
            });
            String cedula = txtCedulaVenta.getText().trim();
            String nombre = txtNombreVenta.getText().trim();
            boolean credito = rbCredito.isSelected();
            int plazo = (Integer) spPlazo.getValue();
            ctl.ejecutar(() -> {
                ResultadoFactura r = credito ? ctl.servicio().facturarCreditoVarios(cedula, nombre, codigos, plazo)
                        : ctl.servicio().facturarEfectivoVarios(cedula, nombre, codigos);
                List<CuotaAmortizacion> cuotas = r.isExito() && credito
                        ? ctl.servicio().obtenerAmortizacion(r.getFactura().getCodCredito())
                        : new ArrayList<CuotaAmortizacion>();
                return new Object[] {r, cuotas};
            }, par -> {
                ResultadoFactura r = (ResultadoFactura) par[0];
                @SuppressWarnings("unchecked")
                List<CuotaAmortizacion> cuotas = (List<CuotaAmortizacion>) par[1];
                if (r.isExito()) {
                    aviso(r.getMensaje());
                    carrito.clear();
                    refrescarCarrito();
                } else {
                    estado.setText(r.getMensaje());
                    punto.poner(Estilo.ORO);
                }
                txtFactura.setText(r.isExito() ? textoFactura(r.getFactura()) : r.getMensaje());
                tablaVenta.mostrar(cuotas);
            }, this::error);
        });
        return p;
    }

    private void agregarAlCarrito() {
        int i = cmbTelefono.getSelectedIndex();
        if (i < 0 || i >= disponibles.size()) {
            error("Elija un celular disponible.");
            return;
        }
        int cod = disponibles.get(i).getCodTelefono();
        carrito.merge(cod, (Integer) spCantidad.getValue(), Integer::sum);
        spCantidad.setValue(1);
        refrescarCarrito();
    }

    private void quitarDelCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila < 0 || fila >= carrito.size()) {
            error("Elija una fila de la compra para quitarla.");
            return;
        }
        Integer cod = new ArrayList<>(carrito.keySet()).get(fila);
        carrito.remove(cod);
        refrescarCarrito();
    }

    private Telefono telefonoDe(int cod) {
        for (Telefono t : disponibles) {
            if (t.getCodTelefono() == cod) {
                return t;
            }
        }
        return null;
    }

    private void refrescarCarrito() {
        modeloCarrito.setRowCount(0);
        carrito.forEach((cod, n) -> {
            Telefono t = telefonoDe(cod);
            String nombre = t == null ? "Celular " + cod : t.getMarca() + " " + t.getModelo();
            modeloCarrito.addRow(new Object[] {nombre, n, Formato.dinero(t == null ? 0 : t.getPrecio() * n)});
        });
        actualizarResumen();
    }

    private double totalCompra() {
        double total = 0;
        for (java.util.Map.Entry<Integer, Integer> e : carrito.entrySet()) {
            Telefono t = telefonoDe(e.getKey());
            total += t == null ? 0 : t.getPrecio() * e.getValue();
        }
        if (carrito.isEmpty()) {
            int i = cmbTelefono.getSelectedIndex();
            total = i < 0 || i >= disponibles.size() ? 0 : disponibles.get(i).getPrecio();
        }
        return total;
    }

    private void actualizarResumen() {
        double precio = totalCompra();
        if (precio <= 0) {
            lblResumen.setText("Elija un celular para ver el precio");
            return;
        }
        if (rbCredito.isSelected()) {
            lblResumen.setText("Total de la compra " + Formato.dinero(precio) + "  ·  el banco decide si lo aprueba");
        } else {
            lblResumen.setText("Total " + Formato.dinero(precio) + "  →  Efectivo " + Formato.dinero(precio * (1 - DESCUENTO_EFECTIVO)));
        }
    }

    private static String lineasTexto(Factura f) {
        if (f.getLineas().isEmpty()) {
            return "Celular:   " + f.getTelefono() + "\n";
        }
        StringBuilder sb = new StringBuilder("Celulares:\n");
        for (ec.edu.monster.cliente.modelos.LineaFactura l : f.getLineas()) {
            sb.append(String.format("  %2d x %-22s %10s%n", l.getCantidad(), l.getDescripcion(), Formato.dinero(l.getSubtotal())));
        }
        return sb.toString().replace("\r", "");
    }

    private static String textoFactura(Factura f) {
        return "FACTURA N. " + f.getNumFactura() + "\nFecha:     " + f.getFecha() + "\nCliente:   " + f.getNombreCliente()
                + " (" + f.getCedula() + ")\n" + lineasTexto(f) + "Pago:      " + f.getFormaPago()
                + (f.getNumCuotas() > 0 ? " a " + f.getNumCuotas() + " cuotas (crédito " + f.getCodCredito() + ")" : "")
                + "\nPrecio:    " + Formato.dinero(f.getPrecioLista()) + "\nDescuento: " + Formato.dinero(f.getDescuento())
                + "\nTOTAL:     " + Formato.dinero(f.getTotal());
    }

    // ------------------------------------------------------------------ credito
    private JPanel panelCredito() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);
        Estilo.Tarjeta busqueda = Estilo.tarjeta("Cliente a evaluar");
        JPanel fila = new JPanel(new BorderLayout(12, 0));
        fila.setOpaque(false);
        fila.add(campo("Cédula", txtCedulaCredito), BorderLayout.CENTER);
        JButton verificar = Estilo.botonPrimario("Verificar sujeto de crédito", Estilo.icono(Estilo.Forma.CHECK, 16, Color.WHITE));
        JPanel boton = new JPanel(new BorderLayout());
        boton.setOpaque(false);
        boton.add(verificar, BorderLayout.SOUTH);
        fila.add(boton, BorderLayout.EAST);
        busqueda.add(fila, BorderLayout.CENTER);
        p.add(busqueda, BorderLayout.NORTH);

        resultadoCredito.setOpaque(false);
        resultadoCredito.add(mensajeVacio("Escriba una cédula y verifique si el cliente puede acceder a un crédito."), BorderLayout.CENTER);
        p.add(resultadoCredito, BorderLayout.CENTER);

        verificar.addActionListener(e -> {
            String cedula = txtCedulaCredito.getText().trim();
            ctl.ejecutar(() -> {
                EvaluacionCredito ev = ctl.servicio().verificarSujetoCredito(cedula);
                DetalleMontoMaximo d = ev.isSujetoCredito() ? ctl.servicio().calcularMontoMaximo(cedula) : null;
                return new Object[] {ev, d};
            }, par -> {
                EvaluacionCredito ev = (EvaluacionCredito) par[0];
                DetalleMontoMaximo d = (DetalleMontoMaximo) par[1];
                mostrarCredito(ev, d);
                aviso("Consulta realizada.");
            }, this::error);
        });
        return p;
    }

    private static JPanel mensajeVacio(String texto) {
        Estilo.Tarjeta t = new Estilo.Tarjeta(new GridBagLayout());
        JLabel l = new JLabel(texto, Estilo.icono(Estilo.Forma.BANCO, 36, new Color(0xB9CBDD)), SwingConstants.CENTER);
        l.setVerticalTextPosition(SwingConstants.BOTTOM);
        l.setHorizontalTextPosition(SwingConstants.CENTER);
        l.setIconTextGap(12);
        l.setFont(Estilo.fuente(Font.PLAIN, 14f));
        l.setForeground(Estilo.GRIS);
        t.add(l);
        return t;
    }

    private void mostrarCredito(EvaluacionCredito ev, DetalleMontoMaximo d) {
        resultadoCredito.removeAll();
        boolean si = ev.isSujetoCredito();
        Color color = si ? Estilo.VERDE : Estilo.ROJO;
        JPanel columna = new JPanel(new BorderLayout(0, 16));
        columna.setOpaque(false);

        Estilo.Tarjeta banner = new Estilo.Tarjeta(new BorderLayout(14, 0));
        JLabel marca = new JLabel(Estilo.icono(si ? Estilo.Forma.CHECK : Estilo.Forma.BASURA, 30, color));
        banner.add(marca, BorderLayout.WEST);
        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        JLabel titulo = new JLabel(si ? "Sí es sujeto de crédito" : "No es sujeto de crédito");
        titulo.setFont(Estilo.fuente(Font.BOLD, 20f));
        titulo.setForeground(color);
        JLabel motivo = new JLabel(ev.getMotivo());
        motivo.setFont(Estilo.fuente(Font.PLAIN, 13.5f));
        motivo.setForeground(Estilo.GRIS);
        textos.add(titulo);
        textos.add(motivo);
        banner.add(textos, BorderLayout.CENTER);
        columna.add(banner, BorderLayout.NORTH);

        if (d != null) {
            JPanel cifras = new JPanel(new GridLayout(1, 3, 16, 0));
            cifras.setOpaque(false);
            cifras.add(cifra("Promedio de depósitos (3 meses)", Formato.dinero(d.getPromedioDepositos()), Estilo.AZUL));
            cifras.add(cifra("Promedio de retiros (3 meses)", Formato.dinero(d.getPromedioRetiros()), Estilo.GRIS));
            cifras.add(cifra("Monto máximo de crédito", Formato.dinero(d.getMontoMaximo()), Estilo.ORO));
            JPanel centro = new JPanel(new BorderLayout(0, 10));
            centro.setOpaque(false);
            JLabel cliente = new JLabel("Cliente: " + d.getNombre());
            cliente.setFont(Estilo.fuente(Font.BOLD, 14f));
            cliente.setForeground(Estilo.NAVY);
            centro.add(cliente, BorderLayout.NORTH);
            centro.add(cifras, BorderLayout.CENTER);
            columna.add(centro, BorderLayout.CENTER);
        }
        resultadoCredito.add(columna, BorderLayout.NORTH);
        resultadoCredito.revalidate();
        resultadoCredito.repaint();
    }

    private static JPanel cifra(String titulo, String valor, Color acento) {
        Estilo.Tarjeta t = new Estilo.Tarjeta(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                super.paintComponent(g);
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(acento);
                g2.fill(new RoundRectangle2D.Float(0, 14, 5, getHeight() - 28, 5, 5));
                g2.dispose();
            }
        };
        JLabel v = new JLabel(valor);
        v.setFont(Estilo.fuente(Font.BOLD, 26f));
        v.setForeground(Estilo.NAVY);
        t.add(Estilo.etiqueta(titulo), BorderLayout.NORTH);
        t.add(v, BorderLayout.CENTER);
        return t;
    }

    // ------------------------------------------------------------------ facturas
    private JPanel panelFacturas() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        Estilo.Tarjeta busqueda = Estilo.tarjeta(null);
        JPanel fila = new JPanel(new BorderLayout(12, 0));
        fila.setOpaque(false);
        fila.add(campo("Cédula del cliente", txtCedulaFacturas), BorderLayout.CENTER);
        JButton buscar = Estilo.botonPrimario("Buscar facturas", Estilo.icono(Estilo.Forma.BUSCAR, 16, Color.WHITE));
        JPanel boton = new JPanel(new BorderLayout());
        boton.setOpaque(false);
        boton.add(buscar, BorderLayout.SOUTH);
        fila.add(boton, BorderLayout.EAST);
        JLabel ayuda = new JLabel("Elija una factura a crédito para ver su tabla de amortización.");
        ayuda.setFont(Estilo.fuente(Font.PLAIN, 12.5f));
        ayuda.setForeground(Estilo.GRIS);
        JPanel contenido = new JPanel(new BorderLayout(0, 6));
        contenido.setOpaque(false);
        contenido.add(fila, BorderLayout.CENTER);
        contenido.add(ayuda, BorderLayout.SOUTH);
        busqueda.add(contenido, BorderLayout.CENTER);
        p.add(busqueda, BorderLayout.NORTH);

        Estilo.tabla(tablaFacturas);
        tablaFacturas.getColumnModel().getColumn(0).setMaxWidth(70);
        tablaFacturas.getColumnModel().getColumn(0).setCellRenderer(Estilo.celda(SwingConstants.CENTER));
        tablaFacturas.getColumnModel().getColumn(1).setCellRenderer(Estilo.celda(SwingConstants.LEFT));
        tablaFacturas.getColumnModel().getColumn(2).setCellRenderer(Estilo.celda(SwingConstants.LEFT));
        tablaFacturas.getColumnModel().getColumn(3).setCellRenderer(Estilo.chips(
                Map.of("EFECTIVO", Estilo.VERDE, "CREDITO", Estilo.AZUL), Estilo.GRIS));
        tablaFacturas.getColumnModel().getColumn(3).setMinWidth(130);
        tablaFacturas.getColumnModel().getColumn(3).setMaxWidth(150);
        tablaFacturas.getColumnModel().getColumn(4).setCellRenderer(Estilo.celda(SwingConstants.RIGHT));
        tablaFacturas.getColumnModel().getColumn(5).setCellRenderer(Estilo.celda(SwingConstants.CENTER));
        tablaFacturas.getColumnModel().getColumn(5).setMaxWidth(100);

        JPanel tablas = new JPanel(new GridLayout(2, 1, 0, 16));
        tablas.setOpaque(false);
        Estilo.Tarjeta compras = Estilo.tarjeta("Compras");
        compras.add(desplazable(tablaFacturas), BorderLayout.CENTER);
        Estilo.Tarjeta amort = Estilo.tarjeta("Tabla de amortización");
        amort.add(desplazable(tablaAmortFacturas), BorderLayout.CENTER);
        tablas.add(compras);
        tablas.add(amort);
        p.add(tablas, BorderLayout.CENTER);

        buscar.addActionListener(e -> ctl.ejecutar(() -> ctl.servicio().consultarFacturas(txtCedulaFacturas.getText().trim()),
                lista -> {
                    facturas = lista;
                    modeloFacturas.setRowCount(0);
                    tablaAmortFacturas.mostrar(new ArrayList<>());
                    for (Factura x : lista) {
                        modeloFacturas.addRow(new Object[] {x.getNumFactura(), x.getFecha(), x.getTelefono(), x.getFormaPago(),
                                Formato.dinero(x.getTotal()), x.getCodCredito() > 0 ? x.getCodCredito() : ""});
                    }
                    aviso(lista.isEmpty() ? "El cliente no tiene facturas." : lista.size() + " facturas encontradas.");
                }, this::error));
        tablaFacturas.getSelectionModel().addListSelectionListener(e -> {
            int i = tablaFacturas.getSelectedRow();
            if (e.getValueIsAdjusting() || i < 0 || i >= facturas.size()) {
                return;
            }
            int cod = facturas.get(i).getCodCredito();
            if (cod <= 0) {
                tablaAmortFacturas.mostrar(new ArrayList<>());
                return;
            }
            ctl.ejecutar(() -> ctl.servicio().obtenerAmortizacion(cod), tablaAmortFacturas::mostrar, this::error);
        });
        return p;
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
