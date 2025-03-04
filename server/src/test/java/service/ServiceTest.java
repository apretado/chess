package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.GameData;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.CreateGameResult;
import service.result.ListGamesResult;
import service.result.LoginResult;
import service.result.RegisterResult;

public class ServiceTest {
    static final UserDAO USER_DAO = new MemoryUserDAO();
    static final AuthDAO AUTH_DAO = new MemoryAuthDAO();
    static final GameDAO GAME_DAO = new MemoryGameDAO();
    static final ClearService CLEAR_SERVICE = new ClearService(USER_DAO, AUTH_DAO, GAME_DAO);
    static final UserService USER_SERVICE = new UserService(USER_DAO, AUTH_DAO);
    static final GameService GAME_SERVICE = new GameService(AUTH_DAO, GAME_DAO);
    
    @BeforeEach
    void reset() {
        CLEAR_SERVICE.clear();
    }

    @Test
    void registerSuccess() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        RegisterResult result = USER_SERVICE.register(registerRequest);
        assertEquals("username", result.username());
        assertNotNull(result.authToken());

        registerRequest = new RegisterRequest("username2", "password2", "email2");
        result = USER_SERVICE.register(registerRequest);
        assertEquals("username2", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void registerFail() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        // Initial registration
        USER_SERVICE.register(registerRequest);
        // Repeat registration with same username
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            USER_SERVICE.register(registerRequest);
        });
        assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    void clear() throws DataAccessException {
        USER_SERVICE.register(new RegisterRequest("username", "password", "email"));
        USER_SERVICE.register(new RegisterRequest("username2", "password2", "email2"));
        CLEAR_SERVICE.clear();

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            USER_DAO.getUser("username");
        });
        assertEquals("Error: unauthorized", exception.getMessage());

        exception = assertThrows(DataAccessException.class, () -> {
            USER_DAO.getUser("username2");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void loginSuccess() throws DataAccessException {
        USER_SERVICE.register(new RegisterRequest("username", "password", "email"));
        LoginResult loginResult = USER_SERVICE.login(new LoginRequest("username", "password"));
        assertEquals("username", loginResult.username());
        assertNotNull(loginResult.authToken());
    }

    @Test
    void loginFail() throws DataAccessException {
        USER_SERVICE.register(new RegisterRequest("username", "password", "email"));
        // Wrong username
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            USER_SERVICE.login(new LoginRequest("username8", "password"));
        });
        assertEquals("Error: unauthorized", exception.getMessage());
        
        // Wrong password
        exception = assertThrows(DataAccessException.class, () -> {
            USER_SERVICE.login(new LoginRequest("username8", "password8"));
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void logoutSuccess() throws DataAccessException {
        RegisterResult registerResult = USER_SERVICE.register(new RegisterRequest("username", "password", "email"));
        USER_SERVICE.logout(registerResult.authToken());
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            ServiceTest.AUTH_DAO.getAuth(registerResult.authToken());
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void logoutFail() throws DataAccessException {
        // Register user
        USER_SERVICE.register(new RegisterRequest("username", "password", "email"));
        // Try to log out with invalid auth token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            USER_SERVICE.logout("invalidToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        // Register user
        String authToken = USER_SERVICE.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        CreateGameResult createGameResult = GAME_SERVICE.createGame(authToken, new CreateGameRequest("game name"));
        assertEquals(new CreateGameResult(1), createGameResult);
    }

    @Test
    void createGameFail() throws DataAccessException {
        // Register user
        USER_SERVICE.register(new RegisterRequest("username", "password", "email")).authToken();
        // Try to create game with invalid auth token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            GAME_SERVICE.createGame("invalidToken", new CreateGameRequest("game name"));
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        // Register user
        String authToken = USER_SERVICE.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        GAME_SERVICE.createGame(authToken, new CreateGameRequest("game name"));
        // List games
        ListGamesResult listGamesResult = GAME_SERVICE.listGames(authToken);
        ListGamesResult expected = new ListGamesResult(Arrays.asList(new GameData(1, null, null, "game name", new ChessGame())));
        assertEquals(expected, listGamesResult);
    }

    @Test
    void listGamesFail() throws DataAccessException {
        // Register user
        USER_SERVICE.register(new RegisterRequest("username", "password", "email")).authToken();
        // Try to list games with invalid auth token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            GAME_SERVICE.listGames("invalidToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void joinGameSuccess() throws DataAccessException {
        // Register user
        String authToken = USER_SERVICE.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        int gameID = GAME_SERVICE.createGame(authToken, new CreateGameRequest("game name")).gameID();
        // Join game
        GAME_SERVICE.joinGame(authToken, new JoinGameRequest("WHITE", gameID));
        assertEquals("username", GAME_DAO.getGame(1).whiteUsername());
    }

    @Test
    void joinGameFail() throws DataAccessException {
        // Register user
        String authToken = USER_SERVICE.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        GAME_SERVICE.createGame(authToken, new CreateGameRequest("game name"));

        // Try to join with invalid auth token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            GAME_SERVICE.joinGame("invalidToken", new JoinGameRequest("WHITE", 1));
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }
}
