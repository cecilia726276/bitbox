package unimelb.bitbox.controller;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class CommonOperation {

    public static boolean registerWrite(SocketChannel socketChannel, String content, boolean isFinal, EventSelector selector) {
        try {
            System.out.println("write:" + content +" " +isFinal);
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = selector.registerChannel(socketChannel, SelectionKey.OP_WRITE);
            Attachment attachment = new Attachment(isFinal, content);
            selectionKey.attach(attachment);
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
