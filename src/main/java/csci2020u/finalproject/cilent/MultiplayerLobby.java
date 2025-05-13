
package csci2020u.finalproject.cilent;

import javax.swing.*;
import java.awt.*;
import javax.swing.Timer;


class MultiplayerLobby {
    public static void showLobby(String p1, String p2, GameSyncState shared) {
        JFrame lobby = new JFrame("Multiplayer Lobby");
        lobby.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lobby.setSize(300, 200);

        JLabel header = new JLabel("Scoreboard", SwingConstants.CENTER);
        JLabel player1 = new JLabel(p1 + ": " + shared.getScores().getOrDefault(p1, 0), SwingConstants.CENTER);
        JLabel player2 = new JLabel(p2 + ": " + shared.getScores().getOrDefault(p2, 0), SwingConstants.CENTER);

        JButton startBtn = new JButton("Start Game");
        startBtn.addActionListener(e -> {
            startBtn.setVisible(false);
            new Thread(() -> new PlayerWindow(1, p1, shared)).start();
            new Thread(() -> new PlayerWindow(2, p2, shared)).start();
        });

        JPanel panel = new JPanel(new GridLayout(4, 1));
        panel.add(header);
        panel.add(player1);
        panel.add(player2);
        panel.add(startBtn);

        lobby.add(panel);
        lobby.setLocationRelativeTo(null);
        lobby.setVisible(true);

        // Timer to refresh the scoreboard every second
        Timer timer = new Timer(1000, e -> SwingUtilities.invokeLater(() -> updateScoreboard(player1, player2, p1, p2, shared)));
        timer.start();
    }

    // Method to update the scores in the labels
    private static void updateScoreboard(JLabel player1Label, JLabel player2Label, String p1, String p2, GameSyncState shared) {
        // Update the labels with the latest scores
        player1Label.setText(p1 + ": " + shared.getScores().getOrDefault(p1, 0));
        player2Label.setText(p2 + ": " + shared.getScores().getOrDefault(p2, 0));
    }
}
