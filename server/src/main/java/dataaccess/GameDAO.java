package dataaccess;

import java.util.List;

import model.GameData;

public interface GameDAO {
    int createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> listGames() throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    void clearGame() throws DataAccessException;
}
