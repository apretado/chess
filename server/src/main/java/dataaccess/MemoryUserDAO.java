package dataaccess;

import java.util.HashMap;
import java.util.Map;

import model.UserData;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<String, UserData>();

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        if (users.containsKey(userData.username())) {
            throw new DataAccessException("Error: already taken");
        }
        users.put(userData.username(), userData);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = users.get(username);
        if (user == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return user;
    }

    @Override
    public void clearUser() {
        users.clear();
    }

}
