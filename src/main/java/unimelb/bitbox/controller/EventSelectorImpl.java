package unimelb.bitbox.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventSelectorImpl implements EventSelector {
    public boolean serverStatus = false;
    public Selector selector;
    private ExecutorService fixedThreadPool;
    private static EventSelectorImpl eventSelector = null;
    public Map<SelectionKey, Boolean> handingMap;

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
    private EventSelectorImpl() {
        fixedThreadPool = Executors.newFixedThreadPool(4);
        try {
            selector = Selector.open();
            handingMap = new ConcurrentHashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * register socketChannel
     * @param socketChannel
     * @param operation
     * @return
     */
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

    public void ControllerRunning(int port) {
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();

            ServerSocket ss = serverSocketChannel.socket();
            ss.bind(new InetSocketAddress(port));
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

            if (numberOfPrepared > 0) {
                int i = 0;
                System.out.println("NP:"+numberOfPrepared);
                Set selectedKeys = selector.selectedKeys();
                Iterator keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {

                    SelectionKey key = (SelectionKey) keyIterator.next();
                    if (handingMap.get(key) != null) {
                        keyIterator.remove();
                        continue;
                    }
                    handingMap.put(key, true);
                    EventHandler eventHandler = new EventHandler(key);
//                    key.cancel();
//                    eventHandler.run();
                    fixedThreadPool.execute(eventHandler);
                    System.out.println(i++);
                    keyIterator.remove();
                }

            }
        }
    }
}
