package client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.*;

import exception.ResponseException;
import server.Server;
import server.ServerFacade;
import service.request.RegisterRequest;
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
    void register() throws Exception {
        RegisterResult registerResult = facade.register(
            new RegisterRequest("username", "password", "email")
        );
        assertTrue(registerResult.authToken().length() > 10);
        assertEquals("username", registerResult.username());
    }

}
