package com.example.healthy;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TextSignature {

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
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("输入0以获取随机的新密钥对，输入1以使用已有私钥进行签名：");
            int idx = scanner.nextInt();
            if (idx == 0) {
                KeyPair pair = null;
                try {
                    pair = generateKeyPair();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (pair != null) {
                    System.out.println("您的私钥：" + byte2Hex(pair.getPrivate().getEncoded()));
                    System.out.println("您的公钥：" + byte2Hex(pair.getPublic().getEncoded()));
                    System.out.println("请务必保管好您的私钥！！！");
                }
            } else if (idx == 1) {
                scanner.nextLine();
                System.out.println("请在下方粘贴您的私钥：");
                String privateK = scanner.nextLine();
                String text = null;
                KeyFactory keyFactory = null;
                PrivateKey privateKey = null;
                try {
                    keyFactory = KeyFactory.getInstance("RSA");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(hexToBytes(privateK));

                try {
                    privateKey = keyFactory.generatePrivate(keySpec);
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }
                System.out.println("请输入您要签名的文本：");
                text = scanner.nextLine();
                String sig = null;
                try {
                    sig = sign(text, privateKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("您的签名如下：");
                System.out.println(sig);
            } else {
                System.out.println("请重新输入命令");
            }
        }
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
