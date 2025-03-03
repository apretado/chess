package server;

import com.google.gson.Gson;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import handler.ClearHandler;
import handler.UserHandler;
import service.ClearService;
import service.UserService;
import spark.*;

public class Server {
    private final Gson gson;
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ClearService clearService;
    private final UserService userService;
    private final ClearHandler clearHandler;
    private final UserHandler userHandler;

    public Server() {
        gson = new Gson();

        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        clearService = new ClearService(userDAO, authDAO, gameDAO);
        userService = new UserService(userDAO, authDAO);

        clearHandler = new ClearHandler(clearService);
        userHandler = new UserHandler(userService, gson);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", (req, res) -> clearHandler.handleClear(req, res));
        Spark.post("/user", (req, res) -> userHandler.handleRegister(req, res));

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
