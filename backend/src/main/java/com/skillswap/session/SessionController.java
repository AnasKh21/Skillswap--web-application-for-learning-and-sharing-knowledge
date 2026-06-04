package com.skillswap.session;

import com.skillswap.session.dto.SessionRequestResponseDto;
import com.skillswap.session.dto.SessionResponseDto;
import com.skillswap.skill.Skill;
import com.skillswap.skill.dto.SkillDto;
import com.skillswap.user.User;
import com.skillswap.user.UserService;
import com.skillswap.user.dto.UserResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * SessionController — confirmed sessions (after a request is accepted).
 *
 * Base URL: /api/sessions
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final SessionRepository sessionRepository;
    private final UserService userService;

    public SessionController(SessionService sessionService,
                             SessionRepository sessionRepository,
                             UserService userService) {
        this.sessionService = sessionService;
        this.sessionRepository = sessionRepository;
        this.userService = userService;
    }

    // GET /api/sessions — all sessions (past + upcoming) where I am a participant
    @GetMapping
    public ResponseEntity<List<SessionResponseDto>> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User current = resolveCurrentUser(userDetails);
        List<SessionResponseDto> dtos = sessionRepository.findByParticipantId(current.getId())
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    // PUT /api/sessions/{id}/complete — mark session as completed (both users can rate after this)
    @PutMapping("/{id}/complete")
    public ResponseEntity<SessionResponseDto> completeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        ensureParticipant(userDetails, id);
        try {
            return ResponseEntity.ok(toDto(sessionService.completeSession(id)));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // PUT /api/sessions/{id}/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SessionResponseDto> cancelSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        ensureParticipant(userDetails, id);
        try {
            return ResponseEntity.ok(toDto(sessionService.cancelSession(id)));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private User resolveCurrentUser(UserDetails userDetails) {
        return userService.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void ensureParticipant(UserDetails userDetails, UUID sessionId) {
        User current = resolveCurrentUser(userDetails);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));
        UUID initiatorId = session.getRequest().getInitiator().getId();
        UUID receiverId = session.getRequest().getReceiver().getId();
        if (!current.getId().equals(initiatorId) && !current.getId().equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a participant of this session");
        }
    }

    private SessionResponseDto toDto(Session session) {
        return SessionResponseDto.builder()
                .id(session.getId()).request(toRequestDto(session.getRequest()))
                .scheduledAt(session.getScheduledAt()).mode(session.getMode())
                .status(session.getStatus()).location(session.getLocation()).build();
    }

    private SessionRequestResponseDto toRequestDto(SessionRequest req) {
        return SessionRequestResponseDto.builder()
                .id(req.getId()).initiator(toUserDto(req.getInitiator()))
                .receiver(toUserDto(req.getReceiver()))
                .offeredSkill(toSkillDto(req.getOfferedSkill()))
                .wantedSkill(toSkillDto(req.getWantedSkill()))
                .status(req.getStatus()).message(req.getMessage()).createdAt(req.getCreatedAt()).build();
    }

    private UserResponseDto toUserDto(User user) {
        return UserResponseDto.builder().id(user.getId()).displayName(user.getDisplayName())
                .email(user.getEmail()).bio(user.getBio())
                .averageRating(user.getAverageRating()).createdAt(user.getCreatedAt()).build();
    }

    private SkillDto toSkillDto(Skill skill) {
        return SkillDto.builder().id(skill.getId()).name(skill.getName()).category(skill.getCategory()).build();
    }
}
