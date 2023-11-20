import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class P2PChat {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java P2PChatExample <Port> <Nickname> <PeerPort>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        String nickname = args[1];
        int peerPort = Integer.parseInt(args[2]);

        SwingUtilities.invokeLater(() -> {
            new P2PChatGUI(port, nickname, peerPort);
        });
    }

    static class P2PChatGUI {
        private JFrame frame;
        private JTextArea chatArea;
        private JTextField inputField;

        private int peerPort;
        private String nickname;

        public P2PChatGUI(int port, String nickname, int peerPort) {
            this.peerPort = peerPort;
            this.nickname = nickname;

            frame = new JFrame("P2P Chat - " + nickname);
            frame.setSize(400, 300);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            chatArea = new JTextArea();
            chatArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(chatArea);
            frame.add(scrollPane, BorderLayout.CENTER);

            inputField = new JTextField();
            inputField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sendMessage(inputField.getText());
                    inputField.setText("");
                }
            });

            frame.add(inputField, BorderLayout.SOUTH);

            frame.setVisible(true);

            // Create a thread for receiving messages
            Thread receiverThread = new Thread(new MessageReceiver(port));
            receiverThread.start();

            // Create a thread for sending messages
            Thread senderThread = new Thread(new MessageSender(peerPort, nickname));
            senderThread.start();
        }

        private void sendMessage(String message) {
            try {
                Socket socket = new Socket("localhost", peerPort);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                writer.println(nickname + ": " + message);

                writer.close();
                socket.close();
                appendMessage("You: " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void appendMessage(String message) {
            SwingUtilities.invokeLater(() -> {
                chatArea.append(message + "\n");
            });
        }

        class MessageReceiver implements Runnable {
            private int port;

            public MessageReceiver(int port) {
                this.port = port;
            }

            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        String message = reader.readLine();
                        appendMessage(message);

                        reader.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        class MessageSender implements Runnable {
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
}
