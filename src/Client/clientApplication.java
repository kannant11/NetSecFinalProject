package Client;

import java.util.Scanner;

class clientApplication {
    public static String username; //username
    public static String password; //password

    public static Scanner sc; //scanner so user can type input

    //main class
    public static void main(String[] args) {
        //user instructed to type in username
        System.out.println("Type in your username: ");

        //scanner so user can type username
        username = sc.nextLine();

        //user instructed to type in password
        System.out.println("Type in your password: ");

        //scanner so user can type password
        username = sc.nextLine();
    }
}