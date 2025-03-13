package dataaccess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

public class DataAccessTest {
    UserDAO userDAO;
    AuthDAO authDAO;
    GameDAO gameDAO;

    DataAccessTest() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
        gameDAO = new MySqlGameDAO();
    }

    @BeforeEach
    void clear() throws DataAccessException {
        userDAO.clearUser();
        authDAO.clearAuth();
        gameDAO.clearGame();
    }

    @Test
    void createUserSuccess() {
        UserData inputUser = new UserData("username", "password", "email");
        // Add the user
        assertDoesNotThrow(() -> userDAO.createUser(inputUser));
        // Query the user
        UserData outputUser = assertDoesNotThrow(() -> userDAO.getUser("username"));
        assertEquals(inputUser, outputUser);
    }
    
    @Test
    void createUserFail() throws DataAccessException {
        UserData inputUser = new UserData("username", "password", "email");
        // Add the user
        assertDoesNotThrow(() -> userDAO.createUser(inputUser));
        // Try to add the user again
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(inputUser);
        });
        assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    void getUserSuccess() throws DataAccessException {
        UserData inputUser = new UserData("username", "password", "email");
        // Add the user
        assertDoesNotThrow(() -> userDAO.createUser(inputUser));
        // Query the user
        UserData outputUser = assertDoesNotThrow(() -> userDAO.getUser("username"));
        assertEquals(inputUser, outputUser);
    }

    @Test
    void getUserFail() throws DataAccessException {
        UserData inputUser = new UserData("username", "password", "email");
        // Add the user
        assertDoesNotThrow(() -> userDAO.createUser(inputUser));
        // Query non-existent user
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userDAO.getUser("bad_username");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void clearUserSuccess() throws DataAccessException {
        // Add users
        userDAO.createUser(new UserData("username", "password", "email"));
        userDAO.createUser(new UserData("username2", "password", "email"));
        // Clear
        userDAO.clearUser();

        // Try to get the users after clearing
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userDAO.getUser("username");
        });
        assertEquals("Error: unauthorized", exception.getMessage());

        exception = assertThrows(DataAccessException.class, () -> {
            userDAO.getUser("username");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void createAuthSuccess() {
        // Add auth data
        assertDoesNotThrow(() -> authDAO.createAuth(new AuthData("authToken", "username")));
    }

    @Test
    void creatAuthFail() {
        // Add auth data
        AuthData authData = new AuthData("authToken", "username");
        assertDoesNotThrow(() -> authDAO.createAuth(authData));

        // Try to add it again
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(authData);
        });
    }

    @Test
    void getAuthSuccess() {
        // Add auth data
        AuthData inputAuthData = new AuthData("authToken", "username");
        assertDoesNotThrow(() -> authDAO.createAuth(inputAuthData));

        // Get auth data
        AuthData outpAuthData = assertDoesNotThrow(() -> authDAO.getAuth("authToken"));
        assertEquals(inputAuthData, outpAuthData);
    }

    @Test
    void getAuthFail() {
        // Try to get non existent auth data
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("invalidAuthToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void deleteAuthSuccess() {
        // Add auth data
        AuthData inputAuthData = new AuthData("authToken", "username");
        assertDoesNotThrow(() -> authDAO.createAuth(inputAuthData));

        // Delete auth data
        assertDoesNotThrow(() -> authDAO.deleteAuth("authToken"));

        // Check if it was deleted
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("authToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void deleteAuthFail() {
        // Add auth data
        AuthData inputAuthData = new AuthData("authToken", "username");
        assertDoesNotThrow(() -> authDAO.createAuth(inputAuthData));

        // Try to delete auth data with wrong token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("badToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void clearAuthSuccess() throws DataAccessException {
        // Add some auth data
        authDAO.createAuth(new AuthData("authToken", "username"));
        authDAO.createAuth(new AuthData("authToken2", "username2"));

        // Clear the auth data
        assertDoesNotThrow(() -> authDAO.clearAuth());

        // Check if it was cleared
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("authToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());

        exception = assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("authToken2");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        // Create game
        gameDAO.createGame(new GameData(
                0, null, null, "gameName", new ChessGame()
        ));

        // Check if it was created
        GameData expectedGameData = new GameData(1, null, null, "gameName", new ChessGame());
        GameData actualGameData = gameDAO.getGame(1);
        assertEquals(expectedGameData, actualGameData);
    }

    @Test
    void createGameFail() {
        // Create game without name
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(new GameData(
                0, null, null, null, null
            ));
        });
        assertEquals("Error: game name cannot be null", exception.getMessage());
    }

    @Test
    void getGameSuccess() {
        // 
    }

    @Test
    void getGameFail() throws DataAccessException {
    // Create game
    gameDAO.createGame(new GameData(
                0, null, null, "gameName", new ChessGame()
    ));

    // Try to get game with invalid id
    DataAccessException exception = assertThrows(DataAccessException.class, () -> {
        gameDAO.getGame(2);
    });
    assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        // Create games
        gameDAO.createGame(new GameData(
            0, null, null, "gameName", new ChessGame()
        ));
        gameDAO.createGame(new GameData(
            0, null, null, "gameName2", new ChessGame()
        ));

        // List games
        List<GameData> expectedGameList = new ArrayList<>();
        expectedGameList.add(new GameData(1, null, null, "gameName", new ChessGame()));
        expectedGameList.add(new GameData(2, null, null, "gameName2", new ChessGame()));
        List<GameData> actualGameList = gameDAO.listGames();
        assertEquals(expectedGameList, actualGameList);
    }

    @Test
    void listGamesFail() throws SQLException, DataAccessException {
        // Drop the games table
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("DROP TABLE games")) {
                preparedStatement.executeUpdate();
            }
        }

        // Try to list games
        assertThrows(DataAccessException.class, () -> {
            gameDAO.listGames();
        });
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        // Create game
        gameDAO.createGame(new GameData(
            0, null, null, "gameName", new ChessGame()
        ));

        // Join game
        GameData expectedGameData = new GameData(
            1, "whiteUsername", null, "gameName", new ChessGame()
        );
        gameDAO.updateGame(expectedGameData);

        // Check if updated
        assertEquals(expectedGameData, gameDAO.getGame(1));
    }

    @Test
    void updateGameFail() throws DataAccessException {
        // Create game
        gameDAO.createGame(new GameData(
            0, null, null, "gameName", new ChessGame()
        ));

        // Try to update with invalid game id
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(new GameData(
                2, "whiteUsername", null, null, new ChessGame()
            ));
        });
        assertEquals("Error: bad request", exception.getMessage());
    }

    @Test
    void clearGame() throws DataAccessException {
        // Create game
        int gameID = gameDAO.createGame(new GameData(
            0, null, null, "gameName", new ChessGame()
        ));

        // Clear
        gameDAO.clearGame();

        // Check if game was cleared
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.getGame(gameID);
        });
        assertEquals("Error: bad request", exception.getMessage());
    }
}
