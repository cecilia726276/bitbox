package unimelb.bitbox;

import java.net.*;
import java.io.*;
public class TCPClient {
    public static void main (String args[]) {
    Socket s = null;
         try{
             int serverPort = 7899;
             s = new Socket(args[1], serverPort);
             System.out.println("Connection Established");
             DataInputStream in = new DataInputStream( s.getInputStream());
             DataOutputStream out =new DataOutputStream( s.getOutputStream());
             System.out.println("Sending data");
             out.writeUTF(args[0]); // UTF is a string encoding see Sn. 4.4
             String data = in.readUTF(); // read a line of data from the stream
             System.out.println("Received: "+ data) ;
             }catch (UnknownHostException e) {
             System.out.println("Socket:"+e.getMessage());
             }catch (EOFException e){
             System.out.println("EOF:"+e.getMessage());
             }catch (IOException e){
             System.out.println("readline:"+e.getMessage());
             }finally {
             if(s!=null) try {
                 s.close();
                 }catch (IOException e){
                 System.out.println("close:"+e.getMessage());
                 }
             }
         }
}
