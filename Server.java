import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) {
        final int PORT = 1337;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            Scanner scanner = new Scanner(System.in); // moved outside the loop

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Incoming connection from: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); // autoFlush = true

                String validation = in.readLine();

                if ("/isthisinfoshare".equals(validation)) {
                    out.println("/thisisinfoshare");
                    System.out.println("Device connected.");
                } else {
                    System.out.println("Invalid handshake, closing...");
                    clientSocket.close();
                    continue;
                }

                while (!clientSocket.isClosed()) {
                    System.out.print(">> ");
                    String data = scanner.nextLine(); // fixed here
                    out.println(data);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

