package unimelb.bitbox.controller;

import unimelb.bitbox.ContextManager;
import unimelb.bitbox.EventDetail;
import unimelb.bitbox.ServerMain;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.udpcontroller.FakeSocketChannel;
import unimelb.bitbox.udpcontroller.UdpSelector;
import unimelb.bitbox.util.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ClientMessageHandler {
    public static Logger log = Logger.getLogger(ServerMain.class.getName());
    private Map peerSet;
    private Set<SocketChannel> sockchannelSet;
    private Client client = ClientImpl.getInstance();
    private EventSelector eventSelector = EventSelectorImpl.getInstance();
    public static ClientMessageHandler clientMessageHandler = null;
    public static ClientMessageHandler getInstance() {
        return clientMessageHandler;
    }
    public ClientMessageHandler(Map peerSet, Set<SocketChannel> sockchannelSet) {
        this.peerSet = peerSet;
        this.sockchannelSet = sockchannelSet;
    }
    public void processEachRequest(SocketChannel socketChannel, String message) {
        Document document = null;
        String AESKey = "";

        try {
            document = Document.parse(message);

        }catch (Exception e) {
            log.info("parse err");
            return;
        }

        if (document == null) {
            log.info("parse err");
            return;
        }


        log.info("input String: " + message);
        Document newdocument = document;
        String command = "";
        String newcontent = document.getString("payload");
        if (newcontent != null) {
            // TODO:解密
            newcontent = AESKeyManager.AESdecrypt(socketChannel, newcontent);
            try {
                newdocument = Document.parse(newcontent);
                command = newdocument.getString("command");
            } catch (Exception e) {
                log.info("parse err");
            }
        } else {
            command = document.getString("command");
        }

        if(command != null) {
            switch (command) {
                case ConstUtil.LIST_PEERS_REQUEST: {
                    System.out.println(peerSet.size());
                    String content = ProtocolUtils.getListPeerResponse(peerSet);
                    String encrypted = AESKeyManager.AESencrypt(socketChannel, content);
                    client.replyRequest(socketChannel, ProtocolUtils.getPayload(encrypted), true);
                    break;
                }
                case ConstUtil.CONNECT_PEER_REQUEST: {
                    String host = newdocument.getString("host");
                    int port = (int)(newdocument.getLong("port"));
                    String handshakeRequest = "";
                    if (ConstUtil.MODE.equals(ConstUtil.UDP_MODE)) {
                        handshakeRequest = ProtocolUtils.getHandShakeRequest(new HostPort(Configuration.getConfigurationValue("advertisedName")
                                , ConstUtil.UDP_PORT).toDoc());
                    } else {
                        handshakeRequest = ProtocolUtils.getHandShakeRequest(new HostPort(Configuration.getConfigurationValue("advertisedName")
                                , ConstUtil.PORT).toDoc());
                    }
                    Map<String, EventDetail> events = new ConcurrentHashMap<>();
                    SocketChannel sc = client.sendRequest(handshakeRequest, host, port);
                    if (sc == null) {
                        String contents = ProtocolUtils.getClientResponse(
                                ConstUtil.CONNECT_PEER_RESPONSE, host, port, false, "failed to connected to Peer");
                        String encrypted = AESKeyManager.AESencrypt(socketChannel, contents);
                        client.replyRequest(socketChannel, ProtocolUtils.getPayload(encrypted), true);
                    } else {
                        ContextManager.eventContext.put(sc, events);
                        String contents = ProtocolUtils.getClientResponse(
                                ConstUtil.CONNECT_PEER_RESPONSE, host, port, true, "connected to Peer");
                        String encrypted = AESKeyManager.AESencrypt(socketChannel, contents);
                        client.replyRequest(socketChannel, ProtocolUtils.getPayload(encrypted), true);
                    }
//                    public boolean removeConnection(SocketChannel socketChannel);
                    break;
                }
                case ConstUtil.DISCONNECT_PEER_REQUEST: {
                    String host = newdocument.getString("host");
                    int port = (int)newdocument.getLong("port");
                    System.out.println("socket set size:"+sockchannelSet.size());
                    System.out.println("peer set size:"+peerSet.size());

                    for (SocketChannel sc : sockchannelSet) {
                        try {
                            String hosttmp = "";
                            int porttmp;
                            if (ConstUtil.MODE.equals(ConstUtil.TCP_MODE)) {
                                hosttmp = ((InetSocketAddress) sc.getRemoteAddress()).getHostName();
                                porttmp = ((InetSocketAddress) sc.getRemoteAddress()).getPort();
                            } else {
                                hosttmp = ((InetSocketAddress)((FakeSocketChannel)sc).getSocketAddress()).getHostName();
                                porttmp = ((InetSocketAddress)((FakeSocketChannel)sc).getSocketAddress()).getPort();
                                System.out.println("testtest"+hosttmp);
                                System.out.println("testtest"+((InetSocketAddress)((FakeSocketChannel)sc).getSocketAddress()).getHostString());
                            }
                            System.out.println("host:"+hosttmp+";port:"+porttmp);
                            System.out.println("host"+host+"port"+port);
                            if (hosttmp.equals(host)
                            && porttmp == port) {
                                System.out.println(peerSet.size());
                                peerSet.remove(sc);
                                sockchannelSet.remove(sc);
                                System.out.println(peerSet.size());

                                if (ConstUtil.MODE.equals(ConstUtil.TCP_MODE)) {
                                    eventSelector.removeConnection(sc);
                                } else {
                                    UdpSelector.getInstance().removeConnection(((FakeSocketChannel)sc).getSocketAddress());
                                }
                                String content = ProtocolUtils.getClientResponse(ConstUtil.DISCONNECT_PEER_RESPONSE,
                                        host,port,true,"disconnect from peer");
                                String encrypt = AESKeyManager.AESencrypt(socketChannel, content);
                                client.replyRequest(socketChannel, ProtocolUtils.getPayload(encrypt), true);
                                return;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    String content = ProtocolUtils.getClientResponse(ConstUtil.DISCONNECT_PEER_RESPONSE,
                            host, port,false,"connection not active");
                    String encrypt = AESKeyManager.AESencrypt(socketChannel, content);
                    client.replyRequest(socketChannel, ProtocolUtils.getPayload(encrypt), true);
                    break;
                }
                case ConstUtil.AUTH_REQUEST: {
                    String identity = document.getString("identity");
                    String key = ConstUtil.PUBLIC_KEYS.get(identity);
                    if (key == null) {
                        client.replyRequest(socketChannel,
                                ProtocolUtils.getAuthFailResponse("public key not found"), true);

                    } else {
                        // TODO:RSA 加密
                        // TODO:AEK 生成
                        String aesKey = AESKeyManager.generateAESKey(socketChannel);
                        System.out.println("AESKEY_origin: "+aesKey);

                        aesKey = RSAManager.RSAEncrypt(ConstUtil.PUBLIC_KEYS.get(identity), aesKey);
                        System.out.println("AESKEY: "+aesKey);
                        client.replyRequest(socketChannel,
                                ProtocolUtils.getAuthSuccessResponse(aesKey, "public key found"), false);

                    }
                    break;
                }
                default: {
                    break;
                }

            }
        }
    }

}
