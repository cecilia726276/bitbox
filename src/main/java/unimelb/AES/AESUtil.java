package unimelb.AES;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Random;

public class AESUtil {
    public static String encrypt(String data, String key) {

//        String ivString = "0000000000000000";
////        //偏移量
////        byte[] iv = ivString.getBytes();
        try {
            //Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            Cipher cipher = Cipher.getInstance("AES");
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = data.getBytes();
//            int length = dataBytes.length;
//            int newLength = 0;
//            //计算需填充长度
//            if (length % blockSize != 0) {
//                newLength = length + (blockSize - (length % blockSize));
//            }
//           // Random random = new Random();
//            byte[] plaintext = new byte[newLength];
//            for (int i = length; i < plaintext.length; i++){
//                plaintext[i] = (byte)random.nextInt(100);
//                System.out.println("plaintext[i]:" + plaintext[i]);
//            }

            //填充

            //System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            //设置偏移量参数
            //IvParameterSpec ivSpec = new IvParameterSpec(iv);
            //cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryped = cipher.doFinal(dataBytes);

            //return parseByte2HexStr(encryped);
            return Base64.getEncoder().encodeToString(encryped);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static String desEncrypt(String data, String key) {

//        String ivString = "0000000000000000";
//        byte[] iv = ivString.getBytes();

        try {
            byte[] encryp = Base64.getDecoder().decode(data);
            //Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            //IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] original = cipher.doFinal(encryp);
            return new String(original);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }


    public static String generateKey() throws Exception {
        //实例化密钥生成器
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        //生成密钥
        SecretKey secretKey = kg.generateKey();
        //获得密钥的字符串形式
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }


    public static void main(String[] args) {
        String data = "1{234{:}:56dopooooooqqq";
        //String key = "186751244B391A6DCA84778E0D6A8910";
        String key = "";
        try{
            key = AESUtil.generateKey();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("key = "+ key);
        String encrypt = encrypt(data, key);
        System.out.println("加密前：" + data);
        System.out.println("加密后：" + encrypt);
        String desEncrypt = desEncrypt(encrypt, key);
        System.out.println("解密后：" + desEncrypt);
    }

}
