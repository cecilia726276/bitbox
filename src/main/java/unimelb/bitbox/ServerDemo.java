package unimelb.bitbox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ServerDemo implements Server {
    public static void main(String[] args) throws IOException {
        ServerDemo server = new ServerDemo();
        server.startServer(9999);
    }

    /**
     * start the server
     *
     * @param serverPort
     */
    @Override
    public void startServer(int serverPort) throws IOException {
        //open the server channel
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            //bind the server port
            server.socket().bind(new InetSocketAddress(serverPort));
            //set unblocking mode
            server.configureBlocking(false);
            //open the selector
            Selector selector = Selector.open();
            //register the key to monitor connection
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Server starts......");
            for (; ; ) {
                //blocks until request comes in
                selector.select();
                System.out.println("Processing request");
                Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                //polling
                while (keyIter.hasNext()) {
                    SelectionKey key = keyIter.next();
                    // see if the key can be processed
                    if (key.isAcceptable()) {
                        this.doAccept(key);
                    }
                    if (key.isReadable()) {
                        System.out.println(this.doRead(key));
                    }
                    keyIter.remove();
                }

            }
        }
    }

    /**
     * buffer size
     */
    private static final int SIZE = 1024;

    /**
     * accept request
     *
     * @param key
     * @throws IOException
     */
    private void doAccept(SelectionKey key) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        channel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(SIZE));
        System.out.println("Connecting successfully!");
    }

    /**
     * read message
     *
     * @param key
     * @return mes
     * @throws IOException
     */
    private String doRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        String mes = "";
        if (channel.read(buffer) == -1) {
            channel.shutdownInput();
            channel.shutdownOutput();
            channel.close();
        } else {
            // Once you need to read the data,
            // we need to switch the buffer from writing mode into reading mode using the flip() method call.
            buffer.flip();
            //read the content
            mes = Coder.INSTANCE.getDecoder().decode(buffer).toString();
            buffer.clear();
            channel.register(key.selector(), SelectionKey.OP_READ, buffer);
        }
        return mes;
    }
}
