package dataaccess;

import java.sql.SQLException;

import model.UserData;

public class MySqlUserDAO implements UserDAO {

    public MySqlUserDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public void clearUser() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearUser'");
    }

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS UserData (
            `username` varchar(256) NOT NULL,
            `password` varchar(256) NOT NULL,
            `email` varchar(256) NOT NULL
            PRIMARY KEY (`username`)
        )
        """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: unable to configure database: " + e.getMessage());
        }
    }

}
