package unimelb.bitbox.controller;

import unimelb.bitbox.draft.Coder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

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

    private boolean response(SocketChannel socketChannel, ByteBuffer buf) {
        try {
            socketChannel.configureBlocking(false);
            SelectionKey selectionKey = selector.registerChannel(socketChannel, SelectionKey.OP_WRITE);
            selectionKey.attach(buf);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        switch (event) {
            case SelectionKey.OP_ACCEPT : {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                selectionKey.attachment();
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("1111");
                    socketChannel.configureBlocking(false);
                    System.out.println("23333");
                    selector.registerChannel(socketChannel, SelectionKey.OP_READ);
                    System.out.println("4444");
                    EventSelectorImpl.getInstance().serverStatus = false;
//                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case SelectionKey.OP_CONNECT: {

                String content = (String) selectionKey.attachment();
                ByteBuffer buf = ByteBuffer.allocate(content.length());
                buf.put(content.getBytes());
                response((SocketChannel) selectionKey.channel(), buf);
                break;
            }
            case SelectionKey.OP_READ: {
                // a channel is ready for reading
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                System.out.println("i am read");
                try {
                    StringBuffer hhd = new StringBuffer();
//                byteBuffer.flip();
                    System.out.println("i am read2");

                    while (socketChannel.read(byteBuffer) != -1) {
                        byteBuffer.flip();
                        hhd.append(Coder.INSTANCE.getDecoder().decode(byteBuffer).toString());
                        byteBuffer.flip();
                        byteBuffer.clear();
                    }
                    System.out.println("i am read3");

                    System.out.println(hhd.toString());
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case SelectionKey.OP_WRITE: {
                // a channel is ready for writing
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                try {
                    socketChannel.write(byteBuffer);
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
        EventSelectorImpl.getInstance().handingMap.remove(selectionKey);
    }
}
