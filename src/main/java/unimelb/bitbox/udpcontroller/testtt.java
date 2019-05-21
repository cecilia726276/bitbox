package unimelb.bitbox.udpcontroller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class testtt {
    public static void main(String[] args) {
        FakeSocketChannel fakeSocketChannel = new FakeSocketChannel(new InetSocketAddress("unimelb.net",6666));
        FakeSocketChannel fakeSocketChannel2 = new FakeSocketChannel(new InetSocketAddress("unimelb.anet",6666));

        System.out.println("done");
        System.out.println(fakeSocketChannel.hashCode());
        System.out.println(((SocketChannel)fakeSocketChannel).hashCode());
        System.out.println(fakeSocketChannel2.hashCode());
        System.out.println(((SocketChannel)fakeSocketChannel2).hashCode());
    }
}
