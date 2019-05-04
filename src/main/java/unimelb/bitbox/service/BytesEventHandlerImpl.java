package unimelb.bitbox.service;

import unimelb.bitbox.ContextManager;
import unimelb.bitbox.EventDetail;
import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.message.FileCoder;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.ConstUtil;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.SocketProcessUtil;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class BytesEventHandlerImpl implements BytesEventHandler {
    private FileSystemManager fileSystemManager;
    private Client client;
    private Logger log;
    private Set socketChannelSet;
    private Map peerSet;

    public BytesEventHandlerImpl(FileSystemManager fileSystemManager, Logger log, Set socketChannelSet, Map peerSet) {
        this.fileSystemManager = fileSystemManager;
        this.log = log;
        this.socketChannelSet = socketChannelSet;
        this.peerSet = peerSet;
        client = ClientImpl.getInstance();
    }

    @Override
    public void processRequest(SocketChannel socketChannel, Document document) {
        Map<String, EventDetail> eventDetails = ContextManager.eventContext.get(socketChannel);

        if (socketChannelSet.contains(socketChannel) || eventDetails != null){
            String pathName = document.getString("pathName");
            long position = document.getLong("position");
            long length = document.getLong("length");

            String recordCommand = null;
            // TODO： 判断状态机是否发过 modify, create, byte response
            // TODO:  byte response 需要通过状态机里存的position配合判断【要不这里就不判断position了，万一有重传呢！】
            EventDetail eventDetail = eventDetails.get(pathName);
            System.out.println(eventDetail.getPosition()+" "+eventDetail.getSentLength()+" "+eventDetail.getCommand());
            boolean getCreateModifyByteResponseBefore
                    = eventDetail != null &&
                    ((eventDetail.getCommand().equals(ConstUtil.FILE_BYTES_RESPONSE)
                    && eventDetail.getPosition() + eventDetail.getSentLength() == position)
                    || eventDetail.getCommand().equals(ConstUtil.FILE_MODIFY_REQUEST)
                    || eventDetail.getCommand().equals(ConstUtil.FILE_CREATE_REQUEST));

            if (getCreateModifyByteResponseBefore){
                Document fileDescriptor = (Document) document.get("fileDescriptor");
                String md5 = fileDescriptor.getString("md5");
                try {
                    ByteBuffer byteBuffer = fileSystemManager.readFile(md5,position,length);
                    String content = FileCoder.INSTANCE.getEncoder().encodeToString(byteBuffer.array());
                    String response = ProtocolUtils.getFileBytesResponse(fileDescriptor,pathName,position,length, content, "Successful read", true);
                    long fileSize = fileDescriptor.getLong("fileSize");
                    client.replyRequest(socketChannel, response, false);

                    if (position+length == fileSize) {
                        eventDetail.setEnd(true);
                    } else {
                        eventDetail.setEnd(false);
                    }
                    // TODO: 状态机更新为bytes 传输状态
                    eventDetail.setCommand(ConstUtil.FILE_BYTES_RESPONSE);
                    eventDetail.setPosition(position);
                    eventDetail.setSentLength(length);
                    eventDetail.setTimestamp(System.currentTimeMillis());
                    eventDetail.setLastContext(response);
                } catch (Exception e){
                    String error = ProtocolUtils.getFileBytesResponse(fileDescriptor,pathName,position,length, " ", "Error occurs when reading file.", false);
                    client.replyRequest(socketChannel, error, false);
                    e.printStackTrace();
                }
            } else {
                boolean isRetransmit = eventDetail != null
                        && (eventDetail.getCommand().equals(ConstUtil.FILE_BYTES_RESPONSE)
                        && eventDetail.getPosition() == position && eventDetail.getSentLength() == length);
                if (isRetransmit) {
                    client.replyRequest(socketChannel, eventDetail.getLastContext(), false);
                    eventDetail.setTimestamp(System.currentTimeMillis());
                } else {
                    String content = ProtocolUtils.getInvalidProtocol("Cannot request bytes without pre-request!");
                    SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
                }
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("Peer is not on the list!");
            SocketProcessUtil.sendRejectResponse(socketChannel, content, socketChannelSet, peerSet);
        }


    }

    @Override
    public void processResponse(SocketChannel socketChannel, Document document) {
        Map<String, EventDetail> eventDetails = ContextManager.eventContext.get(socketChannel);

        if (socketChannelSet.contains(socketChannel) || eventDetails != null){
            Document fileDescriptor = (Document) document.get("fileDescriptor");
            long fileSize = fileDescriptor.getLong("fileSize");
            long pos = document.getLong("position");
            long len = document.getLong("length");
            long currentPos = pos + len;
            String pathName = document.getString("pathName");

            // TODO: 判断状态机是否发过ByteRequest请求
            EventDetail eventDetail = eventDetails.get(pathName);

            boolean sendRequestBefore = eventDetail != null
                    && eventDetail.getCommand().equals(ConstUtil.FILE_BYTES_REQUEST)
                    && eventDetail.getPosition() == pos && eventDetail.getSentLength() == len;

            if (sendRequestBefore) {
                boolean status = document.getBoolean("status");
                if (status) {
                    String content = document.getString("content");
                    byte[] buf = FileCoder.INSTANCE.getDecoder().decode(content);
                    ByteBuffer src = ByteBuffer.wrap(buf);
                    try {
                        if (fileSystemManager.writeFile(pathName, src, pos)) {
                            if (fileSystemManager.checkWriteComplete(pathName)){
                                /**
                                 * The write operation of this file has been completed. No further action is required.
                                 */
                                // TODO:  更改状态：pathName 为 aaa 的文件已经收到了所有文件了
                                eventDetails.remove(eventDetail);
                            } else {
                                /**
                                 * when the size of the rest part of the modified file is no larger than than the blocksize
                                 */
                                String newBytes = "";
                                long newSize = 0;
                                if (currentPos + len >= fileSize){
                                    newSize = fileSize - currentPos;
                                    newBytes = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName,currentPos,newSize);
                                    client.replyRequest(socketChannel, newBytes, false);
                                    // TODO:  更改状态： pathName 为 aaa 的文件已经收到了标记为pos的文件了，目前需要标记为currentPos的文件(应为最后一次请求)
                                }
                                /**
                                 * when the size of the rest part of the modified file is larger than the blocksize
                                 */
                                else if (currentPos + len < fileSize){
                                    newSize = len;
                                    newBytes = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName, currentPos, len);
                                    client.replyRequest(socketChannel, newBytes,false);
                                    // TODO:  更改状态： pathName 为 aaa 的文件已经收到了标记为pos的文件了，目前需要标记为currentPos的文件(后面还会有多次请求)
                                }
                                eventDetail.setLastContext(newBytes);
                                eventDetail.setPosition(currentPos);
                                eventDetail.setSentLength(newSize);
                                eventDetail.setCommand(ConstUtil.FILE_BYTES_REQUEST);
                                eventDetail.setTimestamp(System.currentTimeMillis());
                                eventDetail.setEnd(false);
                            }
                        }
                    } catch (Exception e) {
                        /**
                         *  the file system failed to write the file.
                         */
                        //String resendByteRequest = ProtocolUtils.getFileBytesRequest(fileDescriptor, pathName, pos, len);
                        // TODO: 重传不需要更新文件状态
                        //client.replyRequest(socketChannel, resendByteRequest, false);
                        e.printStackTrace();
                    }

                } else {
                    // TODO: 如果response为false说明远程peer创建bytes出现问题，此时如果(重传次数<=3)? 重传并把状态机重传次数+1 : closeFilerLoader更改状态机
                    log.info("status is false, need resending");
                    client.replyRequest(socketChannel, eventDetail.getLastContext(), false);
                    eventDetail.setTimestamp(System.currentTimeMillis());
                    if (eventDetail.getRetransNumber() < ConstUtil.RETRANS_MAXNUM) {
                        eventDetail.setRetransNumber(eventDetail.getRetransNumber() + 1);
                    }
                }
            } else {
                String str = ProtocolUtils.getInvalidProtocol("I havent send a file_bytes_req for this file, why send me response?");
                SocketProcessUtil.sendRejectResponse(socketChannel, str, socketChannelSet, peerSet);
            }
        } else {
            String content = ProtocolUtils.getInvalidProtocol("Peer is not on the list!");
            SocketProcessUtil.sendRejectResponse(socketChannel, content,socketChannelSet, peerSet);
        }

    }


}
