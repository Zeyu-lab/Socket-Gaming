/*
//MainLauncher.java - Full Hangman Game Client
package csci2020u.finalproject.cilent;

import csci2020u.finalproject.server.WordBank;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static csci2020u.finalproject.cilent.MultiplayerLobby.showLobby;
// Entry point for the game
public class MainLauncher {
    public static void main(String[] args) {
        // Launch the game in the Swing event dispatch thread
        SwingUtilities.invokeLater(() -> {
            // Prompt user to select game mode
            Object[] options = {"Single Player", "Multiplayer", "Word Bank Editor"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Choose Game Mode:", "Game Mode",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);
            // Launch respective mode based on user selection
            if (choice == 0) {
                new SinglePlayerMode();
            } else if (choice == 1) {
                String p1 = JOptionPane.showInputDialog("Enter name for Player 1:");
                String p2 = JOptionPane.showInputDialog("Enter name for Player 2:");
                if (p1 == null || p1.isBlank()) p1 = "Player 1";
                if (p2 == null || p2.isBlank()) p2 = "Player 2";
                GameSyncState shared = new GameSyncState(p1, p2);

                showLobby(p1,p2,shared);

                // Word Bank Editor Mode
            } else if (choice == 2) {
                WordBank wordBank = new WordBank();
                while (true) {
                    String newWord = JOptionPane.showInputDialog("Enter a new word:");
                    String newHint = JOptionPane.showInputDialog("Enter a hint for the word:");
                    // Validate input
                    if (newWord == null || newHint == null || newWord.isBlank() || newHint.isBlank() || newHint.contains(newWord)){
                        break;
                    }

                    try (FileWriter writer = new FileWriter("src/main/java/csci2020u/finalproject/wordBank.csv", true)) {
                        writer.write(newWord + "," + newHint + "\n");
                        JOptionPane.showMessageDialog(null, "Word added!");
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Failed to save word.");
                    }
                    int cont = JOptionPane.showConfirmDialog(null,
                            "Add another word?", "Continue?", JOptionPane.YES_NO_OPTION);
                    if (cont != JOptionPane.YES_OPTION) break;
                }
                main(args); // restart after editor
            } else {
                System.exit(0);
            }
        });
    }
}

// Class handling the Single Player mode of Hangman
class SinglePlayerMode {
    // Word bank instance for retrieving words and hints
    private final WordBank wordBank = new WordBank();

    // Target word, masked word for display, guessed letters and words
    private String target;
    private StringBuilder display;
    private Set<Character> guessedLetters = new HashSet<>();
    private Set<String> guessedWords = new HashSet<>();

    // Remaining incorrect attempts
    private int attempts = 6;

    // GUI components
    private JFrame frame;
    private JLabel wordLabel, hintLabel, attemptsLabel, statusLabel, stickMan;
    private JPanel panel, fullPanel;
    private JTextField inputLetter, inputWord;
    private JButton btnLetter, btnWord;

    // Logger for game state (used even in single player)
    private final GameSyncState logState = new GameSyncState("SinglePlayer", "N/A");

    // Constructor initializes the word and GUI
    public SinglePlayerMode() {
        target = wordBank.getRandomWord(); // Get random word
        display = new StringBuilder("_".repeat(target.length())); // Hidden version of word
        buildUI(); // Initialize GUI
    }

    // Constructs the game window and layout
    private void buildUI() {
        frame = new JFrame("Single Player Hangman");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        // Initialize labels with word, hint, and attempts left
        wordLabel = new JLabel("Word: " + format(display), SwingConstants.CENTER);
        hintLabel = new JLabel("Hint: " + wordBank.getHintForWord(target), SwingConstants.CENTER);
        attemptsLabel = new JLabel("Attempts Left: " + attempts, SwingConstants.CENTER);
        statusLabel = new JLabel("Guess a letter", SwingConstants.CENTER);

        // Text fields and buttons for user input
        inputLetter = new JTextField();
        inputWord = new JTextField();
        btnLetter = new JButton("Guess Letter");
        btnWord = new JButton("Guess Word");

        // Load initial hangman image
        try {
            BufferedImage img = ImageIO.read(new File("src/Resources/HangMan6.png"));
            ImageIcon icon = new ImageIcon(img);
            stickMan = new JLabel(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Hook up buttons to guess logic
        btnLetter.addActionListener(e -> guessLetter());
        btnWord.addActionListener(e -> guessWord());

        // Layout panel setup
        fullPanel = new JPanel((new GridLayout(1, 2)));
        panel = new JPanel(new GridLayout(10, 1));
        panel.add(wordLabel);
        panel.add(hintLabel);
        panel.add(attemptsLabel);
        panel.add(inputLetter);
        panel.add(btnLetter);
        panel.add(inputWord);
        panel.add(btnWord);
        panel.add(statusLabel);
        panel.add(stickMan);

        // Add subpanels to frame
        fullPanel.add(panel);
        fullPanel.add(stickMan);

        frame.add(fullPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Handles guessing a single letter
    private void guessLetter() {
        String guess = inputLetter.getText().toLowerCase().trim();
        inputLetter.setText("");
        if (guess.length() != 1 || !Character.isLetter(guess.charAt(0))) {
            statusLabel.setText("Enter one letter.");
            return;
        }
        char c = guess.charAt(0);
        if (guessedLetters.contains(c)) {
            statusLabel.setText("Already guessed.");
            return;
        }

        guessedLetters.add(c);
        boolean correct = false;

        // Reveal all instances of the guessed letter
        for (int i = 0; i < target.length(); i++) {
            if (target.charAt(i) == c) {
                display.setCharAt(i, c);
                correct = true;
            }
        }

        // Penalize for incorrect guess
        if (!correct) attempts--;
        updateUI();
    }

    // Handles full word guess
    private void guessWord() {
        String guess = inputWord.getText().toLowerCase().trim();
        inputWord.setText("");
        if (guess.isEmpty()) {
            statusLabel.setText("Please enter a single word.");
            return;
        }
        if (guessedWords.contains(guess)) {
            statusLabel.setText("Already guessed.");
            return;
        }

        guessedWords.add(guess);

        // If correct, reveal the whole word
        if (guess.equals(target)) {
            display = new StringBuilder(target);
            statusLabel.setText("You win!");
            endRound();
        } else {
            attempts--;
            updateUI();
        }
    }

    // Updates game state and GUI after every guess
    private void updateUI() {
        wordLabel.setText("Word: " + format(display));
        attemptsLabel.setText("Attempts Left: " + attempts);

        // Update hangman image
        try {
            BufferedImage img = ImageIO.read(new File("src/Resources/HangMan" + attempts +".png"));
            ImageIcon icon = new ImageIcon(img);
            stickMan.setIcon(icon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Check win or loss
        if (display.toString().equals(target)) {
            statusLabel.setText("You win!");
            endRound();
        } else if (attempts == 0) {
            statusLabel.setText("You lost! Word: " + target);
            endRound();
        }
    }

    // Ends the game round and prompts restart
    private void endRound() {
        // Disable inputs
        inputLetter.setEnabled(false);
        inputWord.setEnabled(false);
        btnLetter.setEnabled(false);
        btnWord.setEnabled(false);

        // Log game result
        boolean win = display.toString().equals(target);
        logState.addResult("SinglePlayer", target, display.toString(), guessedLetters, guessedWords, attempts, win);
        logState.saveToFile();

        // Ask user to play again
        int again = JOptionPane.showConfirmDialog(frame,
                "Play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (again == JOptionPane.YES_OPTION) {
            frame.dispose();
            new SinglePlayerMode(); // Start a new game
        } else {
            System.exit(0);
        }
    }

    // Formats the display string by adding spaces between characters
    private String format(StringBuilder sb) {
        return sb.toString().replaceAll(".", "$0 ").trim();
    }
}


//SHARED STATE FOR MULTIPLAYER
// Shared game state for multiplayer Hangman mode
class GameSyncState {
    // Flags to track if players are done with the current round
    public volatile boolean p1Done = false, p2Done = false;

    // Flags to manage synchronized decision-making (e.g., play again)
    public volatile boolean decisionMade = false;
    public volatile boolean playAgain = false;

    // Log to keep track of game results for both players
    private final StringBuilder log = new StringBuilder();

    // Tracks the current round and total number of rounds
    private int currentRound = 0;
    private int rounds = 1;

    // Player score mapping: player name → total remaining attempts
    private final Map<String, Integer> playerScores = new HashMap<>();

    // Constructor initializes log header with player names
    public GameSyncState(String p1, String p2) {
        log.append("=== Hangman Multiplayer Log ===\n");
        log.append("Players: ").append(p1).append(" vs ").append(p2).append("\n\n");
    }

    // Mark a player as finished (1 = Player 1, 2 = Player 2)
    public synchronized void setDone(int id) {
        if (id == 1) p1Done = true;
        else p2Done = true;
        notifyAll(); // Wake up any thread waiting for both players to finish
    }

    // Block the thread until both players are finished
    public synchronized void waitForBoth() {
        while (!p1Done || !p2Done) {
            try {
                wait(); // Wait for notification
            } catch (InterruptedException ignored) {}
        }
    }

    // Set the shared play-again decision (called by Player 1)
    public synchronized void setDecision(boolean decision) {
        playAgain = decision;
        decisionMade = true;
        notifyAll(); // Notify Player 2 waiting on this decision
    }

    // Wait until Player 1 makes a play-again decision
    public synchronized void waitForDecision() {
        while (!decisionMade) {
            try {
                wait(); // Wait until a decision is made
            } catch (InterruptedException ignored) {}
        }
    }

    // Reset sync flags for the next round
    public synchronized void resetState() {
        p1Done = false;
        p2Done = false;
        decisionMade = false;
        playAgain = false;
    }

    // Add a player's round result to the game log
    public synchronized void addResult(String name, String word, String shown,
                                       Set<Character> letters, Set<String> words, int attempts, boolean win) {
        log.append(">> ").append(name).append("\n");
        log.append("Word: ").append(word).append("\n");
        log.append("Guessed: ").append(shown).append("\n");
        log.append("Letters: ").append(letters).append("\n");
        log.append("Words: ").append(words).append("\n");
        log.append("Attempts left: ").append(attempts).append("\n");
        log.append("Result: ").append(win ? "Win" : "Lose").append("\n\n");
    }

    // Save the game log to a timestamped text file
    public synchronized void saveToFile() {
        try {
            Path folder = Paths.get("client_shared");
            if (!Files.exists(folder)) Files.createDirectories(folder);
            String filename = "client_shared/match_result_" + System.currentTimeMillis() + ".txt";
            Files.writeString(Paths.get(filename), log.toString());
        } catch (IOException e) {
            System.err.println(" Failed to save log: " + e.getMessage());
        }
    }

    // Add score (remaining attempts) for the given player
    public synchronized void incrementScore(String playerName, int attempts) {
        playerScores.put(playerName, playerScores.getOrDefault(playerName, 0) + attempts);
    }

    // Return a copy of the player scores (to avoid external modification)
    public synchronized Map<String, Integer> getScores() {
        return new HashMap<>(playerScores);
    }

    // Set total number of rounds for the session
    public synchronized void setRounds(int rounds) {
        this.rounds = rounds;
    }

    // Get how many total rounds were set
    public synchronized int getRounds() {
        return rounds;
    }

    // Advance to the next round
    public synchronized void incrementRound() {
        currentRound++;
    }

    // Check if all rounds are completed
    public synchronized boolean isGameOver() {
        return currentRound >= rounds;
    }
}



// MULTIPLAYER WINDOW
// Represents one of the two player's game windows in multiplayer mode
class PlayerWindow {
    // Local map to track scores (not used for syncing)
    private final Map<String, Integer> playerScores = new HashMap<>();

    // Player ID (1 or 2), name, and shared sync state
    private final int playerId;
    private final String name;
    private final GameSyncState shared;

    // Word bank used for selecting word and hint
    private final WordBank wordBank = new WordBank();

    // GUI components
    private JFrame frame;
    private JPanel panel, finalPanel;
    private JLabel wordLabel, hintLabel, attemptsLabel, statusLabel, stickMan;
    private JTextField inputLetter, inputWord;
    private JButton btnLetter, btnWord;

    // Game state variables
    private String target;
    private StringBuilder display;
    private Set<Character> guessedLetters;
    private Set<String> guessedWords;
    private int attempts;

    // Constructor starts the first round immediately
    public PlayerWindow(int id, String name, GameSyncState shared) {
        this.playerId = id;
        this.name = name;
        this.shared = shared;
        startRound();
    }

    // Initializes round state and UI
    private void startRound() {
        target = wordBank.getRandomWord();
        display = new StringBuilder("_".repeat(target.length()));
        guessedLetters = new HashSet<>();
        guessedWords = new HashSet<>();
        attempts = 6;
        createWindow();
    }

    // Sets up the window and GUI components
    private void createWindow() {
        frame = new JFrame("Hangman - " + name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        // Create and initialize labels and buttons
        wordLabel = new JLabel("Word: " + format(display), SwingConstants.CENTER);
        hintLabel = new JLabel("Hint: " + wordBank.getHintForWord(target), SwingConstants.CENTER);
        attemptsLabel = new JLabel("Attempts: " + attempts, SwingConstants.CENTER);
        statusLabel = new JLabel("Your turn", SwingConstants.CENTER);
        inputLetter = new JTextField();
        inputWord = new JTextField();
        btnLetter = new JButton("Guess Letter");
        btnWord = new JButton("Guess Word");

        // Load initial stickman image
        try {
            BufferedImage img = ImageIO.read(new File("src/Resources/HangMan6.png"));
            ImageIcon icon = new ImageIcon(img);
            stickMan = new JLabel(icon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Register guess actions
        btnLetter.addActionListener(e -> guessLetter());
        btnWord.addActionListener(e -> guessWord());

        // Layout organization
        finalPanel = new JPanel(new GridLayout(1, 2));
        panel = new JPanel(new GridLayout(10, 1));
        panel.add(wordLabel);
        panel.add(hintLabel);
        panel.add(attemptsLabel);
        panel.add(inputLetter);
        panel.add(btnLetter);
        panel.add(inputWord);
        panel.add(btnWord);
        panel.add(statusLabel);

        finalPanel.add(panel);
        finalPanel.add(stickMan);
        frame.add(finalPanel);

        // Position window: P1 on left, P2 on right
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (playerId == 1) ? 100 : screenSize.width / 2 + 100;
        int y = screenSize.height / 4;
        frame.setLocation(x, y);

        frame.setVisible(true);
    }

    // Handles letter guess logic
    private void guessLetter() {
        String txt = inputLetter.getText().toLowerCase().trim();
        inputLetter.setText("");
        if (txt.length() != 1 || !Character.isLetter(txt.charAt(0))) {
            statusLabel.setText("Enter one letter.");
            return;
        }
        char c = txt.charAt(0);
        if (guessedLetters.contains(c)) {
            statusLabel.setText("Already guessed.");
            return;
        }
        guessedLetters.add(c);
        boolean correct = false;

        // Reveal correct letters in display
        for (int i = 0; i < target.length(); i++) {
            if (target.charAt(i) == c) {
                display.setCharAt(i, c);
                correct = true;
            }
        }
        if (!correct) attempts--;
        updateUI();
    }

    // Handles word guess logic
    private void guessWord() {
        String w = inputWord.getText().toLowerCase().trim();
        inputWord.setText("");
        if (w.isEmpty()) {
            statusLabel.setText("Please enter a single word.");
            return;
        }
        if (guessedWords.contains(w)) {
            statusLabel.setText("Already guessed.");
            return;
        }
        guessedWords.add(w);
        if (w.equals(target)) {
            display = new StringBuilder(target);
            statusLabel.setText("You win!");
            endRound(true);
        } else {
            attempts--;
            updateUI();
        }
    }

    // Update GUI and check win/loss
    private void updateUI() {
        wordLabel.setText("Word: " + format(display));
        attemptsLabel.setText("Attempts: " + attempts);

        // Update stickman image
        try {
            BufferedImage img = ImageIO.read(new File("src/Resources/HangMan" + attempts +".png"));
            ImageIcon icon = new ImageIcon(img);
            stickMan.setIcon(icon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (display.toString().equals(target)) {
            statusLabel.setText("You win!");
            endRound(true);
        } else if (attempts == 0) {
            statusLabel.setText("You lost! Word: " + target);
            endRound(false);
        }
    }

    // Ends the round and synchronizes with the other player
    private void endRound(boolean win) {
        // Disable inputs
        inputLetter.setEnabled(false);
        inputWord.setEnabled(false);
        btnLetter.setEnabled(false);
        btnWord.setEnabled(false);

        // Update shared results and score
        shared.incrementScore(name, attempts);
        shared.addResult(name, target, display.toString(), guessedLetters, guessedWords, attempts, win);
        shared.setDone(playerId); // Notify the shared state this player is done

        // Wait for the other player and restart round or end game
        new Thread(() -> {
            shared.waitForBoth(); // Wait until both players finish

            if (playerId == 1) {
                // P1 decides whether to continue
                int again = JOptionPane.showConfirmDialog(frame,
                        "Both players are done. Play another round?", "Play Again?", JOptionPane.YES_NO_OPTION);
                shared.setDecision(again == JOptionPane.YES_OPTION);
                if (!shared.playAgain) {
                    shared.saveToFile(); // Save logs before exit
                    System.exit(0);
                }
            } else {
                // P2 waits for P1's decision
                shared.waitForDecision();
                if (!shared.playAgain) {
                    System.exit(0);
                }
            }

            // Start a new round
            SwingUtilities.invokeLater(() -> {
                frame.dispose();
                shared.resetState();  // Reset sync state
                new PlayerWindow(playerId, name, shared);
            });
        }).start();
    }

    // Adds spacing to the display word (e.g., "_ a _ _")
    private String format(StringBuilder sb) {
        return sb.toString().replaceAll(".", "$0 ").trim();
    }
}

 */

package csci2020u.finalproject.cilent;

import csci2020u.finalproject.server.WordBank;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class MainLauncher {
    private final WordBank wordBank = new WordBank();

    //Game state
    private String targetWord;
    private StringBuilder displayedWord;
    private int attemptsLeft = 6;
    private Set<Character> guessedLetters = new HashSet<>();
    private HashSet<String> guessedWords = new HashSet<>();

    //Multiplayer state
    private boolean multiplayer = false;
    private String player1 = "Player 1";
    private String player2 = "Player 2";
    private int score1 = 0;
    private int score2 = 0;
    private int turn = 1;

    //GUI components
    private JLabel wordLabel;
    private JLabel attemptsLabel;
    private JLabel statusLabel;
    private JLabel playerLabel;
    private JLabel hintLabel;
    private JTextField input;
    private JTextField wordGuessInput;
    private JButton guessBtn;
    private JButton wordGuessBtn;

    //One file per game session
    private Path gameLogFile;

    public MainLauncher() {
        askGameMode();
        startNewGame();
    }

    private void askGameMode() {
        Object[] options = {"Single Player", "Multiplayer"};
        int mode = JOptionPane.showOptionDialog(null, "Choose Game Mode:", "Game Mode",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (mode == 1) {
            multiplayer = true;
            player1 = JOptionPane.showInputDialog("Enter name for Player 1:");
            player2 = JOptionPane.showInputDialog("Enter name for Player 2:");
            if (player1 == null || player1.isBlank()) player1 = "Player 1";
            if (player2 == null || player2.isBlank()) player2 = "Player 2";
        }
    }

    private void startNewGame() {
        //Reset word and state
        targetWord = wordBank.getRandomWord();
        displayedWord = new StringBuilder("_".repeat(targetWord.length()));
        guessedLetters.clear();
        guessedWords.clear();
        attemptsLeft = 6;

        //Create one log file for this entire game session
        try {
            Path folder = Paths.get("client_shared");
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            String filename = "client_shared/" + (multiplayer ? "multi_result_" : "game_")
                    + System.currentTimeMillis() + ".txt";
            gameLogFile = Paths.get(filename);

            StringBuilder content = new StringBuilder();
            content.append("=== GuessWord Game Start ===\n");
            if (multiplayer) {
                content.append("Players: ").append(player1).append(" vs ").append(player2).append("\n");
            }
            content.append("Word: ").append("_".repeat(targetWord.length())).append("\n");
            content.append("Hint: ").append(wordBank.getHintForWord(targetWord)).append("\n");
            content.append("Attempts: ").append(attemptsLeft).append("\n\n");

            Files.writeString(gameLogFile, content.toString());
        } catch (IOException e) {
            System.err.println("❌ Could not create game log file: " + e.getMessage());
        }

        initGUI();
    }

    private void initGUI() {
        JFrame frame = new JFrame("GuessWord");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 360);

        //UI elements
        wordLabel = new JLabel("Word: " + formatDisplayedWord(), SwingConstants.CENTER);
        hintLabel = new JLabel("Hint: " + wordBank.getHintForWord(targetWord), SwingConstants.CENTER);
        attemptsLabel = new JLabel("Attempts Left: " + attemptsLeft, SwingConstants.CENTER);
        statusLabel = new JLabel("Guess a letter", SwingConstants.CENTER);
        playerLabel = new JLabel(multiplayer ? getCurrentPlayer() + "'s turn" : "", SwingConstants.CENTER);
        input = new JTextField(5);
        guessBtn = new JButton("Guess");
        wordGuessBtn = new JButton("Guess the word");
        wordGuessInput = new JTextField(10);

        //Letter guess button
        guessBtn.addActionListener(e -> {
            String guessText = input.getText().toLowerCase();
            input.setText("");
            if (guessText.length() != 1 || !Character.isLetter(guessText.charAt(0))) {
                statusLabel.setText("Enter a single letter.");
                return;
            }

            char guess = guessText.charAt(0);
            if (guessedLetters.contains(guess)) {
                statusLabel.setText("Already guessed '" + guess + "'");
                return;
            }

            guessedLetters.add(guess);
            handleGuess(guess);
        });

        //Full word guess button
        wordGuessBtn.addActionListener(e -> {
            String guessWord = wordGuessInput.getText().toLowerCase();
            wordGuessInput.setText("");
            if (guessedWords.contains(guessWord)) {
                statusLabel.setText("Already Guessed '" + guessWord + "'");
                return;
            }

            guessedWords.add(guessWord);
            handleWordGuess(guessWord);
        });

        //Layout setup
        JPanel panel = new JPanel(new GridLayout(9, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(playerLabel);
        panel.add(wordLabel);
        panel.add(hintLabel);
        panel.add(attemptsLabel);
        panel.add(input);
        panel.add(guessBtn);
        panel.add(wordGuessInput);
        panel.add(wordGuessBtn);
        panel.add(statusLabel);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String getCurrentPlayer() {
        return (turn % 2 == 1) ? player1 : player2;
    }

    private void switchTurn() {
        turn++;
        playerLabel.setText(getCurrentPlayer() + "'s turn");
    }

    private String formatDisplayedWord() {
        return displayedWord.toString().replaceAll(".", "$0 ").trim();
    }

    private void handleGuess(char letter) {
        boolean correct = false;
        for (int i = 0; i < targetWord.length(); i++) {
            if (targetWord.charAt(i) == letter) {
                displayedWord.setCharAt(i, letter);
                correct = true;
            }
        }

        if (!correct) attemptsLeft--;

        wordLabel.setText("Word: " + formatDisplayedWord());
        attemptsLabel.setText("Attempts Left: " + attemptsLeft);

        if (displayedWord.toString().equals(targetWord)) {
            statusLabel.setText("You win!");
            updateScore(true);
            toggleInput(false);
            saveGameResult(true);
            tryAgain();
        } else if (attemptsLeft == 0) {
            statusLabel.setText("You lost! Word was: " + targetWord);
            updateScore(false);
            toggleInput(false);
            saveGameResult(false);
            tryAgain();
        } else {
            statusLabel.setText(correct ? "Good guess!" : "Wrong guess!");
        }
    }

    private void handleWordGuess(String word) {
        boolean correct = word.equals(targetWord);
        if (!correct) attemptsLeft--;

        wordLabel.setText("Word: " + formatDisplayedWord());
        attemptsLabel.setText("Attempts Left: " + attemptsLeft);

        if (correct) {
            statusLabel.setText("You win!");
            wordLabel.setText("Word: " + targetWord);
            updateScore(true);
            toggleInput(false);
            saveGameResult(true);
            tryAgain();
        } else if (attemptsLeft == 0) {
            statusLabel.setText("You lost! Word was: " + targetWord);
            updateScore(false);
            toggleInput(false);
            saveGameResult(false);
            tryAgain();
        } else {
            statusLabel.setText("Wrong guess!");
        }
    }

    private void updateScore(boolean won) {
        if (!multiplayer) return;
        if (turn % 2 == 1 && won) score1++;
        else if (turn % 2 == 0 && won) score2++;
    }

    private void toggleInput(boolean choice) {
        input.setEnabled(choice);
        guessBtn.setEnabled(choice);
        wordGuessInput.setEnabled(choice);
        wordGuessBtn.setEnabled(choice);
    }

    private void saveGameResult(boolean won) {
        try {
            if (gameLogFile == null) return;

            StringBuilder content = new StringBuilder();
            content.append("=== Round End ===\n");
            if (multiplayer) {
                content.append("Final Turn: ").append(getCurrentPlayer()).append("\n")
                        .append("Win: ").append(won).append("\n")
                        .append("Final Score: ").append(player1).append(" ").append(score1)
                        .append(" - ").append(player2).append(" ").append(score2).append("\n");
            } else {
                content.append("Result: ").append(won ? "Win" : "Lose").append("\n");
            }
            content.append("Word: ").append(targetWord).append("\n");
            content.append("Guessed Letters: ").append(guessedLetters).append("\n");
            content.append("Guessed Words: ").append(guessedWords).append("\n");
            content.append("Attempts Left: ").append(attemptsLeft).append("\n");

            Files.writeString(gameLogFile, content.toString(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("❌ Failed to write final game result: " + e.getMessage());
        }
    }

    private void tryAgain() {
        int choice = JOptionPane.showConfirmDialog(null, "Do you want to play again?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (multiplayer) switchTurn(); //Switch player before displaying message
            JOptionPane.showMessageDialog(null, "Next turn: " + (multiplayer ? getCurrentPlayer() : "New Game!"));
            newGame();
        } else {
            JOptionPane.showMessageDialog(null, "Thanks for playing!");
            System.exit(0);
        }
    }

    private void newGame() {
        attemptsLeft = 6;
        targetWord = wordBank.getRandomWord();
        displayedWord = new StringBuilder("_".repeat(targetWord.length()));
        guessedLetters.clear();
        guessedWords.clear();
        wordLabel.setText("Word: " + formatDisplayedWord());
        attemptsLabel.setText("Attempts Left: " + attemptsLeft);
        statusLabel.setText("Guess a letter");

        //Also update hint
        hintLabel.setText("Hint: " + wordBank.getHintForWord(targetWord));

        if (multiplayer) switchTurn();
        toggleInput(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainLauncher::new);
    }
}