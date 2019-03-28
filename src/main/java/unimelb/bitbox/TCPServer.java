package unimelb.bitbox;

import java.net.*;
import java.io.*;

public class TCPServer {
 public static void main (String args[]) {

         try{
             int serverPort = 7899; // the server port
             ServerSocket listenSocket = new ServerSocket(serverPort);
             int i = 0;
             while(true) {
                 System.out.println("Server listening for a connection");
                 Socket clientSocket = listenSocket.accept();
                 i++;
                 System.out.println("Received connection " + i );
                 Connection c = new Connection(clientSocket);
                 }
             }
         catch(IOException e)
         {
             System.out.println("Listen socket:"+e.getMessage());
             }
         }
}
