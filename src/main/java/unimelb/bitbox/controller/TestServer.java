package unimelb.bitbox.controller;

public class TestServer {

    public static void main(String args[]) {
        EventSelector eventSelector = EventSelectorImpl.getInstance();
        eventSelector.ControllerRunning(8111);
//        try {
//            ServerDemo serverDemo = new ServerDemo();
//            serverDemo.startServer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
    }


}
