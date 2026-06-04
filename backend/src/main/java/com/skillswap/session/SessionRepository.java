package com.skillswap.session;

import com.skillswap.common.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByRequestId(UUID requestId);

    List<Session> findByStatus(SessionStatus status);

    // Returns all sessions where the given user is either the initiator or the receiver.
    // JPQL query: navigates the Session → SessionRequest → User relationship.
    @Query("SELECT s FROM Session s WHERE " +
           "s.request.initiator.id = :userId OR s.request.receiver.id = :userId")
    List<Session> findByParticipantId(@Param("userId") UUID userId);
}
