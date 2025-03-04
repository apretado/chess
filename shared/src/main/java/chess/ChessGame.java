package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((teamTurn == null) ? 0 : teamTurn.hashCode());
        result = prime * result + ((board == null) ? 0 : board.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChessGame other = (ChessGame) obj;
        if (teamTurn != other.teamTurn) {
            return false;
        }
        if (board == null) {
            if (other.board != null)
                return false;
        } else if (!board.equals(other.board))
            return false;
        return true;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        // Store the moved piece and its original position,
        ChessPiece movingPiece = board.getPiece(startPosition);
        if (movingPiece == null) {
            return null;
        }
        var validMoves = new ArrayList<ChessMove>();
        
        // for each move in piece.pieceMoves
        for (ChessMove move : movingPiece.pieceMoves(board, startPosition)) {
            // Simulate moving the piece
            ChessPiece capturedPiece = board.getPiece(move.getEndPosition());
            board.addPiece(startPosition, null);
            board.addPiece(move.getEndPosition(), movingPiece);

            // if not isInCheck then add to list of valid moves
            if (!isInCheck(movingPiece.getTeamColor())) {
                validMoves.add(move);
            }

            // Restore the moved piece and the captured piece to their original positions
            board.addPiece(startPosition, movingPiece);
            board.addPiece(move.getEndPosition(), capturedPiece);
        }
            
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());

        // Check if the move is valid
        if (movingPiece == null) {
            throw new InvalidMoveException("Invalid move (missing piece): " + move);
        }
        if (movingPiece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("Invalid move (wrong team):" + move);
        }
        if (!validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("Invalid move (cannot be in check): " + move);
        }

        // Move the piece
        if (move.getPromotionPiece() != null) {
            movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
        }
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), movingPiece);

        // Switch turns
        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    private ArrayList<ChessPosition> endPositions(TeamColor teamColor) {
        var endPositions = new ArrayList<ChessPosition>();
        // Iterate over each square
        for (int row = 1; row <= ChessBoard.getBoardSize(); row++) {
            for (int col = 1; col <= ChessBoard.getBoardSize(); col++) {
                var position = new ChessPosition(row, col);
                ChessPiece piece = this.getBoard().getPiece(position);
                // Check if its the right color
                if (piece != null && piece.getTeamColor() == teamColor) {
                    var moves = piece.pieceMoves(getBoard(), position);
                    for (ChessMove move : moves) {
                        endPositions.add(move.getEndPosition());
                    }
                }
            }
        }
        return endPositions;
    }

    private ChessPosition getKingPosition(TeamColor teamColor) {
        // Iterate over each square
        for (int row = 1; row <= ChessBoard.getBoardSize(); row++) {
            for (int col = 1; col <= ChessBoard.getBoardSize(); col++) {
                var position = new ChessPosition(row, col);
                ChessPiece piece = this.getBoard().getPiece(position);
                // Check if its the right color and a king
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor enemyColor = teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
        ChessPosition kingPosition = getKingPosition(teamColor);
        // Check every enemy end position to see if its the king's position
        for (ChessPosition enemyEndPosition : endPositions(enemyColor)) {
            if (enemyEndPosition.equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= ChessBoard.getBoardSize(); row++) {
            for (int col = 1; col <= ChessBoard.getBoardSize(); col++) {
                var position = new ChessPosition(row, col);
                ChessPiece piece = this.getBoard().getPiece(position);
                // Check if its the right color & has valid moves
                if (piece != null && piece.getTeamColor() == teamColor && !validMoves(position).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasValidMoves(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }
}
