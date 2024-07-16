package com.liblary.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liblary.lib.Security;

import java.util.Scanner;

public class LiblaryKonsole
{
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void clearConsole(Scanner scanner)
    {
        try {
            if(System.getProperty("os.name").toLowerCase().contains("win"))
            {
                new ProcessBuilder("cls").inheritIO().start().waitFor();
            }
            else
            {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        }
        catch(Exception e)
        {
            System.out.println("ERROR happened: " + e);
            System.out.println("Press any key to exit...");
            scanner.nextLine();
            scanner.close();
            System.exit(0);
        }
    }

    public static Login requestLogin(Scanner scanner)
    {
        System.out.print("Login: ");
        String login = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = Security.sha256(scanner.nextLine().trim());
        String token = Security.token(login, password);
        var r = Requests.method("users/login/" + token, "GET");
        if(r.status() == 200)
        {
            try
            {
                return new Login(login, password, token, Long.parseLong(r.content()));
            }
            catch(UnsupportedOperationException ignored)
            {
                throw new RuntimeException("unreachable, if you see this you do something wrong");
            }
        }
        else
        {
            clearConsole(scanner);
            System.out.println("Password or login incorrect, try again!");
            System.out.println();
            return requestLogin(scanner);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login login = requestLogin(scanner);
        clearConsole(scanner);

        System.out.println("Enter your commands. Type 'man' for help.");

        while(true)
        {
            try
            {
                System.out.printf("%s@liblary$ ", login.username());
                CmdProcessor.process(login, scanner.nextLine().trim(), scanner);
            }
            catch(Exception e)
            {
                System.out.println("ERROR happened: " + e);
                System.out.println("Press any key to exit...");
                scanner.nextLine();
                break;
            }
        }

        scanner.close();
    }

    public record Login(String username, String password, String token, Long id)
    {}
}