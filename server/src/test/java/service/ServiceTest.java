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
import service.exception.UsernameTakenException;
import service.request.RegisterRequest;
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
    void registerUserSuccess() throws UsernameTakenException {
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
    void registerUserError() throws UsernameTakenException {
        RegisterRequest registerRequest = new RegisterRequest("username", "password", "email");
        // Initial registration
        userService.register(registerRequest);
        // Repeat registration with same username
        UsernameTakenException exception = assertThrows(UsernameTakenException.class, () -> {
            userService.register(registerRequest);
        });
        assertEquals("Error: already taken", exception.getMessage());
    }

    @Test
    void clear() throws UsernameTakenException {
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

}
