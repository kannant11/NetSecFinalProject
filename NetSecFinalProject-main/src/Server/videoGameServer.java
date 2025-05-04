package Server;

import merrimackutil.json.types.JSONObject;
import merrimackutil.util.NonceCache;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class videoGameServer {
    private static NonceCache nonceCache;

    public static void main(String[] args) {
        try {
            // 1. Load configurations
            loadConfig config = new loadConfig("config.json");
            int port = config.getServerPort();
            nonceCache = new NonceCache(16, 120); 
            SessionManager sessionManager = new SessionManager();     
            userManager userMgr = new userManager(config.getFilePath());
            videoGameManager gameMgr = new videoGameManager(config.gamesFilePath());
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Secure server running on port " + port);

            while(true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new clientHandler(clientSocket, userMgr, gameMgr,nonceCache,sessionManager)).start();
            }
        } catch (Exception e) {
            System.err.println("Server startup failed:");
            e.printStackTrace();
        }
    }
}