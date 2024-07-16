package com.liblary.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Security
{
    public static String token(String username, String password)
    {
        return "TOKEN:" + sha256(sha256(username) + password);
    }

    public static String sha256(String input)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());

            StringBuilder hexHash = new StringBuilder();
            for (byte b : hash)
            {
                String hex = String.format("%02x", b);
                hexHash.append(hex);
            }

            return hexHash.toString();
        }
        catch(NoSuchAlgorithmException ignored)
        {
            // SHA-256 exists
            throw new RuntimeException("unreachable");
        }
    }
}
