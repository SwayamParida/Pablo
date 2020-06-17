package pablo.server;

import pablo.Player;
import pablo.Protocol;

import java.io.*;
import java.net.Socket;

public class ServerThread implements Runnable {
    private final Socket curClientSocket;

    public ServerThread(Socket curSocket) {
        curClientSocket = curSocket;
    }

    @Override
    public void run() {
        System.out.println("Connected to " + getSocketName(curClientSocket));
        informClientOfExistingPlayers();
        Player newPlayer = createPlayerForClient();
        informExistingClientsOfNewPlayer(newPlayer);

        if (!Server.threads.isEmpty())
            Server.threads.remove().start();
        else
            Server.startNextThread = true;
    }

    private Player createPlayerForClient() {
        Player newPlayer = Server.gameState.createNewPlayer(getPlayerName());
        Server.playerSocketMap.put(newPlayer, curClientSocket);
        return newPlayer;
    }

    private void informExistingClientsOfNewPlayer(Player newPlayer) {
        for (Socket clientSocket : Server.outputStreamMap.keySet()) {
            if (clientSocket.equals(curClientSocket))
                continue;
            ObjectOutputStream out = Server.outputStreamMap.computeIfAbsent(clientSocket, ServerThread::getObjectOutputStream);
            assert out != null;
            try {
                out.writeObject(Protocol.sendNewPlayer(newPlayer));
            } catch (IOException ignore) { }
        }
    }

    private void informClientOfExistingPlayers() {
        ObjectOutputStream out = Server.outputStreamMap.computeIfAbsent(curClientSocket, ServerThread::getObjectOutputStream);
        assert out != null;
        if (Server.gameState.getNumPlayers() == 0)
            return;
        try {
            out.writeObject(Protocol.sendExistingPlayers(Server.gameState.getPlayers()));
        } catch (IOException ignore) { }
    }

    private String getPlayerName() {
        try {
            ObjectInputStream in = Server.inputStreamMap.computeIfAbsent(curClientSocket, ServerThread::getObjectInputStream);
            assert in != null;
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.printf("Unable to read player name of client %s\n", getSocketName(curClientSocket));
            e.printStackTrace();
            return "New Player";
        }
    }

    private static String getSocketName(Socket s) {
        return s.getRemoteSocketAddress().toString() + s.getPort();
    }

    private synchronized static ObjectOutputStream getObjectOutputStream(Socket s) {
        try {
            return new ObjectOutputStream(s.getOutputStream());
        } catch (IOException e) {
            System.out.println("Unable to write to client " + getSocketName(s));
            e.printStackTrace();
            return null;
        }
    }

    private synchronized static ObjectInputStream getObjectInputStream(Socket s) {
        try {
            return new ObjectInputStream(s.getInputStream());
        } catch (IOException e) {
            System.out.println("Unable to read from client " + getSocketName(s));
            e.printStackTrace();
            return null;
        }
    }
}
