package com.mycompany.t1_consumo_api_pub_gr09.vista;

import com.mycompany.t1_consumo_api_pub_gr09.controlador.PaisController;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.EstadisticasPaises;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.Pais;
import com.mycompany.t1_consumo_api_pub_gr09.modelo.ResultadoImportacion;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public final class AtlasFrame extends JFrame {
    private static final NumberFormat ENTERO = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-EC"));

    private final PaisController controller;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "País", "Capital", "Región", "Población", "Moneda"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return switch (column) {
                case 0 -> Integer.class;
                case 4 -> Long.class;
                default -> String.class;
            };
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
    private final List<Pais> paisesMostrados = new ArrayList<>();
    private final Map<String, javax.swing.ImageIcon> banderas = new HashMap<>();
    private final CardLayout pagesLayout = new CardLayout();
    private final JPanel pages = new JPanel(pagesLayout);
    private final JTextField searchField = new JTextField();
    private final JComboBox<String> regionFilter = new JComboBox<>(new String[]{"Todas"});
    private final JLabel status = new JLabel("Preparando Atlas…");
    private final JLabel detailFlag = new JLabel("Elige un país para ver su bandera", SwingConstants.CENTER);
    private final JLabel detailName = new JLabel("Sin selección");
    private final JLabel detailCode = new JLabel("—");
    private final JLabel detailCapital = new JLabel("—");
    private final JLabel detailRegion = new JLabel("—");
    private final JLabel detailPopulation = new JLabel("—");
    private final JLabel detailCurrency = new JLabel("—");
    private final JLabel totalMetric = new JLabel("—");
    private final JLabel regionsMetric = new JLabel("—");
    private final JLabel populationMetric = new JLabel("—");
    private final JLabel reportPopulation = new JLabel("No disponible");
    private final JTextArea reportTopCountries = new JTextArea("Sin datos");
    private final JTextArea reportTopRegions = new JTextArea("Sin datos");
    private final JPanel regionBars = new JPanel();
    private final JProgressBar progress = new JProgressBar();
    private final JButton importButton = AtlasTheme.boton("Importar catálogo", AtlasTheme.GREEN, Color.WHITE);
    private final JButton apiSearchButton = AtlasTheme.boton("Consultar API", AtlasTheme.MINT, AtlasTheme.DEEP_GREEN);
    private final JButton saveApiButton = AtlasTheme.boton("Guardar resultado", new Color(237, 226, 209), AtlasTheme.INK);
    private final JButton createButton = AtlasTheme.boton("+ Nuevo", AtlasTheme.GREEN, Color.WHITE);
    private final JButton editButton = AtlasTheme.boton("Editar", new Color(239, 242, 238), AtlasTheme.INK);
    private final JButton deleteButton = AtlasTheme.boton("Eliminar", new Color(250, 233, 229), AtlasTheme.DANGER);
    private final JButton refreshButton = AtlasTheme.boton("Actualizar", new Color(239, 242, 238), AtlasTheme.INK);
    private final JButton filterButton = AtlasTheme.boton("Filtrar", AtlasTheme.DEEP_GREEN, Color.WHITE);
    private final JButton clearButton = AtlasTheme.boton("Limpiar", new Color(239, 242, 238), AtlasTheme.INK);
    private final JButton countriesNav = new JButton("Países");
    private final JButton statisticsNav = new JButton("Estadísticas");

    private Pais seleccionado;
    private EstadisticasPaises estadisticas;
    private boolean modoApi;
    private boolean actualizandoRegiones;
    private boolean ocupado;
    private long tokenBandera;

    public AtlasFrame(PaisController controller) {
        this.controller = controller;
        setTitle("Atlas GR09 · Explorador de países");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1160, 760));
        setSize(1380, 860);
        setLocationRelativeTo(null);
        configurarTabla();
        construirVentana();
        configurarEventos();
    }

    public void refrescarCatalogo() {
        ejecutar("Conectando con el catálogo MySQL…", this::leerSnapshot, snapshot -> {
            modoApi = false;
            mostrarSnapshot(snapshot);
        }, false);
    }

    private void construirVentana() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AtlasTheme.CANVAS);
        root.add(construirSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(AtlasTheme.CANVAS);
        main.setBorder(new EmptyBorder(22, 24, 18, 24));
        JPanel upper = new JPanel();
        upper.setOpaque(false);
        upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
        upper.add(construirHero());
        upper.add(Box.createVerticalStrut(16));
        upper.add(construirMetricas());
        main.add(upper, BorderLayout.NORTH);

        pages.setOpaque(false);
        pages.add(construirExplorador(), "paises");
        pages.add(construirEstadisticas(), "estadisticas");
        main.add(pages, BorderLayout.CENTER);
        main.add(construirBarraEstado(), BorderLayout.SOUTH);
        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
        pagesLayout.show(pages, "paises");
    }

    private JPanel construirSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(AtlasTheme.DEEP_GREEN);
        sidebar.setBorder(new EmptyBorder(25, 16, 20, 16));
        sidebar.setPreferredSize(new Dimension(214, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("ATLAS");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 24));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel group = new JLabel("GR09 · PAÍSES");
        group.setForeground(new Color(178, 205, 192));
        group.setFont(new Font("Segoe UI", Font.BOLD, 10));
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(group);
        sidebar.add(Box.createVerticalStrut(42));

        prepararNav(countriesNav);
        prepararNav(statisticsNav);
        sidebar.add(countriesNav);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(statisticsNav);
        seleccionarNav(countriesNav);
        sidebar.add(Box.createVerticalGlue());

        JLabel sourceTitle = new JLabel("FUENTES");
        sourceTitle.setForeground(new Color(154, 184, 169));
        sourceTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel sourceText = new JLabel("MySQL · REST Countries");
        sourceText.setForeground(new Color(222, 234, 225));
        sourceText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel version = new JLabel("Cliente Swing · Java 17+");
        version.setForeground(new Color(154, 184, 169));
        version.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sourceTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sourceText.setAlignmentX(Component.LEFT_ALIGNMENT);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sourceTitle);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(sourceText);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(version);
        return sidebar;
    }

    private static void prepararNav(JButton button) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(new Color(225, 239, 229));
        button.setBackground(AtlasTheme.DEEP_GREEN);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(12, 14, 12, 12));
        button.setFocusPainted(false);
        button.setOpaque(true);
    }

    private JPanel construirHero() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBorder(new EmptyBorder(20, 24, 18, 22));
        hero.setPreferredSize(new Dimension(850, 156));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 156));
        hero.setOpaque(false);
        hero.setBackground(AtlasTheme.GREEN);
        hero.setLayout(new BorderLayout(20, 0));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("EXPLORACIÓN DEL MUNDO");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 10));
        eyebrow.setForeground(new Color(191, 221, 202));
        JLabel title = new JLabel("Atlas de países");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Consulta, compara y administra tu catálogo en un solo lugar.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(220, 236, 224));
        text.add(eyebrow);
        text.add(Box.createVerticalStrut(7));
        text.add(title);
        text.add(Box.createVerticalStrut(5));
        text.add(subtitle);
        hero.add(text, BorderLayout.CENTER);
        hero.add(new GlobePanel(), BorderLayout.EAST);
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(17, 67, 60)),
                new EmptyBorder(17, 24, 16, 22)));
        return new HeroPanel(hero);
    }

    private JPanel construirMetricas() {
        JPanel metrics = new JPanel(new GridBagLayout());
        metrics.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 12);
        metrics.add(metrica("PAÍSES EN MYSQL", totalMetric, "Catálogo local"), c);
        c.gridx = 1;
        metrics.add(metrica("REGIONES", regionsMetric, "Presentes en tu catálogo"), c);
        c.gridx = 2;
        c.insets = new Insets(0, 0, 0, 0);
        metrics.add(metrica("MAYOR POBLACIÓN", populationMetric, "Dato guardado"), c);
        return metrics;
    }

    private static JPanel metrica(String heading, JLabel value, String caption) {
        JPanel card = cardPanel(new BorderLayout(0, 4));
        card.setBorder(new EmptyBorder(11, 15, 11, 15));
        JLabel title = new JLabel(heading);
        title.setForeground(AtlasTheme.MUTED);
        title.setFont(new Font("Segoe UI", Font.BOLD, 9));
        value.setForeground(AtlasTheme.INK);
        value.setFont(new Font("Segoe UI", Font.BOLD, 21));
        JLabel note = new JLabel(caption);
        note.setForeground(AtlasTheme.MUTED);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        card.add(title, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        card.setPreferredSize(new Dimension(160, 82));
        return card;
    }

    private JPanel construirExplorador() {
        JPanel explorer = new JPanel(new BorderLayout(0, 14));
        explorer.setOpaque(false);
        explorer.add(construirFiltrosYAcciones(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(AtlasTheme.LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        JPanel right = construirFicha();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, right);
        split.setResizeWeight(.74);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setOpaque(false);
        explorer.add(split, BorderLayout.CENTER);
        return explorer;
    }

    private JPanel construirFiltrosYAcciones() {
        JPanel wrapper = cardPanel(new BorderLayout(0, 12));
        wrapper.setBorder(new EmptyBorder(13, 15, 13, 15));
        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 8);
        c.anchor = GridBagConstraints.WEST;
        filters.add(etiqueta("Buscar en catálogo"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        searchField.setPreferredSize(new Dimension(260, 38));
        searchField.setToolTipText("Filtra los países guardados en MySQL");
        searchField.getAccessibleContext().setAccessibleName("Buscar países guardados en MySQL");
        filters.add(searchField, c);
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        filters.add(etiqueta("Región"), c);
        c.gridx = 3;
        regionFilter.setPreferredSize(new Dimension(150, 38));
        filters.add(regionFilter, c);
        c.gridx = 4;
        filters.add(filterButton, c);
        c.gridx = 5;
        filters.add(clearButton, c);
        c.gridx = 6;
        c.insets = new Insets(0, 4, 0, 0);
        filters.add(refreshButton, c);
        wrapper.add(filters, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        importButton.setToolTipText("Consulta todas las páginas y guarda países nuevos en MySQL");
        apiSearchButton.setToolTipText("Busca en la API sin guardar hasta que elijas un resultado");
        saveApiButton.setEnabled(false);
        actions.add(importButton);
        actions.add(apiSearchButton);
        actions.add(saveApiButton);
        actions.add(Box.createHorizontalStrut(12));
        actions.add(createButton);
        actions.add(editButton);
        actions.add(deleteButton);
        wrapper.add(actions, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel construirFicha() {
        JPanel detail = cardPanel(new BorderLayout(0, 13));
        detail.setBorder(new EmptyBorder(16, 16, 16, 16));
        detail.setPreferredSize(new Dimension(300, 420));
        detailFlag.setOpaque(true);
        detailFlag.setBackground(new Color(239, 244, 238));
        detailFlag.setForeground(AtlasTheme.MUTED);
        detailFlag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        detailFlag.setPreferredSize(new Dimension(250, 132));
        detailFlag.setBorder(BorderFactory.createLineBorder(AtlasTheme.LINE));

        JPanel heading = new JPanel(new BorderLayout(0, 5));
        heading.setOpaque(false);
        detailName.setFont(new Font("Segoe UI", Font.BOLD, 19));
        detailName.setForeground(AtlasTheme.INK);
        detailCode.setFont(new Font("Segoe UI", Font.BOLD, 10));
        detailCode.setForeground(AtlasTheme.GREEN);
        heading.add(detailName, BorderLayout.NORTH);
        heading.add(detailCode, BorderLayout.SOUTH);

        JPanel facts = new JPanel(new GridBagLayout());
        facts.setOpaque(false);
        agregarDato(facts, 0, "CAPITAL", detailCapital);
        agregarDato(facts, 1, "REGIÓN", detailRegion);
        agregarDato(facts, 2, "POBLACIÓN", detailPopulation);
        agregarDato(facts, 3, "MONEDA", detailCurrency);

        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(heading, BorderLayout.NORTH);
        center.add(facts, BorderLayout.CENTER);
        detail.add(detailFlag, BorderLayout.NORTH);
        detail.add(center, BorderLayout.CENTER);
        return detail;
    }

    private JPanel construirEstadisticas() {
        prepararTextoResumen(reportTopCountries);
        prepararTextoResumen(reportTopRegions);
        JPanel content = cardPanel(new BorderLayout(0, 18));
        content.setBorder(new EmptyBorder(22, 24, 22, 24));
        JLabel heading = new JLabel("Resumen del catálogo local");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 21));
        heading.setForeground(AtlasTheme.INK);
        JLabel caption = new JLabel("Las cifras se calculan sobre países guardados en MySQL, no sobre resultados externos sin guardar.");
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        caption.setForeground(AtlasTheme.MUTED);
        JPanel top = new JPanel(new BorderLayout(0, 5));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(caption, BorderLayout.CENTER);

        JPanel highlights = new JPanel(new GridBagLayout());
        highlights.setOpaque(false);
        GridBagConstraints card = new GridBagConstraints();
        card.gridy = 0;
        card.weightx = 1;
        card.fill = GridBagConstraints.BOTH;
        card.insets = new Insets(0, 0, 0, 10);
        highlights.add(resumen("POBLACIÓN MÁXIMA", reportPopulation), card);
        card.gridx = 1;
        highlights.add(resumen("PAÍSES MÁS POBLADOS", reportTopCountries), card);
        card.gridx = 2;
        card.insets = new Insets(0, 0, 0, 0);
        highlights.add(resumen("REGIÓN CON MÁS PAÍSES", reportTopRegions), card);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(top);
        north.add(Box.createVerticalStrut(15));
        north.add(highlights);
        content.add(north, BorderLayout.NORTH);

        regionBars.setOpaque(false);
        regionBars.setLayout(new BoxLayout(regionBars, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(regionBars);
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(AtlasTheme.LINE), "Países por región"));
        scroll.getViewport().setBackground(Color.WHITE);
        content.add(scroll, BorderLayout.CENTER);
        return content;
    }

    private static void prepararTextoResumen(JTextArea area) {
        area.setEditable(false);
        area.setFocusable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setRows(2);
    }

    private static JPanel resumen(String titulo, JComponent dato) {
        JPanel panel = cardPanel(new BorderLayout(0, 8));
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel title = etiqueta(titulo);
        panel.add(title, BorderLayout.NORTH);
        dato.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dato.setForeground(AtlasTheme.INK);
        panel.add(dato, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(200, 93));
        return panel;
    }

    private JPanel construirBarraEstado() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        status.setForeground(AtlasTheme.MUTED);
        status.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progress.setIndeterminate(true);
        progress.setPreferredSize(new Dimension(118, 12));
        progress.setVisible(false);
        bar.add(status, BorderLayout.CENTER);
        bar.add(progress, BorderLayout.EAST);
        return bar;
    }

    private void configurarTabla() {
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(AtlasTheme.INK);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(221, 237, 226));
        table.setSelectionForeground(AtlasTheme.DEEP_GREEN);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(238, 243, 237));
        table.getTableHeader().setForeground(AtlasTheme.MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        table.getTableHeader().setPreferredSize(new Dimension(10, 37));
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.setDefaultRenderer(Integer.class, new ZebraRenderer());
        table.setDefaultRenderer(Long.class, new NumberRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(95);
        table.getColumnModel().getColumn(4).setPreferredWidth(115);
        table.getColumnModel().getColumn(5).setPreferredWidth(170);
        table.getColumnModel().getColumn(0).setMaxWidth(55);
    }

    private void configurarEventos() {
        filterButton.addActionListener(event -> cargarFiltroLocal());
        searchField.addActionListener(event -> cargarFiltroLocal());
        regionFilter.addActionListener(event -> {
            if (!actualizandoRegiones) {
                cargarFiltroLocal();
            }
        });
        clearButton.addActionListener(event -> {
            searchField.setText("");
            regionFilter.setSelectedItem("Todas");
            cargarFiltroLocal();
        });
        refreshButton.addActionListener(event -> refrescarCatalogo());
        importButton.addActionListener(event -> importar());
        apiSearchButton.addActionListener(event -> buscarApi());
        saveApiButton.addActionListener(event -> guardarResultadoApi());
        createButton.addActionListener(event -> crearPais());
        editButton.addActionListener(event -> editarPais());
        deleteButton.addActionListener(event -> eliminarPais());
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                actualizarSeleccion();
            }
        });
        countriesNav.addActionListener(event -> mostrarPagina("paises", countriesNav));
        statisticsNav.addActionListener(event -> mostrarPagina("estadisticas", statisticsNav));
        searchField.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "focusSearch");
        searchField.getActionMap().put("focusSearch", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                searchField.requestFocusInWindow();
            }
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent event) { actualizarAcciones(); }
            @Override public void removeUpdate(DocumentEvent event) { actualizarAcciones(); }
            @Override public void changedUpdate(DocumentEvent event) { actualizarAcciones(); }
        });
        actualizarAcciones();
    }

    private void cargarFiltroLocal() {
        ejecutar("Filtrando el catálogo local…", this::leerSnapshot, snapshot -> {
            modoApi = false;
            mostrarSnapshot(snapshot);
            status.setText(snapshot.paises().size() + " países guardados coinciden con los filtros.");
        }, true);
    }

    private Snapshot leerSnapshot() throws SQLException {
        String region = regionFilter.getSelectedItem() == null ? "Todas" : RegionLabels.canonica(regionFilter.getSelectedItem().toString());
        return new Snapshot(
                controller.listar(searchField.getText(), region),
                controller.listarRegiones(),
                controller.obtenerEstadisticas()
        );
    }

    private void mostrarSnapshot(Snapshot snapshot) {
        mostrarPaises(snapshot.paises());
        actualizarOpcionesRegion(snapshot.regiones());
        actualizarMetricas(snapshot.estadisticas());
        actualizarEstadisticas(snapshot.estadisticas());
        status.setText("Catálogo conectado · MySQL · " + snapshot.estadisticas().totalPaises() + " registros locales");
        status.setForeground(AtlasTheme.MUTED);
    }

    private void actualizarOpcionesRegion(List<String> regiones) {
        String seleccion = regionFilter.getSelectedItem() == null ? "Todas" : RegionLabels.canonica(regionFilter.getSelectedItem().toString());
        actualizandoRegiones = true;
        regionFilter.removeAllItems();
        regionFilter.addItem("Todas");
        regiones.stream().map(RegionLabels::mostrar).forEach(regionFilter::addItem);
        regionFilter.setSelectedItem(regiones.contains(seleccion) ? RegionLabels.mostrar(seleccion) : "Todas");
        actualizandoRegiones = false;
    }

    private void mostrarPaises(List<Pais> paises) {
        paisesMostrados.clear();
        paisesMostrados.addAll(paises);
        tableModel.setRowCount(0);
        for (Pais pais : paises) {
            tableModel.addRow(new Object[]{
                pais.id(), pais.nombre(), valor(pais.capital()), RegionLabels.mostrar(pais.region()), pais.poblacion(), valor(pais.moneda())
            });
        }
        table.clearSelection();
        seleccionado = null;
        actualizarFicha(null);
        actualizarAcciones();
    }

    private void actualizarMetricas(EstadisticasPaises stats) {
        totalMetric.setText(Long.toString(stats.totalPaises()));
        regionsMetric.setText(Integer.toString(stats.paisesPorRegion().size()));
        if (stats.poblacionMaxima() == null || stats.paisesMasPoblados().isEmpty()) {
            populationMetric.setText("Sin datos");
        } else {
            populationMetric.setText(stats.paisesMasPoblados().get(0));
            populationMetric.setToolTipText("Población: " + ENTERO.format(stats.poblacionMaxima()));
        }
    }

    private void actualizarEstadisticas(EstadisticasPaises stats) {
        estadisticas = stats;
        reportPopulation.setText(stats.poblacionMaxima() == null ? "No disponible" : ENTERO.format(stats.poblacionMaxima()));
        reportTopCountries.setText(stats.paisesMasPoblados().isEmpty() ? "No disponible" : String.join(", ", stats.paisesMasPoblados()));
        reportTopRegions.setText(stats.regionesConMasPaises().isEmpty()
                ? "No disponible"
                : stats.regionesConMasPaises().stream().map(RegionLabels::mostrar).reduce((a, b) -> a + ", " + b).orElse("No disponible"));
        regionBars.removeAll();
        if (stats.totalPaises() == 0) {
            JLabel empty = new JLabel("Aún no hay países guardados. Importa desde la API o crea un registro.");
            empty.setForeground(AtlasTheme.MUTED);
            empty.setBorder(new EmptyBorder(22, 16, 22, 16));
            regionBars.add(empty);
        } else {
            long max = stats.paisesPorRegion().values().stream().mapToLong(Long::longValue).max().orElse(1);
            stats.paisesPorRegion().entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                    .forEach(entry -> regionBars.add(filaRegion(RegionLabels.mostrar(entry.getKey()), entry.getValue(), max)));
        }
        regionBars.revalidate();
        regionBars.repaint();
    }

    private static JPanel filaRegion(String region, long count, long max) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(9, 13, 9, 13));
        JLabel name = new JLabel(region);
        name.setForeground(AtlasTheme.INK);
        name.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel number = new JLabel(ENTERO.format(count));
        number.setForeground(AtlasTheme.MUTED);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue((int) Math.max(1, Math.round((count * 100.0) / max)));
        bar.setStringPainted(false);
        bar.setForeground(AtlasTheme.GREEN);
        bar.setBackground(new Color(233, 240, 233));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 16);
        row.add(name, c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        row.add(bar, c);
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(0, 16, 0, 0);
        row.add(number, c);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return row;
    }

    private void buscarApi() {
        String query = searchField.getText().trim();
        ejecutar("Consultando REST Countries…", () -> controller.buscarEnApi(query), paises -> {
            modoApi = true;
            mostrarPaises(paises);
            actualizarAcciones();
            status.setText(paises.isEmpty()
                    ? "La API no encontró países para «" + query + "». No se modificó MySQL."
                    : paises.size() + " resultados externos. Selecciona uno y pulsa «Guardar resultado» para persistirlo.");
            status.setForeground(AtlasTheme.GREEN);
        }, true);
    }

    private void importar() {
        ejecutar("Consultando páginas de REST Countries…", controller::importarPaises, resultado -> {
            JOptionPane.showMessageDialog(this,
                    "Registros recibidos: " + resultado.recibidos() + "\nPaíses nuevos: " + resultado.insertados()
                    + "\nYa existentes conservados: " + resultado.omitidos()
                    + "\nSin código ISO alfa-3, omitidos: " + resultado.sinCodigoIso(),
                    "Importación finalizada", JOptionPane.INFORMATION_MESSAGE);
            refrescarCatalogo();
        }, true);
    }

    private void guardarResultadoApi() {
        Pais pais = seleccionado;
        if (pais == null || !modoApi) {
            return;
        }
        ejecutar("Guardando el resultado seleccionado…", () -> controller.guardarResultadoApi(pais), resultado -> {
            String message = resultado.insertados() == 1
                    ? pais.nombre() + " se guardó en MySQL."
                    : pais.nombre() + " ya existe; se conservó el registro local y sus cambios.";
            JOptionPane.showMessageDialog(this, message, "Resultado de API", JOptionPane.INFORMATION_MESSAGE);
            refrescarCatalogo();
        }, true);
    }

    private void crearPais() {
        mostrarPagina("paises", countriesNav);
        PaisDialog.mostrar(this, null, regionesActuales()).ifPresent(pais ->
                ejecutar("Guardando el país…", () -> controller.crear(pais), id -> {
                    JOptionPane.showMessageDialog(this, "País guardado con ID " + id + ".", "Catálogo actualizado", JOptionPane.INFORMATION_MESSAGE);
                    refrescarCatalogo();
                }, true));
    }

    private void editarPais() {
        Pais pais = seleccionado;
        if (pais == null || modoApi) {
            return;
        }
        PaisDialog.mostrar(this, pais, regionesActuales()).ifPresent(actualizado ->
                ejecutar("Actualizando el país…", () -> controller.actualizar(actualizado), cambio -> {
                    JOptionPane.showMessageDialog(this,
                            cambio ? "Los datos del país se actualizaron." : "El registro no cambió o ya no existe.",
                            "Edición", JOptionPane.INFORMATION_MESSAGE);
                    refrescarCatalogo();
                }, true));
    }

    private void eliminarPais() {
        Pais pais = seleccionado;
        if (pais == null || modoApi) {
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar " + pais.nombre() + " del catálogo local? Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion == JOptionPane.YES_OPTION) {
            ejecutar("Eliminando el registro…", () -> controller.eliminar(pais.id()), eliminado -> {
                JOptionPane.showMessageDialog(this, eliminado ? "El registro fue eliminado." : "El país ya no existe.", "Catálogo", JOptionPane.INFORMATION_MESSAGE);
                refrescarCatalogo();
            }, true);
        }
    }

    private List<String> regionesActuales() {
        if (estadisticas == null) {
            return List.of();
        }
        return new ArrayList<>(estadisticas.paisesPorRegion().keySet());
    }

    private void actualizarSeleccion() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            seleccionado = null;
        } else {
            int modelRow = table.convertRowIndexToModel(viewRow);
            seleccionado = modelRow >= 0 && modelRow < paisesMostrados.size() ? paisesMostrados.get(modelRow) : null;
        }
        actualizarFicha(seleccionado);
        actualizarAcciones();
    }

    private void actualizarAcciones() {
        boolean haySeleccion = table.getSelectedRow() >= 0;
        editButton.setEnabled(!ocupado && !modoApi && haySeleccion);
        deleteButton.setEnabled(!ocupado && !modoApi && haySeleccion);
        saveApiButton.setEnabled(!ocupado && modoApi && haySeleccion);
        createButton.setEnabled(!ocupado && !modoApi);
        importButton.setEnabled(!ocupado);
        apiSearchButton.setEnabled(!ocupado);
        refreshButton.setEnabled(!ocupado);
        filterButton.setEnabled(!ocupado);
        clearButton.setEnabled(!ocupado);
    }

    private void actualizarFicha(Pais pais) {
        tokenBandera++;
        if (pais == null) {
            detailName.setText("Sin selección");
            detailCode.setText("Selecciona una fila de la tabla");
            detailCapital.setText("—");
            detailRegion.setText("—");
            detailPopulation.setText("—");
            detailCurrency.setText("—");
            detailFlag.setIcon(null);
            detailFlag.setText("Elige un país para ver su bandera");
            return;
        }
        detailName.setText(pais.nombre());
        detailCode.setText("ISO · " + pais.codigoIso());
        detailCapital.setText(valor(pais.capital()));
        detailRegion.setText(RegionLabels.mostrar(pais.region()));
        detailPopulation.setText(pais.poblacion() == null ? "No disponible" : ENTERO.format(pais.poblacion()));
        detailCurrency.setText(valor(pais.moneda()));
        detailFlag.setText("Cargando bandera…");
        detailFlag.setIcon(null);
        javax.swing.ImageIcon cached = banderas.get(pais.codigoIso());
        if (cached != null) {
            detailFlag.setText("");
            detailFlag.setIcon(cached);
        } else if (pais.banderaUrl() != null) {
            cargarBandera(pais, tokenBandera);
        } else {
            detailFlag.setText("Bandera no disponible");
        }
    }

    private void cargarBandera(Pais pais, long token) {
        new SwingWorker<javax.swing.ImageIcon, Void>() {
            @Override
            protected javax.swing.ImageIcon doInBackground() throws Exception {
                URLConnection connection = URI.create(pais.banderaUrl()).toURL().openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(7000);
                try (InputStream input = connection.getInputStream()) {
                    BufferedImage image = ImageIO.read(input);
                    if (image == null) {
                        return null;
                    }
                    java.awt.Image scaled = image.getScaledInstance(220, 128, java.awt.Image.SCALE_SMOOTH);
                    return new javax.swing.ImageIcon(scaled);
                }
            }

            @Override
            protected void done() {
                if (token != tokenBandera || seleccionado == null || !seleccionado.codigoIso().equals(pais.codigoIso())) {
                    return;
                }
                try {
                    javax.swing.ImageIcon image = get();
                    detailFlag.setText(image == null ? "Bandera no disponible" : "");
                    detailFlag.setIcon(image);
                    if (image != null) {
                        banderas.put(pais.codigoIso(), image);
                    }
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    detailFlag.setText("Bandera no disponible");
                    detailFlag.setIcon(null);
                } catch (ExecutionException error) {
                    detailFlag.setText("Bandera no disponible");
                    detailFlag.setIcon(null);
                }
            }
        }.execute();
    }

    private <T> void ejecutar(String mensaje, Callable<T> tarea, Consumer<T> alCompletar, boolean mostrarError) {
        if (ocupado) {
            return;
        }
        ocupado = true;
        progress.setVisible(true);
        status.setText(mensaje);
        status.setForeground(AtlasTheme.MUTED);
        actualizarAcciones();
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                ocupado = false;
                progress.setVisible(false);
                actualizarAcciones();
                try {
                    alCompletar.accept(get());
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    presentarError("La operación fue interrumpida.", mostrarError);
                } catch (ExecutionException error) {
                    presentarError(mensajeError(error.getCause()), mostrarError);
                }
            }
        }.execute();
    }

    private void presentarError(String mensaje, boolean mostrarDialogo) {
        status.setText(mensaje);
        status.setForeground(AtlasTheme.DANGER);
        if (mostrarDialogo) {
            JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String mensajeError(Throwable error) {
        if (error instanceof SQLException sql && "23000".equals(sql.getSQLState())) {
            return "El código ISO ya existe o el registro incumple una restricción de MySQL.";
        }
        String message = error == null ? null : error.getMessage();
        return message == null || message.isBlank() ? "Ocurrió un error inesperado." : message;
    }

    private void mostrarPagina(String pagina, JButton nav) {
        pagesLayout.show(pages, pagina);
        seleccionarNav(nav);
    }

    private void seleccionarNav(JButton seleccionado) {
        for (JButton button : List.of(countriesNav, statisticsNav)) {
            boolean active = button == seleccionado;
            button.setBackground(active ? new Color(32, 102, 87) : AtlasTheme.DEEP_GREEN);
            button.setForeground(active ? Color.WHITE : new Color(225, 239, 229));
        }
    }

    private static JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(AtlasTheme.MUTED);
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        return label;
    }

    private static void agregarDato(JPanel panel, int fila, String titulo, JLabel valor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 13, 0);
        JPanel block = new JPanel(new BorderLayout(0, 3));
        block.setOpaque(false);
        JLabel heading = new JLabel(titulo);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 9));
        heading.setForeground(AtlasTheme.MUTED);
        valor.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valor.setForeground(AtlasTheme.INK);
        valor.setVerticalAlignment(SwingConstants.TOP);
        block.add(heading, BorderLayout.NORTH);
        block.add(valor, BorderLayout.CENTER);
        panel.add(block, c);
    }

    private static JPanel cardPanel(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(AtlasTheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AtlasTheme.LINE),
                new EmptyBorder(10, 10, 10, 10)));
        return panel;
    }

    private static String valor(String value) {
        return value == null || value.isBlank() ? "No disponible" : value;
    }

    private record Snapshot(List<Pais> paises, List<String> regiones, EstadisticasPaises estadisticas) {
    }

    private static final class HeroPanel extends JPanel {
        private final JPanel contenido;

        private HeroPanel(JPanel contenido) {
            super(new BorderLayout());
            this.contenido = contenido;
            setOpaque(false);
            add(contenido, BorderLayout.CENTER);
            setPreferredSize(new Dimension(850, 156));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 156));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, AtlasTheme.GREEN, getWidth(), getHeight(), AtlasTheme.DEEP_GREEN));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focused, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            cell.setForeground(selected ? AtlasTheme.DEEP_GREEN : AtlasTheme.INK);
            cell.setBackground(selected ? new Color(221, 237, 226) : (row % 2 == 0 ? Color.WHITE : new Color(249, 251, 248)));
            setBorder(new EmptyBorder(0, 9, 0, 9));
            return cell;
        }
    }

    private static final class NumberRenderer extends ZebraRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focused, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table,
                    value instanceof Number number ? ENTERO.format(number.longValue()) : "No disponible",
                    selected, focused, row, column);
            setHorizontalAlignment(SwingConstants.RIGHT);
            return cell;
        }
    }
}
