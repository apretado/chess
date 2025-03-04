package handler;

import com.google.gson.Gson;

import dataaccess.DataAccessException;
import service.GameService;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.CreateGameResult;
import service.result.ListGamesResult;
import spark.Request;
import spark.Response;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson;
    private final ErrorHandler errorHandler;

    public GameHandler(GameService gameService, Gson gson) {
        this.gameService = gameService;
        this.gson = gson;
        this.errorHandler = new ErrorHandler(gson);
    }

    public Object handleCreateGame(Request req, Response res) {
        // Get authToken
        String authToken = req.headers("authorization");
        // Parse json
        CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
        // Try to create game
        try {
            CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest);
            res.status(200);
            return gson.toJson(createGameResult);
        } catch (DataAccessException e) {
            return errorHandler.handleError(e, res);
        }
    }

    public Object handleListGames(Request req, Response res) {
        // Get authToken
        String authToken = req.headers("authorization");
        // Try to list games
        try {
            ListGamesResult listGamesResult = gameService.listGames(authToken);
            res.status(200);
            return gson.toJson(listGamesResult);
        } catch (DataAccessException e) {
            return errorHandler.handleError(e, res);
        }
    }

    public Object handleJoinGame(Request req, Response res) {
        // Get authToken
        String authToken = req.headers("authorization");
        // Parse json
        JoinGameRequest joinGameRequest = gson.fromJson(req.body(), JoinGameRequest.class);
        // Try to join game
        try {
            gameService.joinGame(authToken, joinGameRequest);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            return errorHandler.handleError(e, res);
        }
    }
}
