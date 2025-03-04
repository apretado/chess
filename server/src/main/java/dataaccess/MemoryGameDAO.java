package dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.GameData;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(GameData gameData) throws DataAccessException {
        GameData newGame = new GameData(
            nextGameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game()
        );
        games.put(nextGameID, newGame);
        return nextGameID++;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }
        return game;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        if (!games.containsKey(gameData.gameID())) {
            throw new DataAccessException("Error: bad request");
        }
        games.put(gameData.gameID(), gameData);
    }

    @Override
    public void clearGame() {
        games.clear();
        nextGameID = 1;
    }

}
