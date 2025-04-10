package client;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

import java.util.HashSet;
import java.util.Objects;

public class BoardProcesser {
    public static String makeStringHighlight(
        ChessBoard chessBoard, TeamColor color, ChessPosition startPosition, HashSet<ChessPosition> highlightPositions
    ) {

        // Set direction based upon color
        int rowStart = ChessBoard.getBoardSize() + 1;
        int rowEnd = -1;
        int rowIncrement = -1;

        int colStart = 1;
        int colEnd = ChessBoard.getBoardSize() + 1;
        int colIncrement = 1;
        
        if (color == TeamColor.BLACK) {
            rowStart = 0;
            rowEnd = ChessBoard.getBoardSize() + 2;
            rowIncrement = 1;

            colStart = ChessBoard.getBoardSize();
            colEnd = 0;
            colIncrement = -1;
        }

        StringBuilder output = new StringBuilder();
        for (int row = rowStart; row != rowEnd; row += rowIncrement) {
            // Row number
            output.append(String.format(" %s%s%s ", SET_BG_COLOR_DARK_GREY, SET_TEXT_COLOR_BLACK,
                row != 0 && row != ChessBoard.getBoardSize() + 1 ? Integer.toString(row) : " "
            ));
            for (int col = colStart; col != colEnd; col += colIncrement) {
                if (row == 0 || row == ChessBoard.getBoardSize() + 1) {
                    // Column letter
                    output.append(String.format(" %s%s%c ", SET_BG_COLOR_DARK_GREY, SET_TEXT_COLOR_BLACK, (char)(col + 96)));
                    continue;
                }
                // Background color
                String backgroundColor = null;
                ChessPosition currentSquare = new ChessPosition(row, col);
                if (Objects.equals(currentSquare, startPosition)) {
                    backgroundColor = SET_BG_COLOR_YELLOW;
                } else if (highlightPositions != null && highlightPositions.contains(currentSquare)) {
                    backgroundColor = (row - col) % 2 == 0 ? SET_BG_COLOR_DARK_GREEN : SET_BG_COLOR_GREEN;
                } else {
                    backgroundColor = (row - col) % 2 == 0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_WHITE;
                }
                output.append(backgroundColor);
                // Chess piece
                ChessPiece chessPiece = chessBoard.getPiece(new ChessPosition(row, col));
                if (chessPiece == null) {
                    output.append("   ");
                    continue;
                }
                output.append(chessPiece.getTeamColor() == TeamColor.WHITE ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE);
                switch(chessPiece.getPieceType()) {
                    case BISHOP: output.append(" B "); break;
                    case KING: output.append(" K "); break;
                    case KNIGHT: output.append(" N "); break;
                    case PAWN: output.append(" P "); break;
                    case QUEEN: output.append(" Q "); break;
                    case ROOK: output.append(" R "); break;                  
                }
            }
            // Row number
            output.append(String.format("%s%s %s \n", SET_BG_COLOR_DARK_GREY, SET_TEXT_COLOR_BLACK,
                row != 0 && row != ChessBoard.getBoardSize() + 1 ? Integer.toString(row) : " "
            ));
        }
        return output.toString();
    }

    public static String makeString(ChessBoard chessBoard, TeamColor color) {
        return makeStringHighlight(chessBoard, color, null, null);
    }
}
