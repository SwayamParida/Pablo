import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    private static final String hostName = "localhost";
    private static final int portNumber = 4444;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(hostName, portNumber);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner systemInScanner = new Scanner(System.in);
        ) {
            out.println(getNameFromStdInput(systemInScanner));
            for (String inputLine = in.readLine(); inputLine != null; inputLine = in.readLine()) {
                System.out.println(inputLine);
                if (inputLine.endsWith("?"))
                    out.println(systemInScanner.nextLine());
            }
            System.out.println("Stopped communicating with server.");
        } catch (IOException ignore) { }
    }

    private static String getNameFromStdInput(Scanner scanner) {
        System.out.print("Welcome to Pablo!\nWhat is your gamer name? ");
        return scanner.nextLine();
    }
}
