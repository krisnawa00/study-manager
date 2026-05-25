package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.User;
import lv.studymanager.repository.UserRepository;
import lv.studymanager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("E-pasts jau reģistrēts: " + req.getEmail());
        }
        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();
        userRepository.save(user);

        String access  = tokenProvider.generateAccessToken(user.getEmail());
        String refresh = tokenProvider.generateRefreshToken(user.getEmail());
        return new AuthResponse(access, refresh, toUserResponse(user));
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        String access  = tokenProvider.generateAccessToken(auth);
        String refresh = tokenProvider.generateRefreshToken(user.getEmail());
        return new AuthResponse(access, refresh, toUserResponse(user));
    }

    public AuthResponse refresh(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Nederīgs refresh token");
        }
        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow();
        String newAccess  = tokenProvider.generateAccessToken(email);
        String newRefresh = tokenProvider.generateRefreshToken(email);
        return new AuthResponse(newAccess, newRefresh, toUserResponse(user));
    }

    private UserResponse toUserResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setFirstName(u.getFirstName());
        r.setLastName(u.getLastName());
        r.setEmail(u.getEmail());
        r.setAvatarUrl(u.getAvatarUrl());
        r.setRole(u.getRole().name());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
