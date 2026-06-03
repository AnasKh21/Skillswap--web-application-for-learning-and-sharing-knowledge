package com.skillswap.rating;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RatingRepository extends JpaRepository<Rating, UUID> {

    // Get all ratings received by a user (target = the person being rated)
    List<Rating> findByTargetId(UUID targetId);

    // Get all ratings written by a user (author = the person who wrote the rating)
    List<Rating> findByAuthorId(UUID authorId);

    // Get all ratings for a specific session (should be at most 2)
    List<Rating> findBySessionId(UUID sessionId);
}
