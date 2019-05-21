package unimelb.bitbox.udpcontroller;

import unimelb.bitbox.ServerMain;
import unimelb.bitbox.util.ConstUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;

public class TestNioUdp2 {

    public static class testRun implements Runnable {

        @Override
        public void run() {
            UdpSelector udpSelector = UdpSelector.getInstance();
            udpSelector.startServer(ConstUtil.PORT);
        }
    }

    public static void main(String[] args) {
        Thread thread = new Thread(new testRun());
        thread.start();
        UdpSelector udpSelector = UdpSelector.getInstance();
        try {
            ServerMain serverMain2 = new ServerMain();
            udpSelector.setServerMain(serverMain2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
//        UdpMessage udpMessage = new UdpMessage(
//                new InetSocketAddress("localhost",9961), "I love you three thousand");
//        udpSelector.registerWrite(udpMessage);
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        UdpMessage udpMessage2 = new UdpMessage(
//                new InetSocketAddress("localhost",9961), "I love you three thousand");
//        udpSelector.registerWrite(udpMessage2);
    }
}
