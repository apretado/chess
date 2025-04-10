package client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import exception.ResponseException;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class WebSocketFacade extends Endpoint {
    Repl repl;
    Session session;
    Gson serializer = createSerializer();
    Gson gson = new Gson();
    
    public WebSocketFacade(String url, Repl repl)  {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.repl = repl;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String> () {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = serializer.fromJson(message, ServerMessage.class);
                    repl.notify(notification);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException e) {
            repl.say("Error connecting to websocket");
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        // Required method left intentionally blank
    }
    
    private static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(ServerMessage.class,
            (JsonDeserializer<ServerMessage>) (el, type, ctx) -> {
                ServerMessage message = null;
                if (el.isJsonObject()) {
                    String serverMessageType = el.getAsJsonObject().get("serverMessageType").getAsString();
                    switch(ServerMessage.ServerMessageType.valueOf(serverMessageType)) {
                        case ERROR -> message = ctx.deserialize(el, ErrorMessage.class);
                        case LOAD_GAME -> message = ctx.deserialize(el, LoadGameMessage.class);
                        case NOTIFICATION -> message = ctx.deserialize(el, NotificationMessage.class);
                    }
                }
                return message;
            });
        
        return gsonBuilder.create();
    }

    public void connect(ConnectCommand command) throws ResponseException {
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException e) {
            throw new ResponseException(500, "Error: could not connect to game");
        }
    }

    public void makeMove(MakeMoveCommand command) throws ResponseException {
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException e) {
            throw new ResponseException(500, "Error: could not make move");
        }
    }

    public void leave(LeaveGameCommand command) throws ResponseException {
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException e) {
            throw new ResponseException(500, "Error: could not leave game");
        }
    }

    public void resign(ResignCommand command) throws ResponseException {
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException e) {
            throw new ResponseException(500, "Error: could not resign");
        }
    }
}
