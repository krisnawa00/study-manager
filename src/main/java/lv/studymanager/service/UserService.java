package lv.studymanager.service;

import lv.studymanager.dto.Dto.*;
import lv.studymanager.model.User;
import lv.studymanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getProfile(String email) {
        return toUserResponse(findByEmail(email));
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = findByEmail(email);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setAvatarUrl(req.getAvatarUrl());
        return toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Pašreizējā parole ir nepareiza");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Lietotājs nav atrasts"));
    }

    private UserResponse toUserResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setFirstName(u.getFirstName());
        r.setLastName(u.getLastName());
        r.setEmail(u.getEmail());
        r.setAvatarUrl(u.getAvatarUrl());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
