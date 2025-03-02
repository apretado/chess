package handler;

import service.ClearService;
import spark.Request;
import spark.Response;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }
    
    public Object handleClear(Request req, Response res) {
        clearService.clear();
        res.status(200);
        res.type("application/json");
        return "{}";
    } 
}
