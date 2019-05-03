package unimelb.bitbox;

import unimelb.bitbox.util.Document;

import java.util.Queue;

public class EventDetail {
    private String path;
    private Document fileDescriptor;
    private String lastContext;
    private String command;
    private long timestamp;
    private boolean isEnd;
    private int retransNumber;

    public EventDetail(String path, Document fileDescriptor, String lastContext, String command, long timestamp, boolean isEnd, int retransNumber) {
        this.path = path;
        this.fileDescriptor = fileDescriptor;
        this.lastContext = lastContext;
        this.command = command;
        this.timestamp = timestamp;
        this.isEnd = isEnd;
        this.retransNumber = retransNumber;
    }

    public EventDetail() {
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public int getRetransNumber() {
        return retransNumber;
    }

    public void setRetransNumber(int retransNumber) {
        this.retransNumber = retransNumber;
    }



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Document getFileDescriptor() {
        return fileDescriptor;
    }

    public void setFileDescriptor(Document fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    public String getLastContext() {
        return lastContext;
    }

    public void setLastContext(String lastContext) {
        this.lastContext = lastContext;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
