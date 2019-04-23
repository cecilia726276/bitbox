package unimelb.bitbox.packageAPI;

import org.json.simple.JSONObject;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemObserver;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class FileCreateHandler implements FileSystemObserver {

    protected FileSystemManager manager;
    private Document doc;

    public FileCreateHandler(Document doc) throws NumberFormatException, IOException, NoSuchAlgorithmException {
        manager=new FileSystemManager(Configuration.getConfigurationValue("path"),this);
        this.doc = doc;
    }

    public boolean receiveReq() throws NoSuchAlgorithmException, IOException
    {
        String pathName = doc.getString("pathName");

        JSONObject fileDescripJson = (JSONObject) doc.get("fileDescriptor");
        long fileSizetmp = Long.valueOf(String.valueOf(fileDescripJson.get("fileSize")));
        long lastMtmp = Long.valueOf(String.valueOf(fileDescripJson.get("lastModified")));
        String md5tmp = String.valueOf(fileDescripJson.get("md5"));
        boolean status = manager.createFileLoader(pathName, md5tmp, fileSizetmp, lastMtmp);

//        FileDescriptor fileDescriptor = manager.new FileDescriptor(lastMtmp, md5tmp, fileSizetmp);
//        FileSystemEvent fileSystemEvent = manager.new FileSystemEvent(path, name, EVENT.FILE_CREATE, fileDescriptor);
//        processFileSystemEvent(fileSystemEvent);
        return status;
    }
    public String sendResponse(boolean status)
    {
        doc.append("command", "FILE_CREATE_RESPONSE");
        if(status == false){
            doc.append("message", "file create fail");
        }
        else{
            doc.append("message", "file loader ready");
        }
        doc.append("status", status);
        return doc.toJson();
    }
    public void processFileSystemEvent(FileSystemManager.FileSystemEvent fileSystemEvent){}
}
