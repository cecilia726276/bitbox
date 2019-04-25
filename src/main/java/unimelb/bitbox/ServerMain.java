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

/**
 * ServerMain is used to process file system event and message from socket channel.
 * It provides an interface processRequest(SocketChannel socketChannel) to the EventHandler.
 */
public class ServerMain implements FileSystemObserver {
    private static Logger log = Logger.getLogger(ServerMain.class.getName());
    protected FileSystemManager fileSystemManager;

//    /**
//     * Record the corresponding HostPort according to SocketChannel.
//     */
//    private ConcurrentHashMap<SocketChannel,HostPort> channelTable = new ConcurrentHashMap<>();
    //private List<RequestState> list = Collections.synchronizedList(new ArrayList());
    private ConcurrentHashMap<String, List<RequestState>> stateMap = new ConcurrentHashMap<>();
    /**
     * Record connected hostPost 一会我把list改成set的数据结构，这样会更好
     */
    private ArrayList<Document> peerLists = new ArrayList<>();
    /**
     * 改好后的数据格式
     */
    private Set peerSet = Collections.synchronizedSet(new HashSet<Document>());

    //
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

            // 此处需要更新状态机
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
    public void processRequest(SocketChannel socketChannel, String string){
        Document document = Document.parse(string);
        String command = document.getString("command");

        switch (command){
            case "INVALID_PROTOCOL":{
                log.info(command + document.getString("message"));
                InetSocketAddress socketAddress;
                try {
                    socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                    String ip = socketAddress.getAddress().toString();
                    int port = socketAddress.getPort();
                    HostPort hostPort = new HostPort(ip, port);

                    /**
                     * update existing connections
                     */
                    if (peerSet.contains(hostPort.toDoc())){
                        peerSet.remove(hostPort.toDoc());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 此处需要更新状态机 - 知道这个invalid protocol 对应的是哪个request失败了
                break;
            }
            case "CONNECTION_REFUSED":{
                log.info(command + document.getString("message"));
                log.info("Peers in connection: "+ document.getString("message"));
                // 连接失败
                // 此处需要更新状态机
                List<Document> existingPeers = (List<Document>) document.get("message");
                HostPort firstPeers = new HostPort(existingPeers.get(0));
                String handshakeRequest = ProtocolUtils.getHandShakeRequest(firstPeers.toDoc());
                client.sendRequest(handshakeRequest,firstPeers.host,firstPeers.port);
                // The peer that tried to connect should do a breadth first search of peers in the peers list, attempt to make a connection to one of them.
                // 发送新请求，我就选了列表里的第一个peer发送请求
                // 此处需要更新状态机
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
                    if (peerSet.contains(hostPort.toDoc())) {
                        String content = ProtocolUtils.getInvalidProtocol("handshaking has already been completed");
                        sendInvalidProtocol(socketChannel, content);
                    }
                    /**
                     * If the maximum incomming connections have been reached:
                     */
                    // 这里 if 条件需要改， MAXIMUM_INCOMMING_CONNECTIONS 如果指的是被动连接数的话。。。
                    // 或者NIO来处理这个条件下的情况
                    else if (peerSet.size() + 1 > MAXIMUM_INCOMMING_CONNECTIONS) {
                        List list = new ArrayList(peerSet);
                        String content = ProtocolUtils.getConnectionRefusedRequest(list);
                        client.replyRequest(socketChannel,content,true);
                        log.info("send CONNECTION_REFUSED");
                    }else{
                        /**
                         * If everything is fine, establish the connection and send back handshake response
                         */
                        String content = ProtocolUtils.getHandShakeResponse(new HostPort(ip,port).toDoc());
                        client.replyRequest(socketChannel,content,false);
                        peerSet.add(hostPort.toDoc());
                        if(!stateMap.containsKey(hostPort.toDoc().toJson()))
                        {
                            List<RequestState> list = Collections.synchronizedList(new ArrayList());
                            stateMap.put(hostPort.toDoc().toJson(), list);
                        }
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
                    if (hostPorts.contains(hostPort)&& !peerSet.contains(hostPort.toDoc())){
                        peerSet.add(hostPort.toDoc());
                        if(!stateMap.containsKey(hostPort.toDoc().toJson()))
                        {
                            List<RequestState> list = Collections.synchronizedList(new ArrayList());
                            stateMap.put(hostPort.toDoc().toJson(), list);
                        }
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

                try{
                    InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                    String ip = socketAddress.getAddress().toString();
                    int port = socketAddress.getPort();
                    HostPort hostPort = new HostPort(ip, port);
                    if(peerSet.contains(hostPort.toDoc())){ //已建立连接，改package有效
                        //允许调用createFileLoader函数
                        Document fileDescriptor = (Document)document.get("fileDescriptor");
                        String md5 = fileDescriptor.getString("md5");
                        long fileSize = fileDescriptor.getLong("fileSize");
                        long lastModified = fileDescriptor.getLong("lastModified");
                        String pathName = document.getString("pathName");
                        if (fileSystemManager.isSafePathName(pathName) && !fileSystemManager.fileNameExists(pathName,fileDescriptor.getString("md5")))
                        {
                            fileSystemManager.createFileLoader(pathName, md5,fileSize,lastModified);
                            /**
                             * If another file already exists with the same content,
                             * use that file's content (i.e. does a copy) to create the intended file.
                             */
                            if (fileSystemManager.checkShortcut(pathName)){
                                // how to deal with this situation?
                            }else {
                                String fileResponse = ProtocolUtils.getFileResponse(command, fileDescriptor, pathName, true, "file loader ready");

                                client.replyRequest(socketChannel, fileResponse, false);
                                RequestState requestState = new RequestState("FILE_CREATE_REQUEST", pathName);
                                stateMap.get(hostPort.toDoc().toJson()).add(requestState);
                                /**
                                 * Else start requesting bytes
                                 */
                                Integer length = (int) fileSize / blockSize;

//                                for (int i = 0; i < length + 1; i++){
//                                String fileBytesRequest = ProtocolUtils.getFileBytesRequest(fileDescriptor,pathName,i,length);
//                                client.replyRequest(socketChannel,fileBytesRequest,false);
//                                ArrayList<String> lists = history.get(socketChannel);
//                                lists.add("FILE_CREATE_REQUEST");
                            }
                        }

                    }
                }catch (IOException e) {
                    String content = ProtocolUtils.getInvalidProtocol("peer not found");
                    sendInvalidProtocol(socketChannel, content);
                    e.printStackTrace();
                }
                catch(NoSuchAlgorithmException nse){
//                    String content = ProtocolUtils.getFileResponse(command,fileDescriptor,pathName,false,"the loader is no longer available in this case");
//                    sendInvalidProtocol(socketChannel, content);
                    nse.printStackTrace();
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
                try{
                    InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                    String ip = socketAddress.getAddress().toString();
                    int port = socketAddress.getPort();
                    HostPort hostPort = new HostPort(ip, port);
                    if(peerSet.contains(hostPort.toDoc()))
                    {
                        String pathName = document.getString("pathName");
                        int position = document.getInteger("position");
                        int length = document.getInteger("length");
                        if(position==0)
                        {
                            RequestState rs = new RequestState("FILE_CREATE_REQUEST",pathName);
                            List<RequestState> list = stateMap.get(hostPort.toDoc().toJson());
                            if(list.contains(rs))
                            {
                                //执行写byte的操作
                                list.remove(rs);
                                rs = new RequestState("FILE_BYTES_REQUEST",pathName, position, length);
                                list.add(rs);
                                stateMap.put(hostPort.toDoc().toJson(), list);

                            }
                            else
                            {
                                String content = ProtocolUtils.getInvalidProtocol("invalid message");
                                sendInvalidProtocol(socketChannel, content);
                                
                            }
                        }
                        else if(position <= length-1)
                        {
                            RequestState rs = new RequestState("FILE_BYTES_REQUEST",pathName, position-1, length);
                            List<RequestState> list = stateMap.get(hostPort.toDoc().toJson());
                            if(list.contains(rs))
                            {
                                //执行写byte操作
                                list.remove(rs);
                                rs = new RequestState("FILE_BYTES_REQUEST",pathName, position, length);
                                list.add(rs);
                                stateMap.put(hostPort.toDoc().toJson(), list);
                            }
                        }
                        else
                        {
                            String content = ProtocolUtils.getInvalidProtocol("position out of length!");
                            sendInvalidProtocol(socketChannel, content);

                        }
                    }
                    else
                    {
                        String content = ProtocolUtils.getInvalidProtocol("peer not found");
                        sendInvalidProtocol(socketChannel, content);

                    }
                }catch(IOException ioe){
                    String content = ProtocolUtils.getInvalidProtocol("can't get address");
                    sendInvalidProtocol(socketChannel, content);
                    ioe.printStackTrace();
                }
                break;
            }
            case "FILE_BYTES_RESPONSE":{

                break;
            }
            default:{
                String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
                client.replyRequest(socketChannel,content,false);
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
        client.replyRequest(socketChannel,content,false);
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
