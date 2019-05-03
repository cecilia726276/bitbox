package unimelb.bitbox.controller;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommonOperation {

    public static boolean registerWrite(SocketChannel socketChannel, String content, boolean isFinal, EventSelector selector) {
        try {
            System.out.println("write:" + content +" " +isFinal);
            socketChannel.configureBlocking(false);
            selector.registerChannel(socketChannel, SelectionKey.OP_WRITE);
            Map<SocketChannel, Attachment> writeAttachments = EventSelectorImpl.getInstance().writeAttachments;
            Attachment attachment = writeAttachments.get(socketChannel);
            if (attachment == null) {
                attachment = new Attachment(false, new ConcurrentLinkedQueue());
                writeAttachments.put(socketChannel, attachment);
            }
            System.out.println("attachment size:"+ attachment.getContent().size());
            attachment.getContent().add(content);
            attachment.setFinished(isFinal);
            Selector s =  selector.getSelector();
            s.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean registerRead(SocketChannel socketChannel, EventSelector selector) {
        try {
            socketChannel.configureBlocking(false);
            selector.registerChannel(socketChannel, SelectionKey.OP_READ);
            selector.getSelector().wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
