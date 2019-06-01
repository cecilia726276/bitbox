package unimelb.bitbox.controller;

import unimelb.bitbox.udpcontroller.FakeSocketChannel;
import unimelb.bitbox.udpcontroller.UdpMessage;
import unimelb.bitbox.udpcontroller.UdpSelector;
import unimelb.bitbox.util.ConstUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class ClientImpl implements Client {
    private EventSelector eventSelector;
    private UdpSelector udpSelector;
    private static ClientImpl client =  new ClientImpl();
    public static ClientImpl getInstance() {
        return client;
    }
    private ClientImpl() {
        eventSelector = EventSelectorImpl.getInstance();
        udpSelector = UdpSelector.getInstance();
    }



    @Override
    public SocketChannel sendRequest(String content, String ip, int port) {
        if (ConstUtil.MODE.equals(ConstUtil.TCP_MODE)) {
            try {
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.connect(new InetSocketAddress(ip, port));
                SelectionKey selectionKey = eventSelector.registerChannel(socketChannel, SelectionKey.OP_CONNECT);
                selectionKey.attach(content);
                Selector s = eventSelector.getSelector();
                eventSelector.getServerMain().addToHandshakeReqHistory(socketChannel);
                eventSelector.getTimeoutManager().put(socketChannel, new Date());
                s.wakeup();
                System.out.println("send1");
                return socketChannel;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else if (ConstUtil.MODE.equals(ConstUtil.UDP_MODE)) {
            try {
                UdpMessage udpMessage = new UdpMessage(new InetSocketAddress(ip, port), content);
                udpSelector.registerWrite(udpMessage);
                FakeSocketChannel fakeSocketChannel = new FakeSocketChannel(new InetSocketAddress(ip, port));
                udpSelector.getServerMain().addToHandshakeReqHistory(fakeSocketChannel);
                eventSelector.getTimeoutManager().put(fakeSocketChannel, new Date());
                return fakeSocketChannel;
            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean replyRequest(SocketChannel socketChannel, String content, boolean isFinal) {
        System.out.println("send2");

        if (EventSelectorImpl.clientSockets.contains(socketChannel) || ConstUtil.MODE.equals(ConstUtil.TCP_MODE)) {
            return CommonOperation.registerWrite(socketChannel, content, isFinal, eventSelector);
        } else if (ConstUtil.MODE.equals(ConstUtil.UDP_MODE)) {
            UdpMessage udpMessage = new UdpMessage(((FakeSocketChannel)socketChannel).getSocketAddress(), content);
            udpSelector.registerWrite(udpMessage);
            if(isFinal) {
                udpSelector.getServerMain().deletePeer(socketChannel);
                udpSelector.removeConnection(((FakeSocketChannel) socketChannel).getSocketAddress());
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean closeSocket(SocketChannel socketChannel) {
        System.out.println("closeSocket has been used");
        if (EventSelectorImpl.clientSockets.contains(socketChannel) || ConstUtil.MODE.equals(ConstUtil.TCP_MODE)) {
            eventSelector.getServerMain().deletePeer(socketChannel);
            if (eventSelector.removeConnection(socketChannel)) {
                return true;
            } else {
                return false;
            }
        } else if (ConstUtil.MODE.equals(ConstUtil.UDP_MODE)) {
            udpSelector.getServerMain().deletePeer(socketChannel);
            udpSelector.removeConnection(((FakeSocketChannel) socketChannel).getSocketAddress());
            return true;
        }
        return true;
    }
}
