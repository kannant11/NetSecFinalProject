package Common;

import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import merrimackutil.codec.Base32; 

public class Authentication {
    private static final int TIME_STEP = 30;
    private static final int CODE_DIGITS = 6;
    private static final int SECRET_BYTES = 20; 

    public static String generateTOTPSecret() {
        byte[] secret = new byte[SECRET_BYTES];
        new SecureRandom().nextBytes(secret);
        return Base32.encodeToString(secret, true); 
    }

    public static String generateTOTP(String base32Secret) {
        try {
            byte[] key = Base32.decode(base32Secret);
            long counter = System.currentTimeMillis() / 1000 / TIME_STEP;
            return generateTOTP(key, counter);
        } catch (Exception e) {
            throw new RuntimeException("TOTP generation failed", e);
        }
    }

    public static boolean verifyTOTP(String base32Secret, String clientCode) {
        try {
            byte[] key = Base32.decode(base32Secret); 
            long counter = System.currentTimeMillis() / 1000 / TIME_STEP;
            
            for (int i = -1; i <= 1; i++) {
                String serverCode = generateTOTP(key, counter + i);
                if (secureCompare(serverCode, clientCode)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static String generateTOTP(byte[] key, long counter) throws Exception {
        byte[] timeBytes = ByteBuffer.allocate(8).putLong(counter).array();
        SecretKeySpec keySpec = new SecretKeySpec(key, "RAW");
        byte[] hash = Security.computeHmac("HmacSHA1", timeBytes, keySpec);
        
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);
        
        return String.format("%0" + CODE_DIGITS + "d", binary % 1_000_000);
    }

    private static boolean secureCompare(String a, String b) {
        return Security.secureCompare(
            a.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
            b.getBytes(java.nio.charset.StandardCharsets.US_ASCII)
        );
    }
}