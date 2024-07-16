package com.liblary.lib;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DBSER implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Book[] books;
    private User[] users;

    @Data
    public static class Book implements Serializable
    {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long id;
        private String title;
        private String author;
    }

    @Data
    public static class User implements Serializable
    {
        private String username;
        private String password;
        private Book[] taken;
    }
}