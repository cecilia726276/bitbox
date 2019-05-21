package unimelb.bitbox.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConstUtil {
    public static final String INVALID_PROTOCOL = "INVALID_PROTOCOL";
    public static final String CONNECTION_REFUSED = "CONNECTION_REFUSED";
    public static final String HANDSHAKE_REQUEST = "HANDSHAKE_REQUEST";
    public static final String HANDSHAKE_RESPONSE = "HANDSHAKE_RESPONSE";
    public static final String FILE_CREATE_REQUEST = "FILE_CREATE_REQUEST";
    public static final String FILE_CREATE_RESPONSE = "FILE_CREATE_RESPONSE";
    public static final String FILE_MODIFY_REQUEST = "FILE_MODIFY_REQUEST";
    public static final String FILE_MODIFY_RESPONSE = "FILE_MODIFY_RESPONSE";
    public static final String FILE_BYTES_REQUEST = "FILE_BYTES_REQUEST";
    public static final String FILE_BYTES_RESPONSE = "FILE_BYTES_RESPONSE";
    public static final String FILE_DELETE_REQUEST = "FILE_DELETE_REQUEST";
    public static final String FILE_DELETE_RESPONSE = "FILE_DELETE_RESPONSE";
    public static final String DIRECTORY_CREATE_REQUEST = "DIRECTORY_CREATE_REQUEST";
    public static final String DIRECTORY_CREATE_RESPONSE = "DIRECTORY_CREATE_RESPONSE";
    public static final String DIRECTORY_DELETE_REQUEST = "DIRECTORY_DELETE_REQUEST";
    public static final String DIRECTORY_DELETE_RESPONSE = "DIRECTORY_DELETE_RESPONSE";
    public static final String HANDSHAKE_TOKEN = "*HANDSHAKE_TOKEN*";
    public static final int MAXIMUM_INCOMMING_CONNECTIONS = Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
    public static final String IP = Configuration.getConfigurationValue("advertisedName");
    public static final int PORT = Integer.parseInt(Configuration.getConfigurationValue("port"));
    public static final int BLOCKSIZE = Integer.parseInt(Configuration.getConfigurationValue("blockSize"));
    public static final int RETRANS_MAXNUM = 3;
    public static final int TIME_OUT = 10000;
    public static final int SYNC_INTERVAL = 1000 * Integer.parseInt(Configuration.getConfigurationValue("syncInterval"));
    public static final int CLIENT_PORT = Integer.parseInt(Configuration.getConfigurationValue("clientPort"));
    public static final String MODE = Configuration.getConfigurationValue("mode");
    public static final String UDP_MODE = "udp";
    public static final String TCP_MODE = "tcp";
    public static final String AUTHORIZED_KETS = Configuration.getConfigurationValue("authorized_keys");
    public static Map<String, String> PUBLIC_KEYS = new ConcurrentHashMap<>();
    static {
        String[] pbKeys = AUTHORIZED_KETS.split(",");
        String[] keyParts;
        for (String s:pbKeys) {
            keyParts = s.split( " ");
            PUBLIC_KEYS.put(keyParts[2], s);
        }
    }
}
