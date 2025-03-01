package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.UserData;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<String, UserData>();

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public void clearUser() throws DataAccessException {
        users.clear();
    }

}
