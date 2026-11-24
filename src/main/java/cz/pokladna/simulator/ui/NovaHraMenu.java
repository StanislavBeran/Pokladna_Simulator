package cz.pokladna.simulator.ui;

import cz.pokladna.simulator.io.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NovaHraMenu extends JPanel {
    private Menu hlavniOkno;

    public NovaHraMenu(Menu okno) {
        this.hlavniOkno = okno;
        setLayout(new BorderLayout());

        Menu.BackgroundPanel bgPanel = okno.new BackgroundPanel("/pozadi.png");
        bgPanel.setLayout(new GridBagLayout());

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        addLabel(container, "Zadejte název tvého obchodu:");
        container.add(Box.createVerticalStrut(5));

        JTextField txtNazev = new JTextField();
        txtNazev.setMaximumSize(new Dimension(300, 36));
        txtNazev.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtNazev.setBackground(new Color(42, 42, 54));
        txtNazev.setForeground(Color.WHITE);
        txtNazev.setCaretColor(Color.WHITE);
        txtNazev.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        txtNazev.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(txtNazev);
        container.add(Box.createVerticalStrut(14));

        addLabel(container, "Vyber pozici pro uložení:");
        container.add(Box.createVerticalStrut(5));
        JComboBox<String> cbSlot = vytvorComboBox(new String[]{"Pozice 1", "Pozice 2", "Pozice 3"});
        container.add(cbSlot);
        container.add(Box.createVerticalStrut(14));

        addLabel(container, "Začáteční počet peněz:");
        container.add(Box.createVerticalStrut(5));
        JComboBox<String> cbPenize = vytvorComboBox(new String[]{"1000 Kč", "2000 Kč", "5000 Kč", "10000 Kč"});
        container.add(cbPenize);
        container.add(Box.createVerticalStrut(24));

        JButton btnVytvorit = Menu.vytvorTlacitko("Vytvořit hru");
        btnVytvorit.addActionListener(e -> {
            String surovyNazev = txtNazev.getText().trim();
            int indexSlotu = cbSlot.getSelectedIndex() + 1;
            String penizeText = ((String) cbPenize.getSelectedItem()).replace(" Kč", "");

            if (!surovyNazev.isEmpty()) {
                String jmenoHry = SpravceSouboru.ocistiJmeno(surovyNazev);
                List<Integer> startovniZbozi = new ArrayList<>();
                startovniZbozi.add(16);
                SpravceSouboru.ulozHruDoSouboru(indexSlotu, jmenoHry, penizeText, 0, "00000",
                        startovniZbozi, "obchod_theme", new ArrayList<>(List.of("obchod_theme")));
                PokladnaSimulator simulator = hlavniOkno.getSimulator();
                simulator.nactiPenize(Integer.parseInt(penizeText));
                simulator.nactiXp(0);
                simulator.nactiVylepseni("00000");
                simulator.nactiOdemceneZbozi(startovniZbozi);
                // Nová hra: čisté statistiky a první směna (cíl se dopočítá z levelu).
                simulator.nactiStatistiky(0, 0, 0, 0, 1, 0, 0);
                simulator.nastavDetailyHry(indexSlotu, jmenoHry, "obchod_theme", null);
                SpravceZvuku.zastav("obchodak_theme_sound");
                SpravceZvuku.prehraj("hra_hudba_v_pozadi", "obchod_theme", 0, true);
                hlavniOkno.zobrazObrazovku("POKLADNA_SIMULATOR");
                simulator.zahajTutorial();
            } else {
                JOptionPane.showMessageDialog(this, "Musíš zadat název!");
            }
        });

        JButton btnZpet = Menu.vytvorTlacitko("Zpět do menu");
        btnZpet.addActionListener(e -> hlavniOkno.zobrazObrazovku("HLAVNI_MENU"));

        container.add(btnVytvorit);
        container.add(Box.createVerticalStrut(10));
        container.add(btnZpet);

        bgPanel.add(container);
        add(bgPanel, BorderLayout.CENTER);
    }

    private void addLabel(JPanel parent, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lbl.setForeground(new Color(190, 190, 205));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        parent.add(lbl);
    }

    private JComboBox<String> vytvorComboBox(String[] polozky) {
        JComboBox<String> cb = new JComboBox<String>(polozky) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, new Color(52, 52, 65), 0, h, new Color(33, 33, 44)));
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                Object sel = getSelectedItem();
                if (sel != null) {
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(sel.toString(), 10, (h + fm.getAscent() - fm.getDescent()) / 2);
                }
                int ax = w - 20, ay = h / 2;
                g2.setColor(new Color(180, 180, 200));
                g2.fillPolygon(new int[]{ax, ax + 10, ax + 5}, new int[]{ay - 3, ay - 3, ay + 4}, 3);
                g2.dispose();
            }
        };
        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setBorder(null);
                b.setVisible(false);
                return b;
            }
            @Override protected ComboPopup createPopup() {
                BasicComboPopup popup = new BasicComboPopup(comboBox) {
                    @Override
                    protected void configureList() {
                        super.configureList();
                        list.setBackground(new Color(42, 42, 54));
                        list.setForeground(Color.WHITE);
                        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        list.setSelectionBackground(new Color(55, 110, 200));
                        list.setSelectionForeground(Color.WHITE);
                        list.setBorder(null);
                    }
                };
                popup.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 90), 1));
                return popup;
            }
        });
        cb.setOpaque(false);
        cb.setBorder(null);
        cb.setFocusable(false);
        cb.setMaximumSize(new Dimension(300, 36));
        cb.setPreferredSize(new Dimension(300, 36));
        cb.setAlignmentX(Component.CENTER_ALIGNMENT);
        return cb;
    }
}
