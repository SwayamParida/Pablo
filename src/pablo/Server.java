package pablo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Constants {
    public static boolean startNextThread = true;
    public static Queue<Thread> threads = new LinkedList<>();

    public static Game gameState = new Game();
    public static Map<Player, Socket> playerSocketMap = Collections.synchronizedMap(new HashMap<>());
    public static Map<Socket, ObjectInputStream> inputStreamMap = Collections.synchronizedMap(new HashMap<>());
    public static Map<Socket, ObjectOutputStream> outputStreamMap = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws IOException, InterruptedException {
        establishConnections();
        initialPeek();
        gameState.initGame();
        while (true) {
            broadcastMessage(Protocol.startTurn(gameState.getCurPlayer(), gameState.topOfPile()));
            Protocol initialMove = getClientInput(gameState.getCurPlayer());
            startTurn(initialMove);
            if (initialMove.getState() == State.CALL_PABLO)
                break;
            Protocol nextMove = getClientInput(gameState.getCurPlayer());
            processNextMove(nextMove);
            gameState.endTurn();
        }
    }

    private static void establishConnections() throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(portNumber);
        Thread t = null;
        for (int numClients = 0; numClients < MAX_CLIENTS; ++numClients) {
            t = new Thread(new ServerThread(serverSocket.accept()));
            if (startNextThread) {
                t.start();
                startNextThread = false;
            } else
                threads.add(t);
        }
        t.join();
        serverSocket.close();
    }

    private static void initialPeek() {
        for (Map.Entry<Player, Socket> socketPlayerPair : playerSocketMap.entrySet()) {
            Player p = socketPlayerPair.getKey();
            Socket s = socketPlayerPair.getValue();
            ObjectOutputStream out = outputStreamMap.get(s);
            try {
                out.writeObject(Protocol.initialPeek(p.initialPeek()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void broadcastMessage(Protocol message) {
        for (ObjectOutputStream out : outputStreamMap.values())
            try {
                out.writeObject(message);
                out.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private static Protocol getClientInput(Player player) {
        Socket playerClientSocket = playerSocketMap.get(player);
        ObjectInputStream in = inputStreamMap.get(playerClientSocket);
        try {
            return (Protocol) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void startTurn(Protocol move) {
        switch (move.getState()) {
            case DRAW_DECK:
                Card drawnCard = gameState.drawDeck();
                broadcastMessage(Protocol.drawnDeck(gameState.getCurPlayer(), drawnCard));
                break;
            case DRAW_PILE:
                drawnCard = gameState.drawPile();
                broadcastMessage(Protocol.drawnPile(gameState.getCurPlayer(), gameState.topOfPile(), drawnCard));
                break;
            case CALL_PABLO:
                broadcastMessage(Protocol.calledPablo(gameState.getPlayers()));
        }
    }

    private static void processNextMove(Protocol nextMove) {
        Card discardedCard = null;
        switch (nextMove.getState()) {
            case KEEP:
                int replaceCardIndex = (int) nextMove.getData();
                discardedCard = gameState.keep(replaceCardIndex);
                broadcastMessage(Protocol.keptCard(gameState.getCurPlayer(), replaceCardIndex, discardedCard));
                break;
            case DISCARD:
                discardedCard = gameState.discard();
                broadcastMessage(Protocol.discardedCard(discardedCard));
        }

        assert discardedCard != null;
        switch (discardedCard.getValue()) {
            case 7: case 8: case 9: break;
            default: return;
        }

        switch (discardedCard.getValue()) {
            case 7: peekSelf();return;
            case 8: peekOther(); return;
            case 9: swap();
        }
    }

    private static void peekSelf() {
        broadcastMessage(Protocol.tellPeekSelf(gameState.getCurPlayer()));
        int peekedCardIndex = (int) getClientInput(gameState.getCurPlayer()).getData();
        Card peekedCard = gameState.peekSelf(peekedCardIndex);
        broadcastMessage(Protocol.peekedSelf(gameState.getCurPlayer(), peekedCardIndex, peekedCard));
    }

    private static void peekOther() {
        broadcastMessage(Protocol.tellPeekOther(gameState.getCurPlayer()));
        List<Object> dataFields = (List<Object>) getClientInput(gameState.getCurPlayer()).getData();
        String peekedPlayerName = (String) dataFields.get(0);
        Player peekedPlayer = gameState.findPlayerByName(peekedPlayerName);
        int peekedCardIndex = (int) dataFields.get(1);
        Card peekedCard = gameState.peekOther(peekedPlayer, peekedCardIndex);
        broadcastMessage(Protocol.peekedOther(gameState.getCurPlayer(), peekedPlayer, peekedCardIndex, peekedCard));
    }

    private static void swap() {
        broadcastMessage(Protocol.tellSwap(gameState.getCurPlayer()));
        List<Object> dataFields = (List<Object>) getClientInput(gameState.getCurPlayer()).getData();
        String swappedPlayerName = (String) dataFields.get(0);
        Player swappedPlayer = gameState.findPlayerByName(swappedPlayerName);
        int givenCardIndex = (int) dataFields.get(1);
        int takenCardIndex = (int) dataFields.get(2);
        gameState.playerSwap(swappedPlayer, givenCardIndex, takenCardIndex);
        broadcastMessage(Protocol.swapped(gameState.getCurPlayer(), swappedPlayer, givenCardIndex, takenCardIndex));
    }
}