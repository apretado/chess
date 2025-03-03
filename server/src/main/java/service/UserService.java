package service;

import java.util.UUID;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import service.exception.UsernameTakenException;
import service.request.RegisterRequest;
import service.result.RegisterResult;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    
    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws UsernameTakenException{
        try {
            userDAO.createUser(
                new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email())
            );
        } catch (DataAccessException e) {
            throw new UsernameTakenException(e.getMessage());
        }
        
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, registerRequest.username()));
        return new RegisterResult(registerRequest.username(), token);
    }
}
