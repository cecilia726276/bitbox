package unimelb.bitbox.controller;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface Client {
    public boolean sendRequest (String content, String ip, int port);
    public boolean replyRequest (SocketChannel socketChannel, String content);
}
