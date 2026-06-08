package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.exceptions.TokenRefreshException;
import com.example.base_spring_boot.models.entities.RefreshToken;
import com.example.base_spring_boot.models.entities.User;
import com.example.base_spring_boot.models.repositories.IRefreshTokenRepository;
import com.example.base_spring_boot.models.repositories.IUserRepository;
import com.example.base_spring_boot.models.services.IRefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl
        implements IRefreshTokenService {

    private final IRefreshTokenRepository refreshTokenRepository;

    private final IUserRepository userRepository;

    @Value("${jwt.expired.refresh}")
    private Long refreshExpired;

    @Override
    public RefreshToken createRefreshToken(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow();

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(UUID.randomUUID().toString())
                        .expiryDate(
                                Instant.now()
                                        .plusMillis(refreshExpired)
                        )
                        .revoked(false)
                        .user(user)
                        .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(
            RefreshToken token
    ) {

        if(token.isRevoked())
        {
            throw new TokenRefreshException(
                    "Refresh token revoked"
            );
        }

        if(token.getExpiryDate()
                .compareTo(Instant.now()) < 0)
        {
            refreshTokenRepository.delete(token);

            throw new TokenRefreshException(
                    "Refresh token expired"
            );
        }

        return token;
    }

    @Override
    public Optional<RefreshToken> findByToken(
            String token
    ) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public void revokeAllUserTokens(
            Long userId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        List<RefreshToken> tokens =
                refreshTokenRepository.findByUser(user);

        tokens.forEach(
                token -> token.setRevoked(true)
        );

        refreshTokenRepository.saveAll(tokens);
    }
}   