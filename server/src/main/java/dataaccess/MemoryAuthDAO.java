package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<String, AuthData>();

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAuth'");
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAuth'");
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAuth'");
    }

    @Override
    public void clearAuth() {
        authTokens.clear();
    }

}
