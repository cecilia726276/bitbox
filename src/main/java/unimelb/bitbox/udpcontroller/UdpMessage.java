package unimelb.bitbox.udpcontroller;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

public class UdpMessage {
    private SocketAddress socketAddress;
    private String message;

    public UdpMessage(SocketAddress socketAddress, String message) {

        this.socketAddress = socketAddress;
        this.message = message;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public void setSocketAddress(SocketAddress socketAddress) {
        this.socketAddress = socketAddress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
