package unimelb.RSA_test.RSAUtil;

import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.interfaces.RSAPublicKey;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.math.BigInteger;
import java.util.Arrays;

public class KeyGenerator {
    public static PrivateKey getPrivateKey(String fileName) throws Exception{
        //openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)file.length()];
        dis.readFully(keyBytes);
        dis.close();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }
    public static byte[] base642Byte(String base64Key) throws IOException{
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(base64Key);
    }

    public static int decodeUInt32(byte[] key, int start_index){
        byte[] test = Arrays.copyOfRange(key, start_index, start_index + 4);
        return new BigInteger(test).intValue();
//      int int_24 = (key[start_index++] << 24) & 0xff;
//      int int_16 = (key[start_index++] << 16) & 0xff;
//      int int_8 = (key[start_index++] << 8) & 0xff;
//      int int_0 = key[start_index++] & 0xff;
//      return int_24 + int_16 + int_8 + int_0;
    }

    public static PublicKey decodePublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException{
        byte[] sshrsa = new byte[] { 0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's',
                'a' };
        int start_index = sshrsa.length;
        /* Decode the public exponent */
        int len = decodeUInt32(key, start_index);
        start_index += 4;
        byte[] pe_b = new byte[len];
        for(int i= 0 ; i < len; i++){
            pe_b[i] = key[start_index++];
        }
        BigInteger pe = new BigInteger(pe_b);
        /* Decode the modulus */
        len = decodeUInt32(key, start_index);
        start_index += 4;
        byte[] md_b = new byte[len];
        for(int i = 0 ; i < len; i++){
            md_b[i] = key[start_index++];
        }
        BigInteger md = new BigInteger(md_b);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        KeySpec ks = new RSAPublicKeySpec(md, pe);
        return keyFactory.generatePublic(ks);
    }

    public static void main(String args[]){
        String pubStr = "AAAAB3NzaC1yc2EAAAADAQABAAABAQC1n+AC0q5yx2UHwpSLXO3KNgeZVoz3rjAZPe797I0NbgFROy1AcF52OF9JHw0k/zpTF0KRDUz8Cn/UxWsBfZU1umvz9qXvorX9RoS9glQJwiL2JntcmSV5F26HbNIYnZlRr5M4lw0CVlZ5JpfETjn40OcrNUBIXSQUruX1lXPwgj0+HhfWhnYn4uzhEwS4ZnVxZR06fp4LsgLtsqoonECZotUfnu800pVFFaZ0H0HIBAiZGCRlPeWtJXaVF1R3Pq5F4O60FHRLdy9F/YAZvfqCbhC7tjyAmcvO6FiEsHT/pbaDxqY1rlKRx916a3UDAMrxUIW0GNhAxzRtXTRVQrrV";

        try{
            //byte[] byteKey = Base64.getDecoder().decode(pubStr);
//
//            for(int i = 0; i < priByteKey.length; i++)
//                System.out.print(priByteKey[i]);

            //PublicKey publicKey = KeyGenerator.decodePublicKey(byteKey);
            PrivateKey privateKey = KeyGenerator.getPrivateKey("E:/COMP90015Proj2/src/private_key.der");
            //System.out.println(publicKey);

            System.out.println(privateKey);
        }catch(Exception e){e.printStackTrace();}


    }

}
