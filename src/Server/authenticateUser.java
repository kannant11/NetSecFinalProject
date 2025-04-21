package Server;

import merrimackutil.json.types.JSONObject;
import merrimackutil.util.NonceCache;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;
import java.security.MessageDigest;

import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.digest.SHA224Digest;
import org.bouncycastle.crypto.params.KeyParameter;


public class authenticateUser {
    private userManager userManager;

    public authenticateUser(userManager userManager){
         this.userManager = userManager;
    }

    public boolean authenticate(String username, String password, String totpCode, String nonce, JSONObject user, String receivedHmac){
        try{
            if(!verifyPassword(password, user)){
                return false;
            }
            
            String secret = user.getString("totpSecret");
            if(!verifyTOTP(secret, totpCode)){
                return false;
            }

            if(user.containsKey("hmacKey")){
                String hmacKey = user.getString("hmacKey");
                String expectedHmac = calculateHmac(username + nonce, hmacKey);

                if(receivedHmac == null || receivedHmac.isEmpty() || !MessageDigest.isEqual
                   (expectedHmac.getBytes(StandardCharsets.UTF_8), receivedHmac.getBytes(StandardCharsets.UTF_8))){
                    return false;
                }
            }
            return true;
        }catch(Exception e){
            return false;
                }
    }
    
    private boolean verifyPassword(String password, JSONObject user) throws Exception{
        byte[] salt = Base64.getDecoder().decode(user.getString("salt"));
        byte[] storedHash = Base64.getDecoder().decode(user.getString("password"));
        byte[] computedHash = SCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, 20000, 10, 1, storedHash.length);

        return MessageDigest.isEqual(storedHash, computedHash);
    }

    private boolean verifyHmac(String username, String hmacKey, String receivedHmac) throws Exception{
        if(receivedHmac == null || receivedHmac.isEmpty()){
            return false;
        }

        String computedHmac = calculateHmac(username, hmacKey);
        return MessageDigest.isEqual(computedHmac.getBytes(StandardCharsets.UTF_8), receivedHmac.getBytes(StandardCharsets.UTF_8));
    }

    private String calculateHmac(String data, String key) throws Exception{
        HMac hmac = new HMac(new SHA256Digest());

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[hmac.getMacSize()];

        hmac.init(new KeyParameter(keyBytes));
        hmac.update(dataBytes, 0, dataBytes.length);
        hmac.doFinal(result, 0);

        return Base64.getEncoder().encodeToString(result);
    }

    private boolean verifyTOTP(String secret, String code){
        return code != null && code.length() >= 5;
    }
}

    
        
