package unimelb.bitbox;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;
import unimelb.bitbox.util.FileSystemObserver;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class ServerMain implements FileSystemObserver {
	private static Logger log = Logger.getLogger(ServerMain.class.getName());
	protected FileSystemManager fileSystemManager;
	
	public ServerMain() throws NumberFormatException, IOException, NoSuchAlgorithmException {
		fileSystemManager=new FileSystemManager(Configuration.getConfigurationValue("path"),this);
	}
// asychronize (blocking method, use 1 or 2 of the threads to process the received message  )
	@Override
	public void processFileSystemEvent(FileSystemEvent fileSystemEvent) {

		// TODO: process events

	}
	
}
