package unimelb.bitbox.packageAPI;

import org.json.simple.JSONObject;
import unimelb.bitbox.util.Configuration;
import unimelb.bitbox.util.Document;

import java.util.ArrayList;

public class HandRequestHandler {
    public static int currentConnectionNumber = 0;
    public static void getHandShakeRequest(Document doc) {
        if(doc.containsKey("hostPort") == false)
        {
            sendInvalidRefuseMsg();
        }
        else if(currentConnectionNumber > Integer.parseInt(Configuration.getConfigurationValue("maximumIncommingConnections")))
        {
            sendOutLimitRefuseMsg(doc);
        }
        else
        {
            //connection succeed
            currentConnectionNumber++;
            sendResponseMsg();
        }
    }
    private static String sendInvalidRefuseMsg()
    {
        Document invalidMsg = new Document();
        invalidMsg.append("command", "INVALID_PROTOCOL");
        invalidMsg.append("message", "message must contain a command field as string");
        return invalidMsg.toJson();
    }
    private static String sendOutLimitRefuseMsg(Document doc)
    {
        Document outLimitMsg = new Document();
        outLimitMsg.append("command", "CONNECTION_REFUSED");
        JSONObject hostPort = (JSONObject) doc.get("hostPort");
//        String host = (String)hostPort.get("host");
//        int port = (int)hostPort.get("port");
        JSONObject ownHostPort = new JSONObject();
        ownHostPort.put("host", Configuration.getConfigurationValue("advertisedName"));
        ownHostPort.put("port", Configuration.getConfigurationValue("port"));
        ArrayList<JSONObject> list = new ArrayList<>();
        list.add(hostPort);
        list.add(ownHostPort);
        outLimitMsg.append("peers", list);
        outLimitMsg.append("message", "connection limit reached");
        return outLimitMsg.toJson();
    }
    private static String sendResponseMsg()
    {
        JSONObject response = new JSONObject();
        response.put("command", "HANDSHAKE_RESPONSE");
        JSONObject ownHostPort = new JSONObject();
        ownHostPort.put("host", Configuration.getConfigurationValue("advertisedName"));
        ownHostPort.put("port", Configuration.getConfigurationValue("port"));
        response.put("hostPort", ownHostPort);
        return response.toJSONString();
    }

}
