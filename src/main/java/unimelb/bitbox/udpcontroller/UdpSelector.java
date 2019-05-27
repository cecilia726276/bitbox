package unimelb.bitbox.udpcontroller;

import unimelb.bitbox.ServerMain;
import unimelb.bitbox.controller.EventHandler;
import unimelb.bitbox.message.Coder;
import unimelb.bitbox.util.ConstUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class UdpSelector {
    private Selector selector;
    private static UdpSelector udpSelector = new UdpSelector();
    private Queue<UdpMessage> messages = new LinkedBlockingQueue<>();
    private DatagramChannel datagramChannel;
    private ByteBuffer byteBuffer;
    private ServerMain serverMain;
    private Set<SocketAddress> connectionControl;
    public ServerMain getServerMain() {
        return serverMain;
    }
    public void setServerMain(ServerMain serverMain) {
        this.serverMain = serverMain;
    }

    public static UdpSelector getInstance() {
        return udpSelector;
    }
    private UdpSelector(){
        try {
            byteBuffer = ByteBuffer.allocate(1024000);
            selector = Selector.open();
            datagramChannel = DatagramChannel.open();
            connectionControl = Collections.synchronizedSet(new HashSet<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addConnection(SocketAddress socketAddresss) {
        if (connectionControl.size() >= ConstUtil.MAXIMUM_INCOMMING_CONNECTIONS) {
            serverMain.replyConnectionError(new FakeSocketChannel(socketAddresss));
            return;
        } else {
            connectionControl.add(socketAddresss);
        }
    }
    public void removeConnection(SocketAddress socketAddress){
        connectionControl.remove(socketAddress);
    }

    public void registerWrite(UdpMessage udpMessage){
        try {
            messages.add(udpMessage);
            SelectionKey selectionKey = datagramChannel.register(selector, SelectionKey.OP_WRITE);
            System.out.println(selectionKey.interestOps());
            selector.wakeup();
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    public void startServer(int port) {
        System.out.println("Port:"+port+" Server start.");
        try {
            System.out.println(port);
            datagramChannel.bind(new InetSocketAddress(port));
            datagramChannel.configureBlocking(false);
            datagramChannel.register(selector, SelectionKey.OP_READ);
            int numberOfPrepared = 0;
            while(true) {
                System.out.println("test selector");
                numberOfPrepared = selector.select();
                if(numberOfPrepared > 0) {
                    Set selectedKeys = selector.selectedKeys();
                    Iterator keyIterator = selectedKeys.iterator();
                    SelectionKey key = null;
                    while (keyIterator.hasNext()) {
                        key = (SelectionKey) keyIterator.next();
                        if(!key.isValid()) {
                            continue;
                        }

                        if(key.isReadable()) {
                            System.out.println("reading");
                            DatagramChannel channel = (DatagramChannel) key.channel();
                            byteBuffer.clear();
                            SocketAddress socketAddress = channel.receive(byteBuffer);
                            byteBuffer.flip();
                            String content = "";
                            if (socketAddress != null) {
                                content = Coder.INSTANCE.getDecoder().decode(byteBuffer).toString();
                                System.out.println("Received from:" + socketAddress.toString());
                                System.out.println("Said:"+ content);
                            }
                            byteBuffer.clear();
                            //fake socket channel ohhhh
                            FakeSocketChannel fakeSocketChannel = new FakeSocketChannel(socketAddress);
                            if (serverMain.checkPeer(fakeSocketChannel)) {
                                addConnection(fakeSocketChannel.getSocketAddress());
                            }
                            serverMain.processRequest(fakeSocketChannel, content);
                        }

                        if(key.isWritable()) {
                            System.out.println("writing");
                            DatagramChannel channel = (DatagramChannel) key.channel();
                            UdpMessage udpMessage = null;
                            while(!messages.isEmpty()) {
                                udpMessage = messages.poll();
                                System.out.println("I write:"+udpMessage.getMessage());
                                byteBuffer.clear();
                                byteBuffer.put(udpMessage.getMessage().getBytes());
                                byteBuffer.flip();
                                channel.send(byteBuffer, udpMessage.getSocketAddress());
                                byteBuffer.clear();
                            }
                            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                            channel.register(selector, SelectionKey.OP_READ);
                        }
                        keyIterator.remove();
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }

}
