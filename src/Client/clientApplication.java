package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import merrimackutil.json.types.JSONObject;

import Server.userManager;
import Server.clientHandler;
import Server.videoGameManager;

public class clientApplication {
    public static String username; //username
    public static String password; //password
    public static String receivedTOTP; //time-based one-time password received from server
    public static String totp; //time-based one-time password entered by user
    public static byte[] games; //list of video games

    public static Scanner sc = new Scanner(System.in); //scanner so user can type input
    public static SSLSocketFactory factory; //socket factory
    public static SSLSocket socket; //socket for establishing connection through
    public static Scanner receiver; //receiver for user to receive information from server
    public static PrintWriter writer; //writer for user to send information to server

    static JSONObject jo; //instance of JSONObject
    static clientHandler ch; //instance of clientHandler
    static userManager um; //instance of class userManager
    static videoGameManager vgm; //instance of class videoGameManager 

    //main class
    public static void main(String[] args) throws InputMismatchException, NoSuchElementException, IllegalStateException, IOException{
        System.setProperty("javax.net.ssl.trustStore", "truststore.jks"); //set up truststore
        System.setProperty("javax.net.ssl.trustStorePassword", "trust12345"); //set up truststore's password

        //start connection to server
        try {
            factory = (SSLSocketFactory) SSLSocketFactory.getDefault(); //socket factory created

            String serversList = new String(Files.readAllBytes(Paths.get("hosts.json"))); //read hosts.json
            JSONObject entries = jo.getObject("entries"); //get every entry in the json file
            JSONObject serversListJSON = entries.getObject(serversList); //list of servers gotten from the entry
            String host = serversListJSON.getString("host"); //get the name of the host
            int port = Integer.parseInt(serversListJSON.getString("port number")); //get the port number

            System.out.println("What service do you want to go to (type in the key [localhost, etc.])?"); //user asked to type in the server they want to go to
            host = sc.nextLine(); //name of server typed by user [which is server key]

            socket = (SSLSocket) factory.createSocket(host, port); //create socket from its factory
            socket.startHandshake(); //start the handshake process using the socket

            receiver = new Scanner(socket.getInputStream()); //receiver set up
            writer = new PrintWriter(socket.getOutputStream(), true); //print writer set up
        } catch (UnknownHostException ue) {
            ue.printStackTrace(); //print error message if IP address of host is undetermined
            return; //exception message is returned
        } catch (IOException ioe) {
            ioe.printStackTrace(); //print error message if there is issue with input and output streams usage
            return; //exception message is returned
        }

        //user instructed to type in username
        System.out.println("Type in your username: ");

        //scanner so user can type username
        username = sc.nextLine();

        //user instructed to type in password
        System.out.println("Type in your password: ");

        //scanner so user can type password
        password = sc.nextLine();

        //username converted to a JSONObject before being sent to server
        JSONObject userJSON = (JSONObject) new JSONObject().put("username", sc.nextLine());

        //password converted to a JSONObject before being sent to server
        JSONObject passwordJSON = (JSONObject) new JSONObject().put("password", sc.nextLine());

        //send username to server
        writer.println(userJSON);

        //send password to server
        writer.println(passwordJSON);

        //if the server verifies that the password is correct
        if(ch.verifyPassword(userJSON, password) == true) {
            //TOTP key client receives from server
            receivedTOTP = um.generateTOTPSecret();
            
            //user instructed to type in totp
            System.out.println("Type in your Time-Based One Time Password: ");

            //scanner so user can type totp
            totp = sc.nextLine();

            //password converted to a JSONObject before being sent to server
            JSONObject totpJSON = (JSONObject) new JSONObject().put("totp", sc.nextLine());

            //send user-typed totp to the server
            writer.println(totpJSON);

            //if server verifies the TOTP
            if (ch.verifyTOTP(userJSON, totp)) {
                //get the list of games
                List<JSONObject> gameList = vgm.getAllGames();

                //print list of games
                System.out.println("Here is the list of games: \n" + gameList);

                //user types in games they want
                System.out.println("Type in the games you want: ");

                //scanner so user can type list of games wanted
                for (int i = 0; i < args.length; i++) {
                    games[i] = sc.nextByte();
                }
            }
        }

        //close connection to server
        try {
            socket.close(); //close the connection by disconnecting the socket
        } catch (IOException ioe) {
            
        }
    }
}