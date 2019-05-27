package unimelb.bitbox.util;

import unimelb.bitbox.ServerMain;
import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.udpcontroller.FakeSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;

public class SocketProcessUtil {
    public static HostPort getHostPort(SocketChannel socketChannel) {
        try {
            InetSocketAddress socketAddress = null;
            if (ConstUtil.MODE.equals(ConstUtil.TCP_MODE)) {
                socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
            } else {
                socketAddress = (InetSocketAddress) ((FakeSocketChannel)socketChannel).getSocketAddress();
            }
            String ip = socketAddress.getAddress().toString();
            int port = socketAddress.getPort();
            HostPort hostPort = new HostPort(ip, port);
            //   log.info("retrieved hostport: ip:"+ hostPort.host + "port: " + port);
            return hostPort;
        } catch (IOException e) {
            String content = ProtocolUtils.getInvalidProtocol("can't get address");
            //   sendRejectResponse(socketChannel, content);
            e.printStackTrace();
            return null;
        }
    }
    // TODO: 这个函数直接把channel关掉是不是不太好啊
    public static void sendRejectResponse(SocketChannel socketChannel, String content, Set socketChannelSet, Map peerSet) {
        Client client = ClientImpl.getInstance();
        client.replyRequest(socketChannel, content, true);
        socketChannelSet.remove(socketChannel);
        InetSocketAddress socketAddress;
        HostPort hostPort = getHostPort(socketChannel);
        /**
         * update existing connections
         */
        peerSet.remove(socketChannel);

    }
    public static void processCDResponse(Document document, String command, SocketChannel socketChannel,  Set socketChannelSet, Map peerSet) {
        ServerMain.log.info(command);
        ServerMain.log.info("status: " + document.getBoolean("status") + ", message: " + document.getString("message"));
        // 此处需要判断状态机 - host有没有给这个peer发送过FILE_CREATE_REQUEST/DELETE请求
        boolean sendCreateRequest = true;
        if (sendCreateRequest) {
            // 此处需要更新状态机 - host已经准备好收到bytes了
        } else {
            String content = ProtocolUtils.getInvalidProtocol("Invalid Response.");
            sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }
}