package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.LoginResult;
import service.result.RegisterResult;

public class ServiceTest {
    static final UserDAO userDAO = new MemoryUserDAO();
    static final AuthDAO authDAO = new MemoryAuthDAO();
    static final GameDAO gameDAO = new MemoryGameDAO();
    static final ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
    static final UserService userService = new UserService(userDAO, authDAO);
    
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
        // Try to log out with invalid token
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.logout("invalidToken");
        });
        assertEquals("Error: unauthorized", exception.getMessage());
    }
}
