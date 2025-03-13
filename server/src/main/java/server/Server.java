package server;

import com.google.gson.Gson;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;

import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;

import dataaccess.MySqlUserDAO;
import dataaccess.MySqlAuthDAO;
import dataaccess.MySqlGameDAO;

import service.ClearService;
import service.UserService;
import service.GameService;

import handler.ClearHandler;
import handler.UserHandler;
import handler.GameHandler;

import spark.*;

public class Server {
    private final Gson gson;

    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;

    private final ClearHandler clearHandler;
    private final UserHandler userHandler;
    private final GameHandler gameHandler;

    public Server() {
        gson = new Gson();
        
        try {
            userDAO = new MySqlUserDAO();
            authDAO = new MySqlAuthDAO();
            gameDAO = new MySqlGameDAO();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize DAOs: " + e.getMessage());
        }

        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(authDAO, gameDAO);

        clearHandler = new ClearHandler(clearService);
        userHandler = new UserHandler(userService, gson);
        gameHandler = new GameHandler(gameService, gson);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", (req, res) -> clearHandler.handleClear(req, res));

        Spark.post("/user", (req, res) -> userHandler.handleRegister(req, res));
        Spark.post("/session", (req, res) -> userHandler.handleLogin(req, res));
        Spark.delete("/session", (req, res) -> userHandler.handleLogout(req, res));

        Spark.get("/game", (req, res) -> gameHandler.handleListGames(req, res));
        Spark.post("/game", (req, res) -> gameHandler.handleCreateGame(req, res));
        Spark.put("/game", (req, res) -> gameHandler.handleJoinGame(req, res));

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
