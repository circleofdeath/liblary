package com.liblary.lib.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable
{
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private BookDTO[] taken;
}