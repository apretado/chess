package client;

import java.util.Arrays;

import exception.ResponseException;

public abstract class PregameClient implements Client {
    protected Repl repl;

    public PregameClient(Repl repl) {
        this.repl = repl;
    }

    @Override
    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            // First token
            String command = (tokens.length > 0) ? tokens[0] : "help";
            // Rest of the tokens
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return handleCommand(command, params);
        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    protected abstract String handleCommand(String command, String[] params) throws ResponseException;
}
