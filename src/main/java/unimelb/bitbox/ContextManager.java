package unimelb.bitbox;

import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.SocketProcessUtil;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class ContextManager {

    /**
     *  record steps of every processing event
     */
    public static Map<SocketChannel, Map<String, EventDetail>> eventContext = new ConcurrentHashMap<>();


    public static EventDetail checkEvents(SocketChannel socketChannel, String pathName) {
        Map<String, EventDetail> events = ContextManager.eventContext.get(socketChannel);
        if (events == null) {
            return null;
        }
        EventDetail eventDetail = events.get(pathName);
        return eventDetail;
    }
    /**
     * record the events of every file, in a same file, the events should be blocked
     */
//    private Map<SocketChannel, Map<String, Queue<FileSystemManager.FileSystemEvent>>> eventQueue;

//    private static ContextManager contextManager = new ContextManager();
//
//    public static ContextManager getInstance() {
//        return contextManager;
//    }
//    private ContextManager() {
//        eventHistory = new ConcurrentHashMap<>();
////        eventQueue = new ConcurrentHashMap<>();
//    }
}
