package service;

import java.util.Objects;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.request.CreateGameRequest;
import service.request.JoinGameRequest;
import service.result.CreateGameResult;
import service.result.ListGamesResult;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest createGameRequest) throws DataAccessException {
        // Check token
        authDAO.getAuth(authToken);
        // Create game
        return new CreateGameResult(gameDAO.createGame(new GameData(
            0, null, null, createGameRequest.gameName(), new ChessGame()
        )));
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        // Check token
        authDAO.getAuth(authToken);
        // List games
        return new ListGamesResult(gameDAO.listGames());
    }

    public void joinGame(String authToken, JoinGameRequest joinGameRequest) throws DataAccessException {
        // Check token
        AuthData authData = authDAO.getAuth(authToken);
        // Get game
        GameData gameData = gameDAO.getGame(joinGameRequest.gameID());
        GameData newGameData = null;
        // Check if color is already taken
        if (Objects.equals(joinGameRequest.playerColor(), "WHITE")) {
            if (gameData.whiteUsername() == null) {
                newGameData = gameData.renameWhite(authData.username());
            } else {
                throw new DataAccessException("Error: already taken");
            }
        } else if (Objects.equals(joinGameRequest.playerColor(), "BLACK")) {
            if (gameData.blackUsername() == null) {
                newGameData = gameData.renameBlack(authData.username());
            } else {
                throw new DataAccessException("Error: already taken");
            }
        } else {
            throw new DataAccessException("Error: bad request");
        }
        gameDAO.updateGame(newGameData);
    }
}
