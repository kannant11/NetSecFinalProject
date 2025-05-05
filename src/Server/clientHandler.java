package Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.util.NonceCache;
import Common.Authentication;
import Common.Security;

public class clientHandler implements Runnable {
    private final Socket socket;
    private final userManager userMgr;
    private final videoGameManager gameMgr;
    private final NonceCache serverNonceCache;
    private final SessionManager sessionManager;
    private final NonceCache clientNonceCache;

    public clientHandler(Socket socket, userManager userMgr, videoGameManager gameMgr, 
     NonceCache serverNonceCache, SessionManager sessionManager) {
        this.socket = socket;
        this.userMgr = userMgr;
        this.gameMgr = gameMgr;
        this.serverNonceCache = serverNonceCache;
        this.sessionManager = sessionManager;
        this.clientNonceCache = new NonceCache(16, 120);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
              PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

             JSONObject authRequest = JsonIO.readObject(in.readLine());
             handleAuthRequest(authRequest, out);
            
             String username = authRequest.getString("username");
             SecretKey userKey = processClientResponse(in.readLine(), out);
             if (userKey == null) return;

             sendAuthSuccess(username, out);
             handleSessionSetup(username, userKey, out);
             sendEncryptedGameList(out);

             while (true) {
                 String request = in.readLine();
                 if (request == null) break;
                
                 JSONObject req = JsonIO.readObject(request);
                 if("genre_search".equals(req.getString("type"))) {
                    handleGenreSearch(req.getString("genre"), out);
                }
            }
        }catch(Exception e) {
            System.err.println("Client error: " + e.getMessage());
        }finally{
            try{
                socket.close(); 
            }catch(Exception e){
            }
        }
    }

    private void handleAuthRequest(JSONObject authRequest, PrintWriter out) {
        if (!authRequest.containsKey("username") || !authRequest.containsKey("client_nonce")) {
            sendError(out, "Invalid auth request");
            return;
        }
        
        byte[] clientNonce = Security.hexToBytes(authRequest.getString("client_nonce"));
        if (clientNonceCache.containsNonce(clientNonce)) {
            sendError(out, "Replayed nonce");
            return;
        }
        clientNonceCache.addNonce(clientNonce);
        
        JSONObject challenge = new JSONObject();
        challenge.put("server_nonce", Security.bytesToHex(serverNonceCache.getNonce()));
        challenge.put("salt", userMgr.getUserSalt(authRequest.getString("username")));
        out.println(challenge.toJSON());
    }

    private SecretKey processClientResponse(String response, PrintWriter out) {
        try {
            JSONObject res = JsonIO.readObject(response);
            JSONObject user = userMgr.getUser(res.getString("username"));
            
            SecretKey userKey = new SecretKeySpec(Base64.getDecoder().decode(user.getString("password")), "AES");
            if (!Security.secureCompare(Security.computeHmac(Security.hexToBytes(res.getString("server_nonce")), 
             userKey),Security.hexToBytes(res.getString("hmac")))) {
                sendError(out, "Authentication failed");
                return null;
            }

            if (!Authentication.verifyTOTP(user.getString("totpSecret"),
             res.getString("totp"))) {
                sendError(out, "Invalid 2FA code");
                return null;
            }
            return userKey;
        }catch(Exception e){
            sendError(out, "Auth error: " + e.getMessage());
            return null;
        }
    }

    private void sendAuthSuccess(String username, PrintWriter out) {
        JSONObject authSuccess = new JSONObject();
        authSuccess.put("success", true);
        authSuccess.put("subscribed", userMgr.isSubscribed(username));
        authSuccess.put("message", "Authenticated");
        out.println(authSuccess.toJSON());
    }

    private void handleSessionSetup(String username, SecretKey userKey, PrintWriter out) throws Exception {
        String sessionId = sessionManager.createSession(username);
        SecretKey sessionKey = sessionManager.getSessionKey(sessionId);
        
        byte[] iv = Security.generateNonce(12);
        byte[] encryptedKey = Security.encrypt(userKey, sessionKey.getEncoded(), iv);
        
        JSONObject keyResponse = new JSONObject();
        keyResponse.put("type", "session_key");
        keyResponse.put("iv", Base64.getEncoder().encodeToString(iv));
        keyResponse.put("key", Base64.getEncoder().encodeToString(encryptedKey));
        out.println(keyResponse.toJSON());
    }

    private void sendEncryptedGameList(PrintWriter out) throws Exception {
        List<JSONObject> games = gameMgr.getAllGames();
        JSONArray gamesArray = new JSONArray(games);
        JSONObject gamesWrapper = new JSONObject();
        gamesWrapper.put("games", gamesArray);
        
        byte[] iv = Security.generateNonce(12);
        byte[] encrypted = Security.encrypt(
            sessionManager.getSessionKey("current_session"), 
            gamesWrapper.toJSON().getBytes(),
            iv
        );
        
        JSONObject response = new JSONObject();
        response.put("type", "game_list");
        response.put("iv", Base64.getEncoder().encodeToString(iv));
        response.put("data", Base64.getEncoder().encodeToString(encrypted));
        out.println(response.toJSON());
    }

    private void handleGenreSearch(String genre, PrintWriter out) {
        try {
            List<JSONObject> filtered = gameMgr.getGamesByGenre(genre);
            JSONObject response = new JSONObject();
            response.put("type", "genre_results");
            response.put("games", new JSONArray(filtered));
            out.println(response.toJSON());
        } catch (Exception e) {
            sendError(out, "Genre search failed");
        }
    }

    private void sendError(PrintWriter out, String message) {
        JSONObject error = new JSONObject();
        error.put("success", false);
        error.put("message", message);
        out.println(error.toJSON());
    }
}