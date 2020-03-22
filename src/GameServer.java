import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GameServer {
    private static final int portNumber = 4444;
    public static final int MAX_CLIENTS = 4;

    public static boolean startNextThread = true;
    public static Queue<Thread> threads = new PriorityQueue<>();

    public static Game gameState = new Game();
    public static Map<Player, Socket> playerSocketMap = Collections.synchronizedMap(new HashMap<>());
    public static Map<Socket, BufferedReader> inputReaderMap = Collections.synchronizedMap(new HashMap<>());
    public static Map<Socket, PrintWriter> outputWriterMap = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws IOException, InterruptedException {
        establishConnections();
        broadcastMessage("\nGame is starting.\n");
        initialPeek();
        gameState.initGame();

        while (true) {
            broadcastMessage(String.format("%s's turn", gameState.getCurPlayer().getPlayerName()));
            broadcastMessage(String.format("Card on top of the discard pile: %s\n", gameState.topOfPile()));
            Socket curPlayerSocket = playerSocketMap.get(gameState.getCurPlayer());
            sendMessage(curPlayerSocket, "What would you like to do?");
            String move = inputReaderMap.get(curPlayerSocket).readLine();
            String playerFeedback = processMove(move);
            sendMessage(curPlayerSocket, playerFeedback);
            String publicFeedback = feedback(gameState.getCurPlayer(), move);
            broadcastMessage(publicFeedback, curPlayerSocket);
            gameState.endTurn();
        }
    }

    private static void establishConnections() throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(portNumber);
        Thread t = null;
        for (int numClients = 0; numClients < MAX_CLIENTS; ++numClients) {
            t = new Thread(new GameClientHandler(serverSocket.accept()));
            if (startNextThread) {
                t.start();
                startNextThread = false;
            } else
                threads.add(t);
        }
        t.join();
    }

    private static void broadcastMessage(String message) {
        for (PrintWriter out : outputWriterMap.values())
            out.println(message);
    }

    private static void broadcastMessage(String message, Socket exception) {
        for (Map.Entry<Socket, PrintWriter> socketWriterPair : outputWriterMap.entrySet()) {
            Socket s = socketWriterPair.getKey();
            PrintWriter out = socketWriterPair.getValue();
            if (!s.equals(exception))
                out.println(message);
        }
    }

    private static void sendMessage(Socket s, String message) {
        outputWriterMap.get(s).println(message);
    }

    private static void initialPeek() {
        for (Map.Entry<Player, Socket> socketPlayerPair : playerSocketMap.entrySet()) {
            Socket s = socketPlayerPair.getValue();
            Player p = socketPlayerPair.getKey();
            String message = String.format("Your initial cards are:\n%s\n%s\n", p.initialPeek()[0], p.initialPeek()[1]);
            sendMessage(s, message);
        }
    }

    private static String processMove(String move) {
        Card drawnCard = null;
        switch(move.toLowerCase()) {
            case "draw from deck":
                drawnCard = gameState.makeMove(Game.Move.DRAW_DECK);
                break;
            case "draw from pile":
                drawnCard = gameState.makeMove(Game.Move.DRAW_PILE);
                break;
        }
        return String.format("You have drawn a: %s\n", drawnCard);
    }

    private static String feedback(Player player, String move) {
        move = move.toLowerCase();
        switch(move.toLowerCase()) {
            case "draw from deck":
                return String.format("%s drew a card from the deck", player.getPlayerName());
            case "draw from pile":
                return String.format("%s drew a card from the pile", player.getPlayerName());
        }
        return null;
    }
}
