package Common;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;
import java.util.List;

public class Protocol {

    public static class LoginRequest {
        public String username;
        public String hmac;   
        public String totp;  

        public LoginRequest(String username, String hmac, String totp) {
            this.username = username;
            this.hmac = hmac;
            this.totp = totp;
        }

        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            obj.put("type", "login");
            obj.put("username", username);
            obj.put("hmac", hmac);
            obj.put("totp", totp);
            return obj;
        }

        public static LoginRequest fromJSON(JSONObject obj) {
            return new LoginRequest(
                obj.getString("username"),
                obj.getString("hmac"),
                obj.getString("totp")
            );
        }
    }

    public static class GameList {
        public String iv;    
        public String data;  

        public GameList(String iv, String data) {
            this.iv = iv;
            this.data = data;
        }

        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            obj.put("type", "game_list");
            obj.put("iv", iv);
            obj.put("data", data);
            return obj;
        }

        public static GameList fromJSON(JSONObject obj) {
            return new GameList(
                obj.getString("iv"),
                obj.getString("data")
            );
        }
    }

    public static class SessionResponse {
        public String sessionId; 

        public SessionResponse(String sessionId) {
            this.sessionId = sessionId;
        }

        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            obj.put("type", "session");
            obj.put("session_id", sessionId);
            return obj;
        }

        public static SessionResponse fromJSON(JSONObject obj) {
            return new SessionResponse(obj.getString("session_id"));
        }
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public JSONObject toJSON() {
            JSONObject obj = new JSONObject();
            obj.put("type", "error");
            obj.put("message", message);
            return obj;
        }

        public static ErrorResponse fromJSON(JSONObject obj) {
            return new ErrorResponse(obj.getString("message"));
        }
    }
}