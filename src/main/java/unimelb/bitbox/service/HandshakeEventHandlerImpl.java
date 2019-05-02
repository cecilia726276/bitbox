package unimelb.bitbox.service;

import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.*;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class HandshakeEventHandlerImpl implements HandshakeEventHandler{
    private FileSystemManager fileSystemManager;
    private Client client;
    private Logger log;
    private Set socketChannelSet;
    private Set peerSet;
    private Set handshakeReqHistory;
    public HandshakeEventHandlerImpl(FileSystemManager fileSystemManager, Logger logger,
                                     Set socketChannelSet, Set peerSet, Set handshakeReqHistory) {
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
            if (socketChannelSet.contains(socketChannel)) {
                //if (peerSet.contains(hostPort.toDoc())) {
                String content = ProtocolUtils.getInvalidProtocol("handshaking has already been completed");
                SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
                /**
                 * Delete the corresponding host port in the peerSet, close the socket
                 */
            }
            /**
             * If the maximum incomming connections have been reached:
             */
            else if (socketChannelSet.size() + 1 > ConstUtil.MAXIMUM_INCOMMING_CONNECTIONS) {
                //else if (peerSet.size() + 1 > MAXIMUM_INCOMMING_CONNECTIONS) {
                List list = new ArrayList(peerSet);
                String content = ProtocolUtils.getConnectionRefusedRequest(list);
                client.replyRequest(socketChannel, content, true);
                log.info("send CONNECTION_REFUSED");
            } else {
                /**
                 * If everything is fine, establish the connection and send back handshake response
                 */
                String content = ProtocolUtils.getHandShakeResponse(new HostPort(ConstUtil.IP, ConstUtil.PORT).toDoc());
                client.replyRequest(socketChannel, content, false);
                socketChannelSet.add(socketChannel);
                peerSet.add(hostPort.toDoc());

                log.info("send HANDSHAKE_RESPONSE");
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }

    @Override
    public void processResponse(SocketChannel socketChannel, Document document) {
        HostPort hostPort = new HostPort((Document) document.get("hostPort"));
        log.info("hostport from handshake response: ip: " + hostPort.host + " port: " + hostPort.port);
        HostPort hostPort1 = SocketProcessUtil.getHostPort(socketChannel);
        log.info("hostport from sss handshake response: ip: " + hostPort1.host + " port: " + hostPort1.port);

        if (hostPort != null) {
            /**
             * get the hostport lists to this hostPort to see if there should be a response
             */
            boolean sentRequestBefore = handshakeReqHistory.contains(hostPort) && !socketChannelSet.contains(hostPort.toDoc());
            //boolean sentRequestBefore = handshakeReqHistory.contains(hostPort) && !peerSet.contains(hostPort.toDoc());
            if (sentRequestBefore) {
                socketChannelSet.add(socketChannel);
                peerSet.add(hostPort.toDoc());
                log.info("establish Connection");
            } else {
                String content = ProtocolUtils.getInvalidProtocol("Invalid handshake response.");
                SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }
}
