// ClientHandler.java - Server-side handler for each Hangman client
package csci2020u.finalproject.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

// Handles interaction with a single client over a socket
public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private final Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private StringBuilder sessionLog = new StringBuilder(); // Logs session info
    private String sharedResultFilename; // Log file name
    private final WordBank wordBank = new WordBank();

    // Initialize client I/O
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up I/O: " + e.getMessage());
        }
    }

    // Main handler logic
    @Override
    public void run() {
        try {
            out.println("[SERVER] Welcome to Hangman!");
            out.println("[SERVER] Pick your game option: (1) Single or (2) Multiplayer?");
            out.println(">");
            String mode = in.readLine();
            if (mode == null) return;

            if (mode.trim().equals("1")) {
                sharedResultFilename = "client_shared/result_singleplayer_" + System.currentTimeMillis() + ".txt";
                handleSinglePlayer();
            } else if (mode.trim().equals("2")) {
                sharedResultFilename = "client_shared/result_multiplayer_" + System.currentTimeMillis() + ".txt";
                handleCoopMultiplayer();
            } else {
                out.println("[SERVER] Invalid choice. Closing connection.");
            }
        } catch (IOException e) {
            System.err.println("Client disconnected.");
        } finally {
            flushSessionLog();
            closeConnection();
        }
    }

    // Handles single player game loop
    private void handleSinglePlayer() throws IOException {
        out.println("[SERVER] Starting Single-Player game...");
        String randomWord = wordBank.getRandomWord();
        String hint = wordBank.getHintForWord(randomWord);
        GameState gameState = new GameState(randomWord, hint);

        out.println("[SERVER] I have chosen a word. Try to guess it!");
        out.println("[SERVER] Hint: " + hint);
        out.println("[SERVER] Type a single letter each time, or type 'exit' to quit.");

        while (!gameState.isGameOver()) {
            out.println("[SERVER] Word so far: " + gameState.getDisplayedWord());
            out.println("[SERVER] Attempts left: " + gameState.getAttemptsLeft());
            out.println(">");
            String guess = in.readLine();
            if (guess == null || guess.equalsIgnoreCase("exit")) {
                out.println("[SERVER] Exiting Single-Player. Goodbye!");
                return;
            }
            handleGuess(gameState, guess);
        }

        endGameResult(gameState, "SinglePlayer");
    }

    // Handles co-op multiplayer game flow
    private void handleCoopMultiplayer() throws IOException {
        out.println("[SERVER] Starting Co-op Multi-Player game...");
        out.println("[SERVER] Name of player 1:");
        out.println(">");
        String player1 = in.readLine();

        out.println("[SERVER] Name of player 2:");
        out.println(">");
        String player2 = in.readLine();

        if (player1 == null || player2 == null) {
            out.println("[SERVER] Missing player names. Closing connection.");
            return;
        }

        boolean keepPlaying = true;
        int round = 1;

        while (keepPlaying) {
            out.println("[SERVER] ROUND " + round + " - " + player1 + "'s turn to guess!");
            playCoopRound(player1);

            out.println("[SERVER] ROUND " + round + " - " + player2 + "'s turn to guess!");
            playCoopRound(player2);

            round++;
            out.println("[SERVER] Do you want to continue playing another round? (Y or N)");
            out.println(">");
            String ans = in.readLine();
            if (ans == null || ans.trim().equalsIgnoreCase("n")) {
                keepPlaying = false;
            }
        }

        out.println("[SERVER] Thanks for playing Co-op Multiplayer Hangman!");
    }

    // Plays one round for a single player in co-op
    private void playCoopRound(String playerName) throws IOException {
        String randomWord = wordBank.getRandomWord();
        String hint = wordBank.getHintForWord(randomWord);
        GameState gameState = new GameState(randomWord, hint);

        out.println("[SERVER] Word has " + randomWord.length() + " letters.");
        out.println("[SERVER] Hint: " + hint);
        out.println("[SERVER] Type a single letter each time, or 'exit' to quit.");

        while (!gameState.isGameOver()) {
            out.println("[SERVER] Word so far: " + gameState.getDisplayedWord());
            out.println("[SERVER] Attempts left: " + gameState.getAttemptsLeft());
            out.println("[SERVER] " + playerName + ", enter your guess:");
            out.println(">");
            String guess = in.readLine();
            if (guess == null || guess.equalsIgnoreCase("exit")) {
                out.println("[SERVER] Exiting this round.");
                return;
            }
            handleGuess(gameState, guess);
        }

        endGameResult(gameState, playerName);
    }

    // Validates and processes player guess input
    private void handleGuess(GameState gameState, String guess) {
        if (guess.length() == 1 && Character.isLetter(guess.charAt(0))) {
            String result = gameState.guess(guess.charAt(0));
            out.println("[SERVER] " + result);
        } else {
            out.println("[SERVER] Please enter a single valid letter.");
        }
    }

    // Finalize results for a player and log it
    private void endGameResult(GameState gameState, String playerName) {
        boolean won = gameState.isWon();
        String outcome = won ? "WIN" : "LOSE";

        if (won) {
            out.println("[SERVER]  You guessed the word! It was: " + gameState.getTargetWord());
        } else {
            out.println("[SERVER] You ran out of attempts. The word was: " + gameState.getTargetWord());
        }

        logger.info("[SERVER] Game Over. The word was: " + gameState.getTargetWord());

        sessionLog.append("Player: ").append(playerName).append("\n");
        sessionLog.append("Result: ").append(outcome).append("\n");
        sessionLog.append("Word: ").append(gameState.getTargetWord()).append("\n");
        sessionLog.append("Guessed Letters: ").append(gameState.toString()).append("\n");
        sessionLog.append("Attempts Left: ").append(gameState.getAttemptsLeft()).append("\n");
        sessionLog.append("--------------------------\n");
    }

    // Save session log to file
    private void flushSessionLog() {
        if (sharedResultFilename == null || sessionLog.length() == 0) return;
        try {
            Path folder = Paths.get("client_shared");
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
            Files.writeString(Paths.get(sharedResultFilename), sessionLog.toString());
        } catch (IOException e) {
            System.err.println(" Failed to write session log: " + e.getMessage());
        }
    }

    // Close client socket cleanly
    private void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket.");
        }
    }
}