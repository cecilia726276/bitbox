package unimelb.bitbox.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ClientImpl implements Client {
    private EventSelector eventSelector;
    public ClientImpl() {
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
            s.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean replyRequest(SocketChannel socketChannel, String content) {
        try {
            socketChannel.configureBlocking(false);
            ByteBuffer buf = ByteBuffer.allocate(content.length());
            buf.put(content.getBytes());
            SelectionKey selectionKey = eventSelector.registerChannel(socketChannel, SelectionKey.OP_WRITE);
            selectionKey.attach(buf);
            Selector s =  eventSelector.getSelector();
            s.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
