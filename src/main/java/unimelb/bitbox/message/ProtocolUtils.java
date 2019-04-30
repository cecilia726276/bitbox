package unimelb.bitbox.message;

import unimelb.bitbox.util.Document;

import java.util.ArrayList;
import java.util.List;

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
        totalReqBody.append("command", "INVALID_PROTOCOL");
        totalReqBody.append("message", message);
        return totalReqBody.toJson();
    }

    /**
     * generate CONNECTION_REFUSED message
     *
     * @param list of HostPort Document
     * @return String of CONNECTION_REFUSED
     */
    public static String getConnectionRefusedRequest(List<Document> list) {
        Document totalReqBody = new Document();
        totalReqBody.append("command", "CONNECTION_REFUSED");
        totalReqBody.append("message", "connection limit reached");
        totalReqBody.append("peers", (ArrayList<?>) list);
        return totalReqBody.toJson();
    }


    /**
     * generate HANDSHAKE_REQUEST message
     *
     * @param hostPort
     * @return String of HANDSHAKE_REQUEST
     */
    public static String getHandShakeRequest(Document hostPort) {
        Document totalReqBody = new Document();
        totalReqBody.append("command", "HANDSHAKE_REQUEST");
        totalReqBody.append("hostPort", hostPort);
        return totalReqBody.toJson();
    }

    /**
     * generate HANDSHAKE_RESPONSE message
     *
     * @param hostPort
     * @return String of HANDSHAKE_RESPONSE
     */
    public static String getHandShakeResponse(Document hostPort){
        Document totalReqBody = new Document();
        totalReqBody.append("command", "HANDSHAKE_RESPONSE");
        totalReqBody.append("hostPort", hostPort);
        return totalReqBody.toJson();
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
        return totalReqBody.toJson();
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
        return totalReqBody.toJson();
    }

    /**
     * generate FILE_BYTES_REQUEST message
     * @param fileDescriptor
     * @param pathName
     * @param position
     * @param length
     * @return String of FILE_BYTES_REQUEST
     */
    public static String getFileBytesRequest(Document fileDescriptor, String pathName, Integer position, Integer length){
        Document totalReqBody = new Document();
        totalReqBody.append("command", "FILE_BYTES_REQUEST");
        totalReqBody.append("fileDescriptor", fileDescriptor);
        totalReqBody.append("pathName", pathName);
        totalReqBody.append("position", position);
        totalReqBody.append("length", length);
        return totalReqBody.toJson();
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
        totalReqBody.append("command", "FILE_BYTES_RESPONSE");
        totalReqBody.append("fileDescriptor", fileDescriptor);
        totalReqBody.append("pathName", pathName);
        totalReqBody.append("position", position);
        totalReqBody.append("length", length);
        totalReqBody.append("content", content);
        totalReqBody.append("message", message);
        totalReqBody.append("status", status);
        return totalReqBody.toJson();
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
        return totalReqBody.toJson();
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
        return totalReqBody.toJson();
    }

}
