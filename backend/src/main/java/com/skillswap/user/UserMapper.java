package com.skillswap.user;

import com.skillswap.media.MediaService;
import com.skillswap.user.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final MediaService mediaService;

    public UserMapper(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    public UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .bio(user.getBio())
                .averageRating(user.getAverageRating())
                .avatarUrl(user.getAvatarUrl() != null ? mediaService.presignUrl(user.getAvatarUrl()) : null)
                .bannerUrl(user.getBannerUrl() != null ? mediaService.presignUrl(user.getBannerUrl()) : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
