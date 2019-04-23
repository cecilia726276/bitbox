package unimelb.bitbox.controller;

public class Attachment {
    public boolean isFinished;
    public String content;

    public Attachment(boolean isFinished, String content) {
        this.isFinished = isFinished;
        this.content = content;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
