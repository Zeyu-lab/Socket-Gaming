// Client.java
package csci2020u.finalproject.cilent;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class Cilent {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner consoleScanner;

    //This constructor is where I set everything up:
    //I connect to the server, set up input/output streams,
    //create the shared folder if needed, and launch a thread to listen to the server.
    public Cilent() throws IOException {
        socket = new Socket("localhost", 9090);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        consoleScanner = new Scanner(System.in);

        //This ensures that the 'client_shared' folder exists — I use this to store game results.
        Path localFolder = Paths.get("client_shared");
        if (!Files.exists(localFolder)) {
            Files.createDirectories(localFolder);
        }

        //This background thread keeps listening for any messages from the server.
        //If the message starts with [SERVER-END]SCOREBOARD, I parse it and save the score.
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("[SERVER-END]SCOREBOARD:")) {
                        handleScoreboardLine(line);
                    } else {
                        System.out.println(line); //Otherwise, just print the server message.
                    }
                }
            } catch (IOException e) {
                System.err.println("Connection closed by server.");
            }
        }).start();
    }

    //This method handles special scoreboard messages from the server.
    //It saves the final score of both players into a local text file for records.
    private void handleScoreboardLine(String line) {
        try {
            String[] parts = line.split(":");
            if (parts.length == 5) {
                String player1 = parts[1];
                String score1 = parts[2];
                String player2 = parts[3];
                String score2 = parts[4];

                String result = "Multi-Player Hangman Final Score\n"
                        + player1 + ": " + score1 + "\n"
                        + player2 + ": " + score2 + "\n";

                String filename = "client_shared/score_" + System.currentTimeMillis() + ".txt";
                Files.writeString(Paths.get(filename), result);
                System.out.println("[CLIENT] Final scores saved to: " + filename);
            }
        } catch (Exception e) {
            System.err.println("Error parsing scoreboard line.");
        }
    }

    //This method handles the core interaction loop with the server.
    //I take user input from the console, send it to the server, and allow exiting by typing 'exit'.
    public void startInteraction() {
        while (true) {
            if (!socket.isConnected() || socket.isClosed()) {
                System.out.println("[CLIENT] Disconnected from server. Exiting...");
                break;
            }

            System.out.print("> ");
            String input = consoleScanner.nextLine();
            System.out.println("[CLIENT DEBUG] Sending: '" + input + "'");
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("[CLIENT] Closing connection...");
                break;
            }
            out.println(input);
        }

        close(); //Once we’re done, I make sure to clean up everything.
    }

    //This method safely closes the socket and scanner when we’re done.
    //It’s good practice to clean up resources properly like this.
    private void close() {
        try {
            socket.close();
            consoleScanner.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }

    //This is where the client app starts running.
    //I create a new Client and start the user interaction.
    public static void main(String[] args) {
        try {
            Cilent client = new Cilent();
            client.startInteraction();
        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }
}