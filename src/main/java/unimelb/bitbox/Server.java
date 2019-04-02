package unimelb.bitbox;


import java.io.IOException;

public interface Server {

    /**
     *
     * @param serverPort
     * @throws IOException
     */
    void startServer(int serverPort) throws IOException;
}
