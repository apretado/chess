package client;

import java.util.Arrays;

import exception.ResponseException;
import server.ServerFacade;

public class PostloginClient implements Client {
    private final ServerFacade server;
    private Repl repl;

    public PostloginClient(ServerFacade server, Repl repl) {
        this.server = server;
        this.repl = repl;
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            // First token
            String command = (tokens.length > 0) ? tokens[0] : "help";
            // Rest of the tokens
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (command) {
                case "help" -> help();
                case "logout" -> logout();
                default -> help();
            };
        } catch (ResponseException e) {
            // TODO: handle exceptions
            return e.getMessage();
        }
    }

    public String help() {
        return """
                create <NAME> - a game
                list - games
                join <ID> [WHITE|BLACK] - a game
                observe <ID> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """;
    }

    public String logout() throws ResponseException {
        server.logout();
        repl.setState(State.LOGGED_OUT);
        return "You successfully logged out.";
    }

}
