package unimelb.bitbox.draft;

import unimelb.bitbox.util.Configuration;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

//        OldPeer Protocol Messages:
//        INVALID_PROTOCOL
//        CONNECTION_REFUSED
//        HANDSHAKE_REQUEST, HANDSHAKE_RESPONSE
//        FILE_CREATE_REQUEST, FILE_CREATE_RESPONSE
//        FILE_DELETE_REQUEST, FILE_DELETE_RESPONSE
//        FILE_MODIFY_REQUEST, FILE_MODIFY_RESPONSE
//        DIRECTORY_CREATE_REQUEST, DIRECTORY_CREATE_RESPONSE
//        DIRECTORY_DELETE_REQUEST, DIRECTORY_DELETE_RESPONSE
//        FILE_BYTES_REQUEST, FILE_BYTES_RESPONSE

public class OldPeer
{
	private static Logger log = Logger.getLogger(OldPeer.class.getName());
    public static void main( String[] args ) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
    	System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox OldPeer starting...");
        Configuration.getConfiguration();
        //FileSystemManager fm = new FileSystemManager();
        //FileSystemManager.Event event = fm.EVENT.DIRECTORY_CREATE;
        //FileSystemManager.FileSystemEvent fm.ev = new FileSystemEvent("localhost","peer1", event);
        ServerDemo server = new ServerDemo();
        Thread serverThread = new Thread(server);
        serverThread.start();
        ClientDemo clientObj = new ClientDemo();
        ClientDemo clientObj2 = new ClientDemo();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        clientObj.startServer("127.0.0.1", 8111);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        clientObj2.startServer("127.0.0.1", 8111);



//        comment out the code produced by Bowen  -- Yizhou
//        OldServerMain serObj = new OldServerMain();
//        Thread thread = new Thread(serObj);
//        thread.start();
//        ClientMain cliObj1 = new ClientMain();
//        ClientMain cliObj2 = new ClientMain();
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("client1 ready:");
//        cliObj1.startClient("127.0.0.1", 8111);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        cliObj2.startClient("localhost", 8111);

        
    }
}
