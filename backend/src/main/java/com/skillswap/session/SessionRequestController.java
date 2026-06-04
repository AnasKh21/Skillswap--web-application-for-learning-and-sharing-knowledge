package com.skillswap.session;

import com.skillswap.session.dto.AcceptRequestDto;
import com.skillswap.session.dto.SendRequestDto;
import com.skillswap.session.dto.SessionRequestResponseDto;
import com.skillswap.session.dto.SessionResponseDto;
import com.skillswap.skill.Skill;
import com.skillswap.skill.dto.SkillDto;
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

/**
 * SessionRequestController — sending, accepting, declining swap requests.
 *
 * Base URL: /api/session-requests
 */
@RestController
@RequestMapping("/api/session-requests")
public class SessionRequestController {

    private final SessionService sessionService;
    private final SessionRequestRepository sessionRequestRepository;
    private final UserService userService;

    public SessionRequestController(SessionService sessionService,
                                    SessionRequestRepository sessionRequestRepository,
                                    UserService userService) {
        this.sessionService = sessionService;
        this.sessionRequestRepository = sessionRequestRepository;
        this.userService = userService;
    }

    // POST /api/session-requests — send a swap request (swipe right)
    @PostMapping
    public ResponseEntity<SessionRequestResponseDto> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid SendRequestDto dto) {
        User current = resolveCurrentUser(userDetails);
        try {
            SessionRequest request = sessionService.sendRequest(
                    current.getId(), dto.getReceiverId(),
                    dto.getOfferedSkillId(), dto.getWantedSkillId(), dto.getMessage());
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(request));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // GET /api/session-requests/incoming — pending requests I received
    @GetMapping("/incoming")
    public ResponseEntity<List<SessionRequestResponseDto>> getIncomingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        User current = resolveCurrentUser(userDetails);
        List<SessionRequestResponseDto> dtos = sessionService.getPendingRequestsForUser(current.getId())
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(dtos);
    }

    // PUT /api/session-requests/{id}/accept — accept a request, creates a Session
    @PutMapping("/{id}/accept")
    public ResponseEntity<SessionResponseDto> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable java.util.UUID id,
            @RequestBody @Valid AcceptRequestDto dto) {
        User current = resolveCurrentUser(userDetails);
        // Verify the current user is the receiver of this request
        SessionRequest req = getRequestOrThrow(id);
        if (!req.getReceiver().getId().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }
        try {
            Session session = sessionService.acceptRequest(id, dto.getScheduledAt(), dto.getMode(), dto.getLocation());
            return ResponseEntity.ok(toSessionDto(session));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // PUT /api/session-requests/{id}/decline
    @PutMapping("/{id}/decline")
    public ResponseEntity<SessionRequestResponseDto> declineRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable java.util.UUID id) {
        User current = resolveCurrentUser(userDetails);
        SessionRequest req = getRequestOrThrow(id);
        if (!req.getReceiver().getId().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }
        try {
            return ResponseEntity.ok(toDto(sessionService.declineRequest(id)));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // PUT /api/session-requests/{id}/cancel — cancel a request I sent
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SessionRequestResponseDto> cancelRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable java.util.UUID id) {
        User current = resolveCurrentUser(userDetails);
        SessionRequest req = getRequestOrThrow(id);
        if (!req.getInitiator().getId().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }
        try {
            return ResponseEntity.ok(toDto(sessionService.cancelRequest(id)));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private User resolveCurrentUser(UserDetails userDetails) {
        return userService.findUserByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private SessionRequest getRequestOrThrow(java.util.UUID id) {
        return sessionRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
    }

    private SessionRequestResponseDto toDto(SessionRequest req) {
        return SessionRequestResponseDto.builder()
                .id(req.getId()).initiator(toUserDto(req.getInitiator()))
                .receiver(toUserDto(req.getReceiver()))
                .offeredSkill(toSkillDto(req.getOfferedSkill()))
                .wantedSkill(toSkillDto(req.getWantedSkill()))
                .status(req.getStatus()).message(req.getMessage()).createdAt(req.getCreatedAt())
                .build();
    }

    private SessionResponseDto toSessionDto(Session session) {
        return SessionResponseDto.builder()
                .id(session.getId()).request(toDto(session.getRequest()))
                .scheduledAt(session.getScheduledAt()).mode(session.getMode())
                .status(session.getStatus()).location(session.getLocation())
                .build();
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
