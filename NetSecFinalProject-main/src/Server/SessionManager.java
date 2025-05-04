package Server;

import javax.crypto.SecretKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static class Session {
        private final SecretKey sessionKey;
        private final long expiryTime;

        public Session(SecretKey sessionKey, long expiryTime) {
            this.sessionKey = sessionKey;
            this.expiryTime = expiryTime;
        }
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public String createSession(String username) {
        String sessionId = java.util.UUID.randomUUID().toString();
        SecretKey key = generateSessionKey();
        sessions.put(sessionId, new Session(key, System.currentTimeMillis() + 3600000)); 
        return sessionId;
    }

    public SecretKey getSessionKey(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session != null && session.expiryTime > System.currentTimeMillis()) {
            return session.sessionKey;
        }
        sessions.remove(sessionId); 
        return null;
    }

    private SecretKey generateSessionKey() {
        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("Session key generation failed", e);
        }
    }
}