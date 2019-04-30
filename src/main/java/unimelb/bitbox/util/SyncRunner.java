package unimelb.bitbox.util;

import unimelb.bitbox.ServerMain;
import unimelb.bitbox.draft.Server;

import java.util.List;

public class SyncRunner implements Runnable {
    private ServerMain serverMain;

    public SyncRunner(ServerMain serverMain) {
        this.serverMain = serverMain;
    }
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            serverMain.syncProcess();
        }
    }
}
