package com.skillswap.session;

import com.skillswap.common.RequestStatus;
import com.skillswap.common.SessionMode;
import com.skillswap.common.SessionStatus;
import com.skillswap.skill.Skill;
import com.skillswap.skill.SkillRepository;
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
class SessionServiceTest {

    @Mock
    private SessionRequestRepository requestRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SessionService sessionService;

    private UUID initiatorId;
    private UUID receiverId;
    private UUID offeredSkillId;
    private UUID wantedSkillId;
    private UUID requestId;
    private UUID sessionId;

    private User alice;
    private User bob;
    private Skill javaSkill;
    private Skill guitarSkill;
    private SessionRequest pendingRequest;
    private Session scheduledSession;

    @BeforeEach
    void setUp() {
        initiatorId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        offeredSkillId = UUID.randomUUID();
        wantedSkillId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        sessionId = UUID.randomUUID();

        alice = new User("alice@example.com", "password", "Alice");
        alice.setId(initiatorId);

        bob = new User("bob@example.com", "password", "Bob");
        bob.setId(receiverId);

        javaSkill = new Skill("Java", "Programming");
        javaSkill.setId(offeredSkillId);

        guitarSkill = new Skill("Guitar", "Music");
        guitarSkill.setId(wantedSkillId);

        pendingRequest = new SessionRequest(alice, bob, javaSkill, guitarSkill, "Let's swap skills!");
        pendingRequest.setId(requestId);
        pendingRequest.setStatus(RequestStatus.PENDING);

        scheduledSession = new Session(pendingRequest, Instant.now().plusSeconds(86400), SessionMode.ONLINE, null);
        scheduledSession.setId(sessionId);
        scheduledSession.setStatus(SessionStatus.SCHEDULED);
    }

    // ─── sendRequest ──────────────────────────────────────────────────────────

    @Test
    void sendRequest_shouldReturnRequestWithStatusPending() {
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(alice));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(bob));
        when(skillRepository.findById(offeredSkillId)).thenReturn(Optional.of(javaSkill));
        when(skillRepository.findById(wantedSkillId)).thenReturn(Optional.of(guitarSkill));
        when(requestRepository.save(any(SessionRequest.class))).thenReturn(pendingRequest);

        SessionRequest result = sessionService.sendRequest(
                initiatorId, receiverId, offeredSkillId, wantedSkillId, "Hello!"
        );

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    void sendRequest_shouldThrowException_whenInitiatorNotFound() {
        when(userRepository.findById(initiatorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.sendRequest(
                initiatorId, receiverId, offeredSkillId, wantedSkillId, "Hello!"
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sendRequest_shouldLinkCorrectInitiatorAndReceiver() {
        when(userRepository.findById(initiatorId)).thenReturn(Optional.of(alice));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(bob));
        when(skillRepository.findById(offeredSkillId)).thenReturn(Optional.of(javaSkill));
        when(skillRepository.findById(wantedSkillId)).thenReturn(Optional.of(guitarSkill));
        when(requestRepository.save(any(SessionRequest.class))).thenReturn(pendingRequest);

        SessionRequest result = sessionService.sendRequest(
                initiatorId, receiverId, offeredSkillId, wantedSkillId, "Let's swap!"
        );

        assertThat(result.getInitiator()).isEqualTo(alice);
        assertThat(result.getReceiver()).isEqualTo(bob);
    }

    // ─── acceptRequest ────────────────────────────────────────────────────────

    @Test
    void acceptRequest_shouldCreateSessionWithStatusScheduled() {
        Instant tomorrow = Instant.now().plusSeconds(86400);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));
        when(requestRepository.save(any(SessionRequest.class))).thenReturn(pendingRequest);
        when(sessionRepository.save(any(Session.class))).thenReturn(scheduledSession);

        Session result = sessionService.acceptRequest(requestId, tomorrow, SessionMode.ONLINE, null);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
    }

    @Test
    void acceptRequest_shouldThrowException_whenRequestIsNotPending() {
        pendingRequest.setStatus(RequestStatus.DECLINED);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> sessionService.acceptRequest(
                requestId, Instant.now(), SessionMode.ONLINE, null
        )).isInstanceOf(IllegalStateException.class);
    }

    // ─── declineRequest ───────────────────────────────────────────────────────

    @Test
    void declineRequest_shouldSetStatusToDeclined() {
        SessionRequest declinedRequest = new SessionRequest(alice, bob, javaSkill, guitarSkill, "msg");
        declinedRequest.setId(requestId);
        declinedRequest.setStatus(RequestStatus.DECLINED);

        when(requestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));
        when(requestRepository.save(any(SessionRequest.class))).thenReturn(declinedRequest);

        SessionRequest result = sessionService.declineRequest(requestId);

        assertThat(result.getStatus()).isEqualTo(RequestStatus.DECLINED);
    }

    @Test
    void declineRequest_shouldThrowException_whenRequestIsNotPending() {
        pendingRequest.setStatus(RequestStatus.ACCEPTED);
        when(requestRepository.findById(requestId)).thenReturn(Optional.of(pendingRequest));

        assertThatThrownBy(() -> sessionService.declineRequest(requestId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ─── completeSession ──────────────────────────────────────────────────────

    @Test
    void completeSession_shouldSetStatusToCompleted() {
        Session completedSession = new Session(pendingRequest, Instant.now(), SessionMode.ONLINE, null);
        completedSession.setId(sessionId);
        completedSession.setStatus(SessionStatus.COMPLETED);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(scheduledSession));
        when(sessionRepository.save(any(Session.class))).thenReturn(completedSession);

        Session result = sessionService.completeSession(sessionId);

        assertThat(result.getStatus()).isEqualTo(SessionStatus.COMPLETED);
    }

    // ─── getPendingRequestsForUser ────────────────────────────────────────────

    @Test
    void getPendingRequestsForUser_shouldReturnOnlyPendingRequests() {
        when(requestRepository.findByReceiverIdAndStatus(receiverId, RequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));

        List<SessionRequest> result = sessionService.getPendingRequestsForUser(receiverId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(RequestStatus.PENDING);
    }
}
