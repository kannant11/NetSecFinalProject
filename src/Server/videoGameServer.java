package Server;

import merrimackutil.json.types.JSONObject;
import java.io.*;
import java.net.*;
import java.util.Arrays;

public class videoGameServer {

    public static void main(String[] args) {
        try {
            loadConfig config = new loadConfig("config.json");
            int port = config.getServerPort();

            userManager userMgr = new userManager("config.json");
            videoGameManager gameMgr = new videoGameManager("config.json");

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started on port" + port);

            while(true){
                Socket clienSocket = serverSocket.accept();
                System.out.println("New client connected!");

                clientHandler handler = new clientHandler (clienSocket, userMgr, gameMgr);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
