package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void createAuth(AuthData auth);
    AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    void clearAuth() throws DataAccessException;
}
