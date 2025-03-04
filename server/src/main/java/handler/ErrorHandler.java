package handler;

import java.util.Objects;

import com.google.gson.Gson;

import service.result.ErrorResult;
import spark.Response;

public class ErrorHandler {
    private final Gson gson;

    public ErrorHandler(Gson gson) {
        this.gson = gson;
    }

    public Object handleError(Exception e, Response res) {
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
