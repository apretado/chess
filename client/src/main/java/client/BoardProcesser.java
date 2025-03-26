package client;

import chess.ChessBoard;
import chess.ChessGame.TeamColor;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;


public class BoardProcesser {
    public static String makeString(ChessBoard chessBoard, TeamColor color) {
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
                } else {
                    // Background color
                    output.append((row - col) % 2 == 0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_WHITE);
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
