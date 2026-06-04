package com.skillswap.skill;

import com.skillswap.skill.dto.SkillDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * SkillController — global skills catalogue.
 *
 * Base URL: /api/skills
 *
 * Endpoints:
 *   GET  /api/skills               → list all skills (used in the "add skill" dropdown)
 *   GET  /api/skills?category=X    → filter by category
 *   POST /api/skills               → create a new skill (admin only in future)
 *
 * These endpoints are fully working from Phase 2 — no JWT needed.
 * The skills catalogue is public data.
 */
@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/skills
    // GET /api/skills?category=Programming
    //
    // Returns all skills, optionally filtered by category.
    // @RequestParam(required = false) means the parameter is optional.
    // ─────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<SkillDto>> getSkills(
            @RequestParam(required = false) String category) {

        List<Skill> skills;

        if (category != null && !category.isBlank()) {
            // Filtered query — only skills in this category
            skills = skillService.findSkillsByCategory(category);
        } else {
            // No filter — return all skills
            skills = skillService.findAllSkills();
        }

        List<SkillDto> dtos = skills.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/skills
    // Creates a new skill in the global catalogue.
    //
    // Body: { "name": "Rust", "category": "Programming" }
    //
    // TODO Phase 3: Restrict this to admin users only.
    //   Add: @PreAuthorize("hasRole('ADMIN')") above the method.
    //   This requires adding a roles system to the User entity.
    // ─────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<SkillDto> createSkill(@RequestBody SkillDto dto) {

        // @NotBlank validation: check that name and category are not empty
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
        if (dto.getCategory() == null || dto.getCategory().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category is required");
        }

        try {
            Skill skill = skillService.createSkill(dto.getName(), dto.getCategory());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(toDto(skill));
        } catch (IllegalArgumentException e) {
            // SkillService.createSkill() throws if the name already exists
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────

    private SkillDto toDto(Skill skill) {
        return SkillDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }
}
