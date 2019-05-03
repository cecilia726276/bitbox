package unimelb.bitbox.util;

import unimelb.bitbox.ServerMain;
import unimelb.bitbox.controller.EventSelector;
import unimelb.bitbox.controller.EventSelectorImpl;

import java.util.Date;

public class SyncRunner implements Runnable {
    private ServerMain serverMain;
    private EventSelector eventSelector;
    public SyncRunner(ServerMain serverMain) {
        this.serverMain = serverMain;
        eventSelector = EventSelectorImpl.getInstance();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Date date = new Date();
            System.out.println("test sync");

            serverMain.syncProcess();
//            for (Map.Entry<SocketChannel, Date> entry :
//                    eventSelector.getTimeoutManager().entrySet()) {
//                if ((date.getTime() - entry.getValue().getTime()) > 5000) {
//                    // 处理timeout
//                    System.out.println(entry.getKey().socket().getPort());
//                    eventSelector.getTimeoutManager().remove(entry.getKey());
//                }
//            }
        }
    }
}
