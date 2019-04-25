package unimelb.bitbox.controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import unimelb.bitbox.util.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class EventSelectorImpl implements EventSelector {

    private Selector selector;
    private ExecutorService fixedThreadPool;
    private static EventSelectorImpl eventSelector = null;
    public Map<SelectionKey, Boolean> handingMap;
    public Map<SocketChannel, Boolean> connectionGroup;

    // configure params
    private Integer port;
    private Integer maxConnection;

    public static EventSelectorImpl getInstance() {
        if (eventSelector == null) {
            synchronized (EventSelector.class) {
                if (eventSelector == null) {
                    eventSelector = new EventSelectorImpl();
                }
            }
        }
        return eventSelector;
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

    @Override
    public boolean createConnection(SocketChannel socketChannel) {
        if (connectionGroup.size() >= maxConnection) {
            try {
                socketChannel.close();
                // reply一个refuse

            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            connectionGroup.put(socketChannel, true);
            return true;
        }

    }

    @Override
    public boolean removeConnection(SocketChannel socketChannel) {
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionGroup.remove(socketChannel);
        return false;
    }


    private EventSelectorImpl() {
        initConfiguration();
        initThreadPool();
        try {
            selector = Selector.open();
            handingMap = new ConcurrentHashMap<>();
            connectionGroup = new ConcurrentHashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean initThreadPool () {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("handler-pool-%d").build();
        fixedThreadPool = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        return true;
    }
    private boolean initConfiguration () {
        port = Integer.valueOf(Configuration.getConfigurationValue("port"));
        maxConnection = Integer.valueOf(
                Configuration.getConfigurationValue("maximumIncommingConnections"));
        return true;
    }



    /**
     * register socketChannel
     * @param socketChannel
     * @param operation
     * @return
     */
    @Override
    public SelectionKey registerChannel(SocketChannel socketChannel, Integer operation) {
        SelectionKey selectionKey = null;
        try {
            selectionKey = socketChannel.register(selector, operation);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            return null;
        }

        return selectionKey;
    }
    @Override
    public void controllerRunning(int pp) {
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();

            ServerSocket ss = serverSocketChannel.socket();
            ss.bind(new InetSocketAddress(pp));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        int numberOfPrepared = 0;
        while (true) {
            // select prepared selector
            try {
                numberOfPrepared = selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            System.out.println("number:"+numberOfPrepared);
            if (numberOfPrepared > 0) {
                int i = 0;
                Set selectedKeys = selector.selectedKeys();
                Iterator keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {

                    SelectionKey key = (SelectionKey) keyIterator.next();
                    if (!key.isValid()) {
                        System.out.println("valid"+key.hashCode());
                        continue;
                    }
                    if (handingMap.get(key) != null) {
                        keyIterator.remove();
                        continue;
                    }
//                    System.out.println(key.hashCode());
                    handingMap.put(key, true);
                    EventHandler eventHandler = new EventHandler(key);
//                    eventHandler.run();
                    fixedThreadPool.execute(eventHandler);
                    keyIterator.remove();
                }
            }
        }
    }
}
