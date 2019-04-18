package unimelb.bitbox.controller;

import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface EventSelector {
    /**
     * Register the Channel to Selector
     * @param socketChannel
     * @param operation
     * @return
     */
    public SelectionKey registerChannel(SocketChannel socketChannel, Integer operation);

    /**
     * Run the controller
     */
    public void ControllerRunning(int port);

}
