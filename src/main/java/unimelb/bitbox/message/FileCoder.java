package unimelb.bitbox.message;

import java.util.Base64;

/**
 * @Author SYZ
 * @create 2019-04-30 17:01
 */
public enum FileCoder {
    /**
     * transfer between ByteBuffer and String
     * @param byteBuffer
     * @return
     */

    INSTANCE;

    Base64.Encoder encoder = Base64.getEncoder();
    Base64.Decoder decoder = Base64.getDecoder();

    public Base64.Decoder getDecoder(){
        return decoder;
    }

    public Base64.Encoder getEncoder() {
        return encoder;
    }
}
