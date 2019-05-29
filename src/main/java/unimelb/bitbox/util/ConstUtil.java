package unimelb.bitbox.util;

import com.sun.org.apache.xpath.internal.operations.Number;

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
    public static final String LIST_PEERS_REQUEST = "LIST_PEERS_REQUEST";
    public static final String LIST_PEERS_RESPONSE = "LIST_PEERS_RESPONSE";
    public static final String CONNECT_PEER_REQUEST = "CONNECT_PEER_REQUEST";
    public static final String CONNECT_PEER_RESPONSE = "CONNECT_PEER_RESPONSE";
    public static final String DISCONNECT_PEER_REQUEST = "DISCONNECT_PEER_REQUEST";
    public static final String DISCONNECT_PEER_RESPONSE = "DISCONNECT_PEER_RESPONSE";
    public static final String LIST_PEERS = "list_peers";
    public static final String CONNECT_PEER = "connect_peer";
    public static final String DISCONNECT_PEER = "disconnect_peer";
    public static final String AUTH_REQUEST = "AUTH_REQUEST";
    public static final String AUTH_RESPONSE = "AUTH_RESPONSE";
    public static final String HANDSHAKE_TOKEN = "*HANDSHAKE_TOKEN*";
    public static final int MAXIMUM_INCOMMING_CONNECTIONS = Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));
    public static final String IP = Configuration.getConfigurationValue("advertisedName");
    public static final int PORT = Integer.parseInt(Configuration.getConfigurationValue("port"));
    public static final int RETRANS_MAXNUM = 3;
    public static final int TIME_OUT = 10000;
    public static final int SYNC_INTERVAL = 1000 * Integer.parseInt(Configuration.getConfigurationValue("syncInterval"));
    public static final int CLIENT_PORT = Integer.parseInt(Configuration.getConfigurationValue("clientPort"));
    public static final String MODE = Configuration.getConfigurationValue("mode");
    public static final int BLOCKSIZE = (ConstUtil.MODE.equals(ConstUtil.UDP_MODE))? Math.min(Integer.parseInt(Configuration.getConfigurationValue("blockSize")), 8192)
                                            : Integer.parseInt(Configuration.getConfigurationValue("blockSize"));
    public static final int UDP_PORT = Integer.parseInt(Configuration.getConfigurationValue("udpPort"));
    public static final String UDP_MODE = "udp";
    public static final String TCP_MODE = "tcp";
    public static final String AUTHORIZED_KETS = Configuration.getConfigurationValue("authorized_keys");
    public static Map<String, String> PUBLIC_KEYS = new ConcurrentHashMap<>();
    static {
        String[] pbKeys = AUTHORIZED_KETS.split(",");
        String[] keyParts;
        for (String s:pbKeys) {
            keyParts = s.split( " ");
            PUBLIC_KEYS.put(keyParts[2], keyParts[1]);
        }
    }
}
