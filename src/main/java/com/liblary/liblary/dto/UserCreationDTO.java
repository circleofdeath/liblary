package com.liblary.liblary.dto;

import lombok.Data;

@Data
public class UserCreationDTO
{
    private String cmdtoken;
    private String username;
    private String password;
}