package ec.edu.monster.escritorio.vista;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Aspecto de la Comercializadora BanQuito: azul marino de banco con detalles dorados (monedas) y tarjetas blancas,
 * en la misma linea limpia de las demas aplicaciones del grupo. Usa FlatLaf y dibuja sus propios iconos.
 */
public final class Estilo {

    public static final Color NAVY = new Color(0x0B2A4A);
    public static final Color AZUL = new Color(0x0F5C99);
    public static final Color AZUL_OSCURO = new Color(0x0A4676);
    public static final Color CELESTE = new Color(0xE3EEF9);
    public static final Color ORO = new Color(0xF2B01E);
    public static final Color VERDE = new Color(0x2E9E6B);
    public static final Color ROJO = new Color(0xD9534F);
    public static final Color FONDO = new Color(0xF3F6FA);
    public static final Color TEXTO = new Color(0x22313F);
    public static final Color GRIS = new Color(0x6B7B8C);
    public static final Color BORDE = new Color(0xD5DDE6);
    public static final Color CEBRA = new Color(0xF7FAFD);

    private static final String FAMILIA = escogerFamilia();

    private Estilo() {
    }

    private static String escogerFamilia() {
        String[] preferidas = {"Segoe UI", "Inter", "Helvetica Neue", "Arial"};
        String[] instaladas = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String f : preferidas) {
            if (Arrays.asList(instaladas).contains(f)) {
                return f;
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font fuente(int estilo, float tam) {
        return new Font(FAMILIA, estilo, 1).deriveFont(estilo, tam);
    }

    /** Instala FlatLaf con los colores y las esquinas redondeadas de la aplicacion. */
    public static void instalar() {
        FlatLaf.setGlobalExtraDefaults(Map.of("@accentColor", "#0F5C99"));
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new FontUIResource(fuente(Font.PLAIN, 14f)));
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.borderColor", BORDE);
        UIManager.put("TextField.margin", new Insets(6, 10, 6, 10));
        UIManager.put("ComboBox.padding", new Insets(5, 10, 5, 8));
        UIManager.put("Spinner.padding", new Insets(5, 8, 5, 4));
        UIManager.put("Panel.background", FONDO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", TEXTO);
        UIManager.put("Table.alternateRowColor", CEBRA);
        UIManager.put("Table.selectionBackground", CELESTE);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("Table.selectionInactiveBackground", CELESTE);
        UIManager.put("Table.selectionInactiveForeground", TEXTO);
        UIManager.put("Table.gridColor", new Color(0xE8EEF4));
        UIManager.put("TableHeader.background", NAVY);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.font", fuente(Font.BOLD, 13f));
        UIManager.put("TableHeader.separatorColor", NAVY);
        UIManager.put("TableHeader.bottomSeparatorColor", ORO);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.width", 11);
        UIManager.put("ScrollBar.thumb", new Color(0xC3CFDB));
    }

    // ------------------------------------------------------------------ etiquetas
    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, 20f));
        l.setForeground(NAVY);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.PLAIN, 13f));
        l.setForeground(GRIS);
        return l;
    }

    public static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, 12f));
        l.setForeground(GRIS);
        return l;
    }

    // ------------------------------------------------------------------ botones
    private static JButton boton(String texto, Icon icono, String fondo, String texto_, String hover, String borde) {
        JButton b = new JButton(texto, icono);
        b.setFont(fuente(Font.BOLD, 13f));
        b.setFocusPainted(false);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.setIconTextGap(8);
        b.putClientProperty(FlatClientProperties.STYLE, "arc:12; margin:8,16,8,16; background:" + fondo + "; foreground:" + texto_
                + "; hoverBackground:" + hover + "; pressedBackground:" + hover + "; borderWidth:" + (borde == null ? "0" : "1")
                + (borde == null ? "" : "; borderColor:" + borde) + "; focusWidth:0");
        return b;
    }

    public static JButton botonPrimario(String texto, Icon icono) {
        return boton(texto, icono, "#0F5C99", "#ffffff", "#0A4676", null);
    }

    public static JButton botonVerde(String texto, Icon icono) {
        return boton(texto, icono, "#2E9E6B", "#ffffff", "#25845A", null);
    }

    public static JButton botonDorado(String texto, Icon icono) {
        return boton(texto, icono, "#F2B01E", "#0B2A4A", "#DE9C0C", null);
    }

    public static JButton botonPeligro(String texto, Icon icono) {
        return boton(texto, icono, "#ffffff", "#D9534F", "#FBEAEA", "#D9534F");
    }

    public static JButton botonSecundario(String texto, Icon icono) {
        return boton(texto, icono, "#ffffff", "#0F5C99", "#E3EEF9", "#B9CBDD");
    }

    // ------------------------------------------------------------------ tarjetas
    /** Panel blanco con esquinas redondeadas y borde fino. */
    public static class Tarjeta extends JPanel {

        public Tarjeta(java.awt.LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
            g2.setColor(BORDE);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Tarjeta con un encabezado (titulo y una linea dorada) arriba. */
    public static Tarjeta tarjeta(String encabezado) {
        Tarjeta t = new Tarjeta(new java.awt.BorderLayout(0, 12));
        if (encabezado != null) {
            JPanel cab = new JPanel(new java.awt.BorderLayout());
            cab.setOpaque(false);
            JLabel l = new JLabel(encabezado);
            l.setFont(fuente(Font.BOLD, 15f));
            l.setForeground(NAVY);
            cab.add(l, java.awt.BorderLayout.CENTER);
            JPanel linea = new JPanel();
            linea.setBackground(ORO);
            linea.setPreferredSize(new Dimension(44, 3));
            JPanel base = new JPanel(new java.awt.BorderLayout(0, 6));
            base.setOpaque(false);
            base.add(cab, java.awt.BorderLayout.NORTH);
            JPanel izq = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
            izq.setOpaque(false);
            izq.add(linea);
            base.add(izq, java.awt.BorderLayout.CENTER);
            t.add(base, java.awt.BorderLayout.NORTH);
        }
        return t;
    }

    // ------------------------------------------------------------------ tablas
    /** Da a una tabla el aspecto de la aplicacion. */
    public static void tabla(JTable t) {
        tabla(t, 12);
    }

    public static void tabla(JTable t, int relleno) {
        t.setRowHeight(34);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
        t.setFont(fuente(Font.PLAIN, 13.5f));
        t.setBorder(BorderFactory.createEmptyBorder());
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 38));
        h.setDefaultRenderer(new EncabezadoRenderer(relleno));
    }

    /** Encabezado azul marino con letra blanca y una linea dorada abajo. */
    private static final class EncabezadoRenderer extends DefaultTableCellRenderer {
        private final int relleno;

        EncabezadoRenderer(int relleno) {
            this.relleno = relleno;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, fila, col);
            l.setBackground(NAVY);
            l.setForeground(Color.WHITE);
            l.setFont(fuente(Font.BOLD, 13f));
            l.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, ORO),
                    BorderFactory.createEmptyBorder(0, relleno, 0, relleno)));
            l.setHorizontalAlignment(alinearDerecha(t, col) ? SwingConstants.RIGHT : SwingConstants.LEFT);
            return l;
        }
    }

    private static boolean alinearDerecha(JTable t, int col) {
        if (col < 0 || t.getColumnCount() <= col) {
            return false;
        }
        TableCellRenderer r = t.getColumnModel().getColumn(col).getCellRenderer();
        if (r instanceof DefaultTableCellRenderer d) {
            return d.getHorizontalAlignment() == SwingConstants.RIGHT;
        }
        return false;
    }

    /** Celda de texto con margen y alineacion (derecha para montos). */
    public static TableCellRenderer celda(int alineacion) {
        return celda(alineacion, 12);
    }

    public static TableCellRenderer celda(int alineacion, int relleno) {
        return new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(alineacion);
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, fila, col);
                l.setHorizontalAlignment(alineacion);
                l.setBorder(BorderFactory.createEmptyBorder(0, relleno, 0, relleno));
                return l;
            }
        };
    }

    /** Celda con una etiqueta redondeada de color, por ejemplo "Disponible" o "CREDITO". */
    public static TableCellRenderer chips(Map<String, Color> colores, Color porDefecto) {
        return new TableCellRenderer() {
            private final CeldaChip celda = new CeldaChip();

            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int fila, int col) {
                String texto = v == null ? "" : v.toString();
                Color fondo = sel ? t.getSelectionBackground() : (fila % 2 == 0 ? t.getBackground() : CEBRA);
                celda.poner(texto, colores.getOrDefault(texto, porDefecto), fondo);
                return celda;
            }
        };
    }

    /** Fondo de la celda y, encima, la etiqueta redondeada; se dibuja sola para no depender del layout. */
    private static final class CeldaChip extends JComponent {
        private String texto = "";
        private Color color = GRIS;
        private Color fondo = Color.WHITE;

        void poner(String texto, Color color, Color fondo) {
            this.texto = texto;
            this.color = color;
            this.fondo = fondo;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (texto.isEmpty()) {
                g2.dispose();
                return;
            }
            g2.setFont(fuente(Font.BOLD, 11.5f));
            FontMetrics fm = g2.getFontMetrics();
            int ancho = fm.stringWidth(texto) + 22;
            int alto = 24;
            int x = 12;
            int y = (getHeight() - alto) / 2;
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 36));
            g2.fill(new RoundRectangle2D.Float(x, y, ancho, alto, alto, alto));
            g2.setColor(color);
            g2.drawString(texto, x + 11, y + (alto + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Etiqueta con forma de pildora. */
    public static final class Chip extends JComponent {
        private String texto = "";
        private Color color = GRIS;

        public void poner(String texto, Color color) {
            this.texto = texto;
            this.color = color;
            setFont(fuente(Font.BOLD, 11.5f));
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(fuente(Font.BOLD, 11.5f));
            return new Dimension(fm.stringWidth(texto) + 22, 24);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 34));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            g2.setColor(color);
            g2.setFont(fuente(Font.BOLD, 11.5f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, (getWidth() - fm.stringWidth(texto)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ logotipo
    /** Cuadro dorado con las iniciales BQ. */
    public static final class Logo extends JComponent {
        private final int lado;

        public Logo(int lado) {
            this.lado = lado;
            setPreferredSize(new Dimension(lado, lado));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(ORO);
            g2.fill(new RoundRectangle2D.Float(0, 0, lado, lado, lado * 0.3f, lado * 0.3f));
            g2.setColor(NAVY);
            g2.setFont(fuente(Font.BOLD, lado * 0.42f));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("BQ", (lado - fm.stringWidth("BQ")) / 2, (lado + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ iconos dibujados
    public enum Forma { TELEFONO, CARRITO, BANCO, RECIBO, BUSCAR, MAS, GUARDAR, BASURA, RECARGAR, CHECK }

    public static Icon icono(Forma forma, int tam, Color color) {
        return new IconoVector(forma, tam, color);
    }

    private static final class IconoVector implements Icon {
        private final Forma forma;
        private final int tam;
        private final Color color;

        IconoVector(Forma forma, int tam, Color color) {
            this.forma = forma;
            this.tam = tam;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return tam;
        }

        @Override
        public int getIconHeight() {
            return tam;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            g2.scale(tam / 24.0, tam / 24.0);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (forma) {
                case TELEFONO -> {
                    g2.draw(new RoundRectangle2D.Float(6.5f, 2.5f, 11, 19, 3, 3));
                    g2.draw(new java.awt.geom.Line2D.Float(10.5f, 18.2f, 13.5f, 18.2f));
                }
                case CARRITO -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(2.5, 4);
                    p.lineTo(5, 4);
                    p.lineTo(7.2, 15);
                    p.lineTo(18, 15);
                    p.lineTo(20, 7);
                    p.lineTo(5.6, 7);
                    g2.draw(p);
                    g2.fill(new Ellipse2D.Float(8, 18, 3, 3));
                    g2.fill(new Ellipse2D.Float(15, 18, 3, 3));
                }
                case BANCO -> {
                    Path2D techo = new Path2D.Float();
                    techo.moveTo(3, 9);
                    techo.lineTo(12, 3.5);
                    techo.lineTo(21, 9);
                    techo.closePath();
                    g2.draw(techo);
                    for (float cx : new float[] {6.5f, 12f, 17.5f}) {
                        g2.draw(new java.awt.geom.Line2D.Float(cx, 11.5f, cx, 17));
                    }
                    g2.draw(new java.awt.geom.Line2D.Float(3.5f, 20, 20.5f, 20));
                }
                case RECIBO -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(5.5, 3);
                    p.lineTo(18.5, 3);
                    p.lineTo(18.5, 21);
                    p.lineTo(16, 19);
                    p.lineTo(14, 21);
                    p.lineTo(12, 19);
                    p.lineTo(10, 21);
                    p.lineTo(8, 19);
                    p.lineTo(5.5, 21);
                    p.closePath();
                    g2.draw(p);
                    g2.draw(new java.awt.geom.Line2D.Float(9, 8, 15, 8));
                    g2.draw(new java.awt.geom.Line2D.Float(9, 12, 15, 12));
                }
                case BUSCAR -> {
                    g2.draw(new Ellipse2D.Float(4, 4, 11, 11));
                    g2.draw(new java.awt.geom.Line2D.Float(13, 13, 20, 20));
                }
                case MAS -> {
                    g2.draw(new java.awt.geom.Line2D.Float(12, 5, 12, 19));
                    g2.draw(new java.awt.geom.Line2D.Float(5, 12, 19, 12));
                }
                case GUARDAR -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(4, 4);
                    p.lineTo(17, 4);
                    p.lineTo(20, 7);
                    p.lineTo(20, 20);
                    p.lineTo(4, 20);
                    p.closePath();
                    g2.draw(p);
                    g2.draw(new RoundRectangle2D.Float(8, 4, 7, 5, 1, 1));
                    g2.draw(new RoundRectangle2D.Float(7, 13, 10, 7, 1, 1));
                }
                case BASURA -> {
                    g2.draw(new java.awt.geom.Line2D.Float(4, 7, 20, 7));
                    g2.draw(new java.awt.geom.Line2D.Float(9.5f, 4, 14.5f, 4));
                    Path2D p = new Path2D.Float();
                    p.moveTo(6, 7);
                    p.lineTo(7, 20);
                    p.lineTo(17, 20);
                    p.lineTo(18, 7);
                    g2.draw(p);
                }
                case RECARGAR -> {
                    g2.draw(new java.awt.geom.Arc2D.Float(4, 4, 16, 16, 40, 270, java.awt.geom.Arc2D.OPEN));
                    Path2D p = new Path2D.Float();
                    p.moveTo(17.5, 2.5);
                    p.lineTo(19, 8);
                    p.lineTo(13.5, 8);
                    g2.draw(p);
                }
                case CHECK -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(4.5, 12.5);
                    p.lineTo(10, 18);
                    p.lineTo(19.5, 6.5);
                    g2.draw(p);
                }
                default -> {
                }
            }
            g2.dispose();
        }
    }
}
