package com.skillswap.rating;

import com.skillswap.rating.dto.CreateRatingDto;
import com.skillswap.rating.dto.RatingResponseDto;
import com.skillswap.user.User;
import com.skillswap.user.UserService;
import com.skillswap.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * RatingController — leave and read ratings.
 *
 * Base URL: /api/ratings
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final UserService userService;

    public RatingController(RatingService ratingService, UserService userService) {
        this.ratingService = ratingService;
        this.userService = userService;
    }

    // POST /api/ratings — leave a rating after a COMPLETED session
    @PostMapping
    public ResponseEntity<RatingResponseDto> createRating(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid CreateRatingDto dto) {
        User author = resolveCurrentUser(userDetails);
        try {
            Rating rating = ratingService.addRating(
                    dto.getSessionId(), author.getId(), dto.getTargetId(),
                    dto.getScore(), dto.getComment());
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(rating));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // GET /api/ratings/user/{userId} — ratings received by a user (public)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RatingResponseDto>> getRatingsForUser(@PathVariable UUID userId) {
        List<RatingResponseDto> dtos = ratingService.getRatingsForUser(userId)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    // ── Helpers ───────────────────────────────────────────────

    private User resolveCurrentUser(UserDetails userDetails) {
        return userService.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private RatingResponseDto toDto(Rating rating) {
        return RatingResponseDto.builder()
                .id(rating.getId()).sessionId(rating.getSession().getId())
                .author(toUserDto(rating.getAuthor())).target(toUserDto(rating.getTarget()))
                .score(rating.getScore()).comment(rating.getComment()).createdAt(rating.getCreatedAt())
                .build();
    }

    private UserResponseDto toUserDto(User user) {
        return UserResponseDto.builder().id(user.getId()).displayName(user.getDisplayName())
                .email(user.getEmail()).bio(user.getBio())
                .averageRating(user.getAverageRating()).createdAt(user.getCreatedAt()).build();
    }
}
