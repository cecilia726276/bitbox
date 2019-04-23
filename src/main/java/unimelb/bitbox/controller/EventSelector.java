package unimelb.bitbox.controller;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

public interface EventSelector {
    /**
     *
     * @param socketChannel
     * @param operation
     * @return
     */
    public SelectionKey registerChannel(SocketChannel socketChannel, Integer operation);

    /**
     * Run the controller
     */
    public void controllerRunning();

    /**
     * get the selector
     * @return
     */
    public Selector getSelector();

    /**
     * create connection
     * @param socketChannel
     * @return
     */
    public boolean createConnection(SocketChannel socketChannel);

    /**
     * remove connection
     * @param socketChannel
     * @return
     */
    public boolean removeConnection(SocketChannel socketChannel);

}
