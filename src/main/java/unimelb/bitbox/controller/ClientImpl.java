package unimelb.bitbox.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class ClientImpl implements Client {
    private EventSelector eventSelector;
    private static ClientImpl client =  new ClientImpl();
    public static ClientImpl getInstance() {
        return client;
    }
    private ClientImpl() {
        eventSelector = EventSelectorImpl.getInstance();
    }

    @Override
    public boolean sendRequest(String content, String ip, int port) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(ip, port));
            SelectionKey selectionKey = eventSelector.registerChannel(socketChannel, SelectionKey.OP_CONNECT);
            selectionKey.attach(content);
            Selector s = eventSelector.getSelector();
            eventSelector.getServerMain().addToHandshakeReqHistory(socketChannel);
            eventSelector.getTimeoutManager().put(socketChannel, new Date());
            s.wakeup();
            System.out.println("send1");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean replyRequest(SocketChannel socketChannel, String content, boolean isFinal) {
        System.out.println("send2");

        return CommonOperation.registerWrite(socketChannel, content, isFinal, eventSelector);
    }

    @Override
    public boolean closeSocket(SocketChannel socketChannel) {
        System.out.println("closeSocket has been used");
        if (eventSelector.removeConnection(socketChannel)) {
            return true;
        } else {
            return false;
        }
    }
}
