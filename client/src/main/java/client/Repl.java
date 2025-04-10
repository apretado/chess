package client;

import java.util.Scanner;

import chess.ChessGame;
import chess.ChessGame.TeamColor;
import model.GameData;
import server.ServerFacade;
import websocket.messages.*;

public class Repl {
    private final PreloginClient preloginClient;
    private final PostloginClient postloginClient;
    private final GameplayClient gameplayClient;
    private Client client;
    private State state = State.LOGGED_OUT;

    private GameData gameData = null;
    private String authToken = null;
    private TeamColor teamColor = null;
    private WebSocketFacade webSocket;

    public Repl(String serverUrl) {
        ServerFacade server = new ServerFacade(serverUrl);
        preloginClient = new PreloginClient(server, this);

        webSocket = new WebSocketFacade(serverUrl, this);
        postloginClient = new PostloginClient(server, webSocket, this);
        gameplayClient = new GameplayClient(webSocket, this);

        client = preloginClient;
    }

    public void run()  {
        System.out.println("Welcome to 240 chess. Type Help to get started.");
        System.out.println(client.help());

        try (Scanner scanner = new Scanner(System.in)) {
            String result = "";
            while (!result.equals("quit")) {
                System.out.printf("\n[%s]>>>> ", state);
                String line = scanner.nextLine();

                try {
                    result = client.eval(line);
                    System.out.print("\n" + result);
                } catch (Throwable e) {
                    System.out.println(e.toString());
                    System.out.printf("\n[%s]>>>> ", state);
                }
            }
        }
        System.out.println();
    }

    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                ChessGame newGameState = ((LoadGameMessage) message).getGame();
                GameData oldGameData = getGameData();
                setGameData(new GameData(
                    oldGameData.gameID(), oldGameData.whiteUsername(), oldGameData.blackUsername(), oldGameData.gameName(), newGameState)
                );
                redraw();
                break;
            case ERROR:
                System.out.println(((ErrorMessage) message).getErrorMessage());
                break;
            case NOTIFICATION:
                System.out.println(((NotificationMessage) message).getMessage());
                break;
        }
    }

    public void redraw() {
        System.out.println('\n' + BoardProcesser.makeString(gameData.game().getBoard(), teamColor));
        System.out.printf("\n[%s]>>>> ", state);
    }

    public void say(String message) {
        System.out.println("\n" + message);
        System.out.printf("\n[%s]>>>> ", state);
    }

    public void setState(State newState) {
        this.state = newState;
        switch(state) {
            case LOGGED_OUT:
                client = preloginClient;
                break;
            case LOGGED_IN:
                client = postloginClient;
                break;
            case OBSERVING:
            case PLAYING:
                client = gameplayClient;
                break;
        }
    }

    public GameData getGameData() {
        return gameData;
    }

    public void setGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(TeamColor teamColor) {
        this.teamColor = teamColor;
    }
}
