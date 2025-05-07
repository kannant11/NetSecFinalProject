package Common;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import org.bouncycastle.crypto.generators.SCrypt;

public class Security {
    public static byte[] computeHmac(byte[] data, SecretKey key) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(key);
        return hmac.doFinal(data);
    }

    public static boolean secureCompare(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }

    public static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            int val = Integer.parseInt(hex.substring(index, index + 2), 16);
            bytes[i] = (byte) val;
        }
        return bytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    public static SecretKey deriveKey(String password, String saltBase64) throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] keyBytes = SCrypt.generate(
            password.getBytes(java.nio.charset.StandardCharsets.UTF_8), salt, 16384, 8, 1, 32);
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] generateNonce(int size) {
        byte[] nonce = new byte[size];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static byte[] computeHmac(String algorithm, byte[] data, SecretKey key) throws Exception {
        Mac hmac = Mac.getInstance(algorithm);
        hmac.init(key);
        return hmac.doFinal(data);
    }

    public static byte[] hashPassword(String password, byte[] salt) throws Exception {
    return SCrypt.generate(password.getBytes(StandardCharsets.UTF_8),salt,16384, 8, 1, 32 );
    }

    public static byte[] encrypt(SecretKey key, byte[] data, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(SecretKey key, byte[] ciphertext, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return cipher.doFinal(ciphertext);
    }
}