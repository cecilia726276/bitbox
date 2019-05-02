package unimelb.bitbox.service;

import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.FileCoder;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.ConstUtil;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
        System.out.println("test send Reject Response, but actually didn't send: content :"+ content);
    }
    @Override
    public void processRequest(SocketChannel socketChannel, Document document) {
        String pathName = document.getString("pathName");
        long position = document.getLong("position");
        long length = document.getLong("length");
//        Map<String, EventDetail> eventDetails =  ContextManager.eventContext.get(socketChannel);
//        EventDetail eventDetail = eventDetails.get(pathName);
//        if (eventDetail == null) {
//            return ;
//        }
        String recordCommand = null;//eventDetail.getCommand();
        if (position == 0) {
            if (true||recordCommand.equals(ConstUtil.FILE_CREATE_RESPONSE) || recordCommand.equals(ConstUtil.FILE_MODIFY_RESPONSE)) {
                Document fileDescriptor = (Document) document.get("fileDescriptor");
                String md5 = fileDescriptor.getString("md5");
                long fileSize = fileDescriptor.getLong("fileSize");
                long lastModified = fileDescriptor.getLong("lastModified");
                //     if (fileSystemManager.fileNameExists(pathName, md5)) {
                try {
                    ByteBuffer byteBuffer = fileSystemManager.readFile(md5, position,length);
                    String content = FileCoder.INSTANCE.getEncoder().encodeToString(byteBuffer.array());
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
                    String content = ProtocolUtils.getInvalidProtocol("invalid message 1");
                    sendRejectResponse(socketChannel, content);
                    ioe.printStackTrace();
                }
                // }
            } else {
                String content = ProtocolUtils.getInvalidProtocol("invalid message 2");
                sendRejectResponse(socketChannel, content);

            }
        } else if (position <= length - 1) {
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

                    String content = ProtocolUtils.getInvalidProtocol("invalid message 3");
                    ioe.printStackTrace();
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

    @Override
    public void processResponse(SocketChannel socketChannel, Document document) {
        Document fileDescriptor = (Document) document.get("fileDescriptor");
        long fileSize = fileDescriptor.getLong("fileSize");
            //if (hostPort != null && peerSet.contains(hostPort.toDoc())) {
            long pos = document.getLong("position");
            long len = document.getLong("length");
            long currentPos = pos + len;
            String pathName = document.getString("pathName");

//            Map<String, EventDetail> eventDetails =  ContextManager.eventContext.get(socketChannel);
//            if (eventDetails == null) {
//                return ;
//            }
//            EventDetail eventDetail = eventDetails.get(pathName);
//            if (eventDetail == null) {
//                return ;
//            }
            //if(pos == 0)
            //{
//            RequestState rs = new RequestState("FILE_BYTES_REQUEST", pathName, pos, len);
//            List<RequestState> list = respStateMap.get(hostPort.toDoc().toJson());
        String recordCommand = null;//eventDetail.getCommand();

        if (true || recordCommand.equals(ConstUtil.FILE_BYTES_REQUEST)) {
                boolean status = document.getBoolean("status");
                if (status) {
                    String content = document.getString("content");
                    byte[] buf = FileCoder.INSTANCE.getDecoder().decode(content);
                    ByteBuffer src = ByteBuffer.wrap(buf);
                    try {
                        if (fileSystemManager.writeFile(pathName, src, pos)) {
                            if (fileSystemManager.checkWriteComplete(pathName)) {
                                //long tmp = Integer.parseInt(Configuration.getConfigurationValue("blockSize"));
                                if (currentPos == fileSize - 1)
                                {
//                                    list.remove(rs);
                                } else if (currentPos + len > fileSize) {
                                    long newlen = fileSize - currentPos;
                                    String packet = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName, currentPos, newlen);
                                    client.replyRequest(socketChannel, packet, false);
//                                    list.remove(rs);
                                    // TODO: 需要更改状态
//                                    rs = new RequestState("FILE_BYTES_REQUEST", pathName, currentPos, newlen);
                                } else if (currentPos + len <= fileSize) {
                                    String packet = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName, currentPos, len);
                                    client.replyRequest(socketChannel, packet, false);
//                                   //TODO: 需要更改状态
//                                    list.remove(rs);
//                                    rs = new RequestState("FILE_BYTES_REQUEST", pathName, currentPos, len);
//                                    list.add(rs);
                                }
//                                respStateMap.put(hostPort.toDoc().toJson(), list);

                            } else {//如果文件没写完，重新request一下，状态机就不用更新了 TODO: 需要更新状态
                                String packet = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName, pos, len);
                                client.replyRequest(socketChannel, packet, false);
                            }
                        }
                    } catch (Exception e) {
                        String str = ProtocolUtils.getInvalidProtocol("invalid message 1");
                        sendRejectResponse(socketChannel, str);
                        e.printStackTrace();
                    }


                } else {
                    String str = ProtocolUtils.getInvalidProtocol("status is false");
                    sendRejectResponse(socketChannel, str);
                }
            } else {
                String str = ProtocolUtils.getInvalidProtocol("I havent send a file_bytes_req for this file, why send me response?");
                sendRejectResponse(socketChannel, str);
            }
        //processCDResponse(document, command, socketChannel);
    }


}
