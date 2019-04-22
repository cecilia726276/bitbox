package unimelb.bitbox.draft;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;

/**
 * A tool used to transfer between ByteBuffer and String
 * @Author SYZ
 * @create 2019-04-22 20:00
 */
public class CoderUtil {

    /**
     * from String to ByteBuffer
     * @param string
     * @return
     */
    public static ByteBuffer getByteBuffer(String string) {
        return ByteBuffer.wrap(string.getBytes());
    }

    /**
     * from ByteBuffer to String
     * @param byteBuffer
     * @return
     */
    public static String createString(ByteBuffer byteBuffer) {
        CharsetDecoder decoder = null;
        CharBuffer charBuffer;
        try {
            charBuffer = decoder.decode(byteBuffer);
            // charBuffer = decoder.decode(byteBuffer.asReadOnlyBuffer()); // is it necessary to use asReadOnlyBuffer?
        } catch (CharacterCodingException e) {
            e.printStackTrace();
            return "";
        }
            return charBuffer.toString();
    }
}
