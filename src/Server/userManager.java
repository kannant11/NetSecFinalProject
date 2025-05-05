package Server;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;

import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.SCrypt;

import Common.Authentication;


public class userManager {
    private final Map<String, JSONObject>users;
    private final String filePath;

    public userManager(String userFilePath)throws Exception{
        this.filePath = userFilePath;
        this.users = loadUsers();
    }

    public JSONObject getUser(String username){
        return users.get(username);
    }

    private Map<String, JSONObject> loadUsers() throws Exception {
        Map<String, JSONObject> userMap = new HashMap<>();
        File userFile = new File(filePath);

        if (!userFile.exists() || userFile.length() == 0) {
            JSONObject emptyUsers = new JSONObject();
            emptyUsers.put("user", new JSONArray());
            PrintWriter out = new PrintWriter(userFile);
            out.println(emptyUsers.getFormattedJSON());
            out.close();
        }
    
        JSONObject usersData = JsonIO.readObject(userFile);
        JSONArray usersArray = usersData.getArray("user");
    
        for (int i = 0; i < usersArray.size(); i++) {
            JSONObject user = usersArray.getObject(i);
            userMap.put(user.getString("username"), user);
        }
        return userMap;
    }

    public boolean isSubscribed(String username){
        JSONObject user = users.get(username);
        return user != null && "subscribed".equals(user.getString("subscription status"));
    }

    public void addUser(String username, String password, boolean isSubscribed)throws Exception{
        if(users.containsKey(username)){
            throw new IllegalArgumentException("user already exist. ");
        }

        JSONObject newUser = createUserObject(username, password, isSubscribed);
        users.put(username, newUser);
        saveUsersToFile();
    }

    public JSONObject createUserObject(String username, String password, boolean isSubscribed)throws Exception{
        JSONObject user = new JSONObject();
        user.put("username", username);

        byte[] salt = generateSalt();
        byte[] hash = SCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, 16384, 8, 1, 32);

        user.put("salt", Base64.getEncoder().encodeToString(salt));
        user.put("password", Base64.getEncoder().encodeToString(hash));
        user.put("subscription status", isSubscribed ? "subscribed" : "Not Subscribed");
        user.put("totpSecret", generateTOTPSecret());
        user.put("hmacKey", generateHmacKey());
        return user;
    }

    public byte[] generateSalt(){
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    public String generateTOTPSecret(){
        return Authentication.generateTOTPSecret();
    }

    public String generateHmacKey(){
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    public void saveUsersToFile()throws Exception{
        JSONObject usersData = new JSONObject();
        JSONArray usersArray = new JSONArray();

        for(JSONObject user : users.values()){
            usersArray.add(user);
        }

        usersData.put("user", usersArray);
        PrintWriter out = new PrintWriter(new File(filePath));
        out.println(usersData.getFormattedJSON());
        out.close();
    }

        public String getUserSalt(String username) {
        JSONObject user = users.get(username);
        return (user != null) ? user.getString("salt") : null;
    }
}
