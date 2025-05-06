package csci2020u.finalproject.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

//This is the main server class that launches the Hangman server.
//It listens for incoming client connections and starts a new handler for each one.
public class Server {
    //The port number we’re using to accept incoming client connections
    private static final int PORT = 9090;

    //We use a thread pool so we can handle multiple clients at once (up to 10 here)
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    //We’ll use this logger to print server events or errors to the console or file
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    //This is the main method — it starts the server and waits for connections
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //Make sure our "server_shared" folder exists for logging and storage
            Path sharedFolder = Paths.get("server_shared");
            if (!Files.exists(sharedFolder)) {
                Files.createDirectories(sharedFolder);
            }

            logger.info("[SERVER] Started on port " + PORT);

            //Log server startup into a text file too
            Files.writeString(
                    Paths.get("server_shared/server_log.txt"),
                    "[SERVER] Started on port " + PORT + "\n",
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );

            //This is our main loop — the server will sit here waiting for clients forever
            while (true) {
                Socket clientSocket = serverSocket.accept(); //accept an incoming connection

                String connectMsg = "[SERVER] New client connected: " + clientSocket.getInetAddress();
                logger.info(connectMsg);

                //Also save the connection log to file
                Files.writeString(
                        Paths.get("server_shared/server_log.txt"),
                        connectMsg + "\n",
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND
                );

                //Once connected, we assign the client to its own handler thread
                ClientHandler handler = new ClientHandler(clientSocket);
                pool.execute(handler); //run the handler using our thread pool
            }
        } catch (IOException e) {
            //If server startup fails, we print and also log the error
            logger.log(Level.SEVERE, "[SERVER] Failed to start server", e);
            try {
                Files.writeString(
                        Paths.get("server_shared/server_log.txt"),
                        "[SERVER] Failed to start server: " + e.getMessage() + "\n",
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND
                );
            } catch (IOException ignored) {}
        }
    }
}