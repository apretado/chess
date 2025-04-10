package websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;

public class ConnectionManager {
    private final ConcurrentHashMap<Session, Connection> connections = new ConcurrentHashMap<>();

    public void add(String authToken, Session session, int gameID) {
        connections.put(session, new Connection(authToken, session, gameID));
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    // Set gameID to -1 to broadcast to everyone
    public void broadcast(int gameID, String excludeAuthToken, String message) throws IOException {
        ArrayList<Session> removeList = new ArrayList<>();

        for (Connection connection : connections.values()) {
            if (connection.getSession().isOpen()) {
                if ((gameID == -1 || connection.getGameID() == gameID)
                && !connection.getAuthToken().equals(excludeAuthToken)) {
                    connection.send(message);
                }
            } else {
                removeList.add(connection.getSession());
            }
        }

        for (Session session : removeList) {
            connections.remove(session);
        }
    }

    public void sendMessage(Session session, String message) throws IOException {
        Connection connection = connections.get(session);
        if (connection.getSession().isOpen()) {
            connection.send(message);
        } else {
            remove(session);
        }
    }
}
