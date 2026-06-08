package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.exceptions.TokenRefreshException;
import com.example.base_spring_boot.models.dtos.req.LoginReq;
import com.example.base_spring_boot.models.dtos.req.RegisterReq;
import com.example.base_spring_boot.models.dtos.req.TokenRefreshReq;
import com.example.base_spring_boot.models.dtos.res.TokenRefreshRes;
import com.example.base_spring_boot.models.dtos.wrapper.DataRes;
import com.example.base_spring_boot.models.entities.RefreshToken;
import com.example.base_spring_boot.models.entities.User;
import com.example.base_spring_boot.models.repositories.IUserRepository;
import com.example.base_spring_boot.models.services.IAuthService;
import com.example.base_spring_boot.models.services.IRefreshTokenService;
import com.example.base_spring_boot.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController
{
    private final IAuthService authService;
    private final IRefreshTokenService refreshTokenService;
    private final IUserRepository userRepository;
    private final JwtUtils jwtUtils;

    /**
     * @param req LoginReq
     * @apiNote handle login with { username , password }
     */
    @PostMapping("/login")
    public ResponseEntity<?> handleLogin(@Valid @RequestBody LoginReq req)
    {
        return ResponseEntity.status(HttpStatus.OK).body(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(authService.login(req))
                        .build()
        );
    }

    /**
     * @param req RegisterReq
     * @apiNote handle register with { fullName , username , password }
     */
    @PostMapping("/register")
    public ResponseEntity<?> handleRegister(@Valid @RequestBody RegisterReq req)
    {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataRes.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data("Register successfully")
                        .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @RequestBody TokenRefreshReq req
    )
    {
        RefreshToken refreshToken =
                refreshTokenService
                        .findByToken(
                                req.getRefreshToken()
                        )
                        .orElseThrow(
                                () -> new TokenRefreshException(
                                        "Refresh token not found"
                                )
                        );

        refreshTokenService
                .verifyExpiration(refreshToken);

        User user =
                refreshToken.getUser();

        String accessToken =
                jwtUtils.generateTokenFromUsername(
                        user.getUsername()
                );

        return ResponseEntity.ok(
                DataRes.builder()
                        .code(200)
                        .status(HttpStatus.OK)
                        .data(
                                TokenRefreshRes.builder()
                                        .accessToken(accessToken)
                                        .refreshToken(
                                                refreshToken.getToken()
                                        )
                                        .build()
                        )
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout()
    {
        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow();

        refreshTokenService
                .revokeAllUserTokens(
                        user.getId()
                );

        return ResponseEntity.ok(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data("Logout success")
                        .build()
        );
    }


}