package unimelb.bitbox.service;

import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.HostPort;

import java.nio.channels.SocketChannel;

public interface HandshakeEventHandler {

    public void processRequest(SocketChannel socketChannel, Document document);
    public void processResponse(SocketChannel socketChannel, Document document);

}
