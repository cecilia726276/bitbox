package unimelb.bitbox;

import unimelb.bitbox.controller.EventSelector;
import unimelb.bitbox.controller.EventSelectorImpl;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.SyncRunner;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Peer
{
    private static Logger log = Logger.getLogger(Peer.class.getName());
    public static void main( String[] args ) throws IOException, NumberFormatException, NoSuchAlgorithmException
    {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tc] %2$s %4$s: %5$s%n");
        log.info("BitBox Peer starting...");
        Configuration.getConfiguration();

        ServerMain serverMain = null;
        try {
            serverMain = new ServerMain();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        EventSelector eventSelector = EventSelectorImpl.getInstance();
        eventSelector.getFixedThreadPool().execute(new SyncRunner(serverMain));

        System.out.println("start3");

        eventSelector.controllerRunning();
    }
}