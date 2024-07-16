package com.liblary.liblary.dto;

import lombok.Data;

@Data
public class UserSetDTO
{
    private String token;
    private String username;
    private String password;
}