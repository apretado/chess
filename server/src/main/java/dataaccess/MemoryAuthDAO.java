package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<String, AuthData>();

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData auth = authTokens.get(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return auth;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if(!authTokens.containsKey(authToken)){
            throw new DataAccessException("Error: unauthorized");
        }
        authTokens.remove(authToken);
    }

    @Override
    public void clearAuth() {
        authTokens.clear();
    }

}
