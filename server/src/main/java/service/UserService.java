package service;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.LoginResult;
import service.result.RegisterResult;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    
    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        // Create user
        userDAO.createUser(
            new UserData(registerRequest.username(), hashedPassword, registerRequest.email())
        );
        // Create auth token
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, registerRequest.username()));
        return new RegisterResult(registerRequest.username(), token);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException {
        // Check username and password
        UserData user = userDAO.getUser(loginRequest.username());
        if (!BCrypt.checkpw(loginRequest.password(), user.password())) {
            throw new DataAccessException("Error: unauthorized");
        }
        // Create auth token
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, loginRequest.username()));
        return new LoginResult(loginRequest.username(), token);
    }

    public void logout(String authToken) throws DataAccessException {
        authDAO.deleteAuth(authToken);
    }
}
