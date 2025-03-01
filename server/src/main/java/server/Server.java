package server;

import spark.*;

public class Server {
    // private ClearHandler clearHandler;

    public Server() {
        // clearHandler = new ClearHandler()
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        // Spark.delete("/db", (req, res) -> clearHandler.handleClear(req, res));
        // Spark.post("/user", (req, res) -> userHandler.handleRegister(req, res));

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
