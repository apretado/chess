package client;

import exception.ResponseException;
import websocket.commands.ResignCommand;

public class ConfirmationClient extends PregameClient {
    WebSocketFacade webSocket;

    public ConfirmationClient(WebSocketFacade webSocket, Repl repl) {
            super(repl);
            this.webSocket = webSocket;
        }
    
    @Override
    public String help() {
        return """
            y - resign
            n - stay in game
            """;
    }

    @Override
    protected String handleCommand(String command, String[] params) throws ResponseException {
        switch (command) {
            case "y":
                webSocket.resign(new ResignCommand(super.repl.getAuthToken(), super.repl.getGameData().gameID()));
                repl.setState(State.LOGGED_IN);
                return "You resigned";
            case "n":
                repl.setState(State.PLAYING);
                return "";
            default:
                return help();
        }
    }

}
