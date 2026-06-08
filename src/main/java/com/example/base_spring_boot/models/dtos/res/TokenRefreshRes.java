package com.example.base_spring_boot.models.dtos.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRefreshRes {

    private String accessToken;

    private String refreshToken;
}