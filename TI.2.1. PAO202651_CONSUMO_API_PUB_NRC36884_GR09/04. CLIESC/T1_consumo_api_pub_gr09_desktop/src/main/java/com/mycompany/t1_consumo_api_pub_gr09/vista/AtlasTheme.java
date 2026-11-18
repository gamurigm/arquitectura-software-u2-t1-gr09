package com.mycompany.t1_consumo_api_pub_gr09.vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public final class AtlasTheme {
    public static final Color CANVAS = new Color(245, 246, 242);
    public static final Color SURFACE = Color.WHITE;
    public static final Color INK = new Color(23, 45, 43);
    public static final Color MUTED = new Color(98, 119, 115);
    public static final Color GREEN = new Color(23, 79, 71);
    public static final Color DEEP_GREEN = new Color(12, 57, 52);
    public static final Color MINT = new Color(226, 239, 231);
    public static final Color LINE = new Color(223, 231, 225);
    public static final Color ACCENT = new Color(209, 112, 78);
    public static final Color DANGER = new Color(179, 64, 54);

    private AtlasTheme() {
    }

    public static void instalar() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Swing conserva su look and feel actual si Nimbus no está disponible.
        }
        UIManager.put("control", CANVAS);
        UIManager.put("text", INK);
        UIManager.put("nimbusBase", GREEN);
        UIManager.put("nimbusFocus", ACCENT);
        UIManager.put("nimbusSelectionBackground", GREEN);
        UIManager.put("nimbusSelection", MINT);
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));
    }

    public static JButton boton(String texto, Color fondo, Color frente) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setForeground(frente);
        boton.setBackground(fondo);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(10, 15, 10, 15));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);
        return boton;
    }
}
