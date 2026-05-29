package com.bookstore.service.catalog;

import com.bookstore.dto.catalog.request.AuthorCreateRequest;
import com.bookstore.dto.catalog.request.AuthorUpdateRequest;
import com.bookstore.dto.catalog.response.AuthorResponse;
import com.bookstore.entity.catalog.Author;
import com.bookstore.entity.user.User;
import com.bookstore.exception.BadRequestException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.mapper.catalog.AuthorMapper;
import com.bookstore.repository.catalog.AuthorRepository;
import com.bookstore.service.storage.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public Page<AuthorResponse> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(authorMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AuthorResponse getAuthor(UUID id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));
        return authorMapper.toResponse(author);
    }

    @Transactional(readOnly = true)
    public Page<AuthorResponse> getMyFollowingAuthors(User user, Pageable pageable) {
        return authorRepository.findFollowedAuthorsByUserId(user.getId(), pageable)
                .map(authorMapper::toResponse);
    }

    @Transactional
    public AuthorResponse createAuthor(AuthorCreateRequest request, MultipartFile profileImage) {
        Author author = authorMapper.toEntity(request);
        if (profileImage != null && !profileImage.isEmpty()) {
            Map<String, String> result = cloudinaryService.uploadImage(profileImage);
            author.setProfileImageUrl(result.get("secure_url"));
            author.setProfileImagePublicId(result.get("public_id"));
        }
        author = authorRepository.save(author);
        return authorMapper.toResponse(author);
    }

    @Transactional
    public AuthorResponse followAuthor(UUID id, User user) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author with id " + id + " not found");
        }
        if (authorRepository.follow(id, user.getId()) > 0) {
            authorRepository.incrementFollowersCount(id);
        } else {
            throw new BadRequestException("You are already following this author.");
        }
        return getAuthor(id);
    }

    @Transactional
    public AuthorResponse unfollowAuthor(UUID id, User user) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author with id " + id + " not found");
        }
        if (authorRepository.unfollow(id, user.getId()) > 0) {
            authorRepository.decrementFollowersCount(id);
        } else {
            throw new BadRequestException("You are not following this author.");
        }
        return getAuthor(id);
    }

    @Transactional
    public AuthorResponse updateAuthor(UUID id, AuthorUpdateRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));
        authorMapper.updateEntity(request, author);
        author = authorRepository.save(author);
        return authorMapper.toResponse(author);
    }

    @Transactional
    public AuthorResponse updateAuthorProfileImage(UUID id, MultipartFile profileImage) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));
        if (profileImage != null && !profileImage.isEmpty()) {
            if (author.getProfileImageUrl() != null) {
                cloudinaryService.deleteImage(author.getProfileImagePublicId());
            }

            Map<String, String> result = cloudinaryService.uploadImage(profileImage);
            author.setProfileImageUrl(result.get("secure_url"));
            author.setProfileImagePublicId(result.get("public_id"));
        }
        author = authorRepository.save(author);
        return authorMapper.toResponse(author);
    }

    @Transactional
    public void deleteAuthorProfileImage(UUID id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));
        if (author.getProfileImagePublicId() != null) {
            cloudinaryService.deleteImage(author.getProfileImagePublicId());
        }
        author.setProfileImageUrl(null);
        author.setProfileImagePublicId(null);
        authorRepository.save(author);
    }

    @Transactional
    public void deleteAuthor(UUID id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author with id " + id + " not found"));
        if (author.getProfileImagePublicId() != null) {
            cloudinaryService.deleteImage(author.getProfileImagePublicId());
        }
        authorRepository.delete(author);
    }
}
