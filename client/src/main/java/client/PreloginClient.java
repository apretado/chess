package client;

import java.util.Arrays;

import exception.ResponseException;
import server.ServerFacade;
import service.request.LoginRequest;
import service.request.RegisterRequest;

public class PreloginClient implements Client {
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGED_OUT;
    private String username = null;

    public PreloginClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        // try {
            String[] tokens = input.toLowerCase().split(" ");
            // First token
            String command = (tokens.length > 0) ? tokens[0] : "help";
            // Rest of the tokens
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "quit" -> "quit";
                case "help" -> help();
                default -> help();
            };
        // } catch (ResponseException e) {
        //     // TODO: handle exceptions
        //     return e.getMessage();
        // }
    }

    public String help() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands
                """;
    }
}
