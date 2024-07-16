package com.liblary.liblary.controller;

import com.liblary.lib.dto.UserDTO;

import com.liblary.liblary.dto.UserCreationDTO;
import com.liblary.liblary.dto.UserSetDTO;
import com.liblary.liblary.entity.UserEntity;
import com.liblary.liblary.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//TODO use permissions bit mask instead of root
@RestController
@RequestMapping("/users")
public class UserController
{
    @Autowired
    UserService userService;

    @PutMapping("/change")
    public ResponseEntity<?> update(@RequestBody UserSetDTO userSetDTO)
    {
        var usr = userService.getByToken(userSetDTO.getToken());
        if(usr == null) return ResponseEntity.status(404).build();
        usr.setUsername(userSetDTO.getUsername());
        usr.setPassword(userSetDTO.getPassword());
        userService.updateToken(usr);
        userService.addUser(usr);
        return ResponseEntity.status(200).build();
    }

    @DeleteMapping("/rm/{id}/{token}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, @PathVariable String token)
    {
        if(userService.hasPermission(token))
        {
            UserEntity entity = userService.getById(id);

            if(entity.getUsername().equals("root"))
            {
                return ResponseEntity.ok("Can't delete root");
            }
            else
            {
                userService.remove(id);
                return ResponseEntity.ok(String.format("User \"%s\" successfully removed!", entity.getUsername()));
            }
        }
        else
        {
            return ResponseEntity.status(200).body("Access denied!");
        }
    }

    @GetMapping("/raw")
    public ResponseEntity<UserDTO[]> list()
    {
        try
        {
            return ResponseEntity.ok(userService.getAllUsers());
        }
        catch(Exception ignored)
        {
            return ResponseEntity.ok(new UserDTO[0]);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserCreationDTO user)
    {
        if(userService.hasPermission(user.getCmdtoken()))
        {
            if(user.getUsername().equals("root"))
            {
                return ResponseEntity.status(200).body("Username root is not allowed!");
            }
            else
            {
                UserEntity entity = new UserEntity();
                entity.setUsername(user.getUsername());
                entity.setPassword(user.getPassword());
                userService.updateToken(entity);
                userService.addUser(entity);

                return ResponseEntity.status(200).body("User registered!");
            }
        }
        else
        {
            return ResponseEntity.status(200).body("Access denied!");
        }
    }

    @GetMapping("/login/{token}")
    public ResponseEntity<Long> login(@PathVariable String token)
    {
        try
        {
            var user = userService.getByToken(token);
            if(user == null)
            {
                return ResponseEntity.status(404).build();
            }
            else
            {
                // Login successful
                return ResponseEntity.status(200).body(user.getId());
            }
        }
        catch(Exception ignored)
        {
            //idk maybe throws exception if not found
            return ResponseEntity.status(508).build();
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id)
    {
        UserEntity usr = userService.getById(id);
        if(usr == null)
        {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.status(200).body(userService.get(usr));
    }
}