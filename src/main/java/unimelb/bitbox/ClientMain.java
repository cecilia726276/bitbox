package unimelb.bitbox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
//the function of this class is to help peers send Events
public class ClientMain {
    // IP and port
//    private static String ip = "localhost";
//    private static int port = 3000;

    public void startClient(String ip, int port) {  //dest_ip and dest_port
        try(Socket socket = new Socket(ip, port);){
            // Output and Input Stream
            System.out.println("client port"+port+"started.");
            DataInputStream input = new DataInputStream(socket.
                    getInputStream());
            DataOutputStream output = new DataOutputStream(socket.
                    getOutputStream());

            output.writeUTF("this is client request from address"+ip+",port"+port);
            output.flush();

//            JSONObject newCommand = new JSONObject();
//            newCommand.put("command_name", "Math");
//            newCommand.put("method_name","add");
//            newCommand.put("first_integer",1);
//            newCommand.put("second_integer",1);
//
//            System.out.println(newCommand.toJSONString());

            // Read hello from server..
            String message = input.readUTF();
            System.out.println("msg from server:"+message);

            // Send RMI to Server
//            output.writeUTF(newCommand.toJSONString());
//            output.flush();
//
//            // Print out results received from server..
//            String result = input.readUTF();
//            System.out.println("Received from server: "+result);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {

        }

    }
}
