package client;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessPiece.PieceType;
import exception.ResponseException;
import websocket.commands.LeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;

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
            // case "highlight" -> highlight(params);
            default -> help();
        };
    }

    private String redraw() {
        return BoardProcesser.makeString(super.repl.getGameData().game().getBoard(), super.repl.getTeamColor());
    }

    private String move(String... params) throws ResponseException {
        if (params.length >= 2) {
            // Convert string coordinates to ChessMove
            String promotion = null;
            if (params.length >= 3) {
                promotion = params[3];
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
        webSocket.resign(new ResignCommand(super.repl.getAuthToken(), super.repl.getGameData().gameID()));
        return "Resigning...";
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

    private static ChessMove coordsToChessMove(String start, String end, String promotion) throws ResponseException {
        if (start.length() != 2 || end.length() != 2) {
            throw new ResponseException(400, "Expected: <START_POSITION> <END_POSITION> [PROMOTION_PIECE]");
        }

        ChessPosition startPosition = new ChessPosition(numToInt(start.charAt(1)), letterToInt(start.charAt(0)));
        ChessPosition endPosition = new ChessPosition(numToInt(end.charAt(1)), letterToInt(end.charAt(0)));
        ChessPiece.PieceType pieceType = stringToPieceType(promotion);
        return new ChessMove(startPosition, endPosition, pieceType);
    }
}
