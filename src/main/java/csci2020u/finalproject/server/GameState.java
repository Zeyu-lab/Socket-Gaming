package csci2020u.finalproject.server;

import java.util.HashSet;
import java.util.Set;
// GameState.java - Represents a single game's logic and state
public class GameState {
    private final String targetWord;
    private final String hint;
    private final StringBuilder displayedWord;
    private final Set<Character> guessedLetters;
    private int attemptsLeft;
    // Constructor initializes state with target word and hint
    public GameState(String targetWord, String hint) {
        this.targetWord = targetWord.toLowerCase();
        this.hint = hint;
        this.displayedWord = new StringBuilder("_".repeat(this.targetWord.length()));
        this.guessedLetters = new HashSet<>();
        this.attemptsLeft = 6;

        //Debug information
        System.out.println("[DEBUG] Target word = '" + this.targetWord + "'");
        System.out.println("[DEBUG] Target length = " + this.targetWord.length());
        System.out.println("[DEBUG] Displayed word = " + this.displayedWord);
    }

    public String getHint() {
        return hint;
    }
    // Returns formatted string of guessed letters and underscores
    public String getDisplayedWord() {
        StringBuilder spaced = new StringBuilder();
        for (int i = 0; i < displayedWord.length(); i++) {
            spaced.append(displayedWord.charAt(i));
            if (i < displayedWord.length() - 1) {
                spaced.append(" ");
            }
        }
        return spaced.toString();
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }
    // Returns true if game is either won or attempts are over
    public boolean isGameOver() {
        return isWon() || attemptsLeft == 0;
    }

    public boolean isWon() {
        return displayedWord.toString().equals(targetWord);
    }

    public String getTargetWord() {
        return targetWord;
    }
    // Handles a player's guess and updates game state

    public String guess(char letter) {
        letter = Character.toLowerCase(letter);

        if (!Character.isLetter(letter) || guessedLetters.contains(letter)) {
            return "Invalid or repeated guess.";
        }

        guessedLetters.add(letter);

        boolean found = false;
        for (int i = 0; i < targetWord.length(); i++) {
            if (targetWord.charAt(i) == letter) {
                displayedWord.setCharAt(i, letter);
                found = true;
            }
        }

        if (!found) {
            attemptsLeft--;
            return "Incorrect!";
        } else {
            return "Correct!";
        }
    }
    // Returns string representation of guessed letters
    @Override
    public String toString() {
        return guessedLetters.toString();
    }
}