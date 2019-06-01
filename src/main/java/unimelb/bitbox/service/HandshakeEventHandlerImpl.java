package unimelb.bitbox.service;

import unimelb.bitbox.ContextManager;
import unimelb.bitbox.EventDetail;
import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.*;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class HandshakeEventHandlerImpl implements HandshakeEventHandler{
    private FileSystemManager fileSystemManager;
    private Client client;
    private Logger log;
    private Set socketChannelSet;
    private Map peerSet;
    private Set handshakeReqHistory;
    public HandshakeEventHandlerImpl(FileSystemManager fileSystemManager, Logger logger,
                                     Set socketChannelSet, Map peerSet, Set handshakeReqHistory) {
        this.fileSystemManager = fileSystemManager;
        this.client = ClientImpl.getInstance();
        this.log = logger;
        this.socketChannelSet = socketChannelSet;
        this.peerSet = peerSet;
        this.handshakeReqHistory = handshakeReqHistory;
    }

    @Override
    public void processRequest(SocketChannel socketChannel, Document document) {
        HostPort hostPort = new HostPort((Document) document.get("hostPort"));
        log.info("hostport from handshake request: ip: " + hostPort.host + " port: " + hostPort.port);
        HostPort hostPort1 = SocketProcessUtil.getHostPort(socketChannel);
        log.info("hostport from sss handshake request: ip: " + hostPort1.host + " port: " + hostPort1.port);
        /**
         * If the hostPort is valid
         */
        if (hostPort != null) {
            /**
             * If the handshake has already been completed
             */
//            if (socketChannelSet.contains(socketChannel)) {
//                String content = ProtocolUtils.getInvalidProtocol("handshaking has already been completed");
//                SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
                /**
                 * Delete the corresponding host port in the peerSet, close the socket
                 */
//            }
            /**
             * If the maximum incomming connections have been reached:
             */
//            else if (socketChannelSet.size() + 1 > ConstUtil.MAXIMUM_INCOMMING_CONNECTIONS) {
//                List list = new ArrayList(peerSet);
//                String content = ProtocolUtils.getConnectionRefusedRequest(list);
//                client.replyRequest(socketChannel, content, true);
//                log.info("send CONNECTION_REFUSED");
//            else {
                /**
                 * If everything is fine, establish the connection and send back handshake response
                 */
                String content = "";
                if (ConstUtil.MODE.equals(ConstUtil.UDP_MODE)) {
                    content = ProtocolUtils.getHandShakeResponse(new HostPort(ConstUtil.IP, ConstUtil.UDP_PORT).toDoc());
                } else {
                    content = ProtocolUtils.getHandShakeResponse(new HostPort(ConstUtil.IP, ConstUtil.PORT).toDoc());
                }
                client.replyRequest(socketChannel, content, false);
                socketChannelSet.add(socketChannel);
                peerSet.put(socketChannel, hostPort.toDoc());
                if (ContextManager.eventContext.get(socketChannel) == null) {
                    // create new context to manage this socketchannel
                    ContextManager.eventContext.put(socketChannel, new ConcurrentHashMap<>(20));
                } else {
                    ContextManager.eventContext.get(socketChannel).remove(ConstUtil.HANDSHAKE_TOKEN);
                }

                log.info("send HANDSHAKE_RESPONSE");
//            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }

    @Override
    public void processSuccessResponse(SocketChannel socketChannel, Document document) {
        HostPort hostPort = new HostPort((Document) document.get("hostPort"));
        log.info("hostport from handshake response: ip: " + hostPort.host + " port: " + hostPort.port);
        HostPort hostPort1 = SocketProcessUtil.getHostPort(socketChannel);
        log.info("hostport from sss handshake response: ip: " + hostPort1.host + " port: " + hostPort1.port);

        Map<String, EventDetail> events = ContextManager.eventContext.get(socketChannel);
        if (events == null) {
            log.info("handshake response invalid: invalid socketchannel");
            return;
        }
//        EventDetail eventDetail = events.get(ConstUtil.HANDSHAKE_TOKEN);
//        if(eventDetail == null || eventDetail.getCommand() != ConstUtil.HANDSHAKE_REQUEST) {
//            log.info("handshake response invalid: no request");
//            return;
//        }

        if (hostPort != null) {

            /**
             * get the hostport lists to this hostPort to see if there should be a response
             */
            boolean sentRequestBefore = handshakeReqHistory.contains(socketChannel) && !socketChannelSet.contains(socketChannel);
            //boolean sentRequestBefore = handshakeReqHistory.contains(hostPort) && !peerSet.contains(hostPort.toDoc());
            if (sentRequestBefore) {
                events.remove(ConstUtil.HANDSHAKE_TOKEN);
                socketChannelSet.add(socketChannel);
                peerSet.put(socketChannel, hostPort.toDoc());
                // create new context to manage this socketchannel
//                ContextManager.eventContext.put(socketChannel, new ConcurrentHashMap<>(20));
                log.info("establish Connection");
            } else {
                events.remove(ConstUtil.HANDSHAKE_TOKEN);
                String content = ProtocolUtils.getInvalidProtocol("Invalid handshake response.");
//                SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }

    @Override
    public void processRejectResponse(SocketChannel socketChannel, Document document) {
        log.info("Peers in connection: " + document.getString("message"));
        Map<String, EventDetail> events = ContextManager.eventContext.get(socketChannel);
        if (events == null) {
            log.info("handshake response invalid: invalid socketchannel");
            return;
        }
        /**
         * Check if it has sent a handshake request before.
         * yes - attempt to establish connection with its neighbour
         * no - send invalid_protocol
         */

        HostPort hostPort = SocketProcessUtil.getHostPort(socketChannel);
        boolean handshakeBefore = handshakeReqHistory.contains(socketChannel);
//        if (handshakeBefore) {
            events.remove(ConstUtil.HANDSHAKE_TOKEN);
            ContextManager.eventContext.remove(socketChannel);
            List<Document> existingPeers = (List<Document>) document.get("peers");
            if (existingPeers.size() == 0) {
                log.info("No existing peers.");
                return;
            }
            HostPort firstPeers = new HostPort(existingPeers.get(0));
            String handshakeRequest = "";
            if (ConstUtil.MODE.equals(ConstUtil.UDP_MODE)) {
                handshakeRequest = ProtocolUtils.getHandShakeRequest(new HostPort(Configuration.getConfigurationValue("advertisedName")
                        , ConstUtil.UDP_PORT).toDoc());
            } else {
                handshakeRequest = ProtocolUtils.getHandShakeRequest(new HostPort(Configuration.getConfigurationValue("advertisedName")
                        , ConstUtil.PORT).toDoc());
            }
            log.info("send new request: "+ firstPeers.host + firstPeers.port);
            SocketChannel nsc = client.sendRequest(handshakeRequest, firstPeers.host, firstPeers.port);
            /**
             * record information which is used to timeout and retry
             */

            Map<String, EventDetail> details = new ConcurrentHashMap<>(20);
            if (ContextManager.eventContext.get(nsc) != null) {
                details = ContextManager.eventContext.get(nsc);
            }
            details.put(ConstUtil.HANDSHAKE_TOKEN,
                    new EventDetail(ConstUtil.HANDSHAKE_TOKEN, null, handshakeRequest,
                            ConstUtil.HANDSHAKE_REQUEST, System.currentTimeMillis(),false,0));
            ContextManager.eventContext.put(nsc, details);
            /**
             * The peer that tried to connect should do a breadth first search of peers in the peers list, attempt to make a connection to one of them.
             */

//        } else {
//            String invalidResponse = ProtocolUtils.getInvalidProtocol("Not waiting for a handshake response from this peer");
//            SocketProcessUtil.sendRejectResponse(socketChannel,invalidResponse, socketChannelSet, peerSet);
//        }
    }
}
