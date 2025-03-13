package dataaccess;

import java.sql.SQLException;

public class DatabaseInitializer {
    public static void initializeTable(String[] createStatements) throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to configure database: " + e.getMessage());
        }
    }
}