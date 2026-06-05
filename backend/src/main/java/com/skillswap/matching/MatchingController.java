package com.skillswap.matching;

import com.skillswap.matching.dto.MatchCandidateDto;
import com.skillswap.user.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    private final MatchingService matchingService;
    private final UserRepository userRepository;

    public MatchingController(MatchingService matchingService, UserRepository userRepository) {
        this.matchingService = matchingService;
        this.userRepository = userRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TODO 11 — Implémente GET /api/matching/candidates
    //
    // Étapes :
    //   a) Récupère l'utilisateur connecté depuis le SecurityContext :
    //
    //        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //        String email = auth.getName();
    //
    //   b) Charge son UUID depuis la base via UserRepository (injecte-le)
    //      Indice : tu auras besoin d'une méthode findByEmail dans UserRepository
    //
    //   c) Appelle matchingService.findCandidates(userId)
    //
    //   d) Retourne ResponseEntity.ok(candidates)
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/candidates")
    public ResponseEntity<List<MatchCandidateDto>> getCandidates() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UUID userId = userRepository.findByEmail(email).orElseThrow().getId();
        List<MatchCandidateDto> candidates = matchingService.findCandidates(userId);
        return ResponseEntity.ok(candidates);
    }
}
