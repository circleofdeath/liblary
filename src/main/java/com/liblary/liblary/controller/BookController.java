package com.liblary.liblary.controller;

import com.liblary.lib.DBSER;
import com.liblary.lib.DBSER.*;
import com.liblary.lib.dto.BookDTO;
import com.liblary.liblary.entity.BookEntity;
import com.liblary.liblary.entity.UserEntity;
import com.liblary.liblary.service.BookService;
import com.liblary.liblary.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/book")
public class BookController
{
    @Autowired
    BookService bookService;
    @Autowired
    UserService userService;

    @PutMapping("/take/{id}/{token}")
    public void takeBook(@PathVariable Long id, @PathVariable String token)
    {
        try {if(!userService.hasPermission(token))
        {
            UserEntity entity = userService.getByToken(token);
            if(entity == null) return;
            entity.getBooks().add(bookService.getBookById(id));
            userService.addUser(entity);
        }}
        catch (Exception ignored) {}
    }

    @PutMapping("/untake/{id}/{token}")
    public void untakeBook(@PathVariable Long id, @PathVariable String token)
    {
        try {if(!userService.hasPermission(token))
        {
            UserEntity entity = userService.getByToken(token);
            if(entity == null) return;
            entity.getBooks().remove(bookService.getBookById(id));
            userService.addUser(entity);
        }}
        catch (Exception ignored) {}
    }

    @GetMapping("/list")
    public ResponseEntity<BookDTO[]> list()
    {
        return ResponseEntity.ok(bookService
                .getAllBooks()
                .stream()
                .sorted(Comparator.comparing(BookEntity::getTitle))
                .map(bookService::get)
                .toArray(BookDTO[]::new)
        );
    }

    @DeleteMapping("/remove/{id}/{token}")
    public void remove(@PathVariable Long id, @PathVariable String token)
    {
        if(userService.hasPermission(token))
        {
            bookService.removeBook(bookService.getBookById(id));
        }
    }

    @GetMapping("/findby/author/{author}")
    public ResponseEntity<BookDTO[]> findByAuthor(@PathVariable String author)
    {
        return ResponseEntity.ok(bookService.findByAuthor(author).stream().map(bookService::get).toArray(BookDTO[]::new));
    }

    @GetMapping("/findby/title/{title}")
    public ResponseEntity<BookDTO[]> findByTitle(@PathVariable String title)
    {
        return ResponseEntity.ok(bookService.findByTitle(title).stream().map(bookService::get).toArray(BookDTO[]::new));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Long id)
    {
        BookEntity book = bookService.getBookById(id);
        if(book == null) return ResponseEntity.status(404).build();
        return ResponseEntity.status(200).body(bookService.get(book));
    }

    @PostMapping("/add/{token}")
    public ResponseEntity<?> addBook(@PathVariable String token, @RequestBody BookDTO dto)
    {
        if(userService.hasPermission(token))
        {
            BookEntity entity = new BookEntity();
            entity.setAuthor(dto.getAuthor());
            entity.setTitle(dto.getTitle());
            bookService.saveBook(entity);
            return ResponseEntity.status(200).build();
        }
        else
        {
            return ResponseEntity.status(473).build();
        }
    }
}