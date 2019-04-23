package unimelb.bitbox;

import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.Coder;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.*;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ServerMain is used to process file system event and message from socket channel.
 * It provides an interface processRequest(SocketChannel socketChannel) to the EventHandler.
 */
public class ServerMain implements FileSystemObserver {
    private static Logger log = Logger.getLogger(ServerMain.class.getName());
    protected FileSystemManager fileSystemManager;


    /**
     * Record connected hostPost
     */
    private ArrayList<Document> peerLists = new ArrayList<>();

    /**
     * Record response/request history
     */
    private Map<SocketChannel, ArrayList<String>> history = new HashMap<>();

    /**
     * maximum incoming connections
     */
    private static int MAXIMUM_INCOMMING_CONNECTIONS = Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections"));

    /**
     * port of the server from configuration.properties
     */
    private static int port = Integer.parseInt(Configuration.getConfigurationValue("port"));

    /**
     * ip of the server
     */
    private static String ip = Configuration.getConfigurationValue("advertisedName");

    /**
     * Up to at most blockSize bytes at a time will be requested
     */
    private static int blockSize = Integer.parseInt(Configuration.getConfigurationValue("blockSize"));

    /**
     * client - implement ClientImpl to send/receive request
     */
    ClientImpl client = new ClientImpl();

    /**
     * hostPorts - store the hosts and ports of a list of peers
     */
    private ArrayList<HostPort> hostPorts = new ArrayList<>();

    public ServerMain() throws NumberFormatException, IOException, NoSuchAlgorithmException {
        fileSystemManager=new FileSystemManager(Configuration.getConfigurationValue("path"),this);
        String[] peers = Configuration.getConfigurationValue("peers").split(",");
        for (String peer:peers){
            HostPort hostPost = new HostPort(peer);
            hostPorts.add(hostPost);
        }

        /**
         * send handshake request in initialization stage
         */
        for (HostPort hostPort: hostPorts){
            String handshakeRequest = ProtocolUtils.getHandShakeRequest(hostPort.toDoc());
            client.sendRequest(handshakeRequest,hostPort.host,hostPort.port);
            // ArrayList<String> requestRecords = new ArrayList<>();
            // requestRecords.add("HANDSHAKE_REQUEST");
            // history.put(hostPort,requestRecords);
        }

    }

    /**
     * only OP_READ would call this method
     * 1. read buffer
     * 2. get command
     * 3. interact with filesystem / replyRequest according to command
     * @param socketChannel
     */
    public void processRequest(SocketChannel socketChannel){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        StringBuffer mes = new StringBuffer();
        try {
            mes = new StringBuffer();
            while (socketChannel.read(byteBuffer) != -1) {
                byteBuffer.flip();
                mes.append(Coder.INSTANCE.getDecoder().decode(byteBuffer).toString());
                byteBuffer.clear();
            }
            System.out.println(mes.toString());
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document document = Document.parse(mes.toString());
        String command = document.getString("command");

        switch (command){
            case "INVALID_PROTOCOL":{
                log.info(command + document.getString("message"));
                break;
            }
            case "CONNECTION_REFUSED":{
                log.info(command + document.getString("message"));
                log.info("Peers in connection: "+ document.getString("message"));

                break;
            }
            case "HANDSHAKE_REQUEST":{
                log.info(command);
                HostPort hostPort = (HostPort)document.get("hostPort");

                /**
                 * If the hostPort is valid
                 */
                if (hostPort != null){
                    /**
                     * If the handshake has already been completed
                     */
                    if (peerLists.contains(hostPort.toDoc())) {
                        String content = ProtocolUtils.getInvalidProtocol("handshaking has already been completed");
                        sendInvalidProtocol(socketChannel, content);
                    }
                    /**
                     * If the maximum incomming connections have been reached:
                     */
                    else if (peerLists.size() + 1 > MAXIMUM_INCOMMING_CONNECTIONS) {
                        String content = ProtocolUtils.getConnectionRefusedRequest(peerLists);
                        client.replyRequest(socketChannel,content);
                        log.info("send CONNECTION_REFUSED");
                    }else{
                        /**
                         * If everything is fine, establish the connection and send back handshake response
                         */
                        String content = ProtocolUtils.getHandShakeResponse(new HostPort(ip,port).toDoc());
                        client.replyRequest(socketChannel,content);
                        peerLists.add(hostPort.toDoc());
                        ArrayList<String> requestLists = new ArrayList<>();
                        history.put(socketChannel,requestLists);
                        log.info("send HANDSHAKE_RESPONSE");
                    }
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
                    sendInvalidProtocol(socketChannel, content);
                }

                break;
            }
            case "HANDSHAKE_RESPONSE":{
                log.info(command);
                HostPort hostPort = (HostPort)document.get("hostPort");
                if (hostPort != null){
                    /**
                     * get the hostport lists to this hostPort to see if there should be a response
                     */
                    ArrayList<String> requestLists = new ArrayList<>();
                    if (hostPorts.contains(hostPort)&& !peerLists.contains(hostPort.toDoc())){
                        peerLists.add(hostPort.toDoc());
                        history.put(socketChannel,requestLists);
                        log.info("establish Connection");
                    }else{
                        String content = ProtocolUtils.getInvalidProtocol("Invalid handshake response, no request has been sent before.");
                        sendInvalidProtocol(socketChannel, content);
                    }
                }else{
                    String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
                    sendInvalidProtocol(socketChannel, content);
                }
                break;
            }
            case "FILE_CREATE_REQUEST":{
                log.info(command);
                Document fileDescriptor = (Document)document.get("fileDescriptor");
                String md5 = fileDescriptor.getString("md5");
                long fileSize = fileDescriptor.getLong("fileSize");
                long lastModified = fileDescriptor.getLong("lastModified");
                String pathName = document.getString("pathName");
                if (fileSystemManager.isSafePathName(pathName) && !fileSystemManager.fileNameExists(pathName,fileDescriptor.getString("md5"))){

                    try {
                        fileSystemManager.createFileLoader(pathName, md5,fileSize,lastModified);
                        /**
                         * If another file already exists with the same content,
                         * use that file's content (i.e. does a copy) to create the intended file.
                         */
                        if (fileSystemManager.checkShortcut(pathName)){
                            // how to deal with this situation?
                        }else{
                            String fileResponse = ProtocolUtils.getFileResponse(command,fileDescriptor,pathName,true,"file loader ready");
                            client.replyRequest(socketChannel,fileResponse);
                            /**
                             * Else start requesting bytes
                             */
                            Integer length = (int) fileSize / blockSize;
                            for (int i = 0; i < length + 1; i++){
                                String fileBytesRequest = ProtocolUtils.getFileBytesRequest(fileDescriptor,pathName,i,length);
                                client.replyRequest(socketChannel,fileBytesRequest);
                                ArrayList<String> lists = history.get(socketChannel);
                                lists.add("FILE_CREATE_REQUEST");
                            }
                        }
                    } catch (Exception e) {
                        String content = ProtocolUtils.getFileResponse(command,fileDescriptor,pathName,false,"the loader is no longer available in this case");
                        sendInvalidProtocol(socketChannel, content);
                        e.printStackTrace();
                    }
                }

                break;
            }
            case "FILE_CREATE_RESPONSE":{
                /**
                 * "command": "FILE_BYTES_RESPONSE",
                 * "fileDescriptor" : {
                 * "md5" : "b1946ac92492d2347c6235b4d2611184",
                 * "lastModified" : 1553417607000,
                 * "fileSize" : 6
                 * },
                 * "pathName" : "hello.txt",
                 * "position" : 0,
                 * "length" : 6,
                 * "content" : "aGVsbG8K"
                 * "message" : "successful read",
                 * "status" : true
                 */
                break;
            }
            case "FILE_DELETE_REQUEST":{

                break;
            }
            case "FILE_DELETE_RESPONSE":{

                break;
            }
            case "FILE_MODIFY_REQUEST":{

                break;
            }
            case "FILE_MODIFY_RESPONSE":{

                break;
            }
            case "DIRECTORY_CREATE_REQUEST":{

                break;
            }
            case "DIRECTORY_CREATE_RESPONSE":{

                break;
            }
            case "DIRECTORY_DELETE_REQUEST":{

                break;
            }
            case "DIRECTORY_DELETE_RESPONSE":{

                break;
            }
            case "FILE_BYTES_REQUEST":{

                break;
            }
            case "FILE_BYTES_RESPONSE":{

                break;
            }
            default:{
                String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
                client.replyRequest(socketChannel,content);
                log.info("send INVALID_PROTOCOL");
            }
        }
        log.info(document.getString("command"));


    }

    /**
     * After sending an INVALID_PROTOCOL message to a peer, the connection should be closed immediately.
     * @param socketChannel
     * @param content
     */
    private void sendInvalidProtocol(SocketChannel socketChannel, String content) {
        client.replyRequest(socketChannel,content);
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("send INVALID_PROTOCOL");
    }

    @Override

    public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
        /**
         * The file system detects and rises events.
         * @author SYZ
         * @create 2019-04-22 15:52
         */
        FileSystemManager.EVENT event = fileSystemEvent.event;
        switch (event){
            case FILE_CREATE: {
                log.info("create file!");
                String createRequest = ProtocolUtils.getFileRequest("FILE_CREATE_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.name);
                for (HostPort hostPort: hostPorts){
                    //client.sendRequest(createRequest,hostPort.host,hostPort.port);
                }
                break;
            }
            case FILE_MODIFY: {
                String modifyRequest = ProtocolUtils.getFileRequest("FILE_MODIFY_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.name);
                break;
            }
            case FILE_DELETE:{
                String deleteRequest = ProtocolUtils.getFileRequest("FILE_DELETE_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.name);
                break;
            }
            case DIRECTORY_CREATE:{
                String createDirRequest = ProtocolUtils.getDirRequest("DIRECTORY_CREATE_REQUEST", fileSystemEvent.name);
                break;
            }
            case DIRECTORY_DELETE:{
                String deleteDirRequest = ProtocolUtils.getDirRequest("DIRECTORY_DELETE_REQUEST",fileSystemEvent.name);
                break;
            }
            default:
        }
    }


}
