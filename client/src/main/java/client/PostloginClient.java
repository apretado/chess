package client;

import java.util.HashMap;
import java.util.Map;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;
import exception.ResponseException;
import model.GameData;
import server.ServerFacade;
import service.request.CreateGameRequest;
import service.result.ListGamesResult;

import static ui.EscapeSequences.*;

public class PostloginClient extends PregameClient {
    private Map<Integer, GameData> gameNumberToData;

    public PostloginClient(ServerFacade server, Repl repl) {
        super(server, repl);
        this.gameNumberToData = new HashMap<>();
    }

    @Override
    protected String handleCommand(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "help" -> help();
            case "logout" -> logout();
            case "create" -> create(params);
            case "list" -> list();
            // case "join" -> join(params);
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
        gameNumberToData = new HashMap<>();
        int number = 1;
        StringBuilder output = new StringBuilder();
        for (GameData gameData : listGamesResult.games()) {
            gameNumberToData.put(number, gameData);
            output.append(String.format("%d: %s\n", number, gameData.gameName()));
            number++;
        }
        return output.toString();
    }

    public String observe(String... params) throws ResponseException {
        if (params.length >= 1) {
            ChessBoard chessBoard = gameNumberToData.get(Integer.parseInt(params[0])).game().getBoard();
            return boardToString(chessBoard);
        }
        throw new ResponseException(400, "Expected: <ID>");
    }

    private String boardToString(ChessBoard chessBoard) {
        StringBuilder output = new StringBuilder();
        for (int row = ChessBoard.getBoardSize() + 1; row >= 0; row--) {
            // Row number
            output.append(String.format(" %s%s%s ", SET_BG_COLOR_DARK_GREY, SET_TEXT_COLOR_BLACK,
                row != 0 && row != ChessBoard.getBoardSize() + 1 ? Integer.toString(row) : " "
            ));
            for (int col = ChessBoard.getBoardSize(); col >= 1; col--) {
                if (row == 0 || row == ChessBoard.getBoardSize() + 1) {
                    // Column number
                    output.append(String.format(" %s%s%d ", SET_BG_COLOR_DARK_GREY, SET_TEXT_COLOR_BLACK, col));
                } else {
                    // Background color
                    output.append((row - col) % 2 == 0 ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK);
                    output.append(" ");
                    // Chess piece
                    ChessPiece chessPiece = chessBoard.getPiece(new ChessPosition(row, col));
                    if (chessPiece == null) {
                        output.append(" ");
                    } else {
                        output.append(chessPiece.getTeamColor() == TeamColor.WHITE ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
                        switch(chessPiece.getPieceType()) {
                            case BISHOP: output.append("B"); break;
                            case KING: output.append("K"); break;
                            case KNIGHT: output.append("N"); break;
                            case PAWN: output.append("P"); break;
                            case QUEEN: output.append("Q"); break;
                            case ROOK: output.append("R"); break;                  
                        }
                    }
                    output.append(" ");
                }
            }
            // Row number
            output.append(String.format("%s%s %s \n", SET_BG_COLOR_DARK_GREY, SET_TEXT_COLOR_BLACK,
                row != 0 && row != ChessBoard.getBoardSize() + 1 ? Integer.toString(row) : " "
            ));
        }
        return output.toString();
    }

}
