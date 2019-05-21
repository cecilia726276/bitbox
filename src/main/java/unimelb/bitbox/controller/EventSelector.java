package unimelb.bitbox.controller;

import unimelb.bitbox.ServerMain;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;

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

    /**
     * get thread pool
     * @return
     */
    public ExecutorService getFixedThreadPool();

    /**
     * get timeout manager
     * @return
     */
    public Map<SocketChannel, Date> getTimeoutManager();

    public ServerMain getServerMain();

    public boolean isClientControlSocket(ServerSocketChannel serverSocketChannel);

    }
