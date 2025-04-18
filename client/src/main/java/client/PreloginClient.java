package client;

import exception.ResponseException;
import server.ServerFacade;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.LoginResult;
import service.result.RegisterResult;

public class PreloginClient extends PregameClient {
    ServerFacade server;
    
    public PreloginClient(ServerFacade server, Repl repl) {
        super(repl);
        this.server = server;
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
            try {
                RegisterResult registerResult = server.register(new RegisterRequest(params[0], params[1], params[2]));
                repl.setAuthToken(registerResult.authToken());
            } catch (ResponseException e) {
                throw new ResponseException(403, "Error: username already taken");
            }
            repl.setState(State.LOGGED_IN);
            return String.format("You registered as %s.", params[0]); 
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            try {
                LoginResult loginResult = server.login(new LoginRequest(params[0], params[1]));
                repl.setAuthToken(loginResult.authToken());
            } catch (ResponseException e) {
                throw new ResponseException(401, "Error: incorrect username or password");
            }
            repl.setState(State.LOGGED_IN);
            return String.format("You logged in as %s.", params[0]);
        }
        throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD>");
    }


}
