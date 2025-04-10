package websocket;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.InvalidMoveException;
import chess.ChessGame.TeamColor;
import model.GameData;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;

import websocket.commands.ConnectCommand;
import websocket.commands.LeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import org.eclipse.jetty.websocket.api.Session;


@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson serializer = createSerializer();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Gson gson;

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO, Gson gson) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.gson = gson;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = serializer.fromJson(message, UserGameCommand.class);
        try {
            String username = authDAO.getAuth(command.getAuthToken()).username();
      
            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, (ConnectCommand) command);
                case MAKE_MOVE -> makeMove(session, username, (MakeMoveCommand) command);
                case LEAVE -> leaveGame(session, username, (LeaveGameCommand) command);
                case RESIGN -> resign(session, username, (ResignCommand) command);
            }
        } catch (DataAccessException ex) {
            // Serializes and sends the error message
            sendMessage(session, new ErrorMessage("Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    private void sendMessage(Session session, ServerMessage serverMessage) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(gson.toJson(serverMessage));
        } else {
            connections.remove(session);
        }
    }

    private void connect(Session session, String username, ConnectCommand command) throws IOException {
        try {
            // 1. Server sends a LOAD_GAME message back to the root client.
            GameData gameData = gameDAO.getGame(command.getGameID());
            connections.add(command.getAuthToken(), session, command.getGameID());
            sendMessage(session, new LoadGameMessage(gameData.game()));

            // 2. Server sends a Notification message to all other clients in that game
            // informing them the root client connected to the game,
            // either as a player (in which case their color must be specified) or as an observer.
            String color;
            if (Objects.equals(username, gameData.whiteUsername())){
                color = "white";
            } else if (Objects.equals(username, gameData.blackUsername())) {
                color = "black";
            } else {
                color = "an observer";
            }
            String notificationMessage = gson.toJson(new NotificationMessage(username + " joined the game as " + color));
            connections.broadcast(command.getGameID(), command.getAuthToken(), notificationMessage);
        } catch (DataAccessException e) {
            sendMessage(session, new ErrorMessage("Error: invalid game ID"));
        }
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws IOException {
        // Check if the game is over
        try {
            GameData gameData = gameDAO.getGame(command.getGameID());
            ChessGame game = gameData.game();
            if (game.getIsOver()) {
                sendMessage(session, new ErrorMessage("Error: game is over"));
                return;
            }

            // 1. Server verifies validity of move
            TeamColor colorTurn = game.getTeamTurn();
            String userTurn = colorTurn == TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername();

            ChessMove move = command.getMove();
            ChessPosition startPosition = move.getStartPosition();
            ChessPiece startPiece = game.getBoard().getPiece(startPosition);

            // Check if it's the user's turn
            if (!Objects.equals(username, userTurn)) {
                sendMessage(session, new ErrorMessage(String.format("Error: not your turn. %s (%s) to play.", userTurn, colorTurn.toString())));
                return;
            }

            // Check if they are moving their own piece
            if (
                startPiece == null
                || colorTurn != startPiece.getTeamColor()
            ) {
                sendMessage(session, new ErrorMessage("Error: you can only move your own pieces"));
                return;
            }

            // 2. Game is updated to represent the move.  Game is updated in the database.
            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                sendMessage(session, new ErrorMessage("Invalid move"));
                return;
            }
            gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));

            // 3. Server sends a LOAD_GAME message to all clients in the game (including the root client) with an updated game.
            String loadGameMessage = gson.toJson(new LoadGameMessage(game));
            connections.broadcast(command.getGameID(), "", loadGameMessage);

            // 4. Server sends a Notification message to all other clients in that game informing them what move was made.
            String notificationMessage = gson.toJson(new NotificationMessage(
                username + " moved " + chessPositionToString(move.getEndPosition()) + " to " + chessPositionToString(move.getEndPosition())
            ));
            connections.broadcast(command.getGameID(), command.getAuthToken(), notificationMessage);

            // 5. If the move results in check, checkmate or stalemate the server sends a Notification message to all clients.
            TeamColor opponentColor = game.getTeamTurn();
            String gameState = null;
            if (game.isInCheckmate(opponentColor)) {
                gameState = " in checkmate";
                game.setIsOver(true);
            } else if (game.isInCheck(opponentColor)) {
                gameState = " in check";
            } else if (game.isInStalemate(opponentColor)) {
                gameState = " in stalemate";
                game.setIsOver(true);
            }

            if (gameState != null) {
                String opponentName = opponentColor == TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername();
                String gameStateMessage = gson.toJson(new NotificationMessage(username + " put " + opponentName + gameState));
                connections.broadcast(-1, "", gameStateMessage);
                gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));
            }
        } catch (DataAccessException e) {
            sendMessage(session, new ErrorMessage("Error: invalid game ID"));
        }
    }

    private void leaveGame(Session session, String username, LeaveGameCommand command) throws IOException {
        try {
            // 1. If a player is leaving, then the game is updated to remove the root client. Game is updated in the database.
            GameData gameData = gameDAO.getGame(command.getGameID());
            if (Objects.equals(username, gameData.whiteUsername())) {
                gameData = gameData.renameWhite(null);
            } else if (Objects.equals(username, gameData.blackUsername())) {
                gameData = gameData.renameBlack(null);
            }
            gameDAO.updateGame(gameData);

            connections.remove(session);

            // 1. Server sends a Notification message to all other clients in that game informing them that the root client left.
            // This applies to both players and observers.
            String leaveMessage = gson.toJson(new NotificationMessage(username + " left the game"));
            connections.broadcast(command.getGameID(), command.getAuthToken(), leaveMessage);
        } catch (DataAccessException e) {
            sendMessage(session, new ErrorMessage("Error: invalid game ID"));
        }
    }

    private void resign(Session session, String username, ResignCommand command) throws IOException {
        try {
            // Check if game is already over
            GameData gameData = gameDAO.getGame(command.getGameID());
            ChessGame game = gameData.game();

            if (game.getIsOver()) {
                sendMessage(session, new ErrorMessage("Error: game is over"));
                return;
            }

            // Check if player
            if (!Objects.equals(username, gameData.whiteUsername()) && !Objects.equals(username, gameData.blackUsername())) {
                sendMessage(session, new ErrorMessage("Error: unable to resign as observer"));
                return;
            }

            // 1. Server marks the game as over (no more moves can be made). Game is updated in the database.
            game.setIsOver(true);
            gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));

            // 2. Server sends a Notification message to all clients in that game informing them that the root client resigned.
            // This applies to both players and observers.
            String resignMessage = gson.toJson(new NotificationMessage(username + " resigned"));
            connections.broadcast(command.getGameID(), "", resignMessage);
        } catch (DataAccessException e) {
            sendMessage(session, new ErrorMessage("Error: invalid game ID"));
        }
    }

    private static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(UserGameCommand.class,
            (JsonDeserializer<UserGameCommand>) (el, type, ctx) -> {
                UserGameCommand command = null;
                if (el.isJsonObject()) {
                    String commandType = el.getAsJsonObject().get("commandType").getAsString();
                    switch(UserGameCommand.CommandType.valueOf(commandType)) {
                        case CONNECT -> command = ctx.deserialize(el, ConnectCommand.class);
                        case LEAVE -> command = ctx.deserialize(el, LeaveGameCommand.class);
                        case MAKE_MOVE -> command = ctx.deserialize(el, MakeMoveCommand.class);
                        case RESIGN -> command = ctx.deserialize(el, ResignCommand.class);
                    }
                }
                return command;
            });
        
        return gsonBuilder.create();
    }

    private static String chessPositionToString(ChessPosition position) {
        String output = String.valueOf((char) ('a' + position.getColumn() - 1));
        output += String.valueOf(position.getRow());
        return output;
    }
}
