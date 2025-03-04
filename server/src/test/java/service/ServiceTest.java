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
    static final UserDAO userDAO = new MemoryUserDAO();
    static final AuthDAO authDAO = new MemoryAuthDAO();
    static final GameDAO gameDAO = new MemoryGameDAO();
    static final ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
    static final UserService userService = new UserService(userDAO, authDAO);
    static final GameService gameService = new GameService(authDAO, gameDAO);
    
    @BeforeEach
    void reset() {
        clearService.clear();
    }

    @Test
    void registerSuccess() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        RegisterResult result = userService.register(registerRequest);
        assertEquals("username", result.username());
        assertNotNull(result.authToken());

        registerRequest = new RegisterRequest("username2", "password2", "email2");
        result = userService.register(registerRequest);
        assertEquals("username2", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    void registerFail() throws DataAccessException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        // Initial registration
        userService.register(registerRequest);
        // Repeat registration with same username
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.register(registerRequest);
        });
        assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    void clear() throws DataAccessException {
        userService.register(new RegisterRequest("username", "password", "email"));
        userService.register(new RegisterRequest("username2", "password2", "email2"));
        clearService.clear();

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userDAO.getUser("username");
        });
        assertEquals("Error: unauthorized", exception.getMessage());

        exception = assertThrows(DataAccessException.class, () -> {
            userDAO.getUser("username2");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void loginSuccess() throws DataAccessException {
        userService.register(new RegisterRequest("username", "password", "email"));
        LoginResult loginResult = userService.login(new LoginRequest("username", "password"));
        assertEquals("username", loginResult.username());
        assertNotNull(loginResult.authToken());
    }

    @Test
    void loginFail() throws DataAccessException {
        userService.register(new RegisterRequest("username", "password", "email"));
        // Wrong username
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.login(new LoginRequest("username8", "password"));
        });
        assertEquals("Error: unauthorized", exception.getMessage());
        
        // Wrong password
        exception = assertThrows(DataAccessException.class, () -> {
            userService.login(new LoginRequest("username8", "password8"));
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void logoutSuccess() throws DataAccessException {
        RegisterResult registerResult = userService.register(new RegisterRequest("username", "password", "email"));
        userService.logout(registerResult.authToken());
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            ServiceTest.authDAO.getAuth(registerResult.authToken());
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void logoutFail() throws DataAccessException {
        // Register user
        userService.register(new RegisterRequest("username", "password", "email"));
        // Try to log out with invalid auth token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.logout("invalidToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        // Register user
        String authToken = userService.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        CreateGameResult createGameResult = gameService.createGame(authToken, new CreateGameRequest("game name"));
        assertEquals(new CreateGameResult(1), createGameResult);
    }

    @Test
    void createGameFail() throws DataAccessException {
        // Register user
        userService.register(new RegisterRequest("username", "password", "email")).authToken();
        // Try to create game with invalid auth token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame("invalidToken", new CreateGameRequest("game name"));
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        // Register user
        String authToken = userService.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        gameService.createGame(authToken, new CreateGameRequest("game name"));
        // List games
        ListGamesResult listGamesResult = gameService.listGames(authToken);
        ListGamesResult expected = new ListGamesResult(Arrays.asList(new GameData(1, null, null, "game name", new ChessGame())));
        assertEquals(expected, listGamesResult);
    }

    @Test
    void listGamesFail() throws DataAccessException {
        // Register user
        userService.register(new RegisterRequest("username", "password", "email")).authToken();
        // Try to list games with invalid auth token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.listGames("invalidToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    void joinGameSuccess() throws DataAccessException {
        // Register user
        String authToken = userService.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        int gameID = gameService.createGame(authToken, new CreateGameRequest("game name")).gameID();
        // Join game
        gameService.joinGame(authToken, new JoinGameRequest("WHITE", gameID));
        assertEquals("username", gameDAO.getGame(1).whiteUsername());
    }

    @Test
    void joinGameFail() throws DataAccessException {
        // Register user
        String authToken = userService.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        gameService.createGame(authToken, new CreateGameRequest("game name"));

        // Try to join with invalid auth token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("invalidToken", new JoinGameRequest("WHITE", 1));
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }
}
