package unimelb.bitbox.controller;

import unimelb.bitbox.ServerMain;
import unimelb.bitbox.util.SyncRunner;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TestServer {

    public static class ServerRun implements Runnable {

        @Override
        public void run() {
            EventSelector eventSelector = EventSelectorImpl.getInstance();
            System.out.println("starting");
            eventSelector.controllerRunning();

        }
    }

    public static void main(String args[]) {
        EventSelector eventSelector = EventSelectorImpl.getInstance();
        ServerMain serverMain = null;
        try {
            serverMain = new ServerMain();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(new ServerRun());
        thread.start();
        System.out.println("start1");


        Thread thread1 = new Thread(new SyncRunner(serverMain));
        thread1.start();
        System.out.println("start2");

//        EventSelector eventSelector = EventSelectorImpl.getInstance();
    //    Client client = new ClientImpl();
      //  client.sendRequest("hahahahah", "localhost", 8112);
       // client.sendRequest("heheheh", "localhost", 8112);
//        while(true) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            client.sendRequest("heiheiheihie", "localhost", 8112);
//        }
//        try {
//            ServerDemo serverDemo = new ServerDemo();
//            serverDemo.startServer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
