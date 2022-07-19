package com.henry.dto;

import lombok.Data;

@Data
public class AuthorizationDto {
    private String authorization;
    private boolean shouldRefresh;
}
