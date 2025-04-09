package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    private String authToken;
    private Session session;
    private int gameID;

    public Connection(String authToken, Session session, int gameID) {
        this.authToken = authToken;
        this.session = session;
        this.gameID = gameID;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }
}
