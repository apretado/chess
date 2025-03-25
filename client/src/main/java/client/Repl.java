package client;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final Client client;

    public Repl(String serverUrl) {
        client = new PreloginClient(serverUrl);
    }

    public void run()  {
        System.out.println("Welcome to 240 chess. Type Help to get started.");
        System.out.println(client.help());

        try (Scanner scanner = new Scanner(System.in)) {
            String result = "";
            while (!result.equals("quit")) {
                System.out.print("\n>>>> ");
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
}
