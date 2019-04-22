package unimelb.bitbox.util;

//import java.io.IOException;
//import java.security.NoSuchAlgorithmException;
//import java.util.ArrayList;
//
////the purpose of this class is to link particular json string to correspond function
//public class MessageFilter {
//    public String command;
//
//    private FileSystemManager.FileDescriptor fileDescriptorExtraction(Document doc)throws IOException, NoSuchAlgorithmException
//    {
//        FileSystemManager manager = new FileSystemManager(Configuration.getConfigurationValue("root"));
//        FileSystemManager.FileDescriptor fileDescriptor;
//        if(doc.containsKey("fileDescriptor")) //return a json obj
//        {
//            JSONObject fileDescripJson = (JSONObject) doc.get("fileDescriptor");
//            long fileSizetmp = Long.valueOf(String.valueOf(fileDescripJson.get("fileSize")));
//            long lastMtmp = Long.valueOf(String.valueOf(fileDescripJson.get("lastModified")));
//            String md5tmp = String.valueOf(fileDescripJson.get("md5"));
//            fileDescriptor = manager.new FileDescriptor(lastMtmp, md5tmp, fileSizetmp);
//            return fileDescriptor;
//        }
//        return null;
//    }
//    private void fileCreateReq(Document doc, String bufStr) throws IOException, NoSuchAlgorithmException
//    {
//        String pathName;
//        FileSystemManager manager = new FileSystemManager(Configuration.getConfigurationValue("root"));
//        FileSystemManager.FileDescriptor fileDescriptor = fileDescriptorExtraction(doc);
//        if(doc.containsKey("pathName"))
//        {
//            pathName = doc.getString("pathName");
//            //processing file create/delete request:
//            manager.createFileLoader(pathName, fileDescriptor.md5, fileDescriptor.fileSize, fileDescriptor.lastModified);
//        }
//
//    }
//    private void handShakeReq(Document doc, String bufStr)
//    {
//        JSONObject hostPort = (JSONObject)doc.get("hostPort");
//        Peerinfo peer = new Peerinfo(String.valueOf(hostPort.get("host")), Integer.parseInt(String.valueOf(hostPort.get("port"))));
//        //processing hand shake request:
//    }
//    private void fileDelReq(Document doc, String bufStr) throws IOException, NoSuchAlgorithmException
//    {
//        String pathName;
//        FileSystemManager manager = new FileSystemManager(Configuration.getConfigurationValue("root"));
//        FileSystemManager.FileDescriptor fileDescriptor = fileDescriptorExtraction(doc);
//        if(doc.containsKey("pathName"))
//        {
//            pathName = doc.getString("pathName");
//            //processing file delete request:
//            manager.deleteFile(pathName, fileDescriptor.lastModified,fileDescriptor.md5);
//        }
//    }
//    private void fileBytesReq(Document doc, String bufStr)throws IOException, NoSuchAlgorithmException
//    {
//        String pathName;
//        long position;
//        long length;
//        FileSystemManager manager = new FileSystemManager(Configuration.getConfigurationValue("root"));
//        FileSystemManager.FileDescriptor fileDescriptor = fileDescriptorExtraction(doc);
//        if(doc.containsKey("pathName")){
//            pathName = doc.getString("pathName");
//            if(doc.containsKey("position")){
//                position = doc.getLong("position");
//                if(doc.containsKey("length")){
//                    length = doc.getLong("length");
//                    //processing buffer writing methods
//                }
//            }
//        }
//
//    }
//    public MessageFilter(String bufStr) throws IOException, NoSuchAlgorithmException {
//        Document doc = Document.parse(bufStr);
//
//        if(doc.containsKey("command"))
//        {
//            this.command = doc.getString("command");
//            switch(this.command){
//                case "FILE_CREATE_REQUEST": fileCreateReq(doc, bufStr); break;
//                case "HANDSHAKE_REQUEST": handShakeReq(doc, bufStr); break;
//                case "FILE_BYTES_REQUEST": fileBytesReq(doc, bufStr); break;
//
//                default: ;
//                                  }
//        }
//        /*
//        if(doc.containsKey("peers"))
//        {
//            Object o = doc.get("peers"); //o is a ArrayList<Object>
//            try {
//                this.peers = (ArrayList<Peerinfo>)o;
//            } catch (Exception e) {
//                System.out.println("can't convert to peer list.");
//            }
//        }
//        if(doc.containsKey("message"))
//        {
//            this.message = doc.getString("message");
//        }
//        if(doc.containsKey("status"))
//        {
//            this.status = doc.getBoolean("status");
//        }   */
//
//    }
//
//    public class Peerinfo {
//        public String host;
//        public int port;
//        public Peerinfo(String host, int port)
//        {
//            this.host = host;
//            this.port = port;
//        }
//    }
//}
