package model;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, String game) {
    GameData renameWhite(String newWhiteUsername) {
        return new GameData(gameID, newWhiteUsername, blackUsername, gameName, game);
    }
    GameData renameBlack(String newBlackUsername) {
        return new GameData(gameID, whiteUsername, newBlackUsername, gameName, game);
    }
}
