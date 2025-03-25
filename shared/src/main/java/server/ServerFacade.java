package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;

import exception.ResponseException;
import model.AuthData;
import model.GameData;


public class ServerFacade {
    private final String serverUrl;
    private final int port;
    private String authToken;

    public ServerFacade(String serverUrl, int port) {
        this.serverUrl = serverUrl;
        this.port = port;
        this.authToken = null;
    }

    public AuthData login(String username, String password) {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public AuthData register(String username, String password, String email) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'register'");
    }

    public void logout() {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public void createGame(String gameName) {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public List<GameData> listGames() {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public void joinGame(String playerColor, int gameID) {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }
}
