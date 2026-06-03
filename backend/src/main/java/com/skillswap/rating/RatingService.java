package com.skillswap.rating;

import com.skillswap.common.SessionStatus;
import com.skillswap.session.Session;
import com.skillswap.session.SessionRepository;
import com.skillswap.user.User;
import com.skillswap.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository,
                         SessionRepository sessionRepository,
                         UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }

    // Ratings are only allowed on COMPLETED sessions to prevent abuse during live exchanges.
    // averageRating on User is denormalized so profile reads stay fast (no aggregation query).
    @Transactional
    public Rating addRating(UUID sessionId, UUID authorId, UUID targetId, int score, String comment) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalStateException("Session is not completed");
        }
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target not found"));
        Rating savedRating = ratingRepository.save(new Rating(session, author, target, score, comment));
        updateAverageRating(targetId);
        return savedRating;
    }

    public List<Rating> getRatingsForUser(UUID userId) {
        return ratingRepository.findByTargetId(userId);
    }

    public double calculateAverageRating(UUID userId) {
        List<Rating> ratings = getRatingsForUser(userId);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToInt(Rating::getScore)
                .average()
                .orElse(0.0);
    }

    // Called after every new rating to keep User.averageRating in sync without a live aggregation.
    @Transactional
    public void updateAverageRating(UUID userId) {
        double average = calculateAverageRating(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAverageRating(average);
        userRepository.save(user);
    }
}
