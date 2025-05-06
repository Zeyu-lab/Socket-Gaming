package csci2020u.finalproject.server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
// WordBank.java - Loads and manages the word-hint list for the game
public class WordBank {
    private final List<WordHintPair> wordHintPairs;

    public WordBank() {
        wordHintPairs = new ArrayList<>();
        loadWordsFromFile("src/main/java/csci2020u/finalproject/wordBank.csv");
    }

    // Reads the CSV file and populates the word-hint list
    private void loadWordsFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    wordHintPairs.add(new WordHintPair(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading word bank file: " + e.getMessage());
        }
    }
    // Returns a random word from the list
    public String getRandomWord() {
        Random random = new Random();
        return wordHintPairs.get(random.nextInt(wordHintPairs.size())).getWord();
    }
    // Check if a word exists in the word bank (unused currently)
    public boolean contains (String word){
        return wordHintPairs.contains(word);
    }
    // Returns the hint associated with a given word
    public String getHintForWord(String word) {
        for (WordHintPair pair : wordHintPairs) {
            if (pair.getWord().equalsIgnoreCase(word)) {
                return pair.getHint();
            }
        }
        return "No hint available";
    }
    // Inner class to store a word and its hint together
    public static class WordHintPair {
        private final String word;
        private final String hint;

        public WordHintPair(String word, String hint) {
            this.word = word;
            this.hint = hint;
        }

        public String getWord() {
            return word;
        }

        public String getHint() {
            return hint;
        }
    }
}