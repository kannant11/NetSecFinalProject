package Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import merrimackutil.json.types.JSONObject;

import java.util.Base64;
import java.util.List;

import org.bouncycastle.crypto.generators.SCrypt;


public class clientHandler implements Runnable{

    private final Socket socket;
    private final userManager userMgr;
    private final videoGameManager gameMgr;

    public clientHandler(Socket socket, userManager userMgr, videoGameManager gameMgr){
        this.socket = socket;
        this.userMgr = userMgr;
        this.gameMgr = gameMgr;
    }

    @Override
    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Welcome to the Video Game Server!");
            boolean loggedIN = false;
            String username = null;

            while (true){
                String input = in.readLine();
                if(input == null) break;

                if(input.startsWith("LOGIN")){
                    String [] parts = input.split("\\s+");
                    if(parts.length < 3){
                        System.out.println("ERROR: LOGIN username password");
                        continue;
                    }
                    username = parts[1];
                    String password = parts[2];

                    JSONObject user = userMgr.getUser(username);
                    if(user == null){
                        System.out.println("ERROR: User not found");
                        continue;
                    }
                    
                    if(verifyPassword(user, password)){
                        System.out.println(("Password verified. Enter your TOTP code:"));
                        String totpCode = in.readLine();

                        if(verifyTOTP(user, totpCode)){
                            loggedIN = true;
                            System.out.println("Login successful!");
                        } else{
                            System.out.println("ERROR: Invalid TOTP code.");
                        }
                    } else{
                        System.out.println("ERROR: Incorrect password.");
                    }
                }

                else if(input.equals("LIST_GAMES")){
                    if(!loggedIN){
                        System.out.println("ERROR: Please login first.");
                        continue;
                    }
                    for (JSONObject game : gameMgr.getAllGames()){
                        System.out.println(game.getString("title")+ " by " + game.getString("author"));
                    }
                } else if (input.equals("EXIT")){
                    System.out.println("Goodbye.");
                    break;
                } else {
                    System.out.println("ERROR: Unknown command.");
                }
            }
        } 
        
        catch (Exception e) {
            System.out.println("Client disconnected.");
        }
    }

    private boolean verifyPassword(JSONObject user, String inputPassword) throws IOException{
        try {
            byte[] salt = Base64.getDecoder().decode(user.getString("salt"));
            byte[] hashedInput = SCrypt.generate(inputPassword.getBytes(StandardCharsets.UTF_8), salt, 20000, 8, 1, 32);
            String storedHash = user.getString("password");
            return Base64.getEncoder().encodeToString(hashedInput).equals(storedHash);
        } catch (Exception e) {
           throw new IOException("Password verification failed.", e);
        }
    }

    private boolean verifyTOTP(JSONObject user, String totpCode){
        try{
            String secret = user.getString("totpSecret");
            String expectedCode = TOTP.getOTP(secret);
            return expectedCode.equals(totpCode);
        } catch (Exception e){
            return false;
        }
    }

    public String generateTOTPSecret(){
        return " ";
    }

}
    

