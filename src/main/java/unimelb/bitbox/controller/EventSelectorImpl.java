package unimelb.bitbox.controller;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import unimelb.bitbox.ServerMain;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.ConstUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

public class EventSelectorImpl implements EventSelector {

    private Selector selector;

    @Override
    public ServerMain getServerMain() {
        return serverMain;
    }

    @Override
    public boolean isClientControlSocket(ServerSocketChannel serverSocketChannel) {
        if (serverSocketChannel.equals(clientControlChannel)) {
            return true;
        }
        return false;
    }

    public void setServerMain(ServerMain serverMain) {
        this.serverMain = serverMain;
    }

    private ServerMain serverMain;
    private ExecutorService fixedThreadPool;
    private static EventSelectorImpl eventSelector = null;
    public Map<SelectionKey, Boolean> handingMap;
    public Map<SocketChannel, Boolean> connectionGroup;
    public Map<SocketChannel, Attachment> writeAttachments;
    //
    public Map<SocketChannel, Date> timeoutManager;

    public static Set clientSockets = Collections.synchronizedSet(new HashSet<SocketChannel>());


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

    //废弃函数
    @Override
    public Map<SocketChannel, Date> getTimeoutManager() {
        return timeoutManager;
    }

    @Override
    public boolean createConnection(SocketChannel socketChannel) {
        if (connectionGroup.size() >= maxConnection) {
//                socketChannel.close();
//                // reply一个refuse
                serverMain.replyConnectionError(socketChannel);

            return false;
        } else {
            connectionGroup.put(socketChannel, true);
            return true;
        }

    }

    @Override
    public boolean removeConnection(SocketChannel socketChannel) {
        try {
            System.out.println("i am removed");
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventSelectorImpl.clientSockets.remove(socketChannel);
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
            timeoutManager = new ConcurrentHashMap<>();
            writeAttachments = new ConcurrentHashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean initThreadPool () {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("handler-pool-%d").build();
        fixedThreadPool = new ThreadPoolExecutor(6, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        return true;
    }
    private boolean initConfiguration () {
        port = Integer.valueOf(Configuration.getConfigurationValue("port"));
        maxConnection = ConstUtil.MAXIMUM_INCOMMING_CONNECTIONS;
        return true;
    }

    @Override
    public ExecutorService getFixedThreadPool() {
        return fixedThreadPool;
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
    private ServerSocketChannel clientControlChannel;
    @Override
    public void controllerRunning() {
        ServerSocketChannel serverSocketChannel = null;
        try {
            ServerSocket ss = null;
            if (ConstUtil.MODE.equals(ConstUtil.TCP_MODE)) {
                serverSocketChannel = ServerSocketChannel.open();

                ss = serverSocketChannel.socket();
                ss.bind(new InetSocketAddress(port));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            }
            /**
             * this server socket channel is serving for the client,
             * this is using the client port
             */
            clientControlChannel = null;
            clientControlChannel = ServerSocketChannel.open();
            ss = clientControlChannel.socket();
            ss.bind(new InetSocketAddress(ConstUtil.CLIENT_PORT));
            clientControlChannel.configureBlocking(false);
            clientControlChannel.register(selector, SelectionKey.OP_ACCEPT);

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
                        continue;
                    }
                    if (handingMap.get(key) != null) {
                        keyIterator.remove();
                        continue;
                    }
                    handingMap.put(key, true);
                    EventHandler eventHandler = new EventHandler(key);
//                    eventHandler.run();
                    fixedThreadPool.execute(eventHandler);
                    keyIterator.remove();
                }
                selectedKeys.clear();
            }
        }
    }
}
