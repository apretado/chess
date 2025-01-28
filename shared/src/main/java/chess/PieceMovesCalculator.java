package chess;

import java.util.ArrayList;

public class PieceMovesCalculator {
    public static ArrayList<ChessMove> bishop(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        int[][] movements = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        return standardPiece(board, myPosition, myColor, movements);
    }

    public static ArrayList<ChessMove> king(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        int[][] movements = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };
        return kPiece(board, myPosition, myColor, movements);
    }

    public static ArrayList<ChessMove> knight(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        int[][] movements = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
        return kPiece(board, myPosition, myColor, movements);
    }

    public static ArrayList<ChessMove> pawn(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        var moves = new ArrayList<ChessMove>();
        int direction = myColor == ChessGame.TeamColor.WHITE ? 1 : -1;
        int[][] attackMovements = new int[][]{{direction, -1}, {direction, 1}};
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        // Pushing the pawn 1 space
        var newPosition = new ChessPosition(row + direction, col);
        if (inBounds(newPosition) && board.getPiece(newPosition) == null) {
            // Promotion case
            moves.addAll(pawnPromotionMoves(board, myPosition, newPosition));

            // Pushing the pawn 2 spaces
            if ((myColor == ChessGame.TeamColor.WHITE && row == 2) || (myColor == ChessGame.TeamColor.BLACK && row == 7)) {
                newPosition = new ChessPosition(row + 2 * direction, col);
                if (board.getPiece(newPosition) == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }

        // Attacking
        for (int[] movement : attackMovements) {
            newPosition = new ChessPosition(row + movement[0], col + movement[1]);
            if (inBounds(newPosition)) {
                ChessPiece occupyingPiece = board.getPiece(newPosition);
                if (occupyingPiece != null && occupyingPiece.getTeamColor() != myColor) {
                    moves.addAll(pawnPromotionMoves(board, myPosition, newPosition));
                }
            }
        }
        return moves;
    }

    public static ArrayList<ChessMove> pawnPromotionMoves(ChessBoard board, ChessPosition myPosition, ChessPosition newPosition) {
        final ChessPiece.PieceType[] promotionPieces = {
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT
        };

        var moves = new ArrayList<ChessMove>();

        if (newPosition.getRow() == 1 || newPosition.getRow() == ChessBoard.getBoardSize()) {
            for (ChessPiece.PieceType promotionPiece : promotionPieces) {
                moves.add(new ChessMove(myPosition, newPosition, promotionPiece));
            }
        } else {
            moves.add(new ChessMove(myPosition, newPosition, null));
        }

        return moves;
    }

    public static ArrayList<ChessMove> queen(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        int[][] movements = {
                {-1, -1}, {-1, 0}, {-1, 1},
                {0, -1}, {0, 1},
                {1, -1}, {1, 0}, {1, 1}
        };
        return standardPiece(board, myPosition, myColor, movements);
    }

    public static ArrayList<ChessMove> rook(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        int[][] movements = {{-1, 0}, {0, -1}, {0, 1}, {1, 0}};
        return standardPiece(board, myPosition, myColor, movements);
    }

    private static ArrayList<ChessMove> standardPiece(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor, int[][] movements) {
        var moves = new ArrayList<ChessMove>();
        for (int[] movement : movements) {
            int newRow = myPosition.getRow() + movement[0];
            int newCol = myPosition.getColumn() + movement[1];
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            while (inBounds(newPosition)) {
                ChessPiece occupyingPiece = board.getPiece(newPosition);
                if (occupyingPiece == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    newRow += movement[0];
                    newCol += movement[1];
                    newPosition = new ChessPosition(newRow, newCol);
                } else if (occupyingPiece.getTeamColor() != myColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break;
                } else {
                    break;
                }
            }
        }
        return moves;
    }

    private static ArrayList<ChessMove> kPiece(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor, int[][] movements) {
        var moves = new ArrayList<ChessMove>();
        for (int[] movement : movements) {
            int newRow = myPosition.getRow() + movement[0];
            int newCol = myPosition.getColumn() + movement[1];
            ChessPosition newPosition = new ChessPosition(newRow, newCol);
            // Boundary check
            if (!inBounds(newPosition)) {
                continue;
            }
            // Check if the space is taken by a friendly piece
            ChessPiece occupyingPiece = board.getPiece(newPosition);
            if (occupyingPiece != null && occupyingPiece.getTeamColor() == myColor) {
                continue;
            }
            moves.add(new ChessMove(myPosition, newPosition, null));
        }
        return moves;
    }

    public static boolean inBounds(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return row >= 1 && row <= ChessBoard.getBoardSize() && col >= 1 && col <= ChessBoard.getBoardSize();
    }
}
