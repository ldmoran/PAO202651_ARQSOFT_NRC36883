package ec.edu.monster.clienteescritorio.vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/** Colores, fuentes y componentes con estilo propio: misma linea grafica en todas las ventanas. */
final class Estilo {

    static final Color AZUL = new Color(0x1F3864);
    static final Color AZUL_MEDIO = new Color(0x2D5090);
    static final Color AZUL_CLARO = new Color(0x4F81BD);
    static final Color CELESTE = new Color(0xDBE8F8);
    static final Color FONDO = new Color(0xEEF3FA);
    static final Color BORDE = new Color(0xC4D1E6);
    static final Color ERROR = new Color(0xB00020);
    static final Color EXITO = new Color(0x1B7F3B);

    private Estilo() {
    }

    static Font fuente(int estilo, float tamano) {
        return new Font("Segoe UI", estilo, Math.round(tamano));
    }

    /** Imagen del tema Monster (la misma de la caratula del ERS), escalada al ancho pedido. */
    static ImageIcon banner(int ancho) {
        try (InputStream in = Estilo.class.getResourceAsStream("monster.jpg")) {
            if (in == null) {
                return null;
            }
            BufferedImage img = ImageIO.read(in);
            int alto = img.getHeight() * ancho / img.getWidth();
            Image escalada = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
            return new ImageIcon(escalada);
        } catch (IOException e) {
            return null;
        }
    }

    private static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Boton redondeado con degradado y efecto al pasar el mouse. */
    static JButton boton(String texto) {
        JButton b = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                suavizar(g);
                Color base = isEnabled() ? getBackground() : new Color(0xB8C2D3);
                if (isEnabled() && getModel().isPressed()) {
                    base = base.darker();
                }
                Color arriba = isEnabled() && getModel().isRollover() ? base.brighter() : base;
                g.setPaint(new GradientPaint(0, 0, arriba, 0, getHeight(), base.darker()));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g.dispose();
                super.paintComponent(g0);
            }
        };
        b.setFont(fuente(Font.BOLD, 14));
        b.setBackground(AZUL_CLARO);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(Math.max(150, b.getPreferredSize().width + 24), 40));
        return b;
    }

    /** Boton de segunda importancia: fondo claro con borde azul. */
    static JButton botonSecundario(String texto) {
        JButton b = boton(texto);
        b.setBackground(Color.WHITE);
        b.setForeground(AZUL);
        return b;
    }

    static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente(Font.BOLD, 13));
        l.setForeground(AZUL);
        return l;
    }

    static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(fuente(Font.BOLD, 21));
        l.setForeground(AZUL);
        return l;
    }

    static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(fuente(Font.PLAIN, 13));
        l.setForeground(new Color(0x5B6B86));
        return l;
    }

    /** Borde redondeado y relleno interno para campos de texto. */
    private static Border bordeCampo() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE, 1, true), new EmptyBorder(5, 9, 5, 9));
    }

    static void estilizarCampo(JComponent c) {
        c.setFont(fuente(Font.PLAIN, 14));
        c.setBorder(bordeCampo());
        c.setBackground(new Color(0xF8FBFF));
    }

    static void estilizarCombo(JComboBox<String> c) {
        c.setFont(fuente(Font.PLAIN, 14));
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(250, 34));
    }

    /** Panel blanco de esquinas redondeadas con sombra suave. */
    static JPanel tarjeta(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                suavizar(g);
                for (int i = 4; i >= 1; i--) {
                    g.setColor(new Color(31, 56, 100, 10 + (4 - i) * 5));
                    g.fillRoundRect(4 - i, 6 - i, getWidth() - 8 + 2 * i, getHeight() - 10 + 2 * i, 22, 22);
                }
                g.setColor(Color.WHITE);
                g.fillRoundRect(4, 3, getWidth() - 8, getHeight() - 10, 18, 18);
                g.setColor(BORDE);
                g.setStroke(new BasicStroke(1f));
                g.drawRoundRect(4, 3, getWidth() - 9, getHeight() - 11, 18, 18);
                g.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 22, 18, 22));
        return p;
    }

    /** Barra superior con degradado azul. */
    static JPanel cabecera(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setPaint(new GradientPaint(0, 0, AZUL, getWidth(), 0, AZUL_MEDIO));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 18, 12, 18));
        return p;
    }
}
