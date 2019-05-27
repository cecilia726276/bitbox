package unimelb.bitbox.message;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public enum Coder {
    /**
     * instance
     *
     *
     * @param byteBuffer
     * @return
     */
    INSTANCE;

    private CharsetDecoder decoder;
    private CharsetEncoder encoder;

    Coder() {

        this.decoder = Charset.forName("utf8").newDecoder();
        this.encoder = Charset.forName("utf8").newEncoder();
    }

    public CharsetDecoder getDecoder() {
        return decoder;
    }
}
