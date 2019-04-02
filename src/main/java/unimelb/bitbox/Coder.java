package unimelb.bitbox;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public enum Coder {
    /**
     * instance
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
