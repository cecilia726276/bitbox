package unimelb.bitbox.controller;

import java.util.List;
import java.util.Queue;

public class Attachment {
    public boolean isFinished;
    public Queue<String> content;

    public Attachment(boolean isFinished, Queue<String> content) {
        this.isFinished = isFinished;
        this.content = content;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }

    public Queue<String> getContent() {
        return content;
    }

    public void setContent(Queue<String> content) {
        this.content = content;
    }
}
