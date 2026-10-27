package ec.edu.monster.escritorio.vista;

import ec.edu.monster.cliente.Formato;
import ec.edu.monster.cliente.modelos.DetalleMontoMaximo;
import ec.edu.monster.cliente.modelos.EvaluacionCredito;
import ec.edu.monster.cliente.modelos.Factura;
import ec.edu.monster.cliente.modelos.Telefono;
import ec.edu.monster.escritorio.controlador.ControladorEscritorio;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

/** Ventana unica con cuatro pestanas: catalogo, venta, credito y facturas. */
public class VentanaPrincipal extends JFrame {

    private final ControladorEscritorio ctl;
    private final JLabel estado = new JLabel(" ");

    // Catalogo
    private final DefaultTableModel modeloCatalogo = soloLectura("Cod", "Marca", "Modelo", "Precio", "Disponible");
    private final JTable tablaCatalogo = new JTable(modeloCatalogo);
    private final JTextField txtMarca = new JTextField(12);
    private final JTextField txtModelo = new JTextField(16);
    private final JTextField txtPrecio = new JTextField(8);
    private final JCheckBox chkDisponible = new JCheckBox("Disponible", true);
    private List<Telefono> catalogo = new ArrayList<>();

    // Venta
    private final JTextField txtCedulaVenta = new JTextField(12);
    private final JTextField txtNombreVenta = new JTextField(20);
    private final JComboBox<String> cmbTelefono = new JComboBox<>();
    private final JRadioButton rbEfectivo = new JRadioButton("Efectivo (42 % de descuento)", true);
    private final JRadioButton rbCredito = new JRadioButton("Credito");
    private final JSpinner spPlazo = new JSpinner(new SpinnerNumberModel(6, 3, 18, 1));
    private final JTextArea txtFactura = new JTextArea(8, 40);
    private final TablaAmortizacion tablaVenta = new TablaAmortizacion();
    private List<Telefono> disponibles = new ArrayList<>();

    // Credito
    private final JTextField txtCedulaCredito = new JTextField(12);
    private final JTextArea txtCredito = new JTextArea(8, 40);

    // Facturas
    private final JTextField txtCedulaFacturas = new JTextField(12);
    private final DefaultTableModel modeloFacturas = soloLectura("N.", "Fecha", "Celular", "Pago", "Total", "Credito");
    private final JTable tablaFacturas = new JTable(modeloFacturas);
    private final TablaAmortizacion tablaAmortFacturas = new TablaAmortizacion();
    private List<Factura> facturas = new ArrayList<>();

    public VentanaPrincipal(ControladorEscritorio ctl) {
        super("Comercializadora - venta de celulares");
        this.ctl = ctl;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Catalogo", panelCatalogo());
        pestanas.addTab("Venta", panelVenta());
        pestanas.addTab("Credito", panelCredito());
        pestanas.addTab("Facturas", panelFacturas());
        add(pestanas, BorderLayout.CENTER);
        estado.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(estado, BorderLayout.SOUTH);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        recargarCatalogo();
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
    }

    private void error(String texto) {
        estado.setText("Error: " + texto);
        JOptionPane.showMessageDialog(this, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static GridBagConstraints celda(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }

    // ---------------- Catalogo ----------------

    private JPanel panelCatalogo() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Marca"));
        formulario.add(txtMarca);
        formulario.add(new JLabel("Modelo"));
        formulario.add(txtModelo);
        formulario.add(new JLabel("Precio"));
        formulario.add(txtPrecio);
        formulario.add(chkDisponible);
        JButton agregar = new JButton("Agregar");
        JButton guardar = new JButton("Guardar cambios");
        JButton eliminar = new JButton("Eliminar");
        JButton recargar = new JButton("Recargar");
        formulario.add(agregar);
        formulario.add(guardar);
        formulario.add(eliminar);
        formulario.add(recargar);
        p.add(formulario, BorderLayout.SOUTH);

        tablaCatalogo.getSelectionModel().addListSelectionListener(e -> {
            int f = tablaCatalogo.getSelectedRow();
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
                error("El precio debe ser un numero, por ejemplo 199.99.");
                return;
            }
            ctl.ejecutar(() -> ctl.servicio().crearTelefono(txtMarca.getText(), txtModelo.getText(), precio), cod -> {
                if (cod > 0) {
                    aviso("Celular agregado con codigo " + cod + ".");
                    recargarCatalogo();
                } else {
                    error("Complete marca, modelo y un precio mayor a cero.");
                }
            }, this::error);
        });
        guardar.addActionListener(e -> {
            int f = tablaCatalogo.getSelectedRow();
            Double precio = numero(txtPrecio.getText());
            if (f < 0) {
                error("Seleccione un celular de la tabla.");
                return;
            }
            if (precio == null) {
                error("El precio debe ser un numero, por ejemplo 199.99.");
                return;
            }
            int cod = catalogo.get(f).getCodTelefono();
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
            int f = tablaCatalogo.getSelectedRow();
            if (f < 0) {
                error("Seleccione un celular de la tabla.");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Eliminar este celular?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            int cod = catalogo.get(f).getCodTelefono();
            ctl.ejecutar(() -> ctl.servicio().eliminarTelefono(cod), ok -> {
                aviso(ok ? "Celular eliminado (si ya tenia facturas queda como no disponible)." : "No existe ese celular.");
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
                        Formato.dinero(t.getPrecio()), t.isDisponible() ? "si" : "no"});
                if (t.isDisponible()) {
                    disponibles.add(t);
                    cmbTelefono.addItem(t.getMarca() + " " + t.getModelo() + " - " + Formato.dinero(t.getPrecio()));
                }
            }
            aviso("Catalogo cargado: " + lista.size() + " celulares.");
        }, this::error);
    }

    // ---------------- Venta ----------------

    private JPanel panelVenta() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel f = new JPanel(new GridBagLayout());
        f.add(new JLabel("Cedula"), celda(0, 0));
        f.add(txtCedulaVenta, celda(1, 0));
        f.add(new JLabel("Nombre (cliente nuevo)"), celda(2, 0));
        f.add(txtNombreVenta, celda(3, 0));
        f.add(new JLabel("Celular"), celda(0, 1));
        GridBagConstraints cc = celda(1, 1);
        cc.gridwidth = 3;
        f.add(cmbTelefono, cc);
        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbEfectivo);
        grupo.add(rbCredito);
        f.add(rbEfectivo, celda(0, 2));
        f.add(rbCredito, celda(1, 2));
        f.add(new JLabel("Plazo (meses)"), celda(2, 2));
        f.add(spPlazo, celda(3, 2));
        spPlazo.setEnabled(false);
        rbCredito.addActionListener(e -> spPlazo.setEnabled(true));
        rbEfectivo.addActionListener(e -> spPlazo.setEnabled(false));
        JButton vender = new JButton("Vender");
        f.add(vender, celda(0, 3));
        p.add(f, BorderLayout.NORTH);

        txtFactura.setEditable(false);
        txtFactura.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        JPanel centro = new JPanel(new BorderLayout(4, 4));
        centro.add(new JScrollPane(txtFactura), BorderLayout.NORTH);
        centro.add(new JScrollPane(tablaVenta), BorderLayout.CENTER);
        p.add(centro, BorderLayout.CENTER);

        vender.addActionListener(e -> {
            int i = cmbTelefono.getSelectedIndex();
            if (i < 0 || i >= disponibles.size()) {
                error("Elija un celular disponible.");
                return;
            }
            int cod = disponibles.get(i).getCodTelefono();
            String cedula = txtCedulaVenta.getText().trim();
            String nombre = txtNombreVenta.getText().trim();
            boolean credito = rbCredito.isSelected();
            int plazo = (Integer) spPlazo.getValue();
            ctl.ejecutar(() -> {
                var r = credito ? ctl.servicio().facturarCredito(cedula, nombre, cod, plazo)
                        : ctl.servicio().facturarEfectivo(cedula, nombre, cod);
                var cuotas = r.isExito() && credito ? ctl.servicio().obtenerAmortizacion(r.getFactura().getCodCredito())
                        : new ArrayList<ec.edu.monster.cliente.modelos.CuotaAmortizacion>();
                return new Object[] {r, cuotas};
            }, par -> {
                var r = (ec.edu.monster.cliente.modelos.ResultadoFactura) par[0];
                @SuppressWarnings("unchecked")
                var cuotas = (List<ec.edu.monster.cliente.modelos.CuotaAmortizacion>) par[1];
                aviso(r.getMensaje());
                txtFactura.setText(r.isExito() ? textoFactura(r.getFactura()) : r.getMensaje());
                tablaVenta.mostrar(cuotas);
            }, this::error);
        });
        return p;
    }

    private static String textoFactura(Factura f) {
        return "FACTURA N. " + f.getNumFactura() + "\nFecha:     " + f.getFecha() + "\nCliente:   " + f.getNombreCliente()
                + " (" + f.getCedula() + ")\nCelular:   " + f.getTelefono() + "\nPago:      " + f.getFormaPago()
                + (f.getNumCuotas() > 0 ? " a " + f.getNumCuotas() + " cuotas (credito " + f.getCodCredito() + ")" : "")
                + "\nPrecio:    " + Formato.dinero(f.getPrecioLista()) + "\nDescuento: " + Formato.dinero(f.getDescuento())
                + "\nTOTAL:     " + Formato.dinero(f.getTotal());
    }

    // ---------------- Credito ----------------

    private JPanel panelCredito() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT));
        f.add(new JLabel("Cedula"));
        f.add(txtCedulaCredito);
        JButton verificar = new JButton("Verificar sujeto de credito");
        f.add(verificar);
        p.add(f, BorderLayout.NORTH);
        txtCredito.setEditable(false);
        txtCredito.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 14));
        p.add(new JScrollPane(txtCredito), BorderLayout.CENTER);
        verificar.addActionListener(e -> {
            String cedula = txtCedulaCredito.getText().trim();
            ctl.ejecutar(() -> {
                EvaluacionCredito ev = ctl.servicio().verificarSujetoCredito(cedula);
                DetalleMontoMaximo d = ev.isSujetoCredito() ? ctl.servicio().calcularMontoMaximo(cedula) : null;
                return new Object[] {ev, d};
            }, par -> {
                EvaluacionCredito ev = (EvaluacionCredito) par[0];
                DetalleMontoMaximo d = (DetalleMontoMaximo) par[1];
                StringBuilder sb = new StringBuilder(ev.isSujetoCredito() ? "SI es sujeto de credito\n" : "NO es sujeto de credito\n");
                sb.append(ev.getMotivo()).append('\n');
                if (d != null) {
                    sb.append("\nCliente: ").append(d.getNombre())
                            .append("\nPromedio de depositos (3 meses): ").append(Formato.dinero(d.getPromedioDepositos()))
                            .append("\nPromedio de retiros (3 meses):   ").append(Formato.dinero(d.getPromedioRetiros()))
                            .append("\nMonto maximo de credito:         ").append(Formato.dinero(d.getMontoMaximo()));
                }
                txtCredito.setText(sb.toString());
                aviso("Consulta realizada.");
            }, this::error);
        });
        return p;
    }

    // ---------------- Facturas ----------------

    private JPanel panelFacturas() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel f = new JPanel(new FlowLayout(FlowLayout.LEFT));
        f.add(new JLabel("Cedula"));
        f.add(txtCedulaFacturas);
        JButton buscar = new JButton("Buscar facturas");
        f.add(buscar);
        f.add(new JLabel("(elija una factura a credito para ver su amortizacion)"));
        p.add(f, BorderLayout.NORTH);
        JPanel centro = new JPanel(new java.awt.GridLayout(2, 1, 4, 4));
        centro.add(new JScrollPane(tablaFacturas));
        centro.add(new JScrollPane(tablaAmortFacturas));
        p.add(centro, BorderLayout.CENTER);

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
}
