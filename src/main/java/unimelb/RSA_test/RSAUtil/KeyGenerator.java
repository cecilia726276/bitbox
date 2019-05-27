package unimelb.RSA_test.RSAUtil;

//import sun.misc.BASE64Decoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;

//java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//import org.bouncycastle.util.io.pem.PemObject;
//import org.bouncycastle.util.io.pem.PemReader;

public class KeyGenerator {
    public static PrivateKey toPrivateKey(String str) {
        str = "MIIEowIBAAKCAQEAtZ/gAtKucsdlB8KUi1ztyjYHmVaM964wGT3u/eyNDW4BUTst" +
                "QHBedjhfSR8NJP86UxdCkQ1M/Ap/1MVrAX2VNbpr8/al76K1/UaEvYJUCcIi9iZ7" +
                "XJkleRduh2zSGJ2ZUa+TOJcNAlZWeSaXxE45+NDnKzVASF0kFK7l9ZVz8II9Ph4X" +
                "1oZ2J+Ls4RMEuGZ1cWUdOn6eC7IC7bKqKJxAmaLVH57vNNKVRRWmdB9ByAQImRgk" +
                "ZT3lrSV2lRdUdz6uReDutBR0S3cvRf2AGb36gm4Qu7Y8gJnLzuhYhLB0/6W2g8am" +
                "Na5Skcfdemt1AwDK8VCFtBjYQMc0bV00VUK61QIDAQABAoIBADfbKi5UErhT4BtJ" +
                "2RsfAjZM9XtP5dyKIlqw9F39MMfvi9Iqi9kkdbiPz6YSOZ2mLI6/OYaYe5OLuxJ5" +
                "gFYeBBRY97g4o4GWHbf9xvbtLOEvZkcjQI8SvjaGYUSez+IoHa3EfFdMBQEyAjgS" +
                "CVyi3itKO73LC7D1jBIcU7Z6NwTA1EUUEEn5OXdms0Qjakb0o2xG38MgIZE2LB9b" +
                "d5qfDZIDh0R8NtvPMCOJAAJXJOtHbMXqnQG2txS+rIBjbgZB1DTLQse8+gcGFk+K" +
                "HSIlvdWRMcakRyUi04uv0DoXIX6dYo0cJod5n712yKitunj2kiFxjTSIORQjkmoP" +
                "kgYyLIUCgYEA4eCOnj/HO7xzxF7lsoo35ud6ZTT9fO4zPsoP2XxXnYwKml5W3kjp" +
                "SnJG3rsFQYHwR7TqUWZsKJmKURoAAmNKYqfpG+uYAxawjHlZ/yLb/Rurgdm/eYAa" +
                "YIAi/miBxHPuBLqGH5h07abU8DbP1SnoMcqXgctPHZfs/DeBRury1FMCgYEAzdiI" +
                "YgBnnrQIwzCwF03PUM9qgj47H0tm5UvXdq5kxPpmvZs4tJ504hPDAvi1hAWMga8b" +
                "YewDBfPKr2tEDN8A2fFfj/xTAz+sIt+8h64hOy2KAaS0HoyvbCO1Qp0JAmXbEPPh" +
                "3Zymkvj2/WBifajKn0Xye0pOH1pcd5II1sv4zzcCgYB0fYIv/QZ8OVGfGa3uqTfx" +
                "XroRzgVZU+Ob40vPR0BMYTfqqvK0Cvg9y7ffEKbCRQgtgxFBT8hCHAVolDcjBCAN" +
                "xzkCjDtGhIIiwEb4vPqli4qlGi6Us8tmr07c0/rw3TUIvUWEr/TFx7+T70C6V7WH" +
                "UEtYxgiUY5D19o42i98WPwKBgCBmA1lBbQ26kmJ+aEjSs12pt77WIqITURen7zq7" +
                "yhqCuub+5lbvVcA7kgcGtDMaWHoU4H9yESu/qlgfzu8jrlOfPQZBlaM+Q06d3mOQ" +
                "kaRpz33guYTRac7gc+gPJVreQzOQ3yztOf6J9v38TKQwi+uzq62iDVe79i/PqVp9" +
                "ciTTAoGBANgZhFUgPFqyrAYp0DUFdAQZQs9l56VyvvaPi5SQER/3RPA2nx4hND00" +
                "G+C1dHGKu8OKhmUHmOLAeAelm10spx2+BCZONrvLqMh8JB1sWG0k+3suOPuA2yio" +
                "SkHXIpS4Kfn+yC3OQq7xAGuZFLghuX1lysWkjF4r5Ry+K493UxkS";
        //FileInputStream fis = new FileInputStream("id_rsa");
        str = str.replaceAll("\n","");
        byte[] decodeKeyinfo = Base64.getDecoder().decode(str);

        //byte[]  = (new BASE64Decoder()).decodeBuffer(keyinfo);

        try{
            //使用 DerInputStream 读取密钥信息
            DerInputStream dis = new DerInputStream(decodeKeyinfo);
//密钥不含 otherPrimeInfos 信息，故只有 9 段
            DerValue[] ders = dis.getSequence(9);
//依次读取 RSA 因子信息
            int version = ders[0].getBigInteger().intValue();
            BigInteger modulus = ders[1].getBigInteger();
            BigInteger publicExponent = ders[2].getBigInteger();
            BigInteger privateExponent = ders[3].getBigInteger();
            BigInteger primeP = ders[4].getBigInteger();
            BigInteger primeQ = ders[5].getBigInteger();
            BigInteger primeExponentP = ders[6].getBigInteger();
            BigInteger primeExponentQ = ders[7].getBigInteger();
            BigInteger crtCoefficient = ders[8].getBigInteger();
//generate public key and private key
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaPublicKeySpec =
                    new RSAPublicKeySpec(modulus, publicExponent);
            PublicKey publicKey = keyFactory.generatePublic(rsaPublicKeySpec);
            RSAPrivateCrtKeySpec rsaPrivateKeySpec =
                    new RSAPrivateCrtKeySpec(modulus,publicExponent,privateExponent,
                            primeP,primeQ,primeExponentP,primeExponentQ,crtCoefficient);
            PrivateKey privateKey = keyFactory.generatePrivate(rsaPrivateKeySpec);
            return privateKey;
        }catch (Exception e){e.printStackTrace();}

//        PublicKey pubk = keyPair.getPublic();
//        System.out.println(pubk);


//        KeySpec keySpec = new X509EncodedKeySpec(pubk.getEncoded());
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        System.out.println(keyFactory.generatePublic(keySpec));
//
//        KeySpec keySpec2 = new PKCS8EncodedKeySpec(prik.getEncoded());
//        System.out.println(keyFactory.generatePrivate(keySpec2));
//        byte[] bytes = Base64.getDecoder().decode(str);
//        //StringReader reader = new StringReader(str);
//
//        try {
//            //FileReader fileReader = new FileReader(privateKeyFileName);
//            //char[] ch = new char[1024] ;
////            int len = 0;
////            while((len = fileReader.read(ch))!= -1){
////                System.out.println(new String(ch,0,len));
////                System.out.println();
////            }
////            System.out.println();fileReader.read();
//
//           // PemReader pemReader = new PemReader(fileReader);
////            PemReader pemReader = new PemReader(reader);
////            PemObject pemObject = pemReader.readPemObject();
////            byte[] pemContent = pemObject.getContent();
////            pemReader.close();
//            //fileReader.close();
//            final PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(bytes);
//            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//            final PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);
//            return privateKey;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
        return null;
    }

//    public static PrivateKey getPrivateKey(String fileName) throws Exception{
//        //openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt
//        File file = new File(fileName);
//        FileInputStream fis = new FileInputStream(file);
//        DataInputStream dis = new DataInputStream(fis);
//        byte[] keyBytes = new byte[(int)file.length()];
//        dis.readFully(keyBytes);
//        dis.close();
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//        return privateKey;
//    }
    public static byte[] base642Byte(String base64Key) throws IOException{
        //BASE64Decoder decoder = new BASE64Decoder();
        return Base64.getDecoder().decode(base64Key);
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
            //PrivateKey privateKey = KeyGenerator.getPrivateKey("E:/COMP90015Proj2/src/private_key.der");
            String privateFileName = "E:/unimelb_cs/distributed systems/project1/bitbox/src/main/java/unimelb/RSA_test/client_rsa.pem";

            PrivateKey privateKey = toPrivateKey(privateFileName);
            System.out.println(privateKey);

            //System.out.println(privateKey);
        }catch(Exception e){e.printStackTrace();}


    }

}
