package Server;

public class UserCreator {
    public static void main(String[] args) {
        try {
            loadConfig config = new loadConfig("config.json");
            userManager mgr = new userManager(config.getFilePath());
            mgr.addUser("alice", "12345", true);
            System.out.println("User 'alice' created successfully!");
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
    }
}
