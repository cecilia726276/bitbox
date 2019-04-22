package unimelb.bitbox.draft;

import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;
import unimelb.bitbox.util.FileSystemObserver;

import javax.net.ServerSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class OldServerMain implements FileSystemObserver, Runnable {
	private static Logger log = Logger.getLogger(OldServerMain.class.getName());
	// Identifies the user number connected
	private static int counter = 0;
	protected FileSystemManager fileSystemManager;

	@Override
	public void run() {
		startServer();
	}

	public OldServerMain() throws NumberFormatException, IOException, NoSuchAlgorithmException {
		fileSystemManager=new FileSystemManager(Configuration.getConfigurationValue("path"),this);
	}

	public void startServer(){
		ServerSocketFactory factory = ServerSocketFactory.getDefault();
		int port = Integer.parseInt(Configuration.getConfigurationValue("port"));
		try(ServerSocket server = factory.createServerSocket(port)){
			System.out.println("server started.");
			System.out.println("peer port:"+port+"Waiting for client connection..");

			// Wait for connections.
			while(true){
				Socket client = server.accept();
				counter++;
				System.out.println("Client "+counter+": Applying for connection!");

				// Start a new thread for a connection
				Thread t = new Thread(() -> serveClient(client));
				t.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private static void serveClient(Socket client){
		try(Socket clientSocket = client){

			// The JSON Parser
			//JSONParser parser = new JSONParser();
			// Input stream
			DataInputStream input = new DataInputStream(clientSocket.
					getInputStream());
			// Output Stream
			DataOutputStream output = new DataOutputStream(clientSocket.
					getOutputStream());
			System.out.println("CLIENT: "+input.readUTF());
			output.writeUTF("Server: Hi Client "+counter+" !!!");

			// Receive more data..
//			while(true){
//				if(input.available() > 0){
//					// Attempt to convert read data to JSON
//					JSONObject command = (JSONObject) parser.parse(input.readUTF());
//					System.out.println("COMMAND RECEIVED: "+command.toJSONString());
//					Integer result = parseCommand(command);
//					JSONObject results = new JSONObject();
//					results.put("result", result);
//					output.writeUTF(results.toJSONString());
//				}
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
// asychronize (blocking method, use 1 or 2 of the threads to process the received message  )
	@Override
	public void processFileSystemEvent(FileSystemEvent fileSystemEvent)
	{
		//start a TCP server of a peer
		//set port number according to the information in "configuration.properties "
		//startServer();
		// TODO: process events
		//convert the system events to json object
		Document doc = fileReqToDoc(fileSystemEvent);

	}
	private Document fileReqToDoc(FileSystemEvent fileSystemEvent) { //request(s) only from fileManager
		Document filReqDoc = new Document();
		Document filDescriDoc = fileSystemEvent.fileDescriptor.toDoc();
		filReqDoc.append("command", fileSystemEvent.event.toString());
		filReqDoc.append("fileDescriptor", filDescriDoc);
		filReqDoc.append("pathName", fileSystemEvent.pathName);
		return filReqDoc;
	}
}
