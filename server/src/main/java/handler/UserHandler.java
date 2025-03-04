package handler;

import com.google.gson.Gson;

import dataaccess.DataAccessException;
import service.UserService;
import service.request.LoginRequest;
import service.request.RegisterRequest;
import service.result.ErrorResult;
import service.result.LoginResult;
import service.result.RegisterResult;
import spark.Request;
import spark.Response;

public class UserHandler {
    private final UserService userService;
    private final Gson gson;
    private final ErrorHandler errorHandler;

    public UserHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
        this.errorHandler = new ErrorHandler(gson);
    }

    public Object handleRegister(Request req, Response res) {
        // Parse json
        RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
        // Check if username, password, or email are null
        if (registerRequest.username() == null || registerRequest.password() == null || registerRequest.email() == null) {
            res.status(400);
            return gson.toJson(new ErrorResult("Error: bad request"));
        }
        // Try to register
        try {
            RegisterResult registerResult = this.userService.register(registerRequest);
            res.status(200);
            return gson.toJson(registerResult);
        } catch (DataAccessException e) {
            return errorHandler.handleError(e, res);
        }
    }

    public Object handleLogin(Request req, Response res) {
        // Parse json
        LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);
        // Try to login
        try {
            LoginResult loginResult = this.userService.login(loginRequest);
            res.status(200);
            return gson.toJson(loginResult);
        } catch (DataAccessException e) {
            return errorHandler.handleError(e, res);
        }
    }

    public Object handleLogout(Request req, Response res) {
        // Get authToken
        String authToken = req.headers("authorization");
        // Try to logout
        try {
            this.userService.logout(authToken);
            res.status(200);
            res.type("application/json");
            return "{}";
        } catch (DataAccessException e) {
            return errorHandler.handleError(e, res);
        }
    }
}
