package unimelb.bitbox.message;

import unimelb.bitbox.util.ConstUtil;
import unimelb.bitbox.util.Document;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author SYZ
 * @create 2019-04-22 11:18
 */
public class ProtocolUtils {
    private ProtocolUtils(){}

    /**
     * generate INVALID_PROTOCOL message
     *
     * @return String of INVALID_PROTOCOL
     */
    public static String getInvalidProtocol(String message){
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.INVALID_PROTOCOL);
        totalReqBody.append("message", message);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    /**
     * generate CONNECTION_REFUSED message
     *
     * @param list of HostPort Document
     * @return String of CONNECTION_REFUSED
     */
    public static String getConnectionRefusedRequest(List<Document> list) {
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.CONNECTION_REFUSED);
        totalReqBody.append("message", "connection limit reached");
        totalReqBody.append("peers", (ArrayList<?>) list);
        return totalReqBody.toJson()+System.lineSeparator();
    }


    /**
     * generate HANDSHAKE_REQUEST message
     *
     * @param hostPort
     * @return String of HANDSHAKE_REQUEST
     */
    public static String getHandShakeRequest(Document hostPort) {
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.HANDSHAKE_REQUEST);
        totalReqBody.append("hostPort", hostPort);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    /**
     * generate HANDSHAKE_RESPONSE message
     *
     * @param hostPort
     * @return String of HANDSHAKE_RESPONSE
     */
    public static String getHandShakeResponse(Document hostPort){
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.HANDSHAKE_RESPONSE);
        totalReqBody.append("hostPort", hostPort);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    /**
     * generate FILE_CREATE_REQUEST,FILE_DELETE_REQUEST,FILE_MODIFY_REQUEST message
     * @param fileDescriptor
     * @param pathName
     * @return String
     */
    public static String getFileRequest(String command, Document fileDescriptor, String pathName){
        Document totalReqBody = new Document();

        totalReqBody.append("command", command);
        totalReqBody.append("fileDescriptor", fileDescriptor);
        totalReqBody.append("pathName", pathName);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    /**
     * generate FILE_CREATE_RESPONSE, FILE_DELETE_RESPONSE,FILE_MODIFY_RESPONSE message
     * @param fileDescriptor
     * @param pathName
     * @param status
     * @param message
     * @return String
     */
    public static String getFileResponse(String command, Document fileDescriptor, String pathName, Boolean status, String message){
        Document totalReqBody = new Document();
        totalReqBody.append("command", command);
        totalReqBody.append("fileDescriptor", fileDescriptor);
        totalReqBody.append("pathName", pathName);
        totalReqBody.append("message", message);
        totalReqBody.append("status", status);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    /**
     * generate FILE_BYTES_REQUEST message
     * @param fileDescriptor
     * @param pathName
     * @param position
     * @param length
     * @return String of FILE_BYTES_REQUEST
     */
    public static String getFileBytesRequest(Document fileDescriptor, String pathName, long position, long length){
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.FILE_BYTES_REQUEST);
        totalReqBody.append("fileDescriptor", fileDescriptor);
        totalReqBody.append("pathName", pathName);
        totalReqBody.append("position", position);
        totalReqBody.append("length", length);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    /**
     * generate FILE_BYTES_RESPONSE message
     * @param fileDescriptor
     * @param pathName
     * @param position
     * @param length
     * @param content
     * @param message "successful read"/"unsuccessful read"
     * @param status
     * @return String of FILE_BYTES_RESPONSE
     */
    public static String getFileBytesResponse(Document fileDescriptor, String pathName, long position, long length, String content, String message, Boolean status){
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.FILE_BYTES_RESPONSE);
        totalReqBody.append("fileDescriptor", fileDescriptor);
        totalReqBody.append("pathName", pathName);
        totalReqBody.append("position", position);
        totalReqBody.append("length", length);
        totalReqBody.append("content", content);
        totalReqBody.append("message", message);
        totalReqBody.append("status", status);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    /**
     * generate DIRECTORY_CREATE_REQUEST, DIRECTORY_DELETE_REQUEST message
     * @param pathName
     * @param command
     * @return String
     */
    public static String getDirRequest(String command, String pathName){
        Document totalReqBody = new Document();
        totalReqBody.append("command", command);
        totalReqBody.append("pathName", pathName);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    /**
     * generate DIRECTORY_CREATE_RESPONSE, DIRECTORY_DELETE_RESPONSE message
     * @param pathName
     * @param command
     * @return String
     */
    public static String getDirResponse(String command, String pathName, String message, Boolean status){
        Document totalReqBody = new Document();
        totalReqBody.append("command", command);
        totalReqBody.append("pathName", pathName);
        totalReqBody.append("message", message);
        totalReqBody.append("status", status);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    public static String getAuthRequest(String identity){
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.AUTH_REQUEST);
        totalReqBody.append("identity", identity);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    public static String getAuthSuccessResponse(String aes128, String message){
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.AUTH_RESPONSE);
        totalReqBody.append("AES128", aes128);
        totalReqBody.append("status", true);
        totalReqBody.append("message", message);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    public static String getAuthFailResponse(String message){
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.AUTH_RESPONSE);
        totalReqBody.append("status", false);
        totalReqBody.append("message", message);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    public static String getListPeersRequest(){
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.LIST_PEERS_REQUEST);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    public static String getClientRequest(String command, String host, int port){
        Document totalReqBody = new Document();
        totalReqBody.append("command", command);
        totalReqBody.append("host", host);
        totalReqBody.append("port", port);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    public static String getClientResponse(String command, String host, int port, Boolean status, String message){
        Document totalReqBody = new Document();
        totalReqBody.append("command", command);
        totalReqBody.append("host", host);
        totalReqBody.append("port", port);
        totalReqBody.append("status", status);
        totalReqBody.append("message", message);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    public static String getListPeerResponse(Map<SocketChannel, Document> peerSet) {
        Document totalReqBody = new Document();
        totalReqBody.append("command", ConstUtil.LIST_PEERS_RESPONSE);

        List list = new ArrayList();
        for (Map.Entry<SocketChannel, Document> peer : peerSet.entrySet()) {
            list.add(peer.getValue());
        }
        totalReqBody.append("peers", (ArrayList<?>) list);
        return totalReqBody.toJson()+System.lineSeparator();
    }

    public static String getPayload(String encryptedMessage){
        Document totalReqBody = new Document();
        totalReqBody.append("payload", encryptedMessage);
        return totalReqBody.toJson()+System.lineSeparator();
    }

}
