package com.liblary.liblary.controller;

import com.liblary.lib.DBSER;
import com.liblary.liblary.entity.BookEntity;
import com.liblary.liblary.entity.UserEntity;
import com.liblary.liblary.service.BookService;
import com.liblary.liblary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/js")
public class SerController
{
    @Autowired
    BookService bookService;
    @Autowired
    UserService userService;

    @PostMapping("/deser/{token}")
    public ResponseEntity<?> deser(@PathVariable String token, @RequestBody DBSER dbser)
    {
        if(!userService.hasPermission(token))
        {
            return ResponseEntity.status(473).build();
        }

        userService.getAllUser2().forEach(user -> {
            user.getBooks().clear();
            userService.addUser(user);
        });

        bookService.getAllBooks().forEach(bookService::removeBook);
        // Creating copy of list to prevent exceptions changing list state on foreach
        Arrays.stream(userService.getAllUser2().toArray(UserEntity[]::new)).forEach((u) ->
        {
            if(!u.getUsername().equals("root"))
            {
                userService.remove(u.getId());
            }
        });

        int[] i = new int[1];
        List<Long> ids = new ArrayList<>();
        Map<Long, Long> idMap = new HashMap<>();
        Arrays.stream(dbser.getBooks()).map(b -> {
            BookEntity e = new BookEntity();
            e.setTitle(b.getTitle());
            e.setAuthor(b.getAuthor());
            ids.add(b.getId());
            return e;
        }).forEach(b -> idMap.put(ids.get(i[0]++), bookService.saveBook(b).getId()));

        i[0] = 0;
        Arrays.stream(dbser.getUsers()).map(u -> {
            UserEntity e2 = new UserEntity();
            e2.setUsername(u.getUsername());
            e2.setPassword(u.getPassword());
            e2.setBooks(Arrays.stream(u.getTaken()).map(b -> {
                BookEntity e = new BookEntity();
                e.setId(idMap.get(ids.get(i[0]++)));
                e.setTitle(b.getTitle());
                e.setAuthor(b.getAuthor());
                return e;
            }).toList());
            userService.updateToken(e2);
            return e2;
        }).forEach(userService::addUser);

        return ResponseEntity.status(200).build();
    }

    @GetMapping("/ser/{token}")
    public ResponseEntity<DBSER> ser(@PathVariable String token)
    {
        if(!userService.hasPermission(token))
        {
            return ResponseEntity.status(473).build();
        }

        DBSER server = new DBSER();
        List<BookEntity> bookList = bookService.getAllBooks();
        List<UserEntity> userList = userService.getAllUser2().stream().filter(u -> !u.getUsername().equals("root")).toList();

        server.setBooks(bookList.stream().map(b -> {
            DBSER.Book book = new DBSER.Book();
            book.setId(b.getId());
            book.setTitle(b.getTitle());
            book.setAuthor(b.getAuthor());
            return book;
        }).toArray(DBSER.Book[]::new));

        server.setUsers(userList.stream().map(u -> {
            DBSER.User user = new DBSER.User();
            user.setTaken(u.getBooks().stream().map(b -> {
                DBSER.Book book = new DBSER.Book();
                book.setId(b.getId());
                book.setTitle(b.getTitle());
                book.setAuthor(b.getAuthor());
                return book;
            }).toArray(DBSER.Book[]::new));
            user.setUsername(u.getUsername());
            user.setPassword(u.getPassword());
            return user;
        }).toArray(DBSER.User[]::new));

        return ResponseEntity.ok(server);
    }
}
