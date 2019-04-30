package unimelb.bitbox;

import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.*;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServerMain is used to process file system event and message from socket channel.
 * It provides an interface processRequest(SocketChannel socketChannel) to the EventHandler.
 */
public class ServerMain implements FileSystemObserver {
    private static Logger log = Logger.getLogger(ServerMain.class.getName());
    protected FileSystemManager fileSystemManager;
// 场景：如果I/O是并行的，两个同名文件传过来了，这样会同时创建filecreateloader。。。
    // 心跳包

//    /**
//     * Record the corresponding HostPort according to SocketChannel.
//     */
//    private ConcurrentHashMap<SocketChannel,HostPort> channelTable = new ConcurrentHashMap<>();
    //private List<RequestState> list = Collections.synchronizedList(new ArrayList());
    /**
     * request state map
     */
    private static ConcurrentHashMap<String, List<RequestState>> stateMap = new ConcurrentHashMap<>();
    /**
     * response state map
     */
    private static ConcurrentHashMap<String, List<RequestState>> respStateMap = new ConcurrentHashMap<>();

    private static List<String> existPathNameList = Collections.synchronizedList(new ArrayList());
    /**
     * Record connected hostPost 一会我把list改成set的数据结构，这样会更好
     */
    private ArrayList<Document> peerLists = new ArrayList<>();
    /**
     * 改好后的数据格式
     */
    private Set peerSet = Collections.synchronizedSet(new HashSet<Document>());

    /**
     * in charge of incoming connections
     */
    private Set incomingPeerSet = Collections.synchronizedSet(new HashSet<Document>());

    /**
     * in charge of bytes transfer (我这个只是临时设计，会有潜在安全问题)
     */
    private ConcurrentHashMap<String, Integer> fileTransferTable = new ConcurrentHashMap<>();

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

            // 此处需要更新状态机 - 记录发送过的握手请求
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
    public void processRequest(SocketChannel socketChannel, String string) {
        Document document = Document.parse(string);
        String command = document.getString("command");

        switch (command) {
            case "INVALID_PROTOCOL": {
                log.info(command + document.getString("message"));
                deletePeer(socketChannel);
                // 此处需要更新状态机 - 知道这个invalid protocol 对应的是哪个request失败了
                break;
            }
            case "CONNECTION_REFUSED": {
                log.info(command + document.getString("message"));
                log.info("Peers in connection: " + document.getString("message"));
                // 连接失败
                // 此处需要判断状态机 - 确认自己是否和这个peer发起过握手请求，没有发送过请求需要发送invalid_protocol，如果发送过，就尝试和这个peer的邻居建立连接
                boolean handshakeBefore = true;
                if (handshakeBefore) {
                    List<Document> existingPeers = (List<Document>) document.get("message");
                    HostPort firstPeers = new HostPort(existingPeers.get(0));
                    String handshakeRequest = ProtocolUtils.getHandShakeRequest(firstPeers.toDoc());
                    client.sendRequest(handshakeRequest, firstPeers.host, firstPeers.port);
                    // The peer that tried to connect should do a breadth first search of peers in the peers list, attempt to make a connection to one of them.
                    // 发送新请求，我就选了列表里的第一个peer发送请求
                    // 此处需要更新状态机 - 记录自己发送了handshakerequest
                } else {
                    String invalidResponse = ProtocolUtils.getInvalidProtocol("Not waiting for a handshake response from this peer");
                    sendRejectResponse(socketChannel, invalidResponse);
                }
                break;
            }
            case "HANDSHAKE_REQUEST": {
                log.info(command);
                HostPort hostPort = (HostPort) document.get("hostPort");

                /**
                 * If the hostPort is valid
                 */
                if (hostPort != null) {
                    /**
                     * If the handshake has already been completed
                     */
                    if (peerSet.contains(hostPort.toDoc())) {
                        String content = ProtocolUtils.getInvalidProtocol("handshaking has already been completed");
                        sendRejectResponse(socketChannel, content);
                        // 此处需要更新状态机 - 删除和这个peer的socket记录，总之要体现链接已经断开了
                    }
                    /**
                     * If the maximum incomming connections have been reached:
                     */
                    // 目前由ServerMain来记录管理incoming connection & existing connection
                    else if (incomingPeerSet.size() + 1 > MAXIMUM_INCOMMING_CONNECTIONS) {
                        List list = new ArrayList(peerSet);
                        String content = ProtocolUtils.getConnectionRefusedRequest(list);
                        client.replyRequest(socketChannel, content, true);
                        // 此处需要更新状态机吗？ - 因为发送这个请求并不会收到回复，记录这个请求没有什么作用，此处可以不用更新
                        log.info("send CONNECTION_REFUSED");
                    } else {
                        /**
                         * If everything is fine, establish the connection and send back handshake response
                         */
                        String content = ProtocolUtils.getHandShakeResponse(new HostPort(ip, port).toDoc());
                        client.replyRequest(socketChannel, content, true);
                        // 返回handshake response的时候是直接close socket的没错吧？
                        // 此处需要更新状态机吗？ - 因为发送这个请求并不会收到回复，记录这个请求没有什么作用，此处可以不用更新
                        peerSet.add(hostPort.toDoc());
                        incomingPeerSet.add(hostPort.toDoc());
                        // ArrayList<String> requestLists = new ArrayList<>();
                        // history.put(socketChannel,requestLists);
                        if (!stateMap.containsKey(hostPort.toDoc().toJson())) {
                            List<RequestState> list = Collections.synchronizedList(new ArrayList());
                            stateMap.put(hostPort.toDoc().toJson(), list);
                        }
                        ArrayList<String> requestLists = new ArrayList<>();
                        history.put(socketChannel, requestLists);
                        log.info("send HANDSHAKE_RESPONSE");
                    }
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
                    sendRejectResponse(socketChannel, content);
                }

                break;
            }
            case "HANDSHAKE_RESPONSE": {
                log.info(command);
                HostPort hostPort = (HostPort) document.get("hostPort");

                if (hostPort != null) {
                    /**
                     * get the hostport lists to this hostPort to see if there should be a response
                     */
                    // 此处需要判断状态机 - 得出结果放在sentRequestBefore里：确认自己是否和这个peer发起过握手请求，没有发送过请求需要发送invalid_protocol，如果发送过，就和这个peer建立连接
                    boolean sentRequestBefore = hostPorts.contains(hostPort) && !peerSet.contains(hostPort.toDoc());
                    // ArrayList<String> requestLists = new ArrayList<>();
                    if (sentRequestBefore) {
                        peerSet.add(hostPort.toDoc());
                        // history.put(socketChannel,requestLists);
                        // 此处需要更新状态机 - 这里可以根据socketChannel建立状态机的 ConcurrentHashMap
                        if (!respStateMap.containsKey(hostPort.toDoc().toJson())) {
                            List<RequestState> list = Collections.synchronizedList(new ArrayList());
                            respStateMap.put(hostPort.toDoc().toJson(), list);
                        }
                        //history.put(socketChannel,requestLists);
                        log.info("establish Connection");
                    } else {
                        String content = ProtocolUtils.getInvalidProtocol("Invalid handshake response, no request has been sent before.");
                        sendRejectResponse(socketChannel, content);
                        // 此处需要更新状态机吗？ - 因为发送这个请求并不会收到回复，记录这个请求没有什么作用，此处可以不用更新
                    }
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
                    sendRejectResponse(socketChannel, content);
                    // 此处需要更新状态机吗？ - 因为发送这个请求并不会收到回复，记录这个请求没有什么作用，此处可以不用更新
                }
                break;
            }
            case "FILE_CREATE_REQUEST": {
                /**
                 * check whether the peer is on the existing connection list
                 * if not on the list - send invalid protocol
                 */
                log.info(command);
                String pathName = document.getString("pathName");
                RequestState requestState1 = new RequestState("FILE_CREATE_REQUEST",pathName);
                RequestState requestState2 = new RequestState("FILE_CREATE_MODIFY",pathName);
                HostPort hostPort = getHostPort(socketChannel);
                boolean isPeerOnTheList = checkOntheList(socketChannel);
                if (isPeerOnTheList && !checkInReqStateMap(requestState1,hostPort) && !checkInReqStateMap(requestState2,hostPort) && !existPathNameList.contains(pathName)) {
                    Document fileDescriptor = (Document) document.get("fileDescriptor");
                    String md5 = fileDescriptor.getString("md5");
                    long fileSize = fileDescriptor.getLong("fileSize");
                    long lastModified = fileDescriptor.getLong("lastModified");
                    //String pathName = document.getString("pathName");
                    if (fileSystemManager.isSafePathName(pathName)) {
                        if (!fileSystemManager.fileNameExists(pathName, fileDescriptor.getString("md5"))) {
                            try {
                                boolean status = fileSystemManager.createFileLoader(pathName, md5, fileSize, lastModified);
                                // 此处需要更新状态机 - 根据一个filedescriptor创建了一个fileloader这个事件
                                /**
                                 * If another file already exists with the same content,
                                 * use that file's content (i.e. does a copy) to create the intended file.
                                 */
                                if (fileSystemManager.checkShortcut(pathName)) {
                                    // 此处需要更新状态机 - 这个fileloader （通过filedescriptor作为key识别）已经被取消
                                    fileSystemManager.cancelFileLoader(pathName);
                                    String fileResponse = ProtocolUtils.getFileResponse("FILE_CREATE_RESPONSE", fileDescriptor, pathName, true, "file create complete");
                                    client.replyRequest(socketChannel, fileResponse, true);
                                    // 此处需要更新状态机吗？ - 因为发送这个请求并不会收到回复，记录这个请求没有什么作用，此处可以不用更新
                                } else {
                                    if (status) {
                                        String fileResponse = ProtocolUtils.getFileResponse("FILE_CREATE_RESPONSE", fileDescriptor, pathName, true, "file loader ready");
                                        client.replyRequest(socketChannel, fileResponse, false);
                                        /**
                                         * Else start requesting bytes
                                         */
                                        Integer length = (int) fileSize / blockSize;
                                        fileTransferTable.put(fileDescriptor.toJson(), length);
                                        String fileBytesRequest = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName, 0, length);
                                        // 此处需要更新状态机

                                        RequestState requestState = new RequestState("FILE_CREATE_REQUEST", pathName);
                                        stateMap.get(hostPort.toDoc().toJson()).add(requestState);
                                        existPathNameList.add(pathName);

                                        client.replyRequest(socketChannel, fileBytesRequest, false);
                                    } else {
                                        String content = ProtocolUtils.getFileResponse("FILE_CREATE_RESPONSE", fileDescriptor, pathName, status, "Failed to create file loader.");
                                        client.replyRequest(socketChannel, content, true);
                                    }

                                }
                            } catch (Exception e) {
                                String content = ProtocolUtils.getFileResponse("FILE_CREATE_RESPONSE", fileDescriptor, pathName, false, "the loader is no longer available in this case");
                                sendRejectResponse(socketChannel, content);
                                e.printStackTrace();
                            }
                        } else {
                            String content = ProtocolUtils.getFileResponse("FILE_CREATE_RESPONSE", fileDescriptor, pathName, false, "pathname already exists");
                            sendRejectResponse(socketChannel, content);
                        }
                    } else {
                        String content = ProtocolUtils.getFileResponse("FILE_CREATE_RESPONSE", fileDescriptor, pathName, false, "unsafe pathname given");
                        sendRejectResponse(socketChannel, content);
                    }
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("This peer has not been handshaked before.");
                    sendRejectResponse(socketChannel, content);
                }
                break;
            }
            case "FILE_CREATE_RESPONSE": {
                processCDResponse(document, command, socketChannel);
                break;
            }
            case "FILE_DELETE_REQUEST": {
                log.info(command);
                boolean isPeerOnTheList = checkOntheList(socketChannel);
                if (isPeerOnTheList) {
                    Document fileDescriptor = (Document) document.get("fileDescriptor");
                    String md5 = fileDescriptor.getString("md5");
                    long lastModified = fileDescriptor.getLong("lastModified");
                    String pathName = document.getString("pathName");
                    if (fileSystemManager.fileNameExists(pathName, fileDescriptor.getString("md5"))) {
                        boolean status = fileSystemManager.deleteFile(pathName, lastModified, md5);
                        if (status) {
                            String content = ProtocolUtils.getFileResponse("FILE_DELETE_RESPONSE", fileDescriptor, pathName, status, "File delete successfully");
                            client.replyRequest(socketChannel, content, true);
                        } else {
                            String content = ProtocolUtils.getFileResponse("FILE_DELETE_RESPONSE", fileDescriptor, pathName, status, "Error when delete file");
                            client.replyRequest(socketChannel, content, true);
                        }
                    } else {
                        String content = ProtocolUtils.getFileResponse("FILE_DELETE_RESPONSE", fileDescriptor, pathName, false, "File doesn't exist");
                        client.replyRequest(socketChannel, content, true);
                    }
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("Peer is not connected");
                    sendRejectResponse(socketChannel, content);
                }
                break;
            }
            case "FILE_DELETE_RESPONSE": {
                processCDResponse(document, command, socketChannel);
                break;
            }
            case "FILE_MODIFY_REQUEST": {
                log.info(command);
                String pathName = document.getString("pathName");
                RequestState requestState1 = new RequestState("FILE_CREATE_REQUEST",pathName);
                RequestState requestState2 = new RequestState("FILE_CREATE_MODIFY",pathName);
                HostPort hostPort = getHostPort(socketChannel);

                boolean isPeerOnTheList = checkOntheList(socketChannel);
                if (isPeerOnTheList && !checkInReqStateMap(requestState1,hostPort) && !checkInReqStateMap(requestState2,hostPort) && !existPathNameList.contains(pathName)) {
                    Document fileDescriptor = (Document) document.get("fileDescriptor");
                    String md5 = fileDescriptor.getString("md5");
                    long fileSize = fileDescriptor.getLong("fileSize");
                    long lastModified = fileDescriptor.getLong("lastModified");

                    if (fileSystemManager.fileNameExists(pathName, fileDescriptor.getString("md5"))) {
                        try {
                            boolean status = fileSystemManager.modifyFileLoader(pathName, md5, lastModified);
                            if (status) {
                                String content = ProtocolUtils.getFileResponse("FILE_MODIFY_RESPONSE", fileDescriptor, pathName, status, "Modify File Loader");
                                client.replyRequest(socketChannel, content, false);
                                Integer length = (int) fileSize / blockSize;
                                fileTransferTable.put(fileDescriptor.toJson(), length);
                                String fileBytesRequest = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName, 0, length);
                                // 此处需要更新状态机
                                RequestState requestState = new RequestState("FILE_MODIFY_REQUEST", pathName);
                                stateMap.get(hostPort.toDoc().toJson()).add(requestState);
                                existPathNameList.add(pathName);

                                client.replyRequest(socketChannel, fileBytesRequest, false);
                            } else {
                                String content = ProtocolUtils.getFileResponse("FILE_MODIFY_RESPONSE", fileDescriptor, pathName, false, "Failed to modify file");
                                client.replyRequest(socketChannel, content, true);
                            }
                        } catch (IOException e) {
                            String content = ProtocolUtils.getFileResponse("FILE_MODIFY_RESPONSE", fileDescriptor, pathName, false, "Failed to modify file");
                            client.replyRequest(socketChannel, content, true);
                            e.printStackTrace();
                        }
                    } else {
                        String content = ProtocolUtils.getFileResponse("FILE_MODIFY_RESPONSE", fileDescriptor, pathName, false, "File doesn't exist.");
                        client.replyRequest(socketChannel, content, true);
                    }
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("Peer is not connected");
                    sendRejectResponse(socketChannel, content);
                }
                break;
            }
            case "FILE_MODIFY_RESPONSE": {
                processCDResponse(document, command, socketChannel);
                break;
            }
            case "DIRECTORY_CREATE_REQUEST": {
                log.info(command);
                boolean isPeerOnTheList = checkOntheList(socketChannel);
                if (isPeerOnTheList) {
                    String pathName = document.getString("pathName");
                    if (!fileSystemManager.dirNameExists(pathName)) {
                        boolean status = fileSystemManager.makeDirectory(pathName);
                        if (status) {
                            String content = ProtocolUtils.getDirResponse("DIRECTORY_CREATE_RESPONSE", pathName, "Create a directory successfully", status);
                            client.replyRequest(socketChannel, content, true);
                        } else {
                            String content = ProtocolUtils.getDirResponse("DIRECTORY_CREATE_RESPONSE", pathName, "Failed to create a directory", status);
                            client.replyRequest(socketChannel, content, true);
                        }
                    } else {
                        String content = ProtocolUtils.getDirResponse("DIRECTORY_CREATE_RESPONSE", pathName, "Directory already exists", false);
                        client.replyRequest(socketChannel, content, true);
                    }
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("Peer is not connected");
                    sendRejectResponse(socketChannel, content);
                }
                break;
            }
            case "DIRECTORY_CREATE_RESPONSE": {
                processCDResponse(document, command, socketChannel);
                break;
            }
            case "DIRECTORY_DELETE_REQUEST": {
                log.info(command);
                boolean isPeerOnTheList = checkOntheList(socketChannel);
                if (isPeerOnTheList) {
                    String pathName = document.getString("pathName");
                    if (fileSystemManager.dirNameExists(pathName)) {
                        boolean status = fileSystemManager.deleteDirectory(pathName);
                        if (status) {
                            String content = ProtocolUtils.getDirResponse("DIRECTORY_DELETE_RESPONSE", pathName, "Delete a directory successfully", status);
                            client.replyRequest(socketChannel, content, true);
                        } else {
                            String content = ProtocolUtils.getDirResponse("DIRECTORY_DELETE_RESPONSE", pathName, "Failed to delete a directory", status);
                            client.replyRequest(socketChannel, content, true);
                        }
                    } else {
                        String content = ProtocolUtils.getDirResponse("DIRECTORY_CREATE_RESPONSE", pathName, "Directory doesn't exists", false);
                        client.replyRequest(socketChannel, content, true);
                    }
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("Peer is not connected");
                    sendRejectResponse(socketChannel, content);
                }
                break;
            }
            case "DIRECTORY_DELETE_RESPONSE": {
                processCDResponse(document, command, socketChannel);
                break;
            }
            case "FILE_BYTES_REQUEST": {
//                log.info(command);
//                boolean isPeerOnTheList = checkOntheList(socketChannel);
//                if (isPeerOnTheList){
//                    Document fileDescriptor = (Document)document.get("fileDescriptor");
//                    String md5 = fileDescriptor.getString("md5");
//                    long fileSize = fileDescriptor.getLong("fileSize");
//                    long lastModified = fileDescriptor.getLong("lastModified");
//                    String pathName = document.getString("pathName");
//                    Integer position = document.getInteger("position");
//                    Integer length = document.getInteger("length");
//                    boolean isFileCreated = true;
//                    if (isFileCreated){
//                        try {
//                            ByteBuffer byteBuffer = fileSystemManager.readFile(md5,position,length);
//                            //String content = ProtocolUtils.getFileBytesResponse(fileDescriptor,pathName,position,length,Base64.getEncoder().encodeToString(byteBuffer),"successful read",true);
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                    HostPort hostPort = getHostPort(socketChannel);
                    if (hostPort != null && peerSet.contains(hostPort.toDoc())) {
                        String pathName = document.getString("pathName");
                        int position = document.getInteger("position");
                        int length = document.getInteger("length");
                        if (position == 0) {
                            RequestState rs1 = new RequestState("FILE_CREATE_REQUEST", pathName);
                            RequestState rs2 = new RequestState("FILE_MODIFY_REQUEST", pathName);
                            List<RequestState> list = stateMap.get(hostPort.toDoc().toJson());
                            if (list.contains(rs1)) {
                                //执行写byte的操作
                                list.remove(rs1);
                                rs1 = new RequestState("FILE_BYTES_REQUEST", pathName, position, length);
                                list.add(rs1);
                                stateMap.put(hostPort.toDoc().toJson(), list);

                            }
                            else if(list.contains(rs2))
                            {
                                //执行写byte的操作
                                list.remove(rs2);
                                rs1 = new RequestState("FILE_BYTES_REQUEST", pathName, position, length);
                                list.add(rs2);
                                stateMap.put(hostPort.toDoc().toJson(), list);
                            }
                            else {
                                String content = ProtocolUtils.getInvalidProtocol("invalid message");
                                sendRejectResponse(socketChannel, content);

                            }
                        } else if (position <= length - 1) {
                            RequestState rs = new RequestState("FILE_BYTES_REQUEST", pathName, position - 1, length);
                            List<RequestState> list = stateMap.get(hostPort.toDoc().toJson());
                            if (list.contains(rs)) {
                                //执行写byte操作
                                list.remove(rs);
                                rs = new RequestState("FILE_BYTES_REQUEST", pathName, position, length);
                                list.add(rs);
                                stateMap.put(hostPort.toDoc().toJson(), list);
                            }
                        } else {
                            String content = ProtocolUtils.getInvalidProtocol("position out of length!");
                            sendRejectResponse(socketChannel, content);

                        }
                    } else {
                        String content = ProtocolUtils.getInvalidProtocol("peer not found");
                        sendRejectResponse(socketChannel, content);

                    }



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
            case "FILE_BYTES_RESPONSE":{
                processCDResponse(document, command, socketChannel);
                break;
            }
            default:{
                String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
                sendRejectResponse(socketChannel,content);
                log.info("send INVALID_PROTOCOL");
            }
        }
        log.info(document.getString("command"));


    }

    private void processCDResponse(Document document, String command, SocketChannel socketChannel){
        log.info(command);
        log.info("status: "+ document.getString("status")+", message: "+ document.getString("message"));
        // 此处需要判断状态机 - host有没有给这个peer发送过FILE_CREATE_REQUEST/DELETE请求
      //  boolean sendCreateRequest = true;

        switch(command){
            case "FILE_CREATE_RESPONSE": {
                HostPort hostPort = getHostPort(socketChannel);
                String pathName = document.getString("pathName");
                RequestState state = new RequestState("FILE_CREATE_REQUEST",pathName);
                String key = hostPort.toDoc().toJson();
                List<RequestState> list = respStateMap.get(key);
                if(list.contains(state))
                {
                    //在此发送FILE_BYPE_REQUEST，调用sendRequest()
                    //此处还需计算block_size为多少
                    int blockSize = 0;
                    //发送完成以后，状态改为已发送FILE_BYTE_REQ：
                    list.remove(state);
                    state = new RequestState("FILE_BYTES_REQUEST",pathName, 0, blockSize);
                    list.add(state);
                    respStateMap.put(key, list);

                }
                else{
                    String content = ProtocolUtils.getInvalidProtocol("unexpected message, lack of request");
                    sendRejectResponse(socketChannel,content);
                    log.info("lack of request");
                }
                break;
            }
            case "FILE_MODIFY_RESPONSE":{
                HostPort hostPort = getHostPort(socketChannel);
                String pathName = document.getString("pathName");
                RequestState state = new RequestState("FILE_MODIFY_REQUEST",pathName);
                String key = hostPort.toDoc().toJson();
                List<RequestState> list = respStateMap.get(key);
                if(list.contains(state))
                {
                    //在此发送FILE_BYPE_REQUEST，调用sendRequest()
                    //此处还需计算block_size为多少
                    int blockSize = 0;
                    //发送完成以后，状态改为已发送FILE_BYTE_REQ：
                    list.remove(state);
                    state = new RequestState("FILE_BYTES_REQUEST",pathName, 0, blockSize);
                    list.add(state);
                    respStateMap.put(key, list);

                }
                else{
                    String content = ProtocolUtils.getInvalidProtocol("unexpected message, lack of request");
                    sendRejectResponse(socketChannel,content);
                    log.info("lack of request");
                }
                break;
            }
            case "FILE_BYTES_RESPONSE":{
                int pos = document.getInteger("position");
                int len = document.getInteger("length");
                HostPort hostPort = getHostPort(socketChannel);
                String pathName = document.getString("pathName");
                RequestState state = new RequestState("FILE_BYTES_REQUEST",pathName, pos,len);
                String key = hostPort.toDoc().toJson();
                List<RequestState> list = respStateMap.get(key);
                if(list.contains(state))
                {
                    if(pos <= len-1)
                    {
                        //在此发送FILE_BYPE_REQUEST pos+1，调用sendRequest()


                        //发送完成以后，状态改为已发送FILE_BYTE_REQ：
                        list.remove(state);
                        state = new RequestState("FILE_BYTES_REQUEST",pathName, pos+1, len);
                        list.add(state);
                        respStateMap.put(key, list);
                    }
                    else{
                        list.remove(state);
                        respStateMap.put(key, list);
                        existPathNameList.remove(pathName);
                    }

                }
                else{
                    String content = ProtocolUtils.getInvalidProtocol("unexpected message, lack of request");
                    sendRejectResponse(socketChannel,content);
                    log.info("lack of request");
                }
                break;
            }
            default: ;
        }
//        if (sendCreateRequest) {
//            // 此处需要更新状态机 - host已经准备好收到bytes了
//        } else {
//            String content = ProtocolUtils.getInvalidProtocol("Invalid Response.");
//            sendRejectResponse(socketChannel, content);
//        }
    }
    private boolean checkOntheList(SocketChannel socketChannel) {
        boolean isPeerOnTheList = false;
        try {
            HostPort hostPort = retrieveHostport(socketChannel);
            isPeerOnTheList = peerSet.contains(hostPort.toDoc());
        } catch (IOException e) {
            e.printStackTrace();
            String content = ProtocolUtils.getInvalidProtocol("Invalid peer Address");
            sendRejectResponse(socketChannel, content);
        }
        return isPeerOnTheList;
    }

    private HostPort retrieveHostport(SocketChannel socketChannel) throws IOException {
        InetSocketAddress socketAddress;
        socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
        String ip = socketAddress.getAddress().toString();
        int port = socketAddress.getPort();
        HostPort hostPort = new HostPort(ip, port);
        return hostPort;
    }

    /**
     * After sending an INVALID_PROTOCOL message to a peer, the connection should be closed immediately.
     * @param socketChannel
     * @param content
     */
    private void sendRejectResponse(SocketChannel socketChannel, String content) {
        client.replyRequest(socketChannel,content,true);
        deletePeer(socketChannel);
        log.info("send Reject Response");
    }

    /**
     * If a socket was closed, the host would remove the peer from its existing set (and incoming connection set)
     * @param socketChannel
     */
    private void deletePeer(SocketChannel socketChannel) {
        InetSocketAddress socketAddress;
        try {
            HostPort hostPort = retrieveHostport(socketChannel);
            /**
             * update existing connections
             */
            if (peerSet.contains(hostPort.toDoc())){
                peerSet.remove(hostPort.toDoc());
            }
            if (incomingPeerSet.contains(hostPort.toDoc())){
                incomingPeerSet.remove(hostPort.toDoc());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {
        /**
         * The file system detects and rises events.
         * @author SYZ
         * @create 2019-04-22 15:52
         */
        FileSystemManager.EVENT event = fileSystemEvent.event;
        log.info(event.toString());
        switch (event){
            case FILE_CREATE: {
                String createRequest = ProtocolUtils.getFileRequest("FILE_CREATE_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.name);
                sendRequest(createRequest);
                String pathName = fileSystemEvent.pathName;
                RequestState state = new RequestState("FILE_CREATE_REQUEST", pathName);
                initialRespState(state);


            }
            case FILE_MODIFY: {
                String modifyRequest = ProtocolUtils.getFileRequest("FILE_MODIFY_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.name);
                sendRequest(modifyRequest);
                String pathName = fileSystemEvent.pathName;
                RequestState state = new RequestState("FILE_MODIFY_REQUEST", pathName);
                initialRespState(state);
                break;
            }
            case FILE_DELETE:{
                String deleteRequest = ProtocolUtils.getFileRequest("FILE_DELETE_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.name);
                sendRequest(deleteRequest);
                break;
            }
            case DIRECTORY_CREATE:{
                String createDirRequest = ProtocolUtils.getDirRequest("DIRECTORY_CREATE_REQUEST", fileSystemEvent.name);
                sendRequest(createDirRequest);
                break;
            }
            case DIRECTORY_DELETE:{
                String deleteDirRequest = ProtocolUtils.getDirRequest("DIRECTORY_DELETE_REQUEST",fileSystemEvent.name);
                sendRequest(deleteDirRequest);
                break;
            }
            default:
        }
    }

    private void sendRequest(String generatedRequest) {
        for (Object peer: peerSet){
            HostPort hp = new HostPort((Document) peer);
            client.sendRequest(generatedRequest,hp.host, hp.port);
        }
        return;
    }

    private void initialRespState(RequestState state)
    {
        for (String key: respStateMap.keySet()){
            respStateMap.get(key).add(state);
        }
    }
    private HostPort getHostPort(SocketChannel socketChannel)
    {
        try{
            InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
            String ip = socketAddress.getAddress().toString();
            int port = socketAddress.getPort();
            HostPort hostPort = new HostPort(ip, port);
            return hostPort;
        }catch(IOException e){
            String content = ProtocolUtils.getInvalidProtocol("can't get address");
            sendRejectResponse(socketChannel, content);
            e.printStackTrace();
            return null;
        }

    }
    private boolean checkInReqStateMap(RequestState requestState, HostPort hostPort)
    {
        List<RequestState> list = stateMap.get(hostPort.toDoc().toJson());
        if(list.contains(requestState))
            return true;
        return false;
    }

    /**
     * manage sync request
     */
    public void syncProcess() {
        List<FileSystemManager.FileSystemEvent> list = fileSystemManager.generateSyncEvents();
        for (FileSystemManager.FileSystemEvent fileSystemEvent : list) {
            processFileSystemEvent(fileSystemEvent);
        }
    }


}
