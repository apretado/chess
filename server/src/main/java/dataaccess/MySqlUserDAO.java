package dataaccess;

import java.sql.SQLException;

import model.UserData;

public class MySqlUserDAO implements UserDAO {

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS users (
            `username` varchar(256) NOT NULL PRIMARY KEY,
            `password` varchar(256) NOT NULL,
            `email` varchar(256) NOT NULL
        )
        """
    };

    public MySqlUserDAO() throws DataAccessException {
        DatabaseInitializer.initializeTable(createStatements);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            // Try to add user to database
            try (var preparedStatement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, userData.username());
                preparedStatement.setString(2, userData.password());
                preparedStatement.setString(3, userData.email());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            // Duplicate key error
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("Error: already taken");
            }
            // Other errors
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT username, password, email FROM users WHERE username = ?")) {
                preparedStatement.setString(1, username);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                    } else {
                        throw new DataAccessException("Error: unauthorized");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting user: " + e.getMessage());
        }
    }

    @Override
    public void clearUser() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE users")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }
}
