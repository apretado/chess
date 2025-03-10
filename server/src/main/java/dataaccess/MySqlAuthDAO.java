package dataaccess;

import model.AuthData;

public class MySqlAuthDAO implements AuthDAO {

    @Override
    public void createAuth(AuthData auth) {
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearAuth'");
    }

}
