package com.skillswap.matching;

import com.skillswap.common.SkillType;
import com.skillswap.matching.dto.MatchCandidateDto;
import com.skillswap.user.UserSkill;
import com.skillswap.user.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final MatchingRepository   matchingRepository;
    private final UserSkillRepository  userSkillRepository;

    public MatchingService(MatchingRepository matchingRepository,
                           UserSkillRepository userSkillRepository) {
        this.matchingRepository  = matchingRepository;
        this.userSkillRepository = userSkillRepository;
    }

    public List<MatchCandidateDto> findCandidates(UUID myId) {

        // ── Étape 1 : scores via SQL ──────────────────────────────────────────
        List<MatchCandidateProjection> raw = matchingRepository.findCandidatesWithScore(myId);

        // ── Étape 2 : calcul du score Java + construction des DTOs ────────────
        List<MatchCandidateDto> dtos = new ArrayList<>();
        for (MatchCandidateProjection p : raw) {
            double ratingFactor = (p.getAverageRating() != null)
                    ? 0.5 + (p.getAverageRating() / 10.0)
                    : 0.75;
            double score = (p.getTheyTeachMe() + p.getITeachThem()) * ratingFactor;
            dtos.add(new MatchCandidateDto(
                    p.getId(),
                    p.getDisplayName(),
                    p.getBio(),
                    p.getAvatarUrl(),
                    p.getAverageRating(),
                    p.getTheyTeachMe(),
                    p.getITeachThem(),
                    score
            ));
        }

        // ── Étape 3 : enrichissement des skills en une seule requête ──────────
        // On récupère tous les skills de tous les candidats en un seul SELECT ... WHERE user_id IN (...)
        // au lieu de faire une requête par candidat (N+1)
        if (!dtos.isEmpty()) {
            List<UUID> candidateIds = dtos.stream()
                    .map(MatchCandidateDto::getUserId)
                    .toList();

            Map<UUID, List<UserSkill>> skillsByUser = userSkillRepository
                    .findByUserIdIn(candidateIds)
                    .stream()
                    .collect(Collectors.groupingBy(us -> us.getUser().getId()));

            for (MatchCandidateDto dto : dtos) {
                List<UserSkill> skills = skillsByUser.getOrDefault(dto.getUserId(), List.of());
                dto.setTeachSkills(skills.stream()
                        .filter(s -> s.getType() == SkillType.OFFERED)
                        .map(s -> s.getSkill().getName())
                        .toList());
                dto.setLearnSkills(skills.stream()
                        .filter(s -> s.getType() == SkillType.WANTED)
                        .map(s -> s.getSkill().getName())
                        .toList());
            }
        }

        // ── Étape 4 : tri par score décroissant ───────────────────────────────
        dtos.sort(Comparator.comparingDouble(MatchCandidateDto::getScore).reversed());
        return dtos;
    }
}
