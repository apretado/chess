package client;

import java.util.HashMap;
import java.util.Map;

import exception.ResponseException;
import model.GameData;
import server.ServerFacade;
import service.request.CreateGameRequest;
import service.result.ListGamesResult;

public class PostloginClient extends PregameClient {
    private Map<Integer, Integer> gameNumberToId;

    public PostloginClient(ServerFacade server, Repl repl) {
        super(server, repl);
        this.gameNumberToId = new HashMap<>();
    }

    @Override
    protected String handleCommand(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "help" -> help();
            case "logout" -> logout();
            case "create" -> create(params);
            case "list" -> list();
            // case "join" -> join(params);
            // case "observe" -> observe(params);
            case "quit" -> "quit";
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

    public String create(String... params) throws ResponseException {
        if (params.length >= 1) {
            server.createGame(new CreateGameRequest(params[0]));
            return String.format("Successfully created game '%s'", params[0]);
        }
        throw new ResponseException(400, "Expected: <NAME>");
    }

    public String list() throws ResponseException {
        ListGamesResult listGamesResult = server.listGames();
        gameNumberToId = new HashMap<>();
        int number = 1;
        StringBuilder output = new StringBuilder();
        for (GameData gameData : listGamesResult.games()) {
            gameNumberToId.put(number, gameData.gameID());
            output.append(String.format("%d: %s\n", number, gameData.gameName()));
            number++;
        }
        return output.toString();
    }
}
