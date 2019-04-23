package unimelb.bitbox.controller;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface Client {
    /**
     * send request to other peer
     * @param content
     * @param ip
     * @param port
     * @return
     */
    public boolean sendRequest(String content, String ip, int port);

    /**
     * send reply to other peer
     * @param socketChannel
     * @param content
     * @return
     */
    public boolean replyRequest(SocketChannel socketChannel, String content, boolean isFinal);

    /**
     *  close the communication
     * @param socketChannel
     * @return
     */
    public boolean closeSocket(SocketChannel socketChannel);
}
