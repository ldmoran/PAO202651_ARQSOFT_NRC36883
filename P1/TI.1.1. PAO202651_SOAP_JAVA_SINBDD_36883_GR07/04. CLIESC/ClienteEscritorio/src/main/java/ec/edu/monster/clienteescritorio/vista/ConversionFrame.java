package ec.edu.monster.clienteescritorio.vista;

import ec.edu.monster.clienteescritorio.controlador.ControladorConversiones;
import ec.edu.monster.clienteescritorio.modelo.RespuestaServicio;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Ventana de conversion (CU-02, CU-03 y CU-04). Solo existe si hay token de
 * sesion; si el servidor rechaza el token, vuelve al login.
 */
public class ConversionFrame extends JFrame {

    private final ControladorConversiones controlador;
    private final JComboBox<String> cmbCategoria = new JComboBox<>();
    private final JComboBox<String> cmbOrigen = new JComboBox<>();
    private final JComboBox<String> cmbDestino = new JComboBox<>();
    private final JTextField txtValor = new JTextField(14);
    private final JLabel lblResultado = new JLabel("Resultado", SwingConstants.CENTER);
    private final JLabel lblEstado = new JLabel(" ", SwingConstants.CENTER);
    private final JButton btnConvertir = Estilo.boton("Convertir");
    private final JButton btnIntercambiar = Estilo.boton("Intercambiar");
    private boolean cargando;

    public ConversionFrame(ControladorConversiones controlador) {
        super("Conversión de Unidades Monster");
        this.controlador = controlador;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Estilo.FONDO);

        // Barra superior
        JPanel barra = Estilo.cabecera(new BorderLayout());
        JLabel titulo = new JLabel("Conversión de Unidades Monster");
        titulo.setFont(Estilo.fuente(Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        barra.add(titulo, BorderLayout.WEST);
        JButton btnCerrar = Estilo.boton("Cerrar sesión");
        btnCerrar.setBackground(Estilo.AZUL_CLARO);
        btnCerrar.setPreferredSize(new Dimension(140, 34));
        barra.add(btnCerrar, BorderLayout.EAST);
        raiz.add(barra, BorderLayout.NORTH);

        // Formulario dentro de una tarjeta
        JPanel centro = new JPanel(new BorderLayout(0, 0));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(16, 18, 14, 18));
        JPanel tarjeta = Estilo.tarjeta(new BorderLayout(0, 10));
        centro.add(tarjeta, BorderLayout.CENTER);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 4, 6, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        for (JComboBox<String> cmb : List.of(cmbCategoria, cmbOrigen, cmbDestino)) {
            Estilo.estilizarCombo(cmb);
        }
        Estilo.estilizarCampo(txtValor);
        fila(form, c, 0, "Categoría", cmbCategoria);
        fila(form, c, 1, "De", cmbOrigen);
        c.gridx = 1;
        c.gridy = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        btnIntercambiar.setPreferredSize(new Dimension(160, 34));
        form.add(btnIntercambiar, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        fila(form, c, 3, "A", cmbDestino);
        fila(form, c, 4, "Valor", txtValor);
        tarjeta.add(form, BorderLayout.CENTER);

        // Resultado y estado
        JPanel sur = new JPanel(new BorderLayout(0, 8));
        sur.setOpaque(false);
        JPanel botonPanel = new JPanel();
        botonPanel.setOpaque(false);
        botonPanel.add(btnConvertir);
        sur.add(botonPanel, BorderLayout.NORTH);
        lblResultado.setFont(Estilo.fuente(Font.BOLD, 20));
        lblResultado.setForeground(Estilo.AZUL);
        lblResultado.setOpaque(true);
        lblResultado.setBackground(Estilo.CELESTE);
        lblResultado.setBorder(new LineBorder(Estilo.AZUL_CLARO, 2, true));
        lblResultado.setPreferredSize(new Dimension(380, 60));
        sur.add(lblResultado, BorderLayout.CENTER);
        lblEstado.setFont(Estilo.fuente(Font.PLAIN, 13));
        lblEstado.setPreferredSize(new Dimension(380, 24));
        sur.add(lblEstado, BorderLayout.SOUTH);
        tarjeta.add(sur, BorderLayout.SOUTH);
        raiz.add(centro, BorderLayout.CENTER);

        setContentPane(raiz);

        // Eventos
        btnCerrar.addActionListener(e -> volverAlLogin());
        btnConvertir.addActionListener(e -> convertir());
        txtValor.addActionListener(e -> convertir());
        btnIntercambiar.addActionListener(e -> intercambiar());
        cmbCategoria.addActionListener(e -> {
            if (!cargando) {
                cargarUnidades();
            }
        });
        getRootPane().setDefaultButton(btnConvertir);

        habilitar(false);
        pack();
        setLocationRelativeTo(null);
        cargarCategorias();
    }

    private void fila(JPanel p, GridBagConstraints c, int fila, String texto, java.awt.Component campo) {
        c.gridx = 0;
        c.gridy = fila;
        p.add(Estilo.etiqueta(texto), c);
        c.gridx = 1;
        p.add(campo, c);
    }

    private void habilitar(boolean si) {
        cmbCategoria.setEnabled(si);
        cmbOrigen.setEnabled(si);
        cmbDestino.setEnabled(si);
        txtValor.setEnabled(si);
        btnConvertir.setEnabled(si);
        btnIntercambiar.setEnabled(si);
    }

    private void estado(String texto, Color color) {
        lblEstado.setForeground(color);
        lblEstado.setText(texto);
    }

    private void volverAlLogin() {
        controlador.cerrarSesion();
        new LoginFrame().setVisible(true);
        dispose();
    }

    /** Muestra el error; si el token ya no sirve, avisa y regresa al login. */
    private void manejarError(RespuestaServicio r) {
        estado("[" + r.getCodigo() + "] " + r.getMensaje(), Estilo.ERROR);
        if (r.requiereNuevoLogin()) {
            JOptionPane.showMessageDialog(this, "Su sesión terminó. Inicie sesión nuevamente.",
                    "Sesión", JOptionPane.WARNING_MESSAGE);
            volverAlLogin();
        }
    }

    // ---------------- catalogo (CU-02 / CU-03) ----------------

    private void cargarCategorias() {
        estado("Cargando catálogo...", Estilo.AZUL);
        new SwingWorker<RespuestaServicio, Void>() {
            @Override
            protected RespuestaServicio doInBackground() {
                return controlador.categorias();
            }

            @Override
            protected void done() {
                try {
                    RespuestaServicio r = get();
                    if (!r.esExitosa()) {
                        manejarError(r);
                        return;
                    }
                    cargando = true;
                    cmbCategoria.removeAllItems();
                    r.getDatos().forEach(cmbCategoria::addItem);
                    cargando = false;
                    cargarUnidades();
                } catch (Exception ex) {
                    estado("Error inesperado: " + ex.getMessage(), Estilo.ERROR);
                }
            }
        }.execute();
    }

    private void cargarUnidades() {
        final String categoria = (String) cmbCategoria.getSelectedItem();
        if (categoria == null) {
            return;
        }
        habilitar(false);
        estado("Cargando unidades de " + categoria + "...", Estilo.AZUL);
        new SwingWorker<RespuestaServicio, Void>() {
            @Override
            protected RespuestaServicio doInBackground() {
                return controlador.unidades(categoria);
            }

            @Override
            protected void done() {
                try {
                    RespuestaServicio r = get();
                    if (!r.esExitosa()) {
                        manejarError(r);
                        cmbCategoria.setEnabled(true);
                        return;
                    }
                    cmbOrigen.removeAllItems();
                    cmbDestino.removeAllItems();
                    r.getDatos().forEach(u -> {
                        cmbOrigen.addItem(u);
                        cmbDestino.addItem(u);
                    });
                    if (cmbDestino.getItemCount() > 1) {
                        cmbDestino.setSelectedIndex(1);
                    }
                    lblResultado.setText("Resultado");
                    estado(" ", Estilo.AZUL);
                    habilitar(true);
                } catch (Exception ex) {
                    estado("Error inesperado: " + ex.getMessage(), Estilo.ERROR);
                }
            }
        }.execute();
    }

    private void intercambiar() {
        Object o = cmbOrigen.getSelectedItem();
        cmbOrigen.setSelectedItem(cmbDestino.getSelectedItem());
        cmbDestino.setSelectedItem(o);
    }

    // ---------------- conversion (CU-04) ----------------

    private void convertir() {
        final String categoria = (String) cmbCategoria.getSelectedItem();
        final String origen = (String) cmbOrigen.getSelectedItem();
        final String destino = (String) cmbDestino.getSelectedItem();
        final double valor;
        try {
            valor = Double.parseDouble(txtValor.getText().trim().replace(',', '.'));
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            estado("Ingrese un número válido (ejemplo: 12.5).", Estilo.ERROR);
            return;
        }
        if (categoria == null || origen == null || destino == null) {
            estado("Seleccione la categoría y las unidades.", Estilo.ERROR);
            return;
        }
        btnConvertir.setEnabled(false);
        estado("Convirtiendo...", Estilo.AZUL);
        new SwingWorker<RespuestaServicio, Void>() {
            @Override
            protected RespuestaServicio doInBackground() {
                return controlador.convertir(categoria, valor, origen, destino);
            }

            @Override
            protected void done() {
                btnConvertir.setEnabled(true);
                try {
                    RespuestaServicio r = get();
                    if (r.esExitosa() && r.getResultado() != null) {
                        lblResultado.setText(ControladorConversiones.formatear(valor) + " " + r.getUnidadOrigen() + " = "
                                + ControladorConversiones.formatear(r.getResultado()) + " " + r.getUnidadDestino());
                        estado(r.getMensaje(), Estilo.EXITO);
                    } else {
                        manejarError(r);
                    }
                } catch (Exception ex) {
                    estado("Error inesperado: " + ex.getMessage(), Estilo.ERROR);
                }
            }
        }.execute();
    }

    // Acceso para pruebas
    JComboBox<String> comboCategoria() { return cmbCategoria; }
    JComboBox<String> comboOrigen() { return cmbOrigen; }
    JComboBox<String> comboDestino() { return cmbDestino; }
    JTextField campoValor() { return txtValor; }
    JLabel etiquetaResultado() { return lblResultado; }
    JLabel etiquetaEstado() { return lblEstado; }
    JButton botonConvertir() { return btnConvertir; }
}
