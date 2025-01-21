package chess;

import java.util.ArrayList;

public class PieceMovesCalculator {
    public static ArrayList<ChessMove> bishop(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        var moves = new ArrayList<ChessMove>();
        return moves;
    }

    public static ArrayList<ChessMove> king(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        var moves = new ArrayList<ChessMove>();
        return moves;
    }

    public static ArrayList<ChessMove> knight(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        var moves = new ArrayList<ChessMove>();
        int[][] movements = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
        for (int[] movement : movements) {
            int newRow = myPosition.getRow() + movement[0];
            int newCol = myPosition.getColumn() + movement[1];
            // Boundary check
            if (newRow > ChessBoard.getBoardSize() || newRow < 1) {
                continue;
            }
            if (newCol > ChessBoard.getBoardSize() || newCol < 1) {
                continue;
            }
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            // Check if the space is taken by a friendly piece
            ChessPiece occupyingPiece = board.getPiece(newPosition);
            if (occupyingPiece != null && occupyingPiece.getTeamColor() == pieceColor) {
                continue;
            }
            moves.add(new ChessMove(myPosition, newPosition, null));
        }
        return moves;
    }

    public static ArrayList<ChessMove> pawn(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        var moves = new ArrayList<ChessMove>();
        return moves;
    }

    public static ArrayList<ChessMove> queen(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        var moves = new ArrayList<ChessMove>();
        return moves;
    }

    public static ArrayList<ChessMove> rook(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        var moves = new ArrayList<ChessMove>();
        return moves;
    }
}
