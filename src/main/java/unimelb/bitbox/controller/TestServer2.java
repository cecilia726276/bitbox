package unimelb.bitbox.controller;

import unimelb.bitbox.ServerMain;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TestServer2 {
    public static void main(String args[]) {
        ServerMain serverMain = null;
        try {
            serverMain = new ServerMain();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        EventSelector eventSelector = EventSelectorImpl.getInstance();
        ((EventSelectorImpl) eventSelector).setServerMain(serverMain);
        eventSelector.controllerRunning(8112);
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
