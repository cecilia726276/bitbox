package unimelb.bitbox.util;

/**
 * @author SYZ
 * @date 2019/4/18 23:01
 */
public class HandShakeUtils{

    private HandShakeUtils(){}

    /**
     * get request string
     *
     * @param host
     * @param port
     * @return request json
     */
    public static String getHandShakeRequest(String host, int port) {
        Document totalReqBody = new Document();
        totalReqBody.append("command", "HANDSHAKE_REQUEST");
        totalReqBody.append("hostPort", getHostPortDocument(host, port));
        return totalReqBody.toJson();
    }

    /**
     * get response string
     *
     * @param host
     * @param port
     * @return request json
     */
    public static String getHandShakeResponse(String host, int port){
        Document totalReqBody = new Document();
        totalReqBody.append("command", "HANDSHAKE_RESPONSE");
        totalReqBody.append("hostPort", getHostPortDocument(host, port));
        return totalReqBody.toJson();
    }

    /**
     * get host detail document
     *
     * @param host
     * @param port
     * @return get host detail document
     */
    private static Document getHostPortDocument(String host, int port) {
        Document hostPort = new Document();
        hostPort.append("host", host);
        hostPort.append("port", port);
        return hostPort;
    }

    public static void main(String[] args) {
        System.out.println(getHandShakeResponse("111",123));
    }
}
