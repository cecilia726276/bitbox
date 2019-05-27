package unimelb.bitbox.client;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import unimelb.RSA_test.RSAUtil.KeyGenerator;
import unimelb.RSA_test.RSAUtil.RSAUtil;
import unimelb.bitbox.message.ProtocolUtils;
import unimelb.bitbox.util.ConstUtil;
import unimelb.bitbox.util.Document;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;


/**
 * @Author SYZ
 * @create 2019-05-22 20:10
 */

// Reference: Tutorial 8: CmdLineArgsDemo
public class Client {
    String identity = "yizhoushen@Yizhous-MacBook-Pro.local";
    static Socket clientSocket;
    static DataInputStream in;
    static DataOutputStream out;
    String pubKey;
    static PublicKey aesKey;
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
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            System.out.println("Verify identity:" + identity);
            String idenMesg = ProtocolUtils.getAuthRequest(identity);
            out.writeUTF(idenMesg);
            out.flush();
            System.out.println("Identification sent, wait for response.");

            while (true){
                String response;
                if((response = in.readUTF()) != null){
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
        if (command == ConstUtil.AUTH_RESPONSE){
            Boolean status = document.getBoolean("status");
            if (status) {
                String aes128 = document.getString("AES128");

                try {
                    byte[] base642Byte = RSAUtil.base642Byte(aes128);
                    //用私钥解密
                    byte[] privateDecrypt = RSAUtil.privateDecrypt(base642Byte, privateKey);
                    pubKey = new String(privateDecrypt);
                    byte[] byteKey = Base64.getDecoder().decode(pubKey);
                    aesKey = KeyGenerator.decodePublicKey(byteKey);
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
                String host = document.getString("host");
                String port = document.getString("port");
                Boolean status = document.getBoolean("status");
                String message = document.getString("message");
                System.out.println("command:"+ command + "\n" + "host: " + host + "\n" + "port: " + port + "\n" + "status: "+status + "message: " + message);
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
        byte[] encryptedBytes = new byte[0];
        // 加密
        try {
            encryptedBytes = RSAUtil.publicEncrypt(request.getBytes(), aesKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 转base64
        String encrypted = RSAUtil.byte2Base64(encryptedBytes);
        String message = ProtocolUtils.getPayload(encrypted);
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        try (InputStream inputStream = new FileInputStream("bitboxclient_rsa")) {
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String priKey = s.hasNext() ? s.next() : "";

            System.out.println(priKey);
            privateKey = RSAUtil.string2PrivateKey(priKey);
        } catch (IOException e) {
            System.out.println("Could not read file ");
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            String[] hostPort = server.split(":");
            String ip = hostPort[0];
            int port = Integer.parseInt(hostPort[1]);
            String peer = argsBean.getPeer();
            String[] peerHostPort = peer.split(":");
            String peerIp = peerHostPort[0];
            int peerPort = Integer.parseInt(peerHostPort[1]);

            Boolean status = client.startConnection(ip, port);

            if (status){
                if(command == ConstUtil.LIST_PEERS){
                    String request = ProtocolUtils.getListPeersRequest();
                    client.encryptSendMsg(request);
                    System.out.println("Send list peers request");
                    String response;
                    while (true){
                        if((response = in.readUTF()) != null){
                            System.out.println("Get List peers Response:" + response);

                            break;
                        }
                    }
                    String payload = Document.parse(response).getString("payload");
                    // 转base64
                    byte[] payloadBytes = RSAUtil.base642Byte(payload);
                    // 解密, 应该用aesKey进行解密，但是没有方法？
                    String decrypted = "";
                    // decrypted 为payload 里解密后的数据
                    client.processMessage(decrypted);





                } else if (command == ConstUtil.CONNECT_PEER){
                    String request = ProtocolUtils.getClientRequest(ConstUtil.CONNECT_PEER_REQUEST, peerIp, peerPort);
                    client.encryptSendMsg(request);
                    System.out.println("Connect peers request Sent");
                    String response;
                    while (true){
                        if((response = in.readUTF()) != null){
                            System.out.println("Get Connect Peer Response:" + response);

                            break;
                        }
                    }
                    String payload = Document.parse(response).getString("payload");
                } else if (command == ConstUtil.DISCONNECT_PEER){
                    String request = ProtocolUtils.getClientRequest(ConstUtil.DISCONNECT_PEER_REQUEST, peerIp, peerPort);
                    // 加密发送
                    client.encryptSendMsg(request);
                    System.out.println("Disconnect peer request Sent");
                    String response;
                    while (true){
                        if((response = in.readUTF()) != null){
                            System.out.println("Get Disconnect Peer Response:" + response);

                            break;
                        }
                    }
                    String payload = Document.parse(response).getString("payload");


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



}