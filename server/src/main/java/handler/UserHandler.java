package handler;

import java.util.Objects;

import com.google.gson.Gson;

import service.UserService;
import service.exception.UsernameTakenException;
import service.request.RegisterRequest;
import service.result.ErrorResult;
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
        RegisterRequest registerRequest = gson.fromJson(req.body(), RegisterRequest.class);
        try {
            RegisterResult registerResult = this.userService.register(registerRequest);
            res.status(200);
            return gson.toJson(registerResult);
        } catch (UsernameTakenException e) {
            return errorHandler(e, res);
        }
    }

    public Object errorHandler(Exception e, Response res) {
        ErrorResult errorResult = new ErrorResult(e.getMessage());
        if (Objects.equals(e.getMessage(), "Error: already taken")){
            res.status(403);
        }
        return gson.toJson(errorResult);
    }
}
