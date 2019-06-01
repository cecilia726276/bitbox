package unimelb.bitbox.client;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import unimelb.AES.AESUtil;
import unimelb.RSA_test.RSAUtil.KeyGenerator;
import unimelb.RSA_test.RSAUtil.RSAUtil;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.ConstUtil;
import unimelb.bitbox.util.Document;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.util.List;



/**
 * @Author SYZ
 * @create 2019-05-22 20:10
 */

// Reference: Tutorial 8: CmdLineArgsDemo
public class Client {
    //String identity = "yizhoushen@Yizhous-MacBook-Pro.local";
    // "Windows10@Bowen-Xu"
    static String identity;
    static Socket clientSocket;
    static BufferedWriter out;
    //static DataOutputStream out;
    static BufferedReader in;

    static String aesKey;
//    static PublicKey aesKey;
    static PrivateKey privateKey;

    /**
     * Start connection
     * @param ip
     * @param port
     * @return the result of connection
     */
    public Boolean startConnection(String ip, int port) {

        try {
            clientSocket = new Socket(ip, port);
            System.out.println("ServerConnection Established");
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8"));
            //out = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Verify identity:" + identity);
            String idenMesg = ProtocolUtils.getAuthRequest(identity);
            System.out.println("send message:"+ idenMesg);
            out.write(idenMesg);
            out.flush();
            System.out.println("Identification sent, wait for response.");

            while (true){
                String response;
                if((response = in.readLine()) != null){
                    System.out.println("Get Response:" + response);
                    Boolean outcome = extractPublicKey(response);
                    return outcome;
                }
            }

        } catch (UnknownHostException e) {
            System.out.println("Socket:" + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF:" + e.getMessage());
        } catch (IOException e) {
            System.out.println("readline:" + e.getMessage());
        }
        return false;
    }

    /**
     * exact AES128 key
     * @param string
     * @return the result of extracting the key
     */
    public Boolean extractPublicKey(String string){
        Document document = Document.parse(string);
        String command = document.getString("command");
        if (command.equals(ConstUtil.AUTH_RESPONSE)){
            Boolean status = document.getBoolean("status");
            if (status) {
                String aes128 = document.getString("AES128");
                System.out.println(aes128);
                try {
                    byte[] base642Byte = RSAUtil.base642Byte(aes128);
                    System.out.println(base642Byte);
                    //用私钥解密
                    byte[] privateDecrypt = RSAUtil.privateDecrypt(base642Byte, privateKey);
                    System.out.println(privateDecrypt);
                    aesKey = new String(privateDecrypt);
                    System.out.println("get aesKey:" + aesKey);
                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("public key not found");
                return false;
            }
        }
        return false;
    }

    /**
     * process Decrypted Message
     * @param string
     */
    public void processMessage(String string){
        Document document = Document.parse(string);
        String command = document.getString("command");
        if(command == null) {
            String content = ProtocolUtils.getInvalidProtocol("message must contain a command field as string");
            System.out.println("Invalid message");
            return;
        }
        switch (command) {
            case ConstUtil.LIST_PEERS_RESPONSE:{
                List<Document> existingPeers = (List<Document>) document.get("peers");
                if (existingPeers.size() == 0){
                    break;
                }
                for (int i = 0; i < existingPeers.size(); i++){
                    Document peer = existingPeers.get(i);
                    System.out.println(peer.toString());
                }
                break;
            }
            case (ConstUtil.CONNECT_PEER_RESPONSE) :{
                System.out.println(document.toString());
                break;
            }
            case ConstUtil.DISCONNECT_PEER_RESPONSE:{
                System.out.println(document.toString());
                break;
            }
            default:{
                System.out.println("Receive Invalid message" + document.toString());
                break;
            }
        }
    }

    /**
     * encrypted Message to be sent
     * @param request
     */
    public void encryptSendMsg(String request){
        String encrypted = AESUtil.encrypt(request, aesKey);
        String message = ProtocolUtils.getPayload(encrypted);
        try {
            out.write(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        privateKey = KeyGenerator.toPrivateKey("client_rsa");
        CmdLineArgs argsBean = new CmdLineArgs();

        //Parser provided by args4j
        CmdLineParser parser = new CmdLineParser(argsBean);
        try {

            //Parse the arguments
            parser.parseArgument(args);

            //After parsing, the fields in argsBean have been updated with the given
            //command line arguments
            String command = argsBean.getCommand();
            String server = argsBean.getServer();
            identity = argsBean.getIdentity();
            String[] hostPort = server.split(":");
            String ip = hostPort[0];
            int port = Integer.parseInt(hostPort[1]);
            System.out.println("Finish configuration.");
            Boolean status = client.startConnection(ip, port);
            System.out.println("status outcome: "+ status);

            if (status){
                if(command.equals(ConstUtil.LIST_PEERS)){
                    System.out.println("list_peer command");
                    String request = ProtocolUtils.getListPeersRequest();
                    client.encryptSendMsg(request);
                    String response;
                    while (true){
                        if((response = in.readLine()) != null){
                            break;
                        }
                    }
                    decryptMessage(client, response);


                } else if (command.equals(ConstUtil.CONNECT_PEER)){
                    System.out.println("connect_peer command");
                    String peer = argsBean.getPeer();
                    String[] peerHostPort = peer.split(":");
                    String peerIp = peerHostPort[0];
                    int peerPort = Integer.parseInt(peerHostPort[1]);

                    String request = ProtocolUtils.getClientRequest(ConstUtil.CONNECT_PEER_REQUEST, peerIp, peerPort);
                    client.encryptSendMsg(request);
                    System.out.println("Connect peers request Sent");
                    String response;
                    while (true){
                        if((response = in.readLine()) != null){
                            System.out.println("Get Connect Peer Response:" + response);

                            break;
                        }
                    }
                    decryptMessage(client, response);

                } else if (command.equals(ConstUtil.DISCONNECT_PEER)){
                    System.out.println("disconnect_peer command");
                    String peer = argsBean.getPeer();
                    String[] peerHostPort = peer.split(":");
                    int peerPort = Integer.parseInt(peerHostPort[1]);
                    String peerIp = peerHostPort[0];

                    String request = ProtocolUtils.getClientRequest(ConstUtil.DISCONNECT_PEER_REQUEST, peerIp, peerPort);
                    // 加密发送
                    client.encryptSendMsg(request);
                    System.out.println("Disconnect peer request Sent");
                    String response;
                    while (true){
                        if((response = in.readLine()) != null){
                            System.out.println("Get Disconnect Peer Response:" + response);

                            break;
                        }
                    }
                    decryptMessage(client, response);


                } else {
                    System.out.println("Invalid Command. Please try again.");
                }
            }

            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (CmdLineException e) {

            System.err.println(e.getMessage());

            //Print the usage to help the user understand the arguments expected
            //by the program
            parser.printUsage(System.err);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void decryptMessage(Client client, String response) {
        String payload = Document.parse(response).getString("payload");
        String decryptedResponse = AESUtil.desEncrypt(payload, aesKey);
        System.out.println("Decrypted Response: " + decryptedResponse);
        client.processMessage(decryptedResponse);
    }


}
