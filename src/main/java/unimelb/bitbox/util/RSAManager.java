package unimelb.bitbox.util;

import unimelb.RSA_test.RSAUtil.KeyGenerator;
import unimelb.RSA_test.RSAUtil.RSAUtil;

import java.security.PublicKey;
import java.util.Base64;
public class RSAManager {

    public static String RSAEncrypt(String pubStr, String rawMsg){ //return RSA encryted AES secret key
        byte[] byteKey = Base64.getDecoder().decode(pubStr);
        try{
            PublicKey publicKey = KeyGenerator.decodePublicKey(byteKey);
            byte[] encrypbytes = RSAUtil.publicEncrypt(rawMsg.getBytes(),publicKey);
            return Base64.getEncoder().encodeToString(encrypbytes);
        }catch (Exception e){e.printStackTrace();}
        return null;
    }



}
