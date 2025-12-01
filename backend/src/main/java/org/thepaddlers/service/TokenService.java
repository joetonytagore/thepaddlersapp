package org.thepaddlers.service;

import org.springframework.stereotype.Service;
import org.thepaddlers.model.User;
import org.thepaddlers.model.RefreshToken;
import org.thepaddlers.repository.RefreshTokenRepository;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Service
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom random = new SecureRandom();
    private final long refreshTtlMs = 7 * 24 * 60 * 60 * 1000L; // 7 days

    public TokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken issueRefreshToken(User user) {
        String token = generateToken();
        Date expiresAt = new Date(System.currentTimeMillis() + refreshTtlMs);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> verifyRefreshToken(String token) {
        Optional<RefreshToken> rt = refreshTokenRepository.findByToken(token);
        if (rt.isPresent() && !rt.get().isRevoked() && rt.get().getExpiresAt().after(new Date())) {
            return rt;
        }
        return Optional.empty();
    }

    public void revokeRefreshToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        revokeRefreshToken(oldToken);
        return issueRefreshToken(oldToken.getUser());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

