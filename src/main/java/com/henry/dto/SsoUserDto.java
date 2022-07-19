package com.henry.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SsoUserDto implements Serializable {
    private Long userId;
    private String userName;
    private String phone;
    private String email;
}
