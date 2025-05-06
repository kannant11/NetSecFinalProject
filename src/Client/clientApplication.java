package Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;
import Common.Security;
import Common.Authentication;

public class clientApplication {
    private static final Scanner sc = new Scanner(System.in);
    private static Socket socket;
    private static PrintWriter writer;
    private static Scanner receiver;

    public static void main(String[] args) {
        try {
            JSONObject hostConfig = JsonIO.readObject(new File("host.json"))
            .getObject("local_server");
            String host = hostConfig.getString("host");
            int port = hostConfig.getInt("port_number");

            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            receiver = new Scanner(socket.getInputStream());

            System.out.print("Username: ");
            String username = sc.nextLine();
            System.out.print("Password: ");
            String password = sc.nextLine();

            byte[] clientNonce = Security.generateNonce(16);
            JSONObject authRequest = new JSONObject();
            authRequest.put("client_nonce", Security.bytesToHex(clientNonce));
            authRequest.put("username", username);
            writer.println(authRequest.toJSON());

            JSONObject challenge = JsonIO.readObject(receiver.nextLine());
            if (challenge.containsKey("success") && !challenge.getBoolean("success")) {
                System.out.println("Authentication failed: " + challenge.getString("message"));
                return;
            }

            byte[] serverNonce = Security.hexToBytes(challenge.getString("server_nonce"));
            String salt = challenge.getString("salt");

            SecretKey userKey = Security.deriveKey(password, salt);
            String totpSecret = getTOTPSecretForUser(username);
            String totpCode = Authentication.generateTOTP(totpSecret);

            JSONObject authResponse = new JSONObject();
            authResponse.put("hmac", Security.bytesToHex(Security.computeHmac(serverNonce, userKey)));
            authResponse.put("totp", totpCode);
            authResponse.put("username", username);
            writer.println(authResponse.toJSON());

            JSONObject authResult = JsonIO.readObject(receiver.nextLine());
            if (!authResult.getBoolean("success")) {
                System.out.println("Authentication failed: " + authResult.getString("message"));
                return;
            }
            System.out.println("Authentication successful!");

            boolean isSubscribed = authResult.getBoolean("subscribed");
            int maxGames = isSubscribed ? 5 : 3;
            System.out.println("\nSubscription Status: " + (isSubscribed ? "Premium (5 games)" :
             "Free Tier (3 games)"));

             JSONObject keyResponse = JsonIO.readObject(receiver.nextLine());
             if (!"session_key".equals(keyResponse.getString("type"))) {
                throw new SecurityException("Missing session key");
             }

            byte[] iv = Base64.getDecoder().decode(keyResponse.getString("iv"));
            byte[] encryptedKey = Base64.getDecoder().decode(keyResponse.getString("key"));
            SecretKey sessionKey = new SecretKeySpec(Security.decrypt(userKey, encryptedKey, iv), "AES");

            JSONObject encryptedGames = JsonIO.readObject(receiver.nextLine());
            if (!"game_list".equals(encryptedGames.getString("type"))) {
                throw new SecurityException("Invalid game list response");
            }

            byte[] gameIV = Base64.getDecoder().decode(encryptedGames.getString("iv"));
            byte[] gameData = Base64.getDecoder().decode(encryptedGames.getString("data"));
            JSONObject gamesWrapper = JsonIO.readObject(new String(Security.decrypt(sessionKey, gameData, gameIV)));
            System.out.println("\nBase Game Library:");
            gamesWrapper.getArray("games").forEach(game -> 
                System.out.println(" - " + ((JSONObject) game).getString("title")));

            List<JSONObject> selectedGames = new ArrayList<>();
            Scanner input = sc;
            
            while (selectedGames.size() < maxGames) {
                System.out.print("\nEnter genre to search (or 'done'): ");
                String genre = input.nextLine().trim();
                
                if ("done".equalsIgnoreCase(genre)) break;
                
                JSONObject genreReq = new JSONObject();
                genreReq.put("type", "genre_search");
                genreReq.put("genre", genre);
                writer.println(genreReq.toJSON());
                

                JSONObject genreResponse = JsonIO.readObject(receiver.nextLine());
                JSONArray genreGames = genreResponse.getArray("games");
                
                System.out.println("\nGames in " + genre + ":");
                for (int i = 0; i < genreGames.size(); i++) {
                    JSONObject game = genreGames.getObject(i);
                    System.out.printf("[%d] %s\n", i+1, game.getString("title"));
                }
                
                System.out.print("Select numbers (comma-separated): ");
                String[] choices = input.nextLine().split(",");
                
                for (String choice : choices) {
                    try {
                        int idx = Integer.parseInt(choice.trim()) - 1;
                        if (idx >= 0 && idx < genreGames.size()) {
                            JSONObject selected = genreGames.getObject(idx);
                            if (!selectedGames.contains(selected) && selectedGames.size() < maxGames) {
                                selectedGames.add(selected);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid selection: " + choice);
                    }
                }
                System.out.println("Selected: " + selectedGames.size() + "/" + maxGames);
            }

            System.out.println("\nYour Selected Games:");
            selectedGames.forEach(game -> System.out.println(" - " + game.getString("title")));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (socket != null) socket.close();
                if (writer != null) writer.close();
                if (receiver != null) receiver.close();
            }catch (IOException e) {
                System.err.println("Cleanup error: " + e.getMessage());
            }
        }
    }

    private static String getTOTPSecretForUser(String username) throws Exception {
        JSONObject users = JsonIO.readObject(new File("user.json"));
        for (int i = 0; i < users.getArray("user").size(); i++) {
            JSONObject user = users.getArray("user").getObject(i);
            if (username.equals(user.getString("username"))) {
                return user.getString("totpSecret");
            }
        }
        throw new SecurityException("User not found");
    }
}