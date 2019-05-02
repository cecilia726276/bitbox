package unimelb.bitbox.service;

import unimelb.bitbox.util.Document;

import java.nio.channels.SocketChannel;

public interface HandshakeEventHandler {

    public void processRequest(SocketChannel socketChannel, Document document);
    public void processSuccessResponse(SocketChannel socketChannel, Document document);
    public void processRejectResponse(SocketChannel socketChannel, Document document);

}
