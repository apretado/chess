package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.google.gson.Gson;

import exception.ResponseException;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.CreateGameResult;
import service.result.ListGamesResult;
import service.result.LoginResult;
import service.result.RegisterResult;

public class ServerFacade {
    private final String serverUrl;
    private String authToken;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        this.authToken = null;
    }

    public LoginResult login(LoginRequest loginRequest) {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public RegisterResult register(RegisterRequest registerRequest) throws ResponseException {
        String path = "/user";
        return makeRequest("POST", path, registerRequest, RegisterResult.class);
    }

    public void logout() {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public ListGamesResult listGames() {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public void joinGame(JoinGameRequest joinGameRequest) {
        // TODO implement method
        throw new UnsupportedOperationException("Unimplemented method");
    }

    public void clear() throws ResponseException {
        makeRequest("DELETE", "/db", null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
