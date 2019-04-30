//package unimelb.bitbox.packageAPI;
//
//import unimelb.bitbox.util.Document;
//
//import java.io.IOException;
//import java.security.NoSuchAlgorithmException;
//
//public class ReceivePackageImp implements  ReceivePackage{
//    @Override
//    public boolean receiveReq(String bufStr) {
//        Document doc = Document.parse(bufStr);
//
//        if (doc.containsKey("command")) {
//            String command = doc.getString("command");
//            switch(command){
//                case "FILE_CREATE_REQUEST":
//                    {
//                        try{
//                            FileCreateHandler fileCreateHandler = new FileCreateHandler(doc);
//                            boolean status = fileCreateHandler.receiveReq();
//                            String response = fileCreateHandler.sendResponse(status);
//                        }catch(Exception ex){System.out.println("exception caught!");}
//                        break;
//                    }
//                case "HANDSHAKE_REQUEST":
//                    {
//
//                        break;
//                    }
//                case "FILE_BYTES_REQUEST":
//                    {
//                        break;
//                    }
//
//                default: ;
//                                  }
//        }
//
//    }
//}
