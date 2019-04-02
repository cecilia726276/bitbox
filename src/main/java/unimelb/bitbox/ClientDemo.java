package unimelb.bitbox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientDemo implements Server {
    public static void main(String[] args) throws IOException {
        new ClientDemo().startServer(9999);
    }

    /**
     * start the client
     *
     * @param serverPort
     * @throws IOException
     */
    @Override
    public void startServer(int serverPort) throws IOException {
        try (SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", 9999))) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.clear();
            buffer.put("Message from client：“Hello server!”".getBytes());
            System.out.println("Complete sending");
            buffer.flip();
            channel.write(buffer);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
