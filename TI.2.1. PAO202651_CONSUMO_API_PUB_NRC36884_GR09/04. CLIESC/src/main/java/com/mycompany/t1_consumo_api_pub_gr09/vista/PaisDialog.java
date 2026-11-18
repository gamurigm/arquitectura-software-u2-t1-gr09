package com.mycompany.t1_consumo_api_pub_gr09.vista;

import com.mycompany.t1_consumo_api_pub_gr09.modelo.Pais;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

final class PaisDialog extends JDialog {
    private final Pais original;
    private final JTextField codigo = new JTextField();
    private final JTextField nombre = new JTextField();
    private final JTextField capital = new JTextField();
    private final JComboBox<String> region = new JComboBox<>();
    private final JTextField poblacion = new JTextField();
    private final JTextField moneda = new JTextField();
    private Pais resultado;

    private PaisDialog(Window owner, Pais original, List<String> regiones) {
        super(owner, original == null ? "Nuevo país" : "Editar país", ModalityType.APPLICATION_MODAL);
        this.original = original;
        construir(regiones);
    }

    static Optional<Pais> mostrar(Window owner, Pais original, List<String> regiones) {
        PaisDialog dialog = new PaisDialog(owner, original, regiones);
        dialog.setVisible(true);
        return Optional.ofNullable(dialog.resultado);
    }

    private void construir(List<String> regiones) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(AtlasTheme.CANVAS);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));

        JLabel title = new JLabel(original == null ? "Agregar al catálogo" : "Editar información");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(AtlasTheme.INK);
        JLabel subtitle = new JLabel("Completa los datos del país. Los campos opcionales pueden quedar vacíos.");
        subtitle.setForeground(AtlasTheme.MUTED);
        JPanel heading = new JPanel(new BorderLayout(0, 5));
        heading.setOpaque(false);
        heading.add(title, BorderLayout.NORTH);
        heading.add(subtitle, BorderLayout.CENTER);
        root.add(heading, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(AtlasTheme.SURFACE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AtlasTheme.LINE),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        codigo.setToolTipText("Código ISO 3166-1 alfa-3, por ejemplo ECU");
        if (original != null) {
            codigo.setText(original.codigoIso());
            codigo.setEditable(false);
            codigo.setBackground(new Color(241, 244, 240));
            nombre.setText(original.nombre());
            capital.setText(orEmpty(original.capital()));
            poblacion.setText(original.poblacion() == null ? "" : original.poblacion().toString());
            moneda.setText(orEmpty(original.moneda()));
        }
        region.setEditable(true);
        region.setBackground(Color.WHITE);
        region.setPreferredSize(new Dimension(320, 38));
        List<String> opciones = new ArrayList<>(List.of("América", "Europa", "Asia", "África", "Oceanía", "Antártida"));
        regiones.stream().map(RegionLabels::mostrar).filter(value -> !opciones.contains(value)).forEach(opciones::add);
        if (original != null && !opciones.contains(RegionLabels.mostrar(original.region()))) {
            opciones.add(RegionLabels.mostrar(original.region()));
        }
        opciones.forEach(region::addItem);
        if (original != null) {
            region.setSelectedItem(RegionLabels.mostrar(original.region()));
        } else {
            region.setSelectedItem("Americas");
        }

        int row = 0;
        agregarCampo(form, row++, "Código ISO alfa-3", codigo);
        agregarCampo(form, row++, "Nombre", nombre);
        agregarCampo(form, row++, "Capital", capital);
        agregarCampo(form, row++, "Región", region);
        agregarCampo(form, row++, "Población", poblacion);
        agregarCampo(form, row, "Moneda", moneda);
        root.add(form, BorderLayout.CENTER);

        JButton cancelar = AtlasTheme.boton("Cancelar", new Color(239, 242, 238), AtlasTheme.INK);
        JButton guardar = AtlasTheme.boton("Guardar país", AtlasTheme.GREEN, Color.WHITE);
        cancelar.addActionListener(event -> dispose());
        guardar.addActionListener(event -> guardar());
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.add(cancelar, BorderLayout.WEST);
        actions.add(guardar, BorderLayout.EAST);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(new JScrollPane(root));
        getContentPane().setBackground(AtlasTheme.CANVAS);
        setSize(540, 660);
        setMinimumSize(new Dimension(500, 620));
        setLocationRelativeTo(getOwner());
        getRootPane().setDefaultButton(guardar);
    }

    private void guardar() {
        try {
            String iso = original == null ? codigo.getText() : original.codigoIso();
            Long habitantes = poblacion.getText().isBlank() ? null : Long.valueOf(poblacion.getText().trim());
            String regionValue = RegionLabels.canonica(String.valueOf(region.getEditor().getItem()));
            resultado = new Pais(
                    original == null ? 0 : original.id(), iso, nombre.getText(), capital.getText(), regionValue,
                    habitantes, moneda.getText(), original == null ? null : original.banderaUrl());
            if (resultado.poblacion() == null) {
                throw new IllegalArgumentException("Ingresa la población para guardar el país.");
            }
            dispose();
        } catch (NumberFormatException error) {
            JOptionPane.showMessageDialog(this, "La población debe ser un número entero.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
            poblacion.requestFocusInWindow();
        } catch (IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Revisa los datos", JOptionPane.WARNING_MESSAGE);
            nombre.requestFocusInWindow();
        }
    }

    private static void agregarCampo(JPanel panel, int fila, String texto, Component componente) {
        GridBagConstraints label = new GridBagConstraints();
        label.gridx = 0;
        label.gridy = fila;
        label.anchor = GridBagConstraints.WEST;
        label.insets = new Insets(7, 0, 7, 14);
        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titulo.setForeground(AtlasTheme.MUTED);
        panel.add(titulo, label);

        GridBagConstraints input = new GridBagConstraints();
        input.gridx = 1;
        input.gridy = fila;
        input.weightx = 1;
        input.fill = GridBagConstraints.HORIZONTAL;
        input.insets = new Insets(7, 0, 7, 0);
        componente.setPreferredSize(new Dimension(320, 38));
        panel.add(componente, input);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
