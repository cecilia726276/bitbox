package unimelb.bitbox.util;

import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.ProtocolUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class SocketProcessUtil {
    public static HostPort getHostPort(SocketChannel socketChannel) {
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
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

    public static void sendRejectResponse(SocketChannel socketChannel, String content, Set socketChannelSet, Set peerSet) {
        Client client = ClientImpl.getInstance();
        client.replyRequest(socketChannel, content, true);
        socketChannelSet.remove(socketChannel);
        InetSocketAddress socketAddress;
        HostPort hostPort = getHostPort(socketChannel);
        /**
         * update existing connections
         */
        if (peerSet.contains(hostPort.toDoc())) {
            peerSet.remove(hostPort.toDoc());
        }

        client.closeSocket(socketChannel);
    }
}