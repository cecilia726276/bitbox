package unimelb.bitbox.draft;

import java.net.*;
import java.io.*;
public class TCPClient {
    Socket clientSocket;
    BufferedWriter out;

    private BufferedReader in;

    public void startConnection(String ip, int port) {

        try {
            clientSocket = new Socket(ip, port);
            // s = new Socket(args[1], serverPort);
            System.out.println("ServerConnection Established");
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
            // DataOutputStream out = new DataOutputStream(s.getOutputStream());
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
            // DataInputStream in = new DataInputStream(s.getInputStream());
            System.out.println("ServerConnection Established");

        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        }
    }
    public String sendMessage(String msg) {
        try {
            out.write(msg);
            // out.writeUTF(msg);
            System.out.println("Sending data");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = null;
        try {
            data = in.readLine();
            // data.readUTF();
            System.out.println("Received: " + data);

        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        }
        return data;
    }

    public void stopConnection() {
//        in.close();
//        out.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("close:" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        TCPClient client = new TCPClient();
        client.startConnection("127.0.0.1",6666);
    }
}
