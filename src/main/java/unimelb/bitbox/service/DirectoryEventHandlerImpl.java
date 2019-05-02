package unimelb.bitbox.service;

import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.ConstUtil;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.SocketProcessUtil;

import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @Author SYZ
 * @create 2019-05-02 19:56
 */
public class DirectoryEventHandlerImpl implements DirectoryEventHandler {
    private FileSystemManager fileSystemManager;
    private Client client;
    private Logger log;
    private Set socketChannelSet;
    private Set peerSet;
    public DirectoryEventHandlerImpl(FileSystemManager fileSystemManager, Logger logger,
                                     Set socketChannelSet, Set peerSet) {
        this.fileSystemManager = fileSystemManager;
        this.client = ClientImpl.getInstance();
        this.log = logger;
        this.socketChannelSet = socketChannelSet;
        this.peerSet = peerSet;
    }

    /**
     * process DIRECTORY_CREATE_RESPONSE message
     * @param socketChannel
     * @param document
     */
    @Override
    public void processDirCreateRequest(SocketChannel socketChannel, Document document) {
        boolean isPeerOnTheList = socketChannelSet.contains(socketChannel);
        if (isPeerOnTheList) {
            String pathName = document.getString("pathName");
            if (!fileSystemManager.dirNameExists(pathName)) {
                boolean status = fileSystemManager.makeDirectory(pathName);
                if (status) {
                    String content = ProtocolUtils.getDirResponse(ConstUtil.DIRECTORY_CREATE_RESPONSE, pathName, "Create a directory successfully", true);
                    client.replyRequest(socketChannel, content, false);
                } else {
                    String content = ProtocolUtils.getDirResponse(ConstUtil.DIRECTORY_CREATE_RESPONSE, pathName, "Failed to create a directory", true);
                    client.replyRequest(socketChannel, content, false);
                }
            } else {
                String content = ProtocolUtils.getDirResponse(ConstUtil.DIRECTORY_CREATE_RESPONSE, pathName, "Directory already exists", false);
                client.replyRequest(socketChannel, content, false);
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("Peer is not connected");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }

    }

    /**
     * process DIRECTORY_DELETE_REQUEST message
     * @param socketChannel
     * @param document
     */
    @Override
    public void processDirDeleteRequest(SocketChannel socketChannel, Document document) {
        boolean isPeerOnTheList = socketChannelSet.contains(socketChannel);
        if (isPeerOnTheList) {
            String pathName = document.getString("pathName");
            if (fileSystemManager.dirNameExists(pathName)) {
                boolean status = fileSystemManager.deleteDirectory(pathName);
                if (status) {
                    String content = ProtocolUtils.getDirResponse(ConstUtil.DIRECTORY_DELETE_RESPONSE, pathName, "Delete a directory successfully", status);
                    client.replyRequest(socketChannel, content, false);
                } else {
                    String content = ProtocolUtils.getDirResponse(ConstUtil.DIRECTORY_DELETE_RESPONSE, pathName, "Failed to delete a directory", status);
                    client.replyRequest(socketChannel, content, false);
                }
            } else {
                String content = ProtocolUtils.getDirResponse(ConstUtil.DIRECTORY_DELETE_RESPONSE, pathName, "Directory doesn't exists", false);
                client.replyRequest(socketChannel, content, false);
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("Peer is not connected");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }

    @Override
    public void processDirCreateResponse(SocketChannel socketChannel, Document document) {
        SocketProcessUtil.processCDResponse(document, ConstUtil.DIRECTORY_CREATE_RESPONSE, socketChannel, socketChannelSet, peerSet);
    }

    @Override
    public void processDirDeleteResponse(SocketChannel socketChannel, Document document) {
        SocketProcessUtil.processCDResponse(document, ConstUtil.DIRECTORY_DELETE_RESPONSE, socketChannel, socketChannelSet, peerSet);
    }
}
