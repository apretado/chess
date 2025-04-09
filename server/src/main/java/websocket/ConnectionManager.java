package websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String authToken, Session session, int gameID) {
        connections.put(authToken, new Connection(authToken, session, gameID));
    }

    public void remove(String authToken) {
        connections.remove(authToken);
    }

    // Set gameID to -1 to broadcast to everyone
    public void broadcast(int gameID, String excludeAuthToken, String message) throws IOException {
        ArrayList<String> removeList = new ArrayList<>();

        for (Connection connection : connections.values()) {
            if (connection.getSession().isOpen()) {
                if ((gameID == -1 || connection.getGameID() == gameID)
                && !connection.getAuthToken().equals(excludeAuthToken)) {
                    connection.send(message);
                }
            } else {
                removeList.add(connection.getAuthToken());
            }
        }

        for (String authToken : removeList) {
            connections.remove(authToken);
        }
    }
}
