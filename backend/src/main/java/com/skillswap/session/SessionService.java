package com.skillswap.session;

import com.skillswap.common.RequestStatus;
import com.skillswap.common.SessionMode;
import com.skillswap.common.SessionStatus;
import com.skillswap.skill.Skill;
import com.skillswap.skill.SkillRepository;
import com.skillswap.user.User;
import com.skillswap.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private final SessionRequestRepository requestRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public SessionService(SessionRequestRepository requestRepository,
                          SessionRepository sessionRepository,
                          UserRepository userRepository,
                          SkillRepository skillRepository) {
        this.requestRepository = requestRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public SessionRequest sendRequest(UUID initiatorId, UUID receiverId,
                                      UUID offeredSkillId, UUID wantedSkillId,
                                      String message) {
        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new IllegalArgumentException("Initiator not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));
        Skill offeredSkill = skillRepository.findById(offeredSkillId)
                .orElseThrow(() -> new IllegalArgumentException("Offered skill not found"));
        Skill wantedSkill = skillRepository.findById(wantedSkillId)
                .orElseThrow(() -> new IllegalArgumentException("Wanted skill not found"));
        return requestRepository.save(new SessionRequest(initiator, receiver, offeredSkill, wantedSkill, message));
    }

    // Accepting transitions the request and creates the scheduled Session in one transaction
    // so both writes succeed or both roll back — no dangling ACCEPTED request without a session.
    @Transactional
    public Session acceptRequest(UUID requestId, Instant scheduledAt, SessionMode mode, String location) {
        SessionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        request.setStatus(RequestStatus.ACCEPTED);
        requestRepository.save(request);
        return sessionRepository.save(new Session(request, scheduledAt, mode, location));
    }

    @Transactional
    public SessionRequest declineRequest(UUID requestId) {
        SessionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        request.setStatus(RequestStatus.DECLINED);
        return requestRepository.save(request);
    }

    @Transactional
    public SessionRequest cancelRequest(UUID requestId) {
        SessionRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }
        request.setStatus(RequestStatus.CANCELLED);
        return requestRepository.save(request);
    }

    @Transactional
    public Session completeSession(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new IllegalStateException("Session is not scheduled");
        }
        session.setStatus(SessionStatus.COMPLETED);
        return sessionRepository.save(session);
    }

    @Transactional
    public Session cancelSession(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new IllegalStateException("Session is not scheduled");
        }
        session.setStatus(SessionStatus.CANCELLED);
        return sessionRepository.save(session);
    }

    public List<SessionRequest> getPendingRequestsForUser(UUID userId) {
        return requestRepository.findByReceiverIdAndStatus(userId, RequestStatus.PENDING);
    }
}
