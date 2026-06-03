package com.skillswap.rating;

import com.skillswap.common.RequestStatus;
import com.skillswap.common.SessionMode;
import com.skillswap.common.SessionStatus;
import com.skillswap.session.Session;
import com.skillswap.session.SessionRepository;
import com.skillswap.session.SessionRequest;
import com.skillswap.user.User;
import com.skillswap.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingService ratingService;

    private UUID sessionId;
    private UUID authorId;
    private UUID targetId;

    private User alice;
    private User bob;
    private Session completedSession;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        targetId = UUID.randomUUID();

        alice = new User("alice@example.com", "password", "Alice");
        alice.setId(authorId);

        bob = new User("bob@example.com", "password", "Bob");
        bob.setId(targetId);

        // A completed session is required to leave a rating
        completedSession = new Session();
        completedSession.setId(sessionId);
        completedSession.setStatus(SessionStatus.COMPLETED);
        completedSession.setMode(SessionMode.ONLINE);
        completedSession.setScheduledAt(Instant.now());
    }

    // ─── addRating ────────────────────────────────────────────────────────────

    @Test
    void addRating_shouldReturnRatingWithCorrectScore() {
        Rating savedRating = new Rating(completedSession, alice, bob, 5, "Great session!");
        savedRating.setId(UUID.randomUUID());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(completedSession));
        when(userRepository.findById(authorId)).thenReturn(Optional.of(alice));
        when(userRepository.findById(targetId)).thenReturn(Optional.of(bob));
        when(ratingRepository.save(any(Rating.class))).thenReturn(savedRating);
        // updateAverageRating (called inside addRating) needs these
        when(ratingRepository.findByTargetId(targetId)).thenReturn(List.of(savedRating));

        Rating result = ratingService.addRating(sessionId, authorId, targetId, 5, "Great session!");

        assertThat(result).isNotNull();
        assertThat(result.getScore()).isEqualTo(5);
        assertThat(result.getAuthor()).isEqualTo(alice);
        assertThat(result.getTarget()).isEqualTo(bob);
    }

    @Test
    void addRating_shouldThrowException_whenScoreIsInvalid() {
        // The hints say to validate score AFTER finding the session
        // So we only need the session stub here
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(completedSession));

        // score of 6 is above the maximum of 5
        assertThatThrownBy(() -> ratingService.addRating(sessionId, authorId, targetId, 6, "Too good!"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addRating_shouldThrowException_whenScoreIsBelowMinimum() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(completedSession));

        // score of 0 is below the minimum of 1
        assertThatThrownBy(() -> ratingService.addRating(sessionId, authorId, targetId, 0, "Bad!"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addRating_shouldThrowException_whenSessionIsNotCompleted() {
        // A session that is still just scheduled — cannot rate yet
        completedSession.setStatus(SessionStatus.SCHEDULED);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(completedSession));

        assertThatThrownBy(() -> ratingService.addRating(sessionId, authorId, targetId, 4, "Nice"))
                .isInstanceOf(IllegalStateException.class);
    }

    // ─── getRatingsForUser ────────────────────────────────────────────────────

    @Test
    void getRatingsForUser_shouldReturnAllRatingsForUser() {
        Rating rating1 = new Rating(completedSession, alice, bob, 4, "Good");
        Rating rating2 = new Rating(completedSession, alice, bob, 5, "Excellent");

        when(ratingRepository.findByTargetId(targetId)).thenReturn(List.of(rating1, rating2));

        List<Rating> result = ratingService.getRatingsForUser(targetId);

        assertThat(result).hasSize(2);
    }

    @Test
    void getRatingsForUser_shouldReturnEmpty_whenNoRatings() {
        when(ratingRepository.findByTargetId(targetId)).thenReturn(List.of());

        List<Rating> result = ratingService.getRatingsForUser(targetId);

        assertThat(result).isEmpty();
    }

    // ─── calculateAverageRating ───────────────────────────────────────────────

    @Test
    void calculateAverageRating_shouldReturnCorrectAverage() {
        Rating rating1 = new Rating(completedSession, alice, bob, 4, "Good");
        Rating rating2 = new Rating(completedSession, alice, bob, 2, "Okay");

        when(ratingRepository.findByTargetId(targetId)).thenReturn(List.of(rating1, rating2));

        double result = ratingService.calculateAverageRating(targetId);

        // Average of 4 and 2 = 3.0
        assertThat(result).isEqualTo(3.0);
    }

    @Test
    void calculateAverageRating_shouldReturnZero_whenNoRatings() {
        when(ratingRepository.findByTargetId(targetId)).thenReturn(List.of());

        double result = ratingService.calculateAverageRating(targetId);

        assertThat(result).isEqualTo(0.0);
    }
}
