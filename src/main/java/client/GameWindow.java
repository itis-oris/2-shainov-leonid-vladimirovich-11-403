package client;

import protocol.ActionType;
import entity.map.Cell;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameWindow extends JFrame {

    private final GameClient client;
    private final JButton[][] cellButtons = new JButton[5][5];
    private final JLabel statusLabel = new JLabel("Waiting...");
    private final JPanel spellPanel = new JPanel();

    private boolean targetMode = false;
    private int activeSpellIndex = -1;

    private List<GameClient.UnitSnapshot> lastUnits = List.of();

    public GameWindow(GameClient client) {
        this.client = client;

        for (int x = 0; x < 5; x++)
            for (int y = 0; y < 5; y++)
                cellButtons[x][y] = new JButton();

        setupUI();

        setTitle("Magical Fights");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel board = new JPanel(new GridLayout(5, 5, 2, 2));
        board.setBackground(Color.DARK_GRAY);

        for (int y = 0; y < 5; y++)
            for (int x = 0; x < 5; x++) {
                int fx = x, fy = y;
                JButton btn = cellButtons[x][y];
                btn.setBackground(Color.WHITE);
                btn.addActionListener(e -> onCellClicked(fx, fy));
                board.add(btn);
            }

        add(board, BorderLayout.CENTER);

        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(250, 0));

        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(statusLabel);

        spellPanel.setLayout(new GridLayout(0, 1, 0, 10));
        side.add(spellPanel);

        JButton endTurn = new JButton("END TURN");
        endTurn.addActionListener(e -> {
            exitTargetMode();
            client.sendAction(ActionType.END_TURN, out -> {});
        });

        side.add(endTurn);
        add(side, BorderLayout.EAST);
    }

    private void onCellClicked(int x, int y) {
        if (targetMode) {
            client.sendAction(ActionType.CAST_SPELL_APPLY, out -> {
                out.writeInt(activeSpellIndex);
                out.writeByte(1);
                out.writeInt(x);
                out.writeInt(y);
            });
            exitTargetMode();
            return;
        }

        client.sendAction(ActionType.INSPECT_UNIT, out -> {
            out.writeInt(x);
            out.writeInt(y);
        });
    }

    public void showSpellTargets(List<Cell> targets) {
        if (targets.isEmpty()) {
            exitTargetMode();
            return;
        }

        targetMode = true;
        redrawBaseBoard();
        for (Cell c : targets) {
            cellButtons[c.x()][c.y()].setBackground(Color.CYAN);
        }
    }


    public void updateBoard(List<GameClient.UnitSnapshot> units) {
        lastUnits = units;
        redrawBaseBoard();
    }

    private void redrawBaseBoard() {
        for (var row : cellButtons)
            for (var b : row) {
                b.setBackground(Color.WHITE);
                b.setText("");
            }

        for (var u : lastUnits) {
            JButton btn = cellButtons[u.x()][u.y()];
            btn.setBackground(
                    u.playerId() == 1
                            ? new Color(200, 230, 255)
                            : new Color(255, 200, 200)
            );

            String arrow = switch (u.direction()) {
                case NORTH -> "↑";
                case SOUTH -> "↓";
                case WEST -> "←";
                case EAST -> "→";
            };

            btn.setText("<html> <div style='text-align:center;'> <div style='font-size:12px; font-weight:bold;'>P%s</div> <div style='font-size:12px;'>%d HP</div> <div style='font-size:28px;'>%s</div> </div> </html> ".formatted(
                    u.playerId() + ": " + u.unitName(),
                    u.hp(),
                    arrow
            ));

        }
    }

    public void updateSpells(List<String> spells, List<Integer> cooldowns) {
        spellPanel.removeAll();

        for (int i = 0; i < spells.size(); i++) {
            int spellIndex = i;
            int cd = cooldowns.get(i);

            String text = cd > 0
                    ? spells.get(i) + " (CD: " + cd + ")"
                    : spells.get(i);

            JButton b = new JButton(text);
            b.setEnabled(cd == 0);

            b.addActionListener(e -> {
                activeSpellIndex = spellIndex;
                client.sendAction(ActionType.CAST_SPELL_REQUEST, out -> {
                    out.writeInt(spellIndex);
                });
            });
            spellPanel.add(b);
        }

        spellPanel.revalidate();
        spellPanel.repaint();
    }


    public void setControlsEnabled(boolean enabled) {
        for (Component c : spellPanel.getComponents())
            c.setEnabled(enabled);
    }

    public void setInfoText(String text) {
        statusLabel.setText(text);
    }

    public void onError() {
        exitTargetMode();
    }

    private void exitTargetMode() {
        targetMode = false;
        activeSpellIndex = -1;
        redrawBaseBoard();
    }
}
