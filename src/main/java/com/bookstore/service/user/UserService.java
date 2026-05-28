package com.bookstore.service.user;

import com.bookstore.dto.user.request.ChangePasswordRequest;
import com.bookstore.dto.user.request.UserUpdateRequest;
import com.bookstore.dto.user.response.UserResponse;
import com.bookstore.entity.user.User;
import com.bookstore.enums.Role;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.exception.UnauthorizedException;
import com.bookstore.mapper.user.UserMapper;
import com.bookstore.repository.user.UserRepository;
import com.bookstore.service.storage.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponse);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return userMapper.toResponse(user);
    }

    public UserResponse getMyProfile(User user) {
        return userMapper.toResponse(user);
    }

    public UserResponse promoteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        user.setRole(Role.ADMIN);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse demoteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        user.setRole(Role.CUSTOMER);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse disableUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        user.setEnabled(false);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse enableUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        user.setEnabled(true);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse updateMyProfile(UserUpdateRequest request, User user) {
        userMapper.updateEntity(request, user);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse updateUserProfileImage(MultipartFile profileImage, User user) {
        if (profileImage != null && !profileImage.isEmpty()) {
            if (user.getProfileImagePublicId() != null) {
                cloudinaryService.deleteImage(user.getProfileImagePublicId());
            }

            Map<String, String> result = cloudinaryService.uploadImage(profileImage);
            user.setProfileImageUrl(result.get("secure_url"));
            user.setProfileImagePublicId(result.get("public_id"));
        }
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public void changePassword(ChangePasswordRequest request, User user) {
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new UnauthorizedException("Old password is wrong");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        userRepository.delete(user);
    }

    public void deleteUserProfileImage(User user) {
        if (user.getProfileImagePublicId() != null) {
            cloudinaryService.deleteImage(user.getProfileImagePublicId());
        }
        user.setProfileImageUrl(null);
        user.setProfileImagePublicId(null);
        userRepository.save(user);
    }

    public void deleteMyProfile(User user) {
        userRepository.delete(user);
    }
}
