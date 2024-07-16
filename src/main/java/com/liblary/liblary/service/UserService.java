package com.liblary.liblary.service;

import com.liblary.lib.Security;
import com.liblary.lib.dto.BookDTO;
import com.liblary.lib.dto.UserDTO;
import com.liblary.liblary.entity.UserEntity;
import com.liblary.liblary.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService
{
    @Autowired
    UserRepo repo;
    @Autowired
    BookService bookService;

    public boolean hasPermission(String token)
    {
        try
        {
            return getByToken(token).getUsername().equals("root");
        }
        catch(Exception ignored)
        {
            // Not found or server error
            return false;
        }
    }

    public void updateToken(UserEntity entity)
    {
        entity.setToken(Security.token(entity.getUsername(), entity.getPassword()));
    }

    public List<UserEntity> getAllUser2()
    {
        return repo.findAll();
    }

    public UserDTO[] getAllUsers()
    {
        return repo
                .findAll()
                .stream()
                .map(entity -> new UserDTO(entity.getId(), entity.getUsername(), entity.getBooks()
                        .stream().map(bookService::get).toArray(BookDTO[]::new)))
                .toArray(UserDTO[]::new);
    }

    public UserEntity addUser(UserEntity entity)
    {
        return repo.save(entity);
    }

    public UserDTO get(UserEntity user)
    {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setTaken(user.getBooks().stream().map(bookService::get).toArray(BookDTO[]::new));
        return dto;
    }

    public void remove(Long id)
    {
        repo.deleteById(id);
    }

    public UserEntity getByToken(String token)
    {
        return repo.findByToken(token);
    }

    public UserEntity getById(long id)
    {
        return repo.findById(id).orElse(null);
    }
}