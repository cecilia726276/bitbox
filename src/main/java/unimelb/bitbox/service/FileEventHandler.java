package unimelb.bitbox.service;

import unimelb.bitbox.util.Document;

import java.nio.channels.SocketChannel;

public interface FileEventHandler {
    public void FileCreateRequestProcess(SocketChannel socketChannel, Document document);
    public void FileCreateResponseProcess(SocketChannel socketChannel, Document document);
    public void FileModifyRequestProcess(SocketChannel socketChannel, Document document);
    public void FileModifyResponseProcess(SocketChannel socketChannel, Document document);
    public void FileDeleteRequestProcess(SocketChannel socketChannel, Document document);
    public void FileDeleteResponseProcess(SocketChannel socketChannel, Document document);

}
