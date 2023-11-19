import java.io.*;
import java.net.*;
import java.util.Scanner;

public class P2PChatExample {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java P2PChatExample <Port> <Nickname> <PeerPort>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String nickname = args[1];
        int peerPort = Integer.parseInt(args[2]);

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            // Cria uma thread para ouvir as mensagens recebidas
            Thread receiverThread = new Thread(new MessageReceiver(serverSocket));
            receiverThread.start();

            // Cria uma thread para enviar mensagens
            Thread senderThread = new Thread(new MessageSender(peerPort, nickname));
            senderThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class MessageReceiver implements Runnable {
        private ServerSocket serverSocket;

        public MessageReceiver(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run() {
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    String message = reader.readLine();
                    System.out.println("Received: " + message);

                    reader.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class MessageSender implements Runnable {
        private int peerPort;
        private String nickname;

        public MessageSender(int peerPort, String nickname) {
            this.peerPort = peerPort;
            this.nickname = nickname;
        }

        public void run() {
            try {
                Scanner scanner = new Scanner(System.in);

                while (true) {
                    System.out.print("Enter a message: ");
                    String message = scanner.nextLine();

                    Socket socket = new Socket("localhost", peerPort);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                    writer.println(nickname + ": " + message);

                    writer.close();
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
