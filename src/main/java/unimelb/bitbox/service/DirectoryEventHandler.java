package unimelb.bitbox.service;

import unimelb.bitbox.util.Document;

import java.nio.channels.SocketChannel;

/**
 * @Author SYZ
 * @create 2019-05-02 19:55
 */
public interface DirectoryEventHandler {
    public void processDirCreateRequest(SocketChannel socketChannel, Document document);
    public void processDirDeleteRequest(SocketChannel socketChannel, Document document);
    public void processDirCreateResponse(SocketChannel socketChannel, Document document);
    public void processDirDeleteResponse(SocketChannel socketChannel, Document document);
}
