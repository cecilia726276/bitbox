package unimelb.bitbox.controller;

import unimelb.bitbox.draft.ClientDemo;

import java.io.IOException;

public class TestClient {

    public static void main(String args[]) throws IOException {
//        EventSelector eventSelector = EventSelectorImpl.getInstance();
//        eventSelector.ControllerRunning(1121);
//        Client client = new ClientImpl();
//        client.sendRequest("hahahahah", "localhost", 1111);
        ClientDemo clientMain = new ClientDemo();
        clientMain.startServer("localhost", 8111);

    }
}
