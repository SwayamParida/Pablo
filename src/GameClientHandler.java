import java.io.*;
import java.net.Socket;

public class GameClientHandler implements Runnable {
    private Socket curClientSocket;

    public GameClientHandler(Socket curSocket) {
        curClientSocket = curSocket;
    }

    @Override
    public void run() {
        System.out.println("Connected to " + getSocketName(curClientSocket));
        informClientOfExistingPlayers();
        Player newPlayer = createPlayerForClient();
        informExistingClientsOfNewPlayer(newPlayer);

        if (!GameServer.threads.isEmpty())
            GameServer.threads.remove().start();
        else
            GameServer.startNextThread = true;
    }

    private Player createPlayerForClient() {
        Player newPlayer = GameServer.gameState.createNewPlayer(getPlayerName());
        GameServer.playerSocketMap.put(newPlayer, curClientSocket);
        return newPlayer;
    }

    private void informExistingClientsOfNewPlayer(Player newPlayer) {
        for (Socket clientSocket : GameServer.outputWriterMap.keySet()) {
            if (clientSocket.equals(curClientSocket))
                continue;
            PrintWriter out = GameServer.outputWriterMap.computeIfAbsent(clientSocket, GameClientHandler::createPrintWriterForSocket);
            assert out != null;
            out.printf("%s has joined the game\n", newPlayer.getPlayerName());
        }
    }

    private void informClientOfExistingPlayers() {
        PrintWriter out = GameServer.outputWriterMap.computeIfAbsent(curClientSocket, GameClientHandler::createPrintWriterForSocket);
        assert out != null;
        out.print("Welcome to the lobby. ");
        if (GameServer.gameState.getNumPlayers() == 0)
            out.println("You are the first one here.");
        else {
            out.println("Players already in the lobby are:");
            for (Player player : GameServer.gameState.getPlayers())
                out.println(player.getPlayerName());
        }
    }

    private String getPlayerName() {
        try {
            BufferedReader in = GameServer.inputReaderMap.computeIfAbsent(curClientSocket, GameClientHandler::createBufferedReaderForSocket);
            assert in != null;
            return in.readLine();
        } catch (IOException e) {
            System.out.printf("Unable to read player name of client %s\n", getSocketName(curClientSocket));
            e.printStackTrace();
            return "New Player";
        }
    }

    private static String getSocketName(Socket s) {
        return s.getRemoteSocketAddress().toString() + s.getPort();
    }

    private synchronized static PrintWriter createPrintWriterForSocket(Socket s) {
        try {
            return new PrintWriter(s.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Unable to write to client " + getSocketName(s));
            e.printStackTrace();
            return null;
        }
    }

    private synchronized static BufferedReader createBufferedReaderForSocket(Socket s) {
        try {
            return new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            System.out.println("Unable to read from client " + getSocketName(s));
            e.printStackTrace();
            return null;
        }
    }
}
