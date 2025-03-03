package handler;

import java.util.Objects;

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

    public UserHandler(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
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
            return errorHandler(e, res);
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
            return errorHandler(e, res);
        }
    }

    public Object errorHandler(Exception e, Response res) {
        ErrorResult errorResult = new ErrorResult(e.getMessage());
        if (Objects.equals(e.getMessage(), "Error: already taken")){
            res.status(403);
        } else if (Objects.equals(e.getMessage(), "Error: bad request")) {
            res.status(400);
        } else if (Objects.equals(e.getMessage(), "Error: unauthorized")) {
            res.status(401);
        } else {
            res.status(500);
        }
        return gson.toJson(errorResult);
    }
}
