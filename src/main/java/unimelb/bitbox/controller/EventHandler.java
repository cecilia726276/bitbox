package unimelb.bitbox.controller;

import unimelb.bitbox.message.Coder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Map;

public class EventHandler implements Runnable{
    private SelectionKey selectionKey;
    private EventSelector selector;
    private int event;

    public EventHandler(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.selector = EventSelectorImpl.getInstance();
        if (selectionKey.isAcceptable()) {
            event = SelectionKey.OP_ACCEPT;
        } else if (selectionKey.isReadable()) {
            event = SelectionKey.OP_READ;
        } else if (selectionKey.isConnectable()) {
            event = SelectionKey.OP_CONNECT;
        } else if (selectionKey.isWritable()) {
            event = SelectionKey.OP_WRITE;
        }
    }

    private void acceptOperation () {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            EventSelectorImpl.getInstance().handingMap.remove(selectionKey);
            if (!selector.createConnection(socketChannel)) {
                System.out.println("the number of connection is too much");
                return;
            }
            CommonOperation.registerRead(socketChannel, selector);
//          System.out.println("hah");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void connectOperation () {
        String content = (String) selectionKey.attachment();
        selectionKey.attach(new Attachment(false, content));
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        if (channel.isConnectionPending()) {
            try {
                if (channel.finishConnect()) {
                    System.out.println("client connect server succ");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        writeOperation();
        CommonOperation.registerWrite((SocketChannel) selectionKey.channel(), content, false, selector);
    }
    private void writeOperation () {
        // a channel is ready for writing
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Attachment attachment = (Attachment) selectionKey.attachment();
        String content = attachment.getContent();
        ByteBuffer byteBuffer = ByteBuffer.allocate(content.length());
        byteBuffer.clear();
        byteBuffer.put(content.getBytes());
        byteBuffer.flip();
        try {
            socketChannel.write(byteBuffer);
            if (attachment.isFinished) {
                selector.removeConnection(socketChannel);
            } else {
                CommonOperation.registerRead(socketChannel, selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (! byteBuffer.hasRemaining()) {
                // cancel write event
                selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
            }
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void readOperation () {
        // a channel is ready for reading
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        try {
            StringBuffer hhd = new StringBuffer();
            while (socketChannel.read(byteBuffer) != -1) {
                byteBuffer.flip();
                hhd.append(Coder.INSTANCE.getDecoder().decode(byteBuffer).toString());
                byteBuffer.flip();
                byteBuffer.clear();
            }
            // need the interface of message process
            System.out.println(hhd.toString());
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        switch (event) {
            case SelectionKey.OP_ACCEPT : {
                acceptOperation();
                break;
            }
            case SelectionKey.OP_CONNECT: {
                connectOperation();
                break;
            }
            case SelectionKey.OP_READ: {
                readOperation();
                break;
            }
            case SelectionKey.OP_WRITE: {
                writeOperation();
                break;
            }
            default:
                break;
        }
        EventSelectorImpl.getInstance().handingMap.remove(selectionKey);
    }
}
