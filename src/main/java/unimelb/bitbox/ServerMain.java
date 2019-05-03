package unimelb.bitbox;

import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.service.*;
import unimelb.bitbox.util.*;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
    public static Logger log = Logger.getLogger(ServerMain.class.getName());
    protected FileSystemManager fileSystemManager;
    HandshakeEventHandler handshakeEventHandler;
    BytesEventHandler bytesEventHandler;
    DirectoryEventHandler directoryEventHandler;
    FileEventHandler fileEventHandler;

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

    //private static List<String> existPathNameList = Collections.synchronizedList(new ArrayList());

    /**
     * Record current connections
     */
    private Set peerSet = Collections.synchronizedSet(new HashSet<Document>());

    /**
     * Record the sending history of HANDSHAKE_REQUEST to other peers to validate the received HANDSHAKE_RESPONSE
     */
    //private Set handshakeReqHistory = Collections.synchronizedSet(new HashSet<SocketChannel>());
    private Set handshakeReqHistory = Collections.synchronizedSet(new HashSet<HostPort>());

    /**
     * Record SocketChannels
     */
    private Set socketChannelSet = Collections.synchronizedSet(new HashSet<SocketChannel>());

    /**
     * in charge of bytes transfer (我这个只是临时设计，会有潜在安全问题)
     */
    private ConcurrentHashMap<String, Long> fileTransferTable = new ConcurrentHashMap<>();

    /**
     * Record handshake response/request history
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
    ClientImpl client = ClientImpl.getInstance();

    /**
     * hostPorts - store the hosts and ports of a list of peers
     */
    private ArrayList<HostPort> hostPorts = new ArrayList<>();

    public ServerMain() throws NumberFormatException, IOException, NoSuchAlgorithmException {
        fileSystemManager=new FileSystemManager(Configuration.getConfigurationValue("path"),this);
        String[] peers = Configuration.getConfigurationValue("peers").split(",");
        handshakeEventHandler = new HandshakeEventHandlerImpl(fileSystemManager,log,socketChannelSet,peerSet,handshakeReqHistory);
        bytesEventHandler = new BytesEventHandlerImpl(fileSystemManager,log,socketChannelSet,peerSet);
        directoryEventHandler = new DirectoryEventHandlerImpl(fileSystemManager,log,socketChannelSet,peerSet);
        fileEventHandler = new FileEventHandlerImpl(fileSystemManager, log, socketChannelSet, peerSet, handshakeReqHistory);
        for (String peer:peers){
            HostPort hostPost = new HostPort(peer);
            hostPorts.add(hostPost);
        }

        /**
         * send handshake request in initialization stage
         */
        for (HostPort hostPort: hostPorts){
            String handshakeRequest = ProtocolUtils.getHandShakeRequest(new HostPort(ip, port).toDoc());
            client.sendRequest(handshakeRequest,hostPort.host,hostPort.port);
            /**
             * The peer records the sending history to other peers.
             */
            handshakeReqHistory.add(hostPort);

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
        String split = "}\\{";
        String[] processList = string.split(split);
        for (String s : processList){
            if (s.charAt(s.length()-1) != '}'){
                s = s + '}';
            }
            if (s.charAt(0) != '{'){
                s = '{' + s;
            }
            processEachMessage(socketChannel,s);
        }

    }

    /**
     * It is called when the peer has reached its maximum connections.
     * @param socketChannel
     */
    public void replyConnectionError(SocketChannel socketChannel){
        List list = new ArrayList(peerSet);
        String content = ProtocolUtils.getConnectionRefusedRequest(list);
        client.replyRequest(socketChannel, content, true);
        log.info("send CONNECTION_REFUSED");
    }

    private void processEachMessage(SocketChannel socketChannel, String string){

        Document document = Document.parse(string);
        log.info("input String: " + string);
        String command = document.getString("command");
        switch (command) {
            case ConstUtil.INVALID_PROTOCOL: {
                log.info(command + document.getString("message"));
                deletePeer(socketChannel);
                break;
            }
            case ConstUtil.CONNECTION_REFUSED: {
                log.info(command);
                handshakeEventHandler.processRejectResponse(socketChannel, document);
//                /**
//                 * Check if it has sent a handshake request before.
//                 * yes - attempt to establish connection with its neighbour
//                 * no - send invalid_protocol
//                 */
//
//                boolean handshakeBefore = checkOntheList(socketChannel, handshakeReqHistory);
//                if (handshakeBefore) {
//                    List<Document> existingPeers = (List<Document>) document.get("message");
//                    HostPort firstPeers = new HostPort(existingPeers.get(0));
//                    String handshakeRequest = ProtocolUtils.getHandShakeRequest(firstPeers.toDoc());
//                    client.sendRequest(handshakeRequest, firstPeers.host, firstPeers.port);
//                    /**
//                     * The peer that tried to connect should do a breadth first search of peers in the peers list, attempt to make a connection to one of them.
//                     */
//                    handshakeReqHistory.add(new HostPort(firstPeers.host, firstPeers.port));
//
//                } else {
//                    String invalidResponse = ProtocolUtils.getInvalidProtocol("Not waiting for a handshake response from this peer");
//                    sendRejectResponse(socketChannel, invalidResponse);
//                }
                break;
            }
            case ConstUtil.HANDSHAKE_REQUEST: {
                log.info(command);
                handshakeEventHandler.processRequest(socketChannel, document);
                break;
            }
            case ConstUtil.HANDSHAKE_RESPONSE: {
                log.info(command);
                handshakeEventHandler.processSuccessResponse(socketChannel,document);
                break;
            }
            case ConstUtil.FILE_CREATE_REQUEST: {
                /**
                 * check whether the peer is on the existing connection list
                 * if not on the list - send invalid protocol
                 */
                log.info(command);
                fileEventHandler.FileCreateRequestProcess(socketChannel, document);
                break;
            }
            case ConstUtil.FILE_CREATE_RESPONSE: {
                log.info(command);
                fileEventHandler.FileCreateResponseProcess(socketChannel, document);
                break;
            }
            case ConstUtil.FILE_DELETE_REQUEST :{
                log.info(command);
                fileEventHandler.FileDeleteRequestProcess(socketChannel, document);
                break;
            }
            case ConstUtil.FILE_DELETE_RESPONSE: {
                log.info(command);
                fileEventHandler.FileDeleteResponseProcess(socketChannel, document);
                break;
            }
            case ConstUtil.FILE_MODIFY_REQUEST: {
                log.info(command);
                fileEventHandler.FileModifyRequestProcess(socketChannel, document);
                break;
            }
            case ConstUtil.FILE_MODIFY_RESPONSE: {
                log.info(command);
                fileEventHandler.FileModifyResponseProcess(socketChannel, document);
                break;
            }
            case ConstUtil.DIRECTORY_CREATE_REQUEST: {
                log.info(command);
                directoryEventHandler.processDirCreateRequest(socketChannel,document);
                break;
            }
            case ConstUtil.DIRECTORY_CREATE_RESPONSE: {
                log.info(command);
                directoryEventHandler.processDirCreateResponse(socketChannel,document);
                break;
            }
            case ConstUtil.DIRECTORY_DELETE_REQUEST: {
                log.info(command);
                directoryEventHandler.processDirDeleteRequest(socketChannel,document);
                break;
            }
            case ConstUtil.DIRECTORY_DELETE_RESPONSE: {
                log.info(command);
                directoryEventHandler.processDirDeleteResponse(socketChannel,document);
                break;
            }
            case ConstUtil.FILE_BYTES_REQUEST: {
                if (socketChannelSet.contains(socketChannel)){
                    bytesEventHandler.processRequest(socketChannel, document);
                }else{
                    String content = ProtocolUtils.getInvalidProtocol("peer not found");
                    sendRejectResponse(socketChannel, content);
                }
                break;

            }
            case ConstUtil.FILE_BYTES_RESPONSE: {
                if (socketChannelSet.contains(socketChannel)){
                    log.info("received response !!");
                    log.info("Response:" + document.toString());
                    bytesEventHandler.processResponse(socketChannel, document);
                }
                break;
            }
            default: {
                String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
                sendRejectResponse(socketChannel, content);
                log.info("send INVALID_PROTOCOL");
            }
        }
    }

    private void processCDResponse(Document document, String command, SocketChannel socketChannel) {
        log.info(command);
        log.info("status: " + document.getBoolean("status") + ", message: " + document.getString("message"));
        // 此处需要判断状态机 - host有没有给这个peer发送过FILE_CREATE_REQUEST/DELETE请求
        boolean sendCreateRequest = true;
        if (sendCreateRequest) {
            // 此处需要更新状态机 - host已经准备好收到bytes了
        } else {
            String content = ProtocolUtils.getInvalidProtocol("Invalid Response.");
            sendRejectResponse(socketChannel, content);
        }
    }

//    private boolean checkOntheList(SocketChannel socketChannel, Set set) {
//        boolean isPeerOnTheList = false;
//        try {
//            HostPort hostPort = retrieveHostport(socketChannel);
//            isPeerOnTheList = set.contains(hostPort.toDoc());
//        } catch (IOException e) {
//            e.printStackTrace();
//            String content = ProtocolUtils.getInvalidProtocol("Invalid peer Address");
//            sendRejectResponse(socketChannel, content);
//        }
//        return isPeerOnTheList;
//    }

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
        socketChannelSet.remove(socketChannel);
        InetSocketAddress socketAddress;
        try {
            HostPort hostPort = retrieveHostport(socketChannel);
            /**
             * update existing connections
             */
            if (peerSet.contains(hostPort.toDoc())){
                peerSet.remove(hostPort.toDoc());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.closeSocket(socketChannel);
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
                String createRequest = ProtocolUtils.getFileRequest("FILE_CREATE_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.pathName);
                sendRequest(createRequest, fileSystemEvent, ConstUtil.FILE_CREATE_REQUEST);
                ByteBuffer byteBuffer = null;
                try {
                    byteBuffer = fileSystemManager.readFile(fileSystemEvent.fileDescriptor.md5, 0,fileSystemEvent.fileDescriptor.fileSize);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

//                log.info("????1111"+ byteBuffer);
//                //ByteBuffer byteBuffer1 = FileCoder.INSTANCE.getEncoder().encode(byteBuffer);
//                log.info("brr: "+ FileCoder.INSTANCE.getEncoder().encodeToString(byteBuffer.array()));
//                String content = FileCoder.INSTANCE.getEncoder().encodeToString(byteBuffer.array());
//                byte[] buf = FileCoder.INSTANCE.getDecoder().decode(content);
//                ByteBuffer src = ByteBuffer.wrap(buf);
//                log.info("????2222"+ src);
//                System.out.println("!!!!!!: " + (byteBuffer == src) );

//                log.info("path: "+ fileSystemEvent.path);
//                log.info("name: " + fileSystemEvent.name);
//                log.info("pathName: "+ fileSystemEvent.pathName);
                String pathName = fileSystemEvent.pathName;
                RequestState state = new RequestState("FILE_CREATE_REQUEST", pathName);
                //initialRespState(state);
                break;

            }
            case FILE_MODIFY: {
                String modifyRequest = ProtocolUtils.getFileRequest("FILE_MODIFY_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.pathName);
                sendRequest(modifyRequest, fileSystemEvent, ConstUtil.FILE_MODIFY_REQUEST);
                String pathName = fileSystemEvent.pathName;
                RequestState state = new RequestState("FILE_MODIFY_REQUEST", pathName);
                //initialRespState(state);
                break;
            }
            case FILE_DELETE:{
                String deleteRequest = ProtocolUtils.getFileRequest("FILE_DELETE_REQUEST", fileSystemEvent.fileDescriptor.toDoc(),fileSystemEvent.pathName);
                sendRequest(deleteRequest, fileSystemEvent, ConstUtil.FILE_DELETE_REQUEST);
                break;
            }
            case DIRECTORY_CREATE:{
                String createDirRequest = ProtocolUtils.getDirRequest("DIRECTORY_CREATE_REQUEST", fileSystemEvent.pathName);
                sendRequest(createDirRequest, fileSystemEvent, ConstUtil.DIRECTORY_CREATE_REQUEST);
                break;
            }
            case DIRECTORY_DELETE:{
                String deleteDirRequest = ProtocolUtils.getDirRequest("DIRECTORY_DELETE_REQUEST",fileSystemEvent.pathName);
                sendRequest(deleteDirRequest, fileSystemEvent, ConstUtil.DIRECTORY_DELETE_REQUEST);
                break;
            }
            default:
        }
    }

    private void sendRequest(String generatedRequest, FileSystemEvent fileSystemEvent, String command) {
        Map<String, EventDetail> events;
        EventDetail eventDetail;

        for (Object socketChannel: socketChannelSet){
            // add context control
            events = ContextManager.eventContext.get(socketChannel);
            eventDetail = new EventDetail(fileSystemEvent.pathName,fileSystemEvent.fileDescriptor.toDoc(),
                    generatedRequest, command, System.currentTimeMillis(), false, 0);
            events.put(fileSystemEvent.pathName, eventDetail);
            
            client.replyRequest((SocketChannel) socketChannel, generatedRequest,false);
            log.info("send to socketchannel: "+ socketChannel.toString());

        }
//        for (Object peer: peerSet){
//            HostPort hp = new HostPort((Document) peer);
//            log.info("sending request to host: " + hp.host + " and ip: " + hp.port );
//            client.sendRequest(generatedRequest,hp.host, hp.port);
//        }
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
            log.info("retrieved hostport: ip:"+ hostPort.host + "port: " + port);
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
        if(list.contains(requestState)) {
            return true;
        }
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
