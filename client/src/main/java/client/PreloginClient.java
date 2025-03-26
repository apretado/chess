package client;

import exception.ResponseException;
import server.ServerFacade;
import service.request.LoginRequest;
import service.request.RegisterRequest;

public class PreloginClient extends PregameClient {
    public PreloginClient(ServerFacade server, Repl repl) {
        super(server, repl);
    }

    @Override
    protected String handleCommand(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "help" -> help();
            case "quit" -> "quit";
            case "login" -> login(params);
            case "register" -> register(params);
            default -> help();
        };
    }

    @Override
    public String help() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands
                """;
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            server.register(new RegisterRequest(params[0], params[1], params[2]));
            repl.setState(State.LOGGED_IN);
            return String.format("You registered as %s.", params[0]); 
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            server.login(new LoginRequest(params[0], params[1]));
            repl.setState(State.LOGGED_IN);
            return String.format("You logged in as %s.", params[0]);
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD>");
    }


}
