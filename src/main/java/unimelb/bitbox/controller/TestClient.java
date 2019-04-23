package unimelb.bitbox.controller;

import unimelb.bitbox.draft.ClientDemo;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestClient {
    public static class SimpleSend implements Runnable {

        @Override
        public void run() {
            ClientDemo clientMain = new ClientDemo();
            try {
                clientMain.startServer("localhost", 8111);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public static void main(String args[]) throws IOException {
//        EventSelector eventSelector = EventSelectorImpl.getInstance();
//        eventSelector.ControllerRunning(1121);
//        Client client = new ClientImpl();
////        client.sendRequest("hahahahah", "localhost", 1111);
//        ClientDemo clientMain = new ClientDemo();
//        clientMain.startServer("localhost", 8111);
//        clientMain.startServer("localhost", 8111);
//
//        clientMain.startServer("localhost", 8111);
//        clientMain.startServer("localhost", 8111);
//        clientMain.startServer("localhost", 8111);
         ExecutorService  fixedThreadPool = Executors.newFixedThreadPool(12);
         for (int i=0; i<12; i++) {
             fixedThreadPool.execute(new SimpleSend());
         }
    }
}
