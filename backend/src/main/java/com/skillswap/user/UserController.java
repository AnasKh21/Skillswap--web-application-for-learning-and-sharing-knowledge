package com.skillswap.user;

import com.skillswap.media.MediaService;
import com.skillswap.user.dto.AddUserSkillDto;
import com.skillswap.user.dto.UpdateProfileDto;
import com.skillswap.user.dto.UserResponseDto;
import com.skillswap.user.dto.UserSkillDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UserController — profile and skill management.
 *
 * Base URL: /api/users
 *
 * @AuthenticationPrincipal UserDetails userDetails
 *   → Spring injects the logged-in user automatically (read from JWT cookie by JwtAuthFilter)
 *   → userDetails.getUsername() returns the email
 *   → we call userService.findUserByEmail() to get the full User entity
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService  userService;
    private final MediaService mediaService;
    private final UserMapper   userMapper;

    public UserController(UserService userService, MediaService mediaService, UserMapper userMapper) {
        this.userService  = userService;
        this.mediaService = mediaService;
        this.userMapper   = userMapper;
    }

    // GET /api/users/me — current user's profile
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = resolveCurrentUser(userDetails);
        return ResponseEntity.ok(toDto(user));
    }

    // PUT /api/users/me — update display name and bio
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UpdateProfileDto dto) {
        User current = resolveCurrentUser(userDetails);
        try {
            User updated = userService.updateProfile(current.getId(), dto.getDisplayName(), dto.getBio());
            return ResponseEntity.ok(toDto(updated));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // GET /api/users — all users except the current one (used on Discover page)
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
        User current = resolveCurrentUser(userDetails);
        List<UserResponseDto> dtos = userService.findAllUsers().stream()
                .filter(u -> !u.getId().equals(current.getId()))
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // GET /api/users/{id} — any user's public profile (used on Discover page)
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID id) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(toDto(user));
    }

    // GET /api/users/me/skills — list my skills (offered + wanted)
    @GetMapping("/me/skills")
    public ResponseEntity<List<UserSkillDto>> getMySkills(@AuthenticationPrincipal UserDetails userDetails) {
        User current = resolveCurrentUser(userDetails);
        List<UserSkillDto> dtos = userService.getSkillsForUser(current.getId())
                .stream().map(this::toSkillDto).toList();
        return ResponseEntity.ok(dtos);
    }

    // POST /api/users/me/skills — add a skill to my profile
    @PostMapping("/me/skills")
    public ResponseEntity<Void> addSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid AddUserSkillDto dto) {
        User current = resolveCurrentUser(userDetails);
        try {
            userService.addSkillToUser(current.getId(), dto.getSkillId(), dto.getType(), dto.getLevel());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // DELETE /api/users/me/skills/{userSkillId} — remove a skill from my profile
    @DeleteMapping("/me/skills/{userSkillId}")
    public ResponseEntity<Void> removeSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID userSkillId) {
        // Ownership check: verify this UserSkill belongs to the current user
        User current = resolveCurrentUser(userDetails);
        List<UserSkill> mySkills = userService.getSkillsForUser(current.getId());
        boolean owned = mySkills.stream().anyMatch(us -> us.getId().equals(userSkillId));
        if (!owned) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your skill");
        }
        try {
            userService.removeSkillFromUser(userSkillId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // GET /api/users/{id}/avatar-url — presigned S3 URL for the avatar (authenticated only)
    @GetMapping("/{id}/avatar-url")
    public ResponseEntity<Map<String, String>> getAvatarUrl(@PathVariable UUID id) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getAvatarUrl() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No avatar for this user");
        }
        return ResponseEntity.ok(Map.of("url", mediaService.presignUrl(user.getAvatarUrl())));
    }

    // GET /api/users/{id}/banner-url — presigned S3 URL for the banner (authenticated only)
    @GetMapping("/{id}/banner-url")
    public ResponseEntity<Map<String, String>> getBannerUrl(@PathVariable UUID id) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getBannerUrl() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No banner for this user");
        }
        return ResponseEntity.ok(Map.of("url", mediaService.presignUrl(user.getBannerUrl())));
    }

    // POST /api/users/me/avatar — upload photo de profil
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        User current = resolveCurrentUser(userDetails);
        try {
            String url = mediaService.storeAvatar(current.getId(), file);
            User updated = userService.updateAvatar(current.getId(), url);
            return ResponseEntity.ok(toDto(updated));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // POST /api/users/me/banner — upload bannière
    @PostMapping(value = "/me/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> uploadBanner(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        User current = resolveCurrentUser(userDetails);
        try {
            String url = mediaService.storeBanner(current.getId(), file);
            User updated = userService.updateBanner(current.getId(), url);
            return ResponseEntity.ok(toDto(updated));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    // Resolves the logged-in user from the JWT-backed UserDetails
    private User resolveCurrentUser(UserDetails userDetails) {
        return userService.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponseDto toDto(User user) {
        return userMapper.toDto(user);
    }

    private UserSkillDto toSkillDto(UserSkill us) {
        return UserSkillDto.builder()
                .id(us.getId())
                .skillId(us.getSkill().getId())
                .skillName(us.getSkill().getName())
                .type(us.getType())
                .level(us.getLevel())
                .build();
    }
}
