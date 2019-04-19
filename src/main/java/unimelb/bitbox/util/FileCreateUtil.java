package unimelb.bitbox.util;

import org.json.simple.JSONObject;
import unimelb.bitbox.util.FileSystemManager;
import unimelb.bitbox.util.FileSystemManager.FileSystemEvent;
import unimelb.bitbox.util.FileSystemManager.FileDescriptor;
import unimelb.bitbox.util.FileSystemObserver;
import unimelb.bitbox.util.FileSystemManager.EVENT;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class FileCreateUtil implements FileSystemObserver, EventProcessing{
    protected FileSystemManager manager;

    public FileCreateUtil() throws NumberFormatException, IOException, NoSuchAlgorithmException {
        manager=new FileSystemManager(Configuration.getConfigurationValue("path"),this);
    }
    public boolean receiveReq(Document doc) throws NoSuchAlgorithmException, IOException
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
    }
    public Document sendResponse(Document doc, boolean status)
    {
        doc.append("command", "FILE_CREATE_RESPONSE");
        if(status == false){
            doc.append("message", "file create fail");
        }
        else{
            doc.append("message", "file loader ready");
        }
        doc.append("status", status);
        return doc;
    }
    public void processFileSystemEvent(FileSystemEvent fileSystemEvent){}

}
