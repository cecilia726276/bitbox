package unimelb.bitbox;

import unimelb.bitbox.util.Document;

import java.util.Queue;

public class EventDetail {
    private String path;
    private Document fileDescriptor;
    private String lastContext;
    private String command;
    private long timestamp;

    public EventDetail(String path, Document fileDescriptor, String lastContext, String command, long timestamp) {
        this.path = path;
        this.fileDescriptor = fileDescriptor;
        this.lastContext = lastContext;
        this.command = command;
        this.timestamp = timestamp;
    }

    public EventDetail() {
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
