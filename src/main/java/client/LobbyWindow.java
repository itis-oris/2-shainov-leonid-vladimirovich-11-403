package client;

import protocol.ActionType;
import protocol.UnitType;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;

public class LobbyWindow extends JFrame {

    private final GameClient client;

    private final JComboBox<UnitType> unitBox;
    private final JLabel statusLabel;
    private final JButton readyButton;
    private boolean unitSelected = false;


    public LobbyWindow(GameClient client) {
        this.client = client;

        setTitle("Character Selection");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Choose your character", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);

        JTextField nameField = new JTextField(10);

        nameField.addActionListener(e ->
                client.sendAction(ActionType.SET_PLAYER_NAME,
                        out -> out.writeUTF(nameField.getText()))
        );

        nameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                updateReadyButton(nameField);
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });


        JPanel namePanel = new JPanel();
        namePanel.add(new JLabel("Your name:"));
        namePanel.add(nameField);

        add(namePanel, BorderLayout.WEST);
        unitBox = new JComboBox<>(UnitType.values());
        unitBox.addActionListener(e -> {
            UnitType type = (UnitType) unitBox.getSelectedItem();
            unitSelected = true;
            client.sendAction(ActionType.SELECT_CHARACTER,
                    out -> out.writeInt(type.ordinal()));
            updateReadyButton(nameField);
        });

        JPanel center = new JPanel();
        center.add(unitBox);
        add(center, BorderLayout.CENTER);

        statusLabel = new JLabel("Waiting for other player...", SwingConstants.CENTER);



        readyButton = new JButton("READY");
        readyButton.setEnabled(false);
        readyButton.addActionListener(e -> {

            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                client.sendAction(ActionType.SET_PLAYER_NAME,
                        out -> out.writeUTF(name));
            }

            client.sendAction(ActionType.PLAYER_READY, out -> {});
        });

        JPanel bottom = new JPanel(new GridLayout(2, 1));

        JButton history = new JButton("GAME HISTORY");
        history.addActionListener(e -> new StatsWindow());

        bottom.add(history);

        bottom.add(statusLabel);
        bottom.add(readyButton);

        add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void updateLobbyState(
            Map<Integer, UnitType> selected,
            Set<Integer> ready
    ) {
        StringBuilder sb = new StringBuilder("<html>");

        for (int pid : selected.keySet()) {
            sb.append("Player ").append(pid)
                    .append(": ")
                    .append(selected.get(pid))
                    .append(ready.contains(pid) ? " (READY)" : "")
                    .append("<br>");
        }

        sb.append("</html>");
        statusLabel.setText(sb.toString());
    }

    private void updateReadyButton(JTextField nameField) {
        String name = nameField.getText().trim();
        readyButton.setEnabled(!name.isEmpty() && unitSelected);
    }
}
