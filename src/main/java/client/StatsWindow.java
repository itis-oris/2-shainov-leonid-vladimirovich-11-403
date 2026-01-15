package client;

import db.Database;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class StatsWindow extends JFrame {

    public StatsWindow() {
        setTitle("Game History");
        setSize(600, 400);

        String[] columns = {
                "Game #",
                "Started",
                "Ended",
                "Player 1",
                "Character 1",
                "Player 2",
                "Character 2",
                "Winner"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFillsViewportHeight(true);



        try (Connection c = Database.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT * FROM games ORDER BY id DESC"
             )) {
            while (rs.next()) {
                int winnerId = rs.getInt("winner_player");

                String winnerName =
                        winnerId == 1
                                ? rs.getString("player1_name")
                                : rs.getString("player2_name");

                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getTimestamp("started_at"),
                        rs.getTimestamp("ended_at"),
                        rs.getString("player1_name"),
                        rs.getString("player1_unit"),
                        rs.getString("player2_name"),
                        rs.getString("player2_unit"),
                        winnerName
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load game history");
            e.printStackTrace();
        }

        add(new JScrollPane(table));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}