package unimelb.bitbox.draft;

import java.net.*;
import java.io.*;

public class TCPServer {
    ServerSocket serverSocket;
    Socket clientSocket;
    BufferedWriter out;
    BufferedReader in;
    public void start(int serverPort) {
        try {
            serverSocket = new ServerSocket(serverPort);
            int i = 0;
            while (true) {

                System.out.println("Server listening for a connection");
                clientSocket = serverSocket.accept();
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));

                i++;
                System.out.println("Received connection " + i);
                ServerConnection c = new ServerConnection(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Listen socket:" + e.getMessage());
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void main(String[] args) {
        TCPServer server=new TCPServer();
        server.start(6666);
    }

}
