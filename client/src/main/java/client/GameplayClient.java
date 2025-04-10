package client;

import java.util.Collection;
import java.util.HashSet;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessPiece.PieceType;
import exception.ResponseException;
import websocket.commands.LeaveGameCommand;
import websocket.commands.MakeMoveCommand;

public class GameplayClient extends PregameClient {
    WebSocketFacade webSocket;

    public GameplayClient(WebSocketFacade webSocket, Repl repl) {
        super(repl);
        this.webSocket = webSocket;
    }
    
    @Override
    public String help() {
        return """
                redraw - the board
                leave - the game
                move <START_POSITION> <END_POSITION> [PROMOTION_PIECE] - a piece
                resign - the game
                highlight <COORDINATE> - legal moves
                help - with possible commands
                """;
    }

    @Override
    protected String handleCommand(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "help" -> help();
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "move" -> move(params);
            case "resign" -> resign();
            case "highlight" -> highlight(params);
            default -> help();
        };
    }

    private String highlight(String... params) throws ResponseException {
        if (params.length >= 1) {
            ChessPosition startPosition = coordsToPosition(params[0]);
            Collection<ChessMove> validMoves = super.repl.getGameData().game().validMoves(startPosition);
            HashSet<ChessPosition> highlightPositions = new HashSet<>();
            if (validMoves != null) {
                for (ChessMove move : validMoves) {
                    highlightPositions.add(move.getEndPosition());
                }
            }
            return BoardProcesser.makeStringHighlight(
                super.repl.getGameData().game().getBoard(), super.repl.getTeamColor(), startPosition, highlightPositions
            );
        }
        throw new ResponseException(400, "Expected: <COORDINATE>");
    }

    private String redraw() {
        return BoardProcesser.makeString(super.repl.getGameData().game().getBoard(), super.repl.getTeamColor());
    }

    private String move(String... params) throws ResponseException {
        if (params.length >= 2) {
            // Convert string coordinates to ChessMove
            String promotion = null;
            if (params.length >= 3) {
                promotion = params[2];
            }
            ChessMove chessMove = coordsToChessMove(params[0], params[1], promotion);
            // Send move over websocket
            webSocket.makeMove(new MakeMoveCommand(super.repl.getAuthToken(), super.repl.getGameData().gameID(), chessMove));
            return "Making move... ";
        }
        throw new ResponseException(400, "Expected: <START_POSITION> <END_POSITION> [PROMOTION_PIECE]");
    }

    private String leave() throws ResponseException {
        webSocket.leave(new LeaveGameCommand(super.repl.getAuthToken(), super.repl.getGameData().gameID()));
        super.repl.setState(State.LOGGED_IN);
        return "Leaving game...";
    }

    private String resign() throws ResponseException {
        super.repl.setState(State.CONFIRMING);
        return "\nAre you sure you want to quit? [y/n] >>>> ";
    }

    private static int letterToInt(char letter) throws ResponseException {
        if (letter >= 'a' && letter <= 'h') {
            return letter - 'a' + 1;
        }
        throw new ResponseException(400, "Invalid position");
    }

    private static int numToInt(char num) throws ResponseException {
        if (num >= '1' && num <= 'h') {
            return num - '0';
        }
        throw new ResponseException(400, "Invalid position");
    }

    private static ChessPiece.PieceType stringToPieceType(String promotion) throws ResponseException {
        return switch(promotion) {
            case null -> null;
            case "king" -> PieceType.KING;
            case "queen" -> PieceType.QUEEN;
            case "bishop" -> PieceType.KNIGHT;
            case "rook" -> PieceType.ROOK;
            default -> throw new ResponseException(400, "Invalid pawn promotion piece type");
        };
    }

    private static ChessPosition coordsToPosition(String coords) throws ResponseException {
        if (coords.length() != 2) {
            throw new ResponseException(400, "Invalid coordinates");
        }
        return new ChessPosition(numToInt(coords.charAt(1)), letterToInt(coords.charAt(0)));
    }

    private static ChessMove coordsToChessMove(String start, String end, String promotion) throws ResponseException {
        return new ChessMove(coordsToPosition(start), coordsToPosition(end), stringToPieceType(promotion));
    }
}
