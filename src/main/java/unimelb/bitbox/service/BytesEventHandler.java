package unimelb.bitbox.service;

import unimelb.bitbox.util.Document;

import java.nio.channels.SocketChannel;

public interface BytesEventHandler {
    public void processRequest(SocketChannel socketChannel, Document document);
    public void processResponse(SocketChannel socketChannel, Document document);

}
