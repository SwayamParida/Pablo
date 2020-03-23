import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GameServer {
    private static final int portNumber = 4444;
    public static final int MAX_CLIENTS = 4;

    public static boolean startNextThread = true;
    public static Queue<Thread> threads = new LinkedList<>();

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
            // Start of new turn
            broadcastMessage(String.format("%s's turn", gameState.getCurPlayer()));
            broadcastMessage(String.format("Card on top of the discard pile: %s\n", gameState.topOfPile()));

            // User draws card from deck or pile
            sendMessage(getCurPlayerSocket(), "Would you like to draw from the deck or the pile?");
            String source = getUserInput();
            drawCard(source);

            String move;
            // User swaps card with own cards or discards card (power card handling already performed by helper methods)
            if (source.equals("deck")) {
                sendMessage(getCurPlayerSocket(), "Would you like to discard the card or keep it?");
                move = getUserInput();
            } else {
                move = "keep";
            }
            processMove(move);

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
            String message = String.format("Your initial cards are:\n%s\t\t(1)\n%s\t\t(2)\n", p.initialPeek()[0], p.initialPeek()[1]);
            sendMessage(s, message);
        }
    }

    private static void drawCard(String drawSource) {
        switch (drawSource) {
            case "deck":
                Card drawnCard = gameState.drawDeck();
                String playerFeedback = String.format("You have drawn a: %s\n", drawnCard);
                sendMessage(getCurPlayerSocket(), playerFeedback);
                broadcastMessage(String.format("%s drew a card from the deck", gameState.getCurPlayer()), getCurPlayerSocket());
                return;
            case "pile":
                drawnCard = gameState.drawPile();
                playerFeedback = String.format("You have drawn a: %s\n", drawnCard);
                sendMessage(getCurPlayerSocket(), playerFeedback);
                broadcastMessage(String.format("%s drew the %s from the pile", gameState.getCurPlayer(), drawnCard), getCurPlayerSocket());
        }
    }

    private static void processMove(String move) throws IOException {
        Card discardedCard = null;
        switch(move.toLowerCase()) {
            case "keep":
                sendMessage(getCurPlayerSocket(), "Which card would you like to replace?");
                int index = Integer.parseInt(getUserInput());
                discardedCard = gameState.keep(index - 1);
                sendMessage(getCurPlayerSocket(), String.format("You discarded: %s\n", discardedCard));
                broadcastMessage(String.format("%s replaced it with a %s that was at position %d\n", gameState.getCurPlayer(), discardedCard, index), getCurPlayerSocket());
                break;
            case "discard":
                discardedCard = gameState.discard();
                sendMessage(getCurPlayerSocket(), String.format("You discarded: %s\n", discardedCard));
                broadcastMessage(String.format("%s discarded the %s that they just picked\n", gameState.getCurPlayer(), discardedCard), getCurPlayerSocket());
        }

        assert discardedCard != null;
        switch (discardedCard.getValue()) {
            case 7: case 8: case 9:
                break;
            default:
                return;
        }

        switch (discardedCard.getValue()) {
            case 7:
                sendMessage(getCurPlayerSocket(), "Would you like to peek at any of your cards?");
                if (getUserInput().toLowerCase().startsWith("y"))
                    peekSelf();
                return;
            case 8:
                sendMessage(getCurPlayerSocket(), "Would you like to peek at anyone else's cards?");
                if (getUserInput().toLowerCase().startsWith("y"))
                    peekOther();
                return;
            case 9:
                sendMessage(getCurPlayerSocket(), "Would you like to swap cards with anyone else?");
                if (getUserInput().toLowerCase().startsWith("y"))
                    swapWithPlayer();
        }
    }

    private static void peekSelf() throws IOException {
        sendMessage(getCurPlayerSocket(), "Which card would you like to peek at?");
        broadcastMessage(String.format("%s is deciding which card to peek at", gameState.getCurPlayer()), getCurPlayerSocket());
        int index = Integer.parseInt(getUserInput());

        Card peekedCard = gameState.peekSelf(index - 1);
        sendMessage(getCurPlayerSocket(), String.format("Card at position %d is: %s\n", index, peekedCard));
        broadcastMessage(String.format("%s peeked at card at position %d\n", gameState.getCurPlayer(), index), getCurPlayerSocket());
    }

    private static void peekOther() throws IOException {
        sendMessage(getCurPlayerSocket(), "Which player's cards would you like to peek at?");
        broadcastMessage(String.format("%s is deciding which player's cards to peek at", gameState.getCurPlayer()), getCurPlayerSocket());
        String playerName = getUserInput();
        Player targetPlayer = gameState.findPlayerByName(playerName);

        sendMessage(getCurPlayerSocket(), String.format("Which of %s's cards would you like to peek at?", targetPlayer));
        broadcastMessage(String.format("%s is deciding which of %s's cards to peek at", gameState.getCurPlayer(), targetPlayer), getCurPlayerSocket());
        int index = Integer.parseInt(getUserInput());

        Card peekedCard = gameState.peekOther(targetPlayer, index - 1);
        sendMessage(getCurPlayerSocket(), String.format("%s's card at position %d is: %s\n", targetPlayer, index, peekedCard));
        broadcastMessage(String.format("%s peeked at %s's card at position %d\n", gameState.getCurPlayer(), targetPlayer, index), getCurPlayerSocket());
    }

    private static void swapWithPlayer() throws IOException {
        sendMessage(getCurPlayerSocket(), "Which player would you like to swap cards with?");
        broadcastMessage(String.format("%s is deciding which player to swap cards with", gameState.getCurPlayer()), getCurPlayerSocket());
        String playerName = getUserInput();
        Player targetPlayer = gameState.findPlayerByName(playerName);

        sendMessage(getCurPlayerSocket(), String.format("Which of %s's cards would you like to take?", targetPlayer));
        broadcastMessage(String.format("%s is deciding which of %s's cards to take", gameState.getCurPlayer(), targetPlayer), getCurPlayerSocket());
        int targetIndex = Integer.parseInt(getUserInput());

        sendMessage(getCurPlayerSocket(), "Which of your cards would like to give?");
        broadcastMessage(String.format("%s is deciding which of their own cards to give", gameState.getCurPlayer()), getCurPlayerSocket());
        int sourceIndex = Integer.parseInt(getUserInput());

        gameState.playerSwap(targetPlayer, sourceIndex - 1, targetIndex - 1);
        sendMessage(getCurPlayerSocket(), String.format("You swapped your card at position %d with %s's card at position %d\n", sourceIndex, targetPlayer, targetIndex));
        broadcastMessage(String.format("%s swapped their card at position %d with %s's card at position %d\n", gameState.getCurPlayer(), sourceIndex, targetPlayer, targetIndex), getCurPlayerSocket());
    }

    private static Socket getCurPlayerSocket() {
        return playerSocketMap.get(gameState.getCurPlayer());
    }

    private static String getUserInput() throws IOException {
        return inputReaderMap.get(getCurPlayerSocket()).readLine();
    }
}
