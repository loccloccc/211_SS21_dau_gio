package com.example.base_spring_boot.models.dtos.req;

import lombok.Data;

@Data
public class TokenRefreshReq {
    private String refreshToken;
}