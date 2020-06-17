package pablo.client;

import pablo.Constants;
import pablo.Protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread implements Runnable, Constants {
    private BoardController controller;

    public ClientThread(BoardController boardController) {
        controller = boardController;
    }

    @Override
    public void run() {
        try (
            Socket socket = new Socket(hostName, portNumber);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            out.writeObject(controller.getPlayerName());
            Protocol input = null;
            boolean responseRequired = false;
            while (true) {
                synchronized (controller.clientOutput) {
                    if (!responseRequired) {
                        input = (Protocol) in.readObject();
                        System.out.println(input.getState());
                        responseRequired = Protocol.processClientInput(controller, input);
                    } else if (Protocol.isValidResponse(input, controller.clientOutput)) {
                        System.out.println(controller.clientOutput.getState());
                        out.writeUnshared(controller.clientOutput);
                        controller.clientOutput.setFields(new Protocol(null, null));
                        responseRequired = false;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!(e instanceof EOFException))
                e.printStackTrace();
        }
    }
}
