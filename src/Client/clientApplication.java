package Client;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

import Server.userManager;

class clientApplication {
    public static String username; //username
    public static String password; //password
    public static byte[] games; //list of video games
    public static String receivedTOTP; //time-based one-time password received from server
    public static String totp; //time-based one-time password entered by user

    public static Scanner sc; //scanner so user can type input

    //main class
    public static void main(String[] args) throws InputMismatchException, NoSuchElementException, IllegalStateException{
        //user instructed to type in username
        System.out.println("Type in your username: ");

        //scanner so user can type username
        username = sc.nextLine();

        //user instructed to type in password
        System.out.println("Type in your password: ");

        //scanner so user can type password
        username = sc.nextLine();

        //user types in games they want
        System.out.println("Type in the games you want: ");

        //scanner so user can type list of games wanted
        for (int i = 0; i < args.length; i++) {
            games[i] = sc.nextByte();
        }

        //TOTP key client receives from server
        receivedTOTP = generateTOTPSecret();

        //user instructed to type in totp
        System.out.println("Type in your Time-Based One Time Password: ");

        //scanner so user can type totp
        totp = sc.nextLine();
    }
}