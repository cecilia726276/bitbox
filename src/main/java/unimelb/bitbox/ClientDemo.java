package unimelb.bitbox;

import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemObserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientDemo implements FileSystemObserver {
//    public static void main(String[] args) throws IOException {
//        new ClientDemo().startServer();
//    }

    /**
     * start the client
     *
     *
     * @throws IOException
     */
    public void startServer(String ip, int port) throws IOException {
        try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, port))) {
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

    @Override
    public void processFileSystemEvent(FileSystemManager.FileSystemEvent fileSystemEvent) {

    }
}
