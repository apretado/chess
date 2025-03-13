package dataaccess;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import chess.ChessGame;
import model.GameData;

public class MySqlGameDAO implements GameDAO {

    private final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS games (
            `game_id` INT NOT NULL AUTO_INCREMENT,
            `white_username` varchar(256) DEFAULT NULL,
            `black_username` varchar(256) DEFAULT NULL,
            `game_name` varchar(256) NOT NULL,
            `game_state` TEXT NOT NULL,
            PRIMARY KEY (`game_id`)
        )  
        """
    };

    public MySqlGameDAO() throws DataAccessException {
        DatabaseInitializer.initializeTable(createStatements);
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            // Try to add user to database
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO games (game_name, game_state) VALUES (?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
                )) {
                preparedStatement.setString(1, game.gameName());
                preparedStatement.setString(2, new Gson().toJson(game.game()));
                preparedStatement.executeUpdate();

                try (var rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        // Return game id
                        return rs.getInt(1);
                    } else {
                        throw new DataAccessException("Error getting game ID after inserting game into database");
                    }
                }
            }
        } catch (SQLException e) {
            // Error code for "Column cannot be null"
            if (e.getErrorCode() == 1048) { 
                throw new DataAccessException("Error: game name cannot be null");
            }
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                "SELECT game_id, white_username, black_username, game_name, game_state FROM games WHERE game_id = ?"
            )) {
                preparedStatement.setInt(1, gameID);
                try (var rs = preparedStatement.executeQuery()) {
                    if (rs.next()) {
                        ChessGame chessGame = new Gson().fromJson(rs.getString("game_state"), ChessGame.class);
                        return new GameData(
                            rs.getInt("game_id"),
                            rs.getString("white_username"),
                            rs.getString("black_username"),
                            rs.getString("game_name"),
                            chessGame
                        );
                    } else {
                        throw new DataAccessException("Error: bad request");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                "SELECT game_id, white_username, black_username, game_name, game_state FROM games"
            )) {
                try (var rs = preparedStatement.executeQuery()) {
                    List<GameData> gameList = new ArrayList<>();
                    while (rs.next()) {
                        ChessGame chessGame = new Gson().fromJson(rs.getString("game_state"), ChessGame.class);
                        gameList.add(new GameData(
                            rs.getInt("game_id"),
                            rs.getString("white_username"),
                            rs.getString("black_username"),
                            rs.getString("game_name"),
                            chessGame
                        ));
                    }
                    return gameList;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                "UPDATE games SET white_username = ?, black_username = ?, game_name = ?, game_state = ? WHERE game_id = ?"
            )) {
                preparedStatement.setString(1, game.whiteUsername());
                preparedStatement.setString(2, game.blackUsername());
                preparedStatement.setString(3, game.gameName());
                preparedStatement.setString(4, new Gson().toJson(game.game()));
                preparedStatement.setInt(5, game.gameID());
                
                int rowsAffected = preparedStatement.executeUpdate();

                // Check if no game was updated
                if (rowsAffected == 0) {
                    throw new DataAccessException("Error: bad request");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public void clearGame() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE games")) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }

}
