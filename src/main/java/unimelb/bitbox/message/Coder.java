package unimelb.bitbox.message;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public enum Coder {
    /**
     * from ByteBuffer to String
     * @param byteBuffer
     * @return
     */
    INSTANCE;

    private CharsetDecoder decoder;

    Coder(){
        this.decoder = Charset.forName("utf8").newDecoder();
    }

    public CharsetDecoder getDecoder(){
        return decoder;
    }
}
