package unimelb.bitbox.service;

import unimelb.bitbox.ContextManager;
import unimelb.bitbox.EventDetail;
import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.FileCoder;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.*;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

public class BytesEventHandlerImpl implements BytesEventHandler {
    private FileSystemManager fileSystemManager;
    private Client client;
    public BytesEventHandlerImpl(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
        client = ClientImpl.getInstance();
    }
    private void sendRejectResponse(SocketChannel socketChannel, String content) {
//        client.replyRequest(socketChannel,content,true);
//        deletePeer(socketChannel);
        System.out.println("test send Reject Response, but actually didn't send");
    }
    public void processRequest(SocketChannel socketChannel, Document document) {
        String pathName = document.getString("pathName");
        long position = document.getLong("position");
        long length = document.getLong("length");
        Map<String, EventDetail> eventDetails =  ContextManager.eventContext.get(pathName);
        EventDetail eventDetail = eventDetails.get(pathName);
        if (eventDetail == null) {
          //  return ;
        }
        String recordCommand = null;//eventDetail.getCommand();
        if (position == 0) {
            if (true||recordCommand.equals(ConstUtil.FILE_CREATE_RESPONSE) || recordCommand.equals(ConstUtil.FILE_MODIFY_RESPONSE)) {
                Document fileDescriptor = (Document) document.get("fileDescriptor");
                String md5 = fileDescriptor.getString("md5");
                long fileSize = fileDescriptor.getLong("fileSize");
                long lastModified = fileDescriptor.getLong("lastModified");
                //     if (fileSystemManager.fileNameExists(pathName, md5)) {
                try {
                    String content = FileCoder.INSTANCE.getEncoder().encode(fileSystemManager.readFile(md5, position, length)).toString();
                    String message = "successful read";
                    String packet = ProtocolUtils.getFileBytesResponse(fileDescriptor, pathName, position, length, content, message, false);
                    client.replyRequest(socketChannel, packet, false);
//                    if (list.contains(rs1)) {
//                        list.remove(rs1);
//                    } else {
//                        list.remove(rs2);
//                    }
//                    rs1 = new RequestState("FILE_BYTES_RESPONSE", pathName, position, length);
//                    list.add(rs1);
//                    stateMap.put(hostPort.toDoc().toJson(), list);

                } catch (Exception ioe) {
                    String content = ProtocolUtils.getInvalidProtocol("invalid message");
                    sendRejectResponse(socketChannel, content);
                }

                // }

            } else {
                String content = ProtocolUtils.getInvalidProtocol("invalid message");
                sendRejectResponse(socketChannel, content);

            }
        } else if (position <= length - 1) { // TODO： position <= length - 1 ?? commented out by SYZ
//            RequestState rs = new RequestState("FILE_BYTES_RESPONSE", pathName, position - Integer.parseInt(Configuration.getConfigurationValue("blockSize")), length);
//            List<RequestState> list = stateMap.get(hostPort.toDoc().toJson());

            if (true||recordCommand.equals(ConstUtil.FILE_BYTES_RESPONSE)) {
                Document fileDescriptor = (Document) document.get("fileDescriptor");
                String md5 = fileDescriptor.getString("md5");
                long fileSize = fileDescriptor.getLong("fileSize");
                long lastModified = fileDescriptor.getLong("lastModified");
                //     if (fileSystemManager.fileNameExists(pathName, md5)) {
                try {
                    String content = FileCoder.INSTANCE.getEncoder().encode(fileSystemManager.readFile(md5, position, length)).toString();
                    String message = "successful read";
                    String packet = ProtocolUtils.getFileBytesResponse(fileDescriptor, pathName, position, length, content, message, true);
                    client.replyRequest(socketChannel,packet,false);
                    //:TODO 需要更改状态


                } catch (Exception ioe) {
                    String content = ProtocolUtils.getInvalidProtocol("invalid message");
                    sendRejectResponse(socketChannel, content);
                }

                //   }
            } else {
                String content = ProtocolUtils.getInvalidProtocol("position out of length!");
                sendRejectResponse(socketChannel, content);

            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("peer not found");
            sendRejectResponse(socketChannel, content);

        }



    }



}
