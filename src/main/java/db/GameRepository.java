package db;

import java.sql.*;
import java.time.LocalDateTime;

public class GameRepository {

    public static void saveGame(
            LocalDateTime start,
            LocalDateTime end,
            String p1Name,
            String p1Unit,
            String p2Name,
            String p2Unit,
            int winner
    ) throws SQLException {

        String sql = """
            INSERT INTO games (
                started_at, ended_at,
                player1_name, player1_unit,
                player2_name, player2_unit,
                winner_player
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setString(3, p1Name);
            ps.setString(4, p1Unit);
            ps.setString(5, p2Name);
            ps.setString(6, p2Unit);
            ps.setInt(7, winner);

            ps.executeUpdate();
        }
    }
}
