package com.example.healthy;


import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class sign_verify {

    public static boolean verifySignature(String text,String sig,String publicKey){
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        PublicKey pKey = null;
        try {
            assert keyFactory != null;
            pKey = keyFactory.generatePublic(new X509EncodedKeySpec(hexToBytes(publicKey)));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

//Let's check the signature
        boolean isCorrect = false;
        try {
            isCorrect = verify(text, sig, pKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isCorrect;
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(512, new SecureRandom());

        return generator.generateKeyPair();
    }

    private static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    private static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }

    public static void main(String[] args) {
        KeyPair pair = null;
        try {
            pair = generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] publicKey = pair.getPublic().getEncoded();

        String pStr = byte2Hex(publicKey);

        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        PublicKey pKey = null;
        try {
            pKey = keyFactory.generatePublic(new X509EncodedKeySpec(hexToBytes("305c300d06092a864886f70d0101010500034b00304802410089b032a12a0342beb9f7bf6537365501f34b2cb5ca77dfb68e5a3d258909b55b023d9cf99cba219aee0d3d7c5076bb88624c32c175823c432876548e86a4a6550203010001")));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

//Let's check the signature
        boolean isCorrect = false;
        try {
            isCorrect = verify("你好呀", "LwjTd3+cwdB99MQmZXRAfxuz1acuuB/fSYQcpzKMQ5PCc9nKjeR0A/C7PvEmY3VD629rJWB0LgE763YKlsvwEg==", pKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Signature correct: " + isCorrect);
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuffer = new StringBuilder();
        String temp = null;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    private static byte[] hexToBytes(String hex) {
        hex = hex.length() % 2 != 0 ? "0" + hex : hex;

        byte[] b = new byte[hex.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(hex.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}
