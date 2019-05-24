package unimelb.RSA_test.RSAUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

//import org.junit.Test;

//import com.zhuyun.rsa.RSAUtil;
// ssh-keygen -m PEM -t rsa

public class TestRSA {

    public static void main(String []args){
        TestRSA test = new TestRSA();
        test.testRSA();
    }
    public void testRSA(){
        try {
            //===============生成公钥和私钥，公钥传给客户端，私钥服务端保留==================
            //生成RSA公钥和私钥，并Base64编码
            //KeyPair keyPair = RSAUtil.getKeyPair();
            //String pubStr = "AAAAB3NzaC1yc2EAAAADAQABAAABAQDnYPzgyKwBT9vdxyXMdIGmx+EEISgdqWNvqRBEF3dtUItLA1JtSH0n8LMcNPm3RA5X1GxTTjrhOi8cACOyZknk+M97i6hTTV55zHOLzztS6s9OcDFb5YEi2je3LjDNBaLYPKI6+fpTKRaXSTWNoF76oW0zAv77AQzvJ1xz7n8wWkfmS6JyC4a/QcUojnBLU6KZPmNAS1Q/xuh2cfhS+6A2XcofwfVaXS2/+Z90/3NHqVWnRIyyJtGxRAYUgpuU9wUqF6+AxLnx4V2xRoB7y/OpJeYP1IhLHjcqmj/C0jqf38tXBum3jjBKd98/WcG5jD07zlxteA699nYPJW9H1TuJ";
            String pubStr = "AAAAB3NzaC1yc2EAAAADAQABAAABAQC1n+AC0q5yx2UHwpSLXO3KNgeZVoz3rjAZPe797I0NbgFROy1AcF52OF9JHw0k/zpTF0KRDUz8Cn/UxWsBfZU1umvz9qXvorX9RoS9glQJwiL2JntcmSV5F26HbNIYnZlRr5M4lw0CVlZ5JpfETjn40OcrNUBIXSQUruX1lXPwgj0+HhfWhnYn4uzhEwS4ZnVxZR06fp4LsgLtsqoonECZotUfnu800pVFFaZ0H0HIBAiZGCRlPeWtJXaVF1R3Pq5F4O60FHRLdy9F/YAZvfqCbhC7tjyAmcvO6FiEsHT/pbaDxqY1rlKRx916a3UDAMrxUIW0GNhAxzRtXTRVQrrV";
            byte[] byteKey = Base64.getDecoder().decode(pubStr);
            PublicKey publicKey = KeyGenerator.decodePublicKey(byteKey);
            //String message = "Hello World!";
            String message = "igYd/UkW+R2ZbWazCoJYMg==";
            System.out.println("raw message:"+message);
            //encrypt with public key
            byte[] publicEncrypt = RSAUtil.publicEncrypt(message.getBytes(), publicKey);
            //加密后的内容Base64编码
            String byte2Base64 = RSAUtil.byte2Base64(publicEncrypt);
            System.out.println("公钥加密并Base64编码的结果：" + byte2Base64);

            //PrivateKey privateKey = KeyGenerator.getPrivateKey("E:/unimelb_cs/distributed systems/project1/bitbox/src/main/java/unimelb/RSA_test/test2.der");
            PrivateKey privateKey = KeyGenerator.toPrivateKey("abc");
            //加密后的内容Base64解码
            byte[] base642Byte = RSAUtil.base642Byte(byte2Base64);
            //用私钥解密
            byte[] privateDecrypt = RSAUtil.privateDecrypt(base642Byte, privateKey);
            //解密后的明文
            System.out.println("解密后的明文: " + new String(privateDecrypt));



//            String publicKeyStr = RSAUtil.getPublicKey(keyPair);
//            String privateKeyStr = RSAUtil.getPrivateKey(keyPair);
//            System.out.println("public key:" + keyPair.getPublic());
//            System.out.println("private key:" + keyPair.getPrivate());
//            System.out.println("RSA公钥Base64编码:" + publicKeyStr);
//            System.out.println("RSA私钥Base64编码:" + privateKeyStr);

//            //=================客户端=================
//            //hello, i am infi, good night!加密
//            String message = "hello, i am infi, good night!";
//            //将Base64编码后的公钥转换成PublicKey对象
//            PublicKey publicKey = RSAUtil.string2PublicKey(publicKeyStr);
//            //用公钥加密
//            byte[] publicEncrypt = RSAUtil.publicEncrypt(message.getBytes(), publicKey);
//            //加密后的内容Base64编码
//            String byte2Base64 = RSAUtil.byte2Base64(publicEncrypt);
//            System.out.println("公钥加密并Base64编码的结果：" + byte2Base64);
//
//
//            //##############	网络上传输的内容有Base64编码后的公钥 和 Base64编码后的公钥加密的内容     #################
//
//
//
//            //===================服务端================
//            //将Base64编码后的私钥转换成PrivateKey对象
//            PrivateKey privateKey = RSAUtil.string2PrivateKey(privateKeyStr);
//            //加密后的内容Base64解码
//            byte[] base642Byte = RSAUtil.base642Byte(byte2Base64);
//            //用私钥解密
//            byte[] privateDecrypt = RSAUtil.privateDecrypt(base642Byte, privateKey);
//            //解密后的明文
//            System.out.println("解密后的明文: " + new String(privateDecrypt));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
