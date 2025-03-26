package client;

import java.util.Scanner;

import server.ServerFacade;

import static ui.EscapeSequences.*;

public class Repl {
    private final PreloginClient preloginClient;
    private final PostloginClient postloginClient;
    private Client client;
    private State state = State.LOGGED_OUT;

    public Repl(String serverUrl) {
        ServerFacade server = new ServerFacade(serverUrl);
        preloginClient = new PreloginClient(server, this);
        postloginClient = new PostloginClient(server, this);
        client = preloginClient;
    }

    public void run()  {
        System.out.println("Welcome to 240 chess. Type Help to get started.");
        System.out.println(client.help());

        try (Scanner scanner = new Scanner(System.in)) {
            String result = "";
            while (!result.equals("quit")) {
                System.out.printf("\n[%s]>>>> ", state);
                String line = scanner.nextLine();

                try {
                    result = client.eval(line);
                    System.out.print(result);
                } catch (Throwable e) {
                    System.out.print(e.toString());
                }
            }
        }
        System.out.println();
    }

    public void setState(State newState) {
        this.state = newState;
        switch(state) {
            case LOGGED_OUT: client = preloginClient; break;
            case LOGGED_IN: client = postloginClient; break;
        }
    }
}
