package client;

import exception.ResponseException;
import server.ServerFacade;

public class PostloginClient extends PregameClient {

    public PostloginClient(ServerFacade server, Repl repl) {
        super(server, repl);
    }

    @Override
    protected String handleCommand(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "help" -> help();
            case "logout" -> logout();
            default -> help();
        };
    }

    @Override
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
