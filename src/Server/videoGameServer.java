package Server;

import merrimackutil.json.types.JSONObject;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class videoGameServer {
    private static NonceCache nonceChace;

    public static void main(String[] args) {
        try {
            loadConfig config = new loadConfig("config.json");
            int port = config.getServerPort();

            nonceChace = new NonceChace(32, 300);
            SessionManager sessionManager = new SessionManager();

            userManager userMgr = new userManager(config.getFilePath());
            videoGameManager gameMgr = new videoGameManager(config.gamesFilePath());

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port" + port);

            while(true){
                Socket clienSocket = serverSocket.accept();
                System.out.println("New client connected!");

                /*clientHandler handler = new clientHandler (clienSocket, userMgr, gameMgr);*/
                new Thread(new clientHandler(clientSocket, userMgr, gameMgr, nonceCache, sessionManager)).start();
            }
        } catch (Exception e) {
            System.err.println("Server failed.");
            e.printStackTrace();
        }
    }
    
}
