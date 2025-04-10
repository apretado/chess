package websocket;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import chess.ChessGame.TeamColor;
import dataaccess.UserDAO;
import model.GameData;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;
import dataaccess.MySqlUserDAO;

import service.GameService;
import service.UserService;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
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
        try {
            UserGameCommand command = serializer.fromJson(message, UserGameCommand.class);
      
            // Throws a custom UnauthorizedException. Yours may work differently.
            //   String username = getUsername(command.getAuthString());
            String username = new MySqlAuthDAO().getAuth(command.getAuthToken()).username();
      
        //   saveSession(command.getGameID(), session);
      
            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, (ConnectCommand) command);
                case MAKE_MOVE -> makeMove(session, username, (MakeMoveCommand) command);
                case LEAVE -> leaveGame(session, username, (LeaveGameCommand) command);
                case RESIGN -> resign(session, username, (ResignCommand) command);
            }
        } catch (DataAccessException ex) {
            // Serializes and sends the error message
            sendMessage(session.getRemote(), new ErrorMessage("Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session.getRemote(), new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    // private void saveSession(int gameID, Session session) {
        
    // }

    private void sendMessage(RemoteEndpoint remote, ServerMessage serverMessage) throws IOException {
        remote.sendString(gson.toJson(serverMessage));
    }

    private void connect(Session session, String username, ConnectCommand command) throws IOException, DataAccessException {
        // how to know which game to join?
        // The WS command doesn't contain the color
        // The join game endpoint is called separately with the color

        connections.add(command.getAuthToken(), session, command.getGameID());

        // 1. Server sends a LOAD_GAME message back to the root client.
        GameData gameData = gameDAO.getGame(command.getGameID());
        sendMessage(session.getRemote(), new LoadGameMessage(gameData.game()));

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
    }

    private void makeMove(Session session, String username, MakeMoveCommand command) throws DataAccessException, IOException {
        // 1. Server verifies validity of move
        GameData gameData = gameDAO.getGame(command.getGameID());
        ChessGame game = gameData.game();

        TeamColor colorTurn = game.getTeamTurn();
        String userTurn = colorTurn == TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername();

        ChessMove move = command.getMove();
        ChessPosition startPosition = move.getStartPosition();
        TeamColor pieceColor = game.getBoard().getPiece(startPosition).getTeamColor();

        if (
            !Objects.equals(username, userTurn) // Check if it's the user's turn
            || colorTurn != pieceColor // Check if they are moving their own piece
        ) {
            sendMessage(session.getRemote(), new ErrorMessage("Invalid move"));
            return;
        }

        // 2. Game is updated to represent the move.  Game is updated in the database.
        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            sendMessage(session.getRemote(), new ErrorMessage("Invalid move"));
            return;
        }
        gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game));

        // 3. Server sends a LOAD_GAME message to all clients in the game (including the root client) with an updated game.
        String loadGameMessage = gson.toJson(new LoadGameMessage(game));
        connections.broadcast(command.getGameID(), "", loadGameMessage);

        // 4. Server sends a Notification message to all other clients in that game informing them what move was made.
        String notificationMessage = gson.toJson(new NotificationMessage(username + " made a move"));
        connections.broadcast(command.getGameID(), command.getAuthToken(), notificationMessage);

        // 5. If the move results in check, checkmate or stalemate the server sends a Notification message to all clients.
        TeamColor opponentColor = game.getTeamTurn();
        String opponentName = opponentColor == TeamColor.WHITE ? gameData.whiteUsername() : gameData.blackUsername();
        if (game.isInCheckmate(opponentColor)) {
            String checkmateMessage = gson.toJson(new NotificationMessage(username + " put " + opponentName + " in checkmate"));
            connections.broadcast(-1, "", checkmateMessage);
        } else if (game.isInCheck(opponentColor)) {
            String checkMessage = gson.toJson(new NotificationMessage(username + " put " + opponentName + " in check"));
            connections.broadcast(-1, "", checkMessage);
        } else if (game.isInStalemate(opponentColor)) {
            String stalemateMessage = gson.toJson(new NotificationMessage(username + " put " + opponentName + " in stalemate"));
            connections.broadcast(-1, "", stalemateMessage);
        }
    }

    private void leaveGame(Session session, String username, LeaveGameCommand command) {
        throw new UnsupportedOperationException("Unimplemented method 'leaveGame'");
    }

    private void resign(Session session, String username, ResignCommand command) {
        throw new UnsupportedOperationException("Unimplemented method 'resign'");
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
}
