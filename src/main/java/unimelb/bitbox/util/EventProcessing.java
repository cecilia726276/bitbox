package unimelb.bitbox.util;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

//this interface is used to provide functions of handling json request
public interface EventProcessing {
    public boolean receiveReq(Document doc) throws NoSuchAlgorithmException, IOException;  //receive and process response
    public Document sendResponse(Document doc, boolean status);
    //public Document sendResponse();
    //public Document sendRequest();
}
