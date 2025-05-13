# Socket-Gaming
# Project Name - Hangman Multiplayer Game
- A complete Hangman game application built in Java. The project supports both terminal-based and GUI-based gameplay with options for single player, multiplayer, and custom word bank editing. The multiplayer mode includes a cooperative guessing system with synchronized turns and game state tracking. Game progress and results are saved locally.

Team Members
1. Abdul Ghafour Ahmed
2. Thaddeus Baturensky
3. Ze Yu Shi
4. Nathaniel White

## GUI Screenshot
1. Single Player mode
<img width="594" alt="SinglePlayer" src="https://github.com/user-attachments/assets/20dd9d66-1541-4897-b6c3-217f7960ab37" />

2. Multiplayer mode
<img width="1384" alt="Multiplayer" src="https://github.com/user-attachments/assets/e7878b3a-b758-4a6f-8e5d-268d96e41b4a" />

3. Menu UI
<img width="508" alt="Menu" src="https://github.com/user-attachments/assets/55a51bca-99cf-419f-812b-21aeeaff3372" />

4. Word Bank Editor
<img width="380" alt="WordBankEditor" src="https://github.com/user-attachments/assets/e7a2cb5e-db1b-44ff-9a69-47cc004d18f2" />


# How to Run
The basic principle is that when you start MainLauncher.java it generates a Ui for the user to select the modes are

1. Single-player mode: provide guessing words, then give the corresponding hints
2. Multiplayer mode: two screens will be generated, users will be allowed to choose their names first, and then the principle is the same as the single mode, except that it is multiplayer.
3. Word Bank Editor: when users want to add new words, they can add them directly through this editor, add custom word-hint pairs.

Both multiplayer and single player modes will ask the user whether to continue at the end of each round, if the user chooses to cancel then the game results are stored in the client shared, and will only be displayed when you actually exit, the results are not based on each round, the game results are the total, that is, it will show how many hands you have played in total. 

It is also possible to play by starting two separate java files for Client.java and Server.java in the terminal, which will also store the results of the game in the client shared.

# Java File Responsibilities
1. Server.java : Starts the server on port 9090. Accepts multiple clients using threads and logs connections.
2. ClientHandler.java : Manages one client connection on the server side. Handles input, game logic, and result logging.
3. GameState.java	: Stores and manages the word, guessed letters, attempts, and game logic for a single round.
4. WordBank.java : Loads words and hints from wordBank.csv, provides random word and matching hint.
5. Client.java : Console-based client. Connects to server, receives messages, sends guesses, and saves results.
6. MainLauncher.java : GUI entry point. Presents game mode menu and starts GUI components accordingly.
   - SinglePlayerMode	Handles single player GUI gameplay: guessing letters/words, updating display, and saving results.
   - PlayerWindow	Used in GUI multiplayer. Creates a player-specific game window and syncs with shared game state.
   - GameSyncState	Maintains multiplayer game state: round completion status, result logging, and replay sync.
7. MultiplayerLobby.java : Set up scoreboard lobby for multiplayer mode

# Other Resources
Libraries Used:Java Standard Library
Word Bank File:wordBank.csv
Build Tool: Maven
Folders Created at Runtime:
- client_shared/: Logs results from GUI and console clients
- server_shared/: Stores server logs and startup info

# Hangman Multiplayer DEMO
https://youtu.be/1yLoyGyqz6k
