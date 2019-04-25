package unimelb.bitbox.controller;

public class TestServer {

    public static class ServerRun implements Runnable {

        @Override
        public void run() {
            EventSelector eventSelector = EventSelectorImpl.getInstance();
            System.out.println("starting");
            eventSelector.controllerRunning(8111);

        }
    }

    public static void main(String args[]) {
        Thread thread = new Thread(new ServerRun());
        thread.start();
        System.out.println("start1");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("start2");
//        EventSelector eventSelector = EventSelectorImpl.getInstance();
        Client client = new ClientImpl();
        client.sendRequest("hahahahah", "localhost", 8112);
        client.sendRequest("heheheh", "localhost", 8112);
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.sendRequest("heiheiheihie", "localhost", 8112);
        }
//        try {
//            ServerDemo serverDemo = new ServerDemo();
//            serverDemo.startServer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }


}
