package Server;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;

import java.nio.charset.StandardCharSets;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.security.SecureRandom;

import org.bouncycastle.crypto.generators.SCrypt;


public class userManager {
    private final Map<String, JSONObject>users;
    private final String filePath;

    public userManager(String configFilePath)throws Exception{
        JSONObject config = JsonIO.readObject(new File(configFilePath));
        this.filePath = config.getString("user file");
        this.users = loadUsers();
    }

    private Map<String, JSONObject> loadUsers() throws Exception{
        Map<String, JSONObject> userMap = new HashMap<>();
        JSONObject usersData = JsonIO.readObject(new File(filePath));
        JSONArray usersArray = usersData.getArray("users");

        for(int i = 0; i < usersArray.size(); i++){
            JSONObject user = usersArray.getObject(i);
            userMap.put(user.getString("username"), user);
        }
        return userMap;
            }

    public JSONObject getUser(String username){
        return users.get(username);
    }

    public boolean isSubscribed(String username){
        JSONObject user = users.get(username);
        return user != null && "subscribed".equals(user.getString("Subscription status"));
    }

    public void addUser(String username, String password, boolean isSubsribed)throws Exception{
        if(users.containsKey(username)){
            throw new IllegalArgumentException("user already exist. ");
        }

        JSONObject newUser = createUserObject(username, password, isSubscribed);
        users.put(username, newUser);
        saveUsersToFile();
    }

    private JSONObject createUserObject(String username, String password, boolean isSubscribed)throws Exception{
        JSONObject user = new JSONObject();
        user.put("username", username);

        byte[] salt = generateSalt();
        byte[] hash = SCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, 20000, 8, 1, 32);

        user.put("salt", Base64.getEncoder().encodeToString(salt));
        user.put("password", Base64.getEncoder().encodeToString(hash));
        user.put("Subscription status", isSubscribed ? "Subscribed" : "Not Subscribed");
        user.put("totpSecret", generateTOTPSecret());
        user.put("hmacKey", generateHmacKey());
        return user;
    }

    private byte[] generateSalt(){
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private String generateTOTPSecret(){
        return "tempPassword";
    }

    private String generateHmacKey(){
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    private void saveUsersToFile()throws Exception{
        JSONObject usersData = new JSONObject();
        JSONArray usersArray = new JSONArray();

        for(JSONObject user : users.values()){
            usersArray.add(user);
        }

        usersData.put("users", usersArray);
        JsonIO.write(usersData, new File(filePath));
    }
    
}
