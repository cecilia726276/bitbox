package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

//        Peer Protocol Messages:
//        INVALID_PROTOCOL
//        CONNECTION_REFUSED
//        HANDSHAKE_REQUEST, HANDSHAKE_RESPONSE
//        FILE_CREATE_REQUEST, FILE_CREATE_RESPONSE
//        FILE_DELETE_REQUEST, FILE_DELETE_RESPONSE
//        FILE_MODIFY_REQUEST, FILE_MODIFY_RESPONSE
//        DIRECTORY_CREATE_REQUEST, DIRECTORY_CREATE_RESPONSE
//        DIRECTORY_DELETE_REQUEST, DIRECTORY_DELETE_RESPONSE
//        FILE_BYTES_REQUEST, FILE_BYTES_RESPONSE

public class Peer 
{
	private static Logger log = Logger.getLogger(Peer.class.getName());
    public static void main( String[] args ) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
    	System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        new ServerMain();
        
    }
}
