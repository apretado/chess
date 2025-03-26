package client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;

import exception.ResponseException;
import server.Server;
import server.ServerFacade;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.ListGamesResult;
import service.result.LoginResult;
import service.result.RegisterResult;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String url = "http://localhost:" + port;
        facade = new ServerFacade(url);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws ResponseException {
        assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    void registerSuccess() throws ResponseException {
        RegisterResult registerResult = facade.register(
            new RegisterRequest("username", "password", "email")
        );
        assertTrue(registerResult.authToken().length() > 10);
        assertEquals("username", registerResult.username());
    }

    @Test
    void registerFail() throws ResponseException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        // Initial registration
        facade.register(registerRequest);
        // Repeat registration with same username
        assertThrows(ResponseException.class, () -> {
            facade.register(registerRequest);
        });
    }

    @Test
    void loginSuccess() throws ResponseException {
        facade.register(new RegisterRequest("myname", "mypass", "mymail"));
        LoginResult loginResult = facade.login(new LoginRequest("myname", "mypass"));
        assertEquals("myname", loginResult.username());
        assertTrue(loginResult.authToken().length() > 10);
    }

    @Test
    void loginFail() throws ResponseException {
        facade.register(new RegisterRequest("username", "password", "email"));
        // Wrong username
        assertThrows(ResponseException.class, () -> {
            facade.login(new LoginRequest("username8", "password"));
        });
        
        // Wrong password
        assertThrows(ResponseException.class, () -> {
            facade.login(new LoginRequest("username8", "password8"));
        });
    }


    @Test
    void logoutSuccess() throws ResponseException {
        facade.register(new RegisterRequest("username", "password", "email"));
        facade.logout();
        // Try to log out with expired auth token
        assertThrows(ResponseException.class, () -> {
            facade.logout();
        });
    }

    @Test
    void logoutFail() throws ResponseException {
        // Try to log out without having logged in
        assertThrows(ResponseException.class, () -> {
            facade.logout();
        });
    }

    @Test
    void createGameSuccess() throws ResponseException {
        // Register user
        RegisterResult registerResult = facade.register(new RegisterRequest("username", "password", "email"));
        // Create game
        facade.createGame(new CreateGameRequest("game name"));
        // Check if the game was created
        ListGamesResult listGamesResult = facade.listGames();
        assertNotNull(listGamesResult.games().get(0));
        }

    @Test
    void createGameFail() throws ResponseException {
        // Try to create game without having logged in
        assertThrows(ResponseException.class, () -> {
            facade.createGame(new CreateGameRequest("game name"));
        });
    }

    @Test
    void listGamesSuccess() throws ResponseException {
        // Register user
        facade.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        facade.createGame(new CreateGameRequest("game name"));
        // List games
        ListGamesResult listGamesResult = facade.listGames();
        assertNotNull(listGamesResult.games().get(0));
    }

    @Test
    void listGamesFail() throws ResponseException {
        // Try to list games without having logged in
        assertThrows(ResponseException.class, () -> {
            facade.listGames();
        });
    }

    @Test
    void joinGameSuccess() throws ResponseException {
        // Register user
        String authToken = facade.register(new RegisterRequest("username", "password", "email")).authToken();
        // Create game
        int gameID = facade.createGame(new CreateGameRequest("game name")).gameID();
        // Join game
        assertDoesNotThrow(() -> facade.joinGame(new JoinGameRequest("WHITE", gameID)));
    }

    @Test
    void joinGameFail() throws ResponseException {
        // Try to join without having logged in
        assertThrows(ResponseException.class, () -> {
            facade.joinGame(new JoinGameRequest("WHITE", 1));
        });
    }

}
