package com.skillswap.session;

import com.skillswap.common.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    // Find the session created from a specific request
    Optional<Session> findByRequestId(UUID requestId);

    // Find all sessions with a given status (e.g. all COMPLETED sessions)
    List<Session> findByStatus(SessionStatus status);
}
