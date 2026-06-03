package com.skillswap.session;

import com.skillswap.common.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRequestRepository extends JpaRepository<SessionRequest, UUID> {

    // All requests sent BY a user (with a specific status)
    List<SessionRequest> findByInitiatorIdAndStatus(UUID initiatorId, RequestStatus status);

    // All requests sent TO a user (with a specific status)
    List<SessionRequest> findByReceiverIdAndStatus(UUID receiverId, RequestStatus status);

    // All pending requests received by a user (useful for the inbox)
    List<SessionRequest> findByReceiverId(UUID receiverId);
}
