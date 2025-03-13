package dataaccess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.AuthData;
import model.UserData;

public class DataAccessTest {
    UserDAO userDAO;
    AuthDAO authDAO;

    DataAccessTest() throws DataAccessException {
        userDAO = new MySqlUserDAO();
        authDAO = new MySqlAuthDAO();
    }

    @BeforeEach
    void clear() throws DataAccessException {
        userDAO.clearUser();
        authDAO.clearAuth();
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

}
