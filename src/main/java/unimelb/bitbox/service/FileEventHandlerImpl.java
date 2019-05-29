package unimelb.bitbox.service;

import unimelb.bitbox.ContextManager;
import unimelb.bitbox.EventDetail;
import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class FileEventHandlerImpl implements FileEventHandler {
    private FileSystemManager fileSystemManager;
    private Client client;
    private Logger log;
    private Set socketChannelSet;
    private Map peerSet;
    private Set handshakeReqHistory;

    public FileEventHandlerImpl(FileSystemManager fileSystemManager, Logger log, Set socketChannelSet, Map peerSet, Set handshakeReqHistory) {
        this.fileSystemManager = fileSystemManager;
        this.log = log;
        this.socketChannelSet = socketChannelSet;
        this.peerSet = peerSet;
        this.handshakeReqHistory = handshakeReqHistory;
        client = ClientImpl.getInstance();
    }
    private String getFileBytesRequest(SocketChannel socketChannel, String content, long fileSize, Document fileDescriptor, String pathName) {
        client.replyRequest(socketChannel, content, false);
        long length = fileSize;
        if (fileSize / ConstUtil.BLOCKSIZE > 1) {
            length = ConstUtil.BLOCKSIZE;
        }
        String fileBytesRequest = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName, 0, length);
        return fileBytesRequest;
    }
    @Override
    public void FileCreateRequestProcess(SocketChannel socketChannel, Document document) {
        String pathName = document.getString("pathName");
//                RequestState requestState1 = new RequestState("FILE_CREATE_REQUEST",pathName);
//                RequestState requestState2 = new RequestState("FILE_CREATE_MODIFY",pathName);
        HostPort hostPort = SocketProcessUtil.getHostPort(socketChannel);
        log.info("hostport from sss file create request: ip: " + hostPort.host + " port: " + hostPort.port);
        boolean isPeerOnTheList = socketChannelSet.contains(socketChannel);

        //get context of events in this channel
        Map<String, EventDetail> eventdetails =  ContextManager.eventContext.get(socketChannel);

        //boolean isPeerOnTheList = checkOntheList(socketChannel,peerSet);
        if (isPeerOnTheList || eventdetails != null)//&& !checkInReqStateMap(requestState1,hostPort) && !checkInReqStateMap(requestState2,hostPort) && !existPathNameList.contains(pathName))
        {
            Document fileDescriptor = (Document) document.get("fileDescriptor");
            String md5 = fileDescriptor.getString("md5");
            long lastModified = fileDescriptor.getLong("lastModified");
            long fileSize = fileDescriptor.getLong("fileSize");

            //String pathName = document.getString("pathName");
            if (fileSystemManager.isSafePathName(pathName)) {
                if (!fileSystemManager.fileNameExists(pathName)) {
                    try {
                        boolean status = fileSystemManager.createFileLoader(pathName, md5, fileSize, lastModified);
                        /**
                         * If another file already exists with the same content,
                         * use that file's content (i.e. does a copy) to create the intended file.
                         */
                        if (fileSystemManager.checkShortcut(pathName)) {
                            fileSystemManager.cancelFileLoader(pathName);
                            String fileResponse = ProtocolUtils.getFileResponse(ConstUtil.FILE_CREATE_RESPONSE, fileDescriptor, pathName, true, "file create complete");
                            client.replyRequest(socketChannel, fileResponse, false);
                        } else {
                            if (status) {
                                String fileResponse = ProtocolUtils.getFileResponse(ConstUtil.FILE_CREATE_RESPONSE, fileDescriptor, pathName, true, "file loader ready");
                                String fileBytesRequest = getFileBytesRequest(socketChannel,fileResponse,fileSize,fileDescriptor,pathName);
                                long length = fileSize;
                                if (fileSize / ConstUtil.BLOCKSIZE > 1) {
                                    length = ConstUtil.BLOCKSIZE;
                                }
                                // 此处需要更新状态机--已更新
//                                        stateMap.get(hostPort.toDoc().toJson()).add(requestState);
//                                        existPathNameList.add(pathName);
                                //初始化file_bytes_response 的状态机（记录下自己已经发送了file_bytes_request）
                                if (client.replyRequest(socketChannel, fileBytesRequest, false)) {
                                    //add context of file byte request.
                                    EventDetail eventDetail = new EventDetail(pathName,fileDescriptor,fileBytesRequest,ConstUtil.FILE_BYTES_REQUEST, System.currentTimeMillis(),
                                            false,0);
                                    eventDetail.setPosition(0);
                                    eventDetail.setSentLength(length);
                                    eventdetails.put(pathName, eventDetail);
                                }
                            } else {
                                String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_CREATE_RESPONSE, fileDescriptor, pathName, status, "Failed to create file loader.");
                                client.replyRequest(socketChannel, content, false);
                            }

                        }
                    } catch (Exception e) {
                        String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_CREATE_RESPONSE, fileDescriptor, pathName, false, "the loader is no longer available in this case");
                        SocketProcessUtil.sendRejectResponse(socketChannel, content,socketChannelSet, peerSet);
                        e.printStackTrace();
                    }
                } else {
                    String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_CREATE_RESPONSE, fileDescriptor, pathName, false, "pathname already exists");
                    client.replyRequest(socketChannel, content, false);
                }
            } else {
                String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_CREATE_RESPONSE, fileDescriptor, pathName, false, "unsafe pathname given");
                client.replyRequest(socketChannel, content, false);
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("This peer has not been handshaked before.");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }

    @Override
    public void FileCreateResponseProcess(SocketChannel socketChannel, Document document) {
        boolean status = document.getBoolean("status");
        HostPort hostPort = SocketProcessUtil.getHostPort(socketChannel);
        String pathName = document.getString("pathName");
        Map<String, EventDetail> events = ContextManager.eventContext.get(socketChannel);
        if (events == null) {
            return;
        }
        EventDetail eventDetail = events.get(pathName);
        if (!status && eventDetail != null) {
            events.remove(pathName);
        }else if (status && eventDetail != null && eventDetail.getCommand().equals(ConstUtil.FILE_CREATE_REQUEST)) {
//            eventDetail.setEnd(true);
            eventDetail.setTimestamp(System.currentTimeMillis());
            SocketProcessUtil.processCDResponse(document, ConstUtil.FILE_CREATE_RESPONSE, socketChannel, socketChannelSet, peerSet);
        }
    }

    @Override
    public void FileModifyRequestProcess(SocketChannel socketChannel, Document document) {
        //get context of events in this channel
        Map<String, EventDetail> eventdetails =  ContextManager.eventContext.get(socketChannel);
        String pathName = document.getString("pathName");
//                RequestState requestState1 = new RequestState("FILE_CREATE_REQUEST",pathName);
//                RequestState requestState2 = new RequestState("FILE_CREATE_MODIFY",pathName);
        HostPort hostPort = SocketProcessUtil.getHostPort(socketChannel);
        boolean isPeerOnTheList = socketChannelSet.contains(socketChannel);
        //boolean isPeerOnTheList = checkOntheList(socketChannel,peerSet);

        if (isPeerOnTheList || eventdetails != null)//&& !checkInReqStateMap(requestState1,hostPort) && !checkInReqStateMap(requestState2,hostPort) && !existPathNameList.contains(pathName))
        {

            Document fileDescriptor = (Document) document.get("fileDescriptor");

            long fileSize = fileDescriptor.getLong("fileSize");
            long lastModified = fileDescriptor.getLong("lastModified");
            String md5 = fileDescriptor.getString("md5");

            if (fileSystemManager.fileNameExists(pathName)) {

                try {
                    boolean status = fileSystemManager.modifyFileLoader(pathName, md5, lastModified);
                    if (status) {
                        String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_MODIFY_RESPONSE, fileDescriptor, pathName, status, "Modify File Loader");
                        //fileTransferTable.put(fileDescriptor.toJson(), length);
                        // 此处需要更新状态机
//                                RequestState requestState = new RequestState("FILE_MODIFY_REQUEST", pathName);
//                                stateMap.get(hostPort.toDoc().toJson()).add(requestState);
//                                existPathNameList.add(pathName);
                        String fileBytesRequest = getFileBytesRequest(socketChannel,content,fileSize,fileDescriptor,pathName);
                        client.replyRequest(socketChannel, fileBytesRequest, false);
                        long length = fileSize;
                        if (fileSize / ConstUtil.BLOCKSIZE > 1) {
                            length = ConstUtil.BLOCKSIZE;
                        }

                        EventDetail eventDetail = new EventDetail(pathName,fileDescriptor,fileBytesRequest,ConstUtil.FILE_BYTES_REQUEST, System.currentTimeMillis(),
                                false,0);
                        eventDetail.setPosition(0);
                        eventDetail.setSentLength(length);
                        eventdetails.put(pathName, eventDetail);

                    } else {
                        String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_MODIFY_RESPONSE, fileDescriptor, pathName, false, "Failed to modify file");
                        client.replyRequest(socketChannel, content, false);
                    }
                } catch (IOException e) {
                    String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_MODIFY_RESPONSE, fileDescriptor, pathName, false, "Failed to modify file");
                    client.replyRequest(socketChannel, content, false);
                    e.printStackTrace();
                }
            } else {
                String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_MODIFY_RESPONSE, fileDescriptor, pathName, false, "File doesn't exist.");
                client.replyRequest(socketChannel, content, false);
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("Peer is not connected");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }

    @Override
    public void FileModifyResponseProcess(SocketChannel socketChannel, Document document) {
        boolean status = document.getBoolean("status");
        String pathName = document.getString("pathName");
        Map<String, EventDetail> events = ContextManager.eventContext.get(socketChannel);
        if (events == null) {
            return;
        }
        EventDetail eventDetail = events.get(pathName);
        if (!status && eventDetail != null) {
            events.remove(pathName);
        } else if (status && eventDetail!=null && eventDetail.getCommand().equals(ConstUtil.FILE_MODIFY_REQUEST)) {
            //TODO: 此处添加状态机
            eventDetail.setEnd(true);
            eventDetail.setTimestamp(System.currentTimeMillis());
            SocketProcessUtil.processCDResponse(document, ConstUtil.FILE_MODIFY_RESPONSE, socketChannel,socketChannelSet,peerSet);
        }
    }

    @Override
    public void FileDeleteRequestProcess(SocketChannel socketChannel, Document document) {
        boolean isPeerOnTheList = socketChannelSet.contains(socketChannel);
        //boolean isPeerOnTheList = checkOntheList(socketChannel,peerSet);
        if (isPeerOnTheList) {
            Document fileDescriptor = (Document) document.get("fileDescriptor");
            String md5 = fileDescriptor.getString("md5");
            long lastModified = fileDescriptor.getLong("lastModified");
            String pathName = document.getString("pathName");
            if (fileSystemManager.fileNameExists(pathName)) {
                boolean status = fileSystemManager.deleteFile(pathName, lastModified, md5);
                if (status) {
                    String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_DELETE_RESPONSE, fileDescriptor, pathName, status, "File delete successfully");
                    client.replyRequest(socketChannel, content, false);
                } else {
                    String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_DELETE_RESPONSE, fileDescriptor, pathName, status, "Error when delete file");
                    client.replyRequest(socketChannel, content, false);
                }
            } else {
                String content = ProtocolUtils.getFileResponse(ConstUtil.FILE_DELETE_RESPONSE, fileDescriptor, pathName, false, "File doesn't exist");
                client.replyRequest(socketChannel, content, false);
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("Peer is not connected");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }
    }

    @Override
    public void FileDeleteResponseProcess(SocketChannel socketChannel, Document document) {
        Map<String, EventDetail> eventDetails = ContextManager.eventContext.get(socketChannel);
        String pathName = document.getString("pathName");
        EventDetail eventDetail = eventDetails.get(pathName);
        if (eventDetail != null && eventDetail.getCommand().equals(ConstUtil.FILE_DELETE_REQUEST)) {
            eventDetails.remove(pathName);
            SocketProcessUtil.processCDResponse(document, ConstUtil.FILE_DELETE_RESPONSE, socketChannel, socketChannelSet, peerSet);
        }
    }
}
