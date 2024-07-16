package com.liblary.app;

import com.liblary.lib.DBSER;
import com.liblary.lib.Security;
import com.liblary.lib.dto.BookDTO;
import com.liblary.lib.dto.UserDTO;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import static com.liblary.app.LiblaryKonsole.gson;

//TODO без костилів зробити
//TODO make it less buggy
public class CmdProcessor
{
    public static String requestRootToken(Scanner scanner)
    {
        System.out.print("password for root: ");
        String password = Security.sha256(scanner.nextLine().trim());
        String token = Security.token("root", password);
        if(Requests.method("users/login/" + token, "GET").status() == 200)
        {
            return token;
        }
        else
        {
            System.out.println("incorrect password!");
            return requestRootToken(scanner);
        }
    }

    public static String cmdToken(LiblaryKonsole.Login login, String input, Scanner scanner)
    {
        if(input.startsWith("sudo ") && !login.username().equals("root"))
        {
            return requestRootToken(scanner);
        }
        else
        {
            return login.token();
        }
    }

    public static void process(LiblaryKonsole.Login login, String input, Scanner scanner)
    {
        String token = cmdToken(login, input, scanner);

        if(input.startsWith("sudo "))
        {
            input = input.substring(5);
        }

        if("exit".equals(input))
        {
            System.out.println("Exiting...");
            System.exit(0);
        }
        else if("token".equals(input))
        {
            System.out.println(login.token());
            System.out.println("Don't share to anyone this key can be used to hack account!");
        }
        else if("showid".equals(input))
        {
            System.out.println("ID: " + login.id());
        }
        else if("man".equals(input))
        {
            System.out.println("""
man                            // Help command
token                          // Prints token of logged user
exit                           // Exit program
showid                         // Get id of logged user

usr get <ID>                   // Get user by id
usr delete <ID>                // Removes user from table
usr list                       // Prints list of users
usr create <name> <password>   // Creates user
usr passwd <new>               // Changes user password
usr namech <new>               // Changes user username

book get <ID>                  // Get book by id
book geta <author>             // Get books by author
book gett <title>              // Get books by name
book take <ID>                 // Take a book
book untake <ID>               // Untake a book
book delete <ID>               // Delete a book from DB
book register <name> <author>  // Adds a book to DB
book listtaken                 // Lists of books that you take
book list                      // Lists of available books

admin download                 // Download .ser list of users
admin upload <FILE>            // Loads list of users from raw

Start command with "sudo" to run as root (requires password of root)"""
            );
        }
        else if(input.startsWith("admin upload "))
        {
            input = input.substring(13);
            File inf = new File(input);

            if(!inf.exists())
            {
                System.out.println("ERROR: file not found!");
            }
            else
            {
                DBSER ser = null;
                try (FileInputStream fileIn = new FileInputStream(inf.getAbsolutePath());
                     ObjectInputStream in = new ObjectInputStream(fileIn)) {
                    ser = (DBSER) in.readObject();
                    System.out.println("Object deserialized successfully.");

                    int status = Requests.method("js/deser/" + token, "POST", gson.toJson(ser)).status();
                    if(status == 200)
                    {
                        System.out.println("To apply changes restart needed");
                        System.out.print("Press any key to exit... ");
                        scanner.nextLine();
                        System.exit(0);
                    }
                    else if(status == 473)
                    {
                        System.out.println("Access denied");
                    }
                    else
                    {
                        System.out.println("ERROR: status " + status);
                    }
                } catch (IOException | ClassNotFoundException ignored) {
                    System.out.println("ERROR: failed to deserialize");
                }
            }
        }
        else if(input.startsWith("admin download"))
        {
            try
            {
                var request = Requests.method("js/ser/" + token, "GET");
                if(request.status() == 200)
                {
                    DBSER dbser = gson.fromJson(request.content(), DBSER.class);

                    String downloadPath = Paths.get(System.getProperty("user.home"), "Downloads")
                            .toString().replace('\\', '/');
                    if(!downloadPath.endsWith("/")) downloadPath += '/';

                    int i = 0;
                    while(new File(downloadPath + "map (" + i + ").ser").exists())
                    {
                        i++;
                    }

                    String name = downloadPath + "map (" + i + ").ser";
                    try (FileOutputStream fileOut = new FileOutputStream(name);
                         ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                        out.writeObject(dbser);
                        System.out.println("Object serialized successfully to \"" + name + "\".");
                    } catch (IOException ignored) {
                        System.out.println("Object serialization failed.");
                    }
                }
                else if(request.status() == 473)
                {
                    System.out.println("Access denied");
                }
                else
                {
                    System.out.println("Unknown: status " + request.status());
                }
            }
            catch(Exception ignored)
            {
                System.out.println("ERROR: error");
            }
        }
        else if(input.startsWith("book "))
        {
            input = input.substring(5);
            if(input.startsWith("register "))
            {
                input = input.substring(9);
                String[] args = input.split(" ");

                if(args.length < 2)
                {
                    System.out.println("ERROR: invalid argument length");
                }
                else if(args[0].isEmpty() || args[1].isEmpty())
                {
                    System.out.println("ERROR: argument is sus");
                }
                else
                {
                    int status = Requests.method("book/add/" + login.token(), "POST",
                            gson.toJson(new BookDTO(-1L, args[0], args[1]))).status();

                    if(status == 200) {} else if(status == 473)
                    {
                        System.out.println("Access denied");
                    }
                    else
                    {
                        System.out.println("ERROR: status " + status);
                    }
                }
            }
            else if(input.startsWith("untake "))
            {
                input = input.substring(7);
                try
                {
                    Long.parseLong(input);

                    try
                    {
                        Requests.method("book/untake/" + input + "/" + login.token(), "PUT");
                    }
                    catch(Exception ignored)
                    {
                        System.out.println("ERROR occurred");
                    }
                }
                catch(Exception ignored)
                {
                    System.out.println("ERROR: not number");
                }
            }
            else if(input.startsWith("take "))
            {
                input = input.substring(5);
                try
                {
                    Long.parseLong(input);

                    try
                    {
                        Requests.method("book/take/" + input + "/" + login.token(), "PUT");
                    }
                    catch(Exception ignored)
                    {
                        System.out.println("ERROR occurred");
                    }
                }
                catch(Exception ignored)
                {
                    System.out.println("ERROR: not number");
                }
            }
            else if(input.equals("listtaken"))
            {
                for(BookDTO dto : Arrays.stream(gson.fromJson(
                        Requests.method("users/id/" + login.id(), "GET").content(),
                        UserDTO.class
                ).getTaken()).sorted(Comparator.comparing(BookDTO::getTitle)).toArray(BookDTO[]::new))
                {
                    System.out.printf("[ID: %s] %s (by %s)\n", dto.getId(), dto.getTitle(), dto.getAuthor());
                }
            }
            else if(input.startsWith("delete "))
            {
                input = input.substring(7);
                try
                {
                    Long.parseLong(input);

                    try
                    {
                        Requests.method("book/remove/" + input + "/" + token, "DELETE");
                    }
                    catch(Exception ignored)
                    {
                        System.out.println("ERROR occurred");
                    }
                }
                catch(Exception ignored)
                {
                    System.out.println("ERROR: not number");
                }
            }
            else if(input.equals("list"))
            {
                for(BookDTO dto : gson.fromJson(Requests.method("book/list", "GET").content(), BookDTO[].class))
                {
                    System.out.printf("[ID: %s] %s (by %s)\n", dto.getId(), dto.getTitle(), dto.getAuthor());
                }
            }
            else if(input.startsWith("geta "))
            {
                input = input.substring(5);
                BookDTO[] books = gson.fromJson(
                        Requests.method("book/findby/author/" + input, "GET").content(),
                        BookDTO[].class
                );
                if(books.length == 0)
                {
                    System.out.println("Not found books with this author");
                }
                else for(BookDTO dto : books)
                {
                    System.out.printf("[ID: %s] %s (by %s)\n", dto.getId(), dto.getTitle(), dto.getAuthor());
                }
            }
            else if(input.startsWith("gett "))
            {
                input = input.substring(5);
                BookDTO[] books = gson.fromJson(
                        Requests.method("book/findby/title/" + input, "GET").content(),
                        BookDTO[].class
                );
                if(books.length == 0)
                {
                    System.out.println("Not found books with this title");
                }
                else for(BookDTO dto : books)
                {
                    System.out.printf("[ID: %s] %s (by %s)\n", dto.getId(), dto.getTitle(), dto.getAuthor());
                }
            }
            else if(input.startsWith("get "))
            {
                input = input.substring(4);
                try
                {
                    Long.parseLong(input);

                    try
                    {
                        BookDTO book = gson.fromJson(Requests.method("book/id/" + input, "GET").content(), BookDTO.class);
                        System.out.println("----- book info -----");
                        System.out.println("ID: " + book.getId());
                        System.out.println("Title: " + book.getTitle());
                        System.out.println("Author: " + book.getAuthor());
                        System.out.println("---------------------");
                    }
                    catch(Exception ignored)
                    {
                        System.out.println("ERROR 404: not found");
                    }
                }
                catch(Exception ignored)
                {
                    System.out.println("ERROR: not number");
                }
            }
            else
            {
                System.out.println("ERROR 404: flag not found!");
            }
        }
        else if(input.startsWith("usr "))
        {
            input = input.substring(4);
            if(input.startsWith("create "))
            {
                input = input.substring(7);
                String[] rgs = input.split(" ");

                if(rgs.length < 2)
                {
                    System.out.println("ERROR: invalid args length: " + rgs.length);
                }
                else
                {
                    if(rgs[0].isEmpty() || rgs[1].isEmpty())
                    {
                        System.out.println("ERROR: the form you provided sus");
                    }
                    else
                    {
                        String json = "{\"cmdtoken\": \"" + token + "\", \"username\": \"" +
                                rgs[0].replace("\"", "\\\"") +
                                "\", \"password\": \"" + Security.sha256(rgs[1]) + "\"}";
                        System.out.println(Requests.method("users/register", "POST", json).content());
                    }
                }
            }
            else if(input.startsWith("get "))
            {
                input = input.substring(4);

                try
                {
                    Long.parseLong(input);
                    var request = Requests.method("users/id/" + input, "GET");

                    if(request.status() == 200)
                    {
                        UserDTO dto = gson.fromJson(request.content(), UserDTO.class);
                        System.out.println("----- USER INFORMATION -----");
                        System.out.println("ID: " + dto.getId());
                        System.out.println("Username: " + dto.getUsername());
                        var taken = dto.getTaken();
                        if(taken.length == 0)
                        {
                            System.out.println("No books taken");
                        }
                        else for(BookDTO dto1 : taken)
                        {
                            System.out.println("   - " + dto1.getTitle());
                        }
                        System.out.println("----------------------------");
                    }
                    else if(request.status() == 404)
                    {
                        System.out.println("ERROR 404: not found");
                    }
                    else
                    {
                        System.out.println("ERROR: status " + request.status());
                    }
                }
                catch(UnsupportedOperationException ignored)
                {
                    System.out.println("ERROR: not number");
                }
            }
            else if(input.equals("list"))
            {
                for(UserDTO userDTO : gson.fromJson(Requests.method("users/raw", "GET").content(), UserDTO[].class))
                {
                    System.out.printf(
                            "[ID: %s] %s (taken: %s)",
                            userDTO.getId().toString(),
                            userDTO.getUsername(),
                            Arrays.stream(userDTO.getTaken()).map(BookDTO::getTitle).toList()
                    );
                    System.out.println();
                }
            }
            else if(input.startsWith("passwd "))
            {
                input = input.substring(7);
                String[] rgs = input.split(" ");

                if(rgs.length < 1)
                {
                    System.out.println("ERROR: invalid args length: " + rgs.length);
                }
                else
                {
                    if(rgs[0].isEmpty())
                    {
                        System.out.println("ERROR: the form you provided sus");
                    }
                    else
                    {
                        System.out.print("Processed? [y/n]: ");

                        if(scanner.nextLine().equals("y"))
                        {
                            int status = Requests.method("users/change",
                                    "PUT", String.format(
                                            "{\"token\": \"%s\", \"username\": \"%s\", \"password\": \"%s\"}",
                                            login.token(), login.username(), Security.sha256(rgs[0])
                                    )).status();

                            if(status == 200)
                            {
                                System.out.println("To apply changes restart needed");
                                System.out.print("Press any key to exit... ");
                                scanner.nextLine();
                                System.exit(0);
                            }
                            else
                            {
                                System.out.println("ERROR: operation failed");
                            }
                        }
                    }
                }
            }
            else if(input.startsWith("namech "))
            {
                input = input.substring(7);
                String[] rgs = input.split(" ");

                if (rgs.length < 1)
                {
                    System.out.println("ERROR: invalid args length: " + rgs.length);
                }
                else
                {
                    if (rgs[0].isEmpty())
                    {
                        System.out.println("ERROR: the form you provided sus");
                    }
                    else
                    {
                        System.out.print("Processed? [y/n]: ");

                        if (scanner.nextLine().equals("y"))
                        {
                            int status = Requests.method("users/change",
                                    "PUT", String.format(
                                            "{\"token\": \"%s\", \"username\": \"%s\", \"password\": \"%s\"}",
                                            login.token(), rgs[0], login.password()
                                    )).status();

                            if (status == 200)
                            {
                                System.out.println("To apply changes restart needed");
                                System.out.print("Press any key to exit... ");
                                scanner.nextLine();
                                System.exit(0);
                            }
                            else
                            {
                                System.out.println("ERROR: operation failed");
                            }
                        }
                    }
                }
            }
            else if(input.startsWith("delete "))
            {
                input = input.substring(7);
                try
                {
                    Long.parseLong(input);
                    System.out.println(Requests.method("users/rm/" + input + "/" + token, "DELETE").content());
                }
                catch(NumberFormatException ignored)
                {
                    System.out.println("ERROR: type number!");
                }
            }
            else
            {
                System.out.println("ERROR 404: flag not found!");
            }
        }
        else
        {
            System.out.println("ERROR 404: command \"" + input + "\" not found!");
        }
    }
}