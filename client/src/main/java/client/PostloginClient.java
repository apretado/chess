package client;

import java.util.HashMap;
import java.util.Map;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import exception.ResponseException;
import model.GameData;
import server.ServerFacade;
import service.request.CreateGameRequest;
import service.result.ListGamesResult;

public class PostloginClient extends PregameClient {
    private Map<Integer, GameData> gameNumberToData;

    public PostloginClient(ServerFacade server, Repl repl) {
        super(server, repl);
    }

    @Override
    protected String handleCommand(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "help" -> help();
            case "logout" -> logout();
            case "create" -> create(params);
            case "list" -> list();
            case "join" -> join(params);
            case "observe" -> observe(params);
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
        try {
            server.logout();
        } catch (ResponseException e) {
            throw new ResponseException(403, "Error: unable to logout");
        }
        repl.setState(State.LOGGED_OUT);
        return "You successfully logged out.";
    }

    public String create(String... params) throws ResponseException {
        if (params.length >= 1) {
            try {
                server.createGame(new CreateGameRequest(params[0]));
            } catch (ResponseException e) {
                throw new ResponseException(403, "Error: unable to create game");
            }
            return String.format("Successfully created game '%s'", params[0]);
        }
        throw new ResponseException(400, "Expected: <NAME>");
    }

    public String list() throws ResponseException {
        try {
            ListGamesResult listGamesResult = server.listGames();
            if (listGamesResult.games().isEmpty()) {
                gameNumberToData = new HashMap<>();
                return "No active games.";
            }
            gameNumberToData = new HashMap<>();
            int number = 1;
            StringBuilder output = new StringBuilder();
            for (GameData gameData : listGamesResult.games()) {
                gameNumberToData.put(number, gameData);
                output.append(String.format("%d: %s\n", number, gameData.gameName()));
                number++;
            }
            return output.toString();
        } catch (ResponseException e) {
            throw new ResponseException(403, "Error: unable to list games");
        }
    }

    public String observe(String... params) throws ResponseException {
        if (params.length >= 1) {
            return join(new String[]{params[0], "WHITE"});
        }
        throw new ResponseException(400, "Expected: <ID>");
    }

    public String join(String... params) throws ResponseException {
        if (gameNumberToData == null) {
            throw new ResponseException(403, "You must call 'list' before calling 'observe' or 'join.'");
        }
        if (params.length >= 2) {
            try {
                TeamColor color = TeamColor.valueOf(params[1].toUpperCase());
                int gameNumber = Integer.parseInt(params[0]);
                ChessBoard chessBoard = gameNumberToData.get(gameNumber).game().getBoard();
                return BoardProcesser.makeString(chessBoard, color);
            } catch (IllegalArgumentException e) {
                throw new ResponseException(400, "Expected: <ID> [WHITE|BLACK]");
            } catch (NullPointerException e) {
                throw new ResponseException(400, "No game has been assigned this ID. Type 'list' to get game IDs");
            }
        }
        throw new ResponseException(400, "Expected: <ID> [WHITE|BLACK]");
    }
}
