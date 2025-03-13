package dataaccess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.UserData;

public class DataAccessTest {
    UserDAO userDAO;

    DataAccessTest() throws DataAccessException {
        userDAO = new MySqlUserDAO();
    }

    @BeforeEach
    void clear() throws DataAccessException {
        userDAO.clearUser();
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


}
