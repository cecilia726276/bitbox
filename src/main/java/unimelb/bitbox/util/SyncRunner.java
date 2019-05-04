package unimelb.bitbox.util;

import unimelb.bitbox.ContextManager;
import unimelb.bitbox.EventDetail;
import unimelb.bitbox.ServerMain;
import unimelb.bitbox.controller.Client;
import unimelb.bitbox.controller.ClientImpl;
import unimelb.bitbox.controller.EventSelector;
import unimelb.bitbox.controller.EventSelectorImpl;

import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Map;

public class SyncRunner implements Runnable {
    private ServerMain serverMain;
    private EventSelector eventSelector;
    private Client client;
    public SyncRunner(ServerMain serverMain) {
        this.serverMain = serverMain;
        eventSelector = EventSelectorImpl.getInstance();
        client = ClientImpl.getInstance();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(ConstUtil.SYNC_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Date date = new Date();
            System.out.println("test sync and timeout");

          //  serverMain.syncProcess();

            //timeout process manage
            System.out.println("***checktimeout***");
            for (Map.Entry<SocketChannel, Map<String, EventDetail>> entry :
            ContextManager.eventContext.entrySet()) {
                for (Map.Entry<String, EventDetail> record : entry.getValue().entrySet()) {
                    System.out.println(record.getKey()+": "+record.getValue().getCommand());
                    if((System.currentTimeMillis() - record.getValue().getTimestamp())
                            > ConstUtil.TIME_OUT) {
                        if (!record.getValue().isEnd() && record.getValue().getRetransNumber() <= ConstUtil.RETRANS_MAXNUM) {
                            System.out.println(record.getKey()+"will be reply");
                            record.getValue().setRetransNumber(record.getValue().getRetransNumber()+1);
                            client.replyRequest(entry.getKey(), record.getValue().getLastContext(), false);
                        } else {
                            entry.getValue().remove(record.getKey());
                        }
                    }
                }
            }
        }
    }
}
