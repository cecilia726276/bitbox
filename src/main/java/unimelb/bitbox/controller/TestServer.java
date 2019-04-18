package unimelb.bitbox.controller;

import unimelb.bitbox.Server;
import unimelb.bitbox.ServerDemo;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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
