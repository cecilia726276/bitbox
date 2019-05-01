package unimelb.bitbox.controller;

import unimelb.bitbox.message.Coder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Map;

public class EventHandler implements Runnable{
    private SelectionKey selectionKey;
    private EventSelector selector;
    private int event;

    public EventHandler(SelectionKey selectionKey) {
        this.selectionKey = selectionKey;
        this.selector = EventSelectorImpl.getInstance();
        if (selectionKey.isAcceptable()) {
            System.out.println("ACCEPT");
            event = SelectionKey.OP_ACCEPT;
        } else if (selectionKey.isReadable()) {
            System.out.println("READ");
            selector.getTimeoutManager().remove(selectionKey.channel());
            event = SelectionKey.OP_READ;
        } else if (selectionKey.isConnectable()) {
            System.out.println("CONNECT");
            selector.getTimeoutManager().remove(selectionKey.channel());
            event = SelectionKey.OP_CONNECT;
        } else if (selectionKey.isWritable()) {
            System.out.println("WRITE");
            event = SelectionKey.OP_WRITE;
        }
    }

    private void acceptOperation () {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
         //   System.out.println(socketChannel.socket().getLocalAddress()+":"+socketChannel.socket().getPort());
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
                return;
            }
        }
        CommonOperation.registerWrite((SocketChannel) selectionKey.channel(), content, false, selector);
    }
    private void writeOperation () {
        // a channel is ready for writing
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Attachment attachment = (Attachment) selectionKey.attachment();
        String content = attachment.getContent();
        ByteBuffer byteBuffer = ByteBuffer.allocate(2*content.length());
        byteBuffer.clear();
        byteBuffer.put(content.getBytes());
        byteBuffer.flip();
        try {
            socketChannel.write(byteBuffer);
            System.out.println("Wirte："+content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (! byteBuffer.hasRemaining()) {
                // cancel write event
                selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
                if (attachment.isFinished) {
                    selector.removeConnection(socketChannel);
//                    selectionKey.cancel();
                } else {
                    System.out.println("i want read again");
                    selector.getTimeoutManager().put(socketChannel,new Date());
                    CommonOperation.registerRead(socketChannel, selector);
                }
            }

        /*    try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }
    private void readOperation () {
        System.out.println("read");
        // a channel is ready for reading
        ByteBuffer byteBuffer = ByteBuffer.allocate(1048576);
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        try {
            StringBuffer hhd = new StringBuffer();
          //  while (socketChannel.read(byteBuffer) != -1) {
            int num = socketChannel.read(byteBuffer);
            // socket has closed
            if (num == -1) {
                socketChannel.close();
                return;
            }
            if (byteBuffer.hasRemaining()) {
                byteBuffer.flip();

                hhd.append(Coder.INSTANCE.getDecoder().decode(byteBuffer).toString());
             //   System.out.println(hhd.toString());

                byteBuffer.flip();
                byteBuffer.clear();

            }
         //   }
            // need the interface of message process
            System.out.println(hhd.toString());
            if (hhd.toString().length() == 0) {
                System.out.println("zero problem "+num);
                return;
            }
         //   selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);

            if(selector.getServerMain()!=null){
                selector.getServerMain().processRequest(socketChannel,hhd.toString());
            }

//            socketChannel.close();
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
