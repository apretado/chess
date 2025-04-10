package client;

import java.util.HashMap;
import java.util.Map;

import chess.ChessGame.TeamColor;
import exception.ResponseException;
import model.GameData;
import server.ServerFacade;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.ListGamesResult;
import websocket.commands.ConnectCommand;

public class PostloginClient extends PregameClient {
    private Map<Integer, GameData> gameNumberToData;
    ServerFacade server;
    WebSocketFacade webSocket;

    public PostloginClient(ServerFacade server, WebSocketFacade webSocket, Repl repl) {
        super(repl);
        this.server = server;
        this.webSocket = webSocket;
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
            repl.setAuthToken(null);
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
                String whiteName = (gameData.whiteUsername() != null) ? gameData.whiteUsername() : "[Available]";
                String blackName = (gameData.blackUsername() != null) ? gameData.blackUsername() : "[Available]";
                output.append(String.format("%d | %s | White: %s | Black: %s\n", number, gameData.gameName(), whiteName, blackName));

                number++;
            }
            return output.toString();
        } catch (ResponseException e) {
            throw new ResponseException(403, "Error: unable to list games");
        }
    }

    public String observe(String... params) throws ResponseException {
        if (gameNumberToData == null) {
            throw new ResponseException(403, "You must call 'list' before calling 'observe.'");
        }
        if (params.length >= 1) {
            try {
                int gameNumber = Integer.parseInt(params[0]);
                GameData gameData = gameNumberToData.get(gameNumber);
                repl.setGameData(gameData);
                webSocket.connect(new ConnectCommand(super.repl.getAuthToken(), gameData.gameID()));
                repl.setState(State.OBSERVING);
                repl.setTeamColor(TeamColor.WHITE);
                return String.format("You joined game %d as an observer", gameNumber);
            } catch (IllegalArgumentException e) {
                throw new ResponseException(400, "Expected: <ID>");
            }
        }
        throw new ResponseException(400, "Expected: <ID>");
    }

    public String join(String... params) throws ResponseException {
        if (gameNumberToData == null) {
            throw new ResponseException(403, "You must call 'list' before calling 'join.'");
        }
        if (params.length >= 2) {
            try {
                TeamColor color = TeamColor.valueOf(params[1].toUpperCase());
                int gameNumber = Integer.parseInt(params[0]);
                GameData gameData = gameNumberToData.get(gameNumber);
                repl.setGameData(gameData);
                server.joinGame(new JoinGameRequest(color.toString(), gameData.gameID()));
                webSocket.connect(new ConnectCommand(super.repl.getAuthToken(), gameData.gameID()));
                repl.setState(State.PLAYING);
                repl.setTeamColor(color);
                return String.format("You joined game %d as %s", gameNumber, color.toString());
            } catch (IllegalArgumentException e) {
                throw new ResponseException(400, "Expected: <ID> [WHITE|BLACK]");
            } catch (ResponseException e) {
                throw new ResponseException(409, "Could not join game: color already taken");
            }
        }
        throw new ResponseException(400, "Expected: <ID> [WHITE|BLACK]");
    }
}
