package com.skillswap.matching;

import com.skillswap.common.SkillType;
import com.skillswap.matching.dto.MatchCandidateDto;
import com.skillswap.media.MediaService;
import com.skillswap.skill.Skill;
import com.skillswap.user.User;
import com.skillswap.user.UserSkill;
import com.skillswap.user.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock private MatchingRepository  matchingRepository;
    @Mock private UserSkillRepository userSkillRepository;
    @Mock private MediaService        mediaService;

    @InjectMocks
    private MatchingService matchingService;

    private UUID aliceId;
    private UUID bobId;
    private UUID carolId;

    @BeforeEach
    void setUp() {
        aliceId = UUID.randomUUID();
        bobId   = UUID.randomUUID();
        carolId = UUID.randomUUID();
    }

    // ─── tri par score ────────────────────────────────────────────────────────

    @Test
    void findCandidates_returnsCandidatesSortedByScoreDescending() {
        MatchCandidateProjection bob   = mockProjection(bobId,   "Bob",   "Bio Bob",   4.0, 1L, 2L);
        MatchCandidateProjection carol = mockProjection(carolId, "Carol", "Bio Carol", 3.5, 1L, 1L);

        when(matchingRepository.findCandidatesWithScore(aliceId)).thenReturn(List.of(carol, bob));
        when(userSkillRepository.findByUserIdIn(any())).thenReturn(List.of());

        List<MatchCandidateDto> result = matchingService.findCandidates(aliceId);

        assertThat(result.get(0).getDisplayName()).isEqualTo("Bob");
        assertThat(result.get(1).getDisplayName()).isEqualTo("Carol");
    }

    // ─── calcul du score ──────────────────────────────────────────────────────

    @Test
    void findCandidates_scoreIsCorrect_whenRatingIsPresent() {
        // (2 + 1) * (0.5 + 4.0/10) = 3 * 0.90 = 2.70
        MatchCandidateProjection bob = mockProjection(bobId, "Bob", null, 4.0, 2L, 1L);
        when(matchingRepository.findCandidatesWithScore(any())).thenReturn(List.of(bob));
        when(userSkillRepository.findByUserIdIn(any())).thenReturn(List.of());

        List<MatchCandidateDto> result = matchingService.findCandidates(aliceId);

        assertThat(result.get(0).getScore()).isEqualTo(2.70, withPrecision(0.001));
    }

    @Test
    void findCandidates_scoreUsesNeutralFactor_whenRatingIsNull() {
        // (1 + 1) * 0.75 = 1.50
        MatchCandidateProjection dave = mockProjection(UUID.randomUUID(), "Dave", null, null, 1L, 1L);
        when(matchingRepository.findCandidatesWithScore(any())).thenReturn(List.of(dave));
        when(userSkillRepository.findByUserIdIn(any())).thenReturn(List.of());

        List<MatchCandidateDto> result = matchingService.findCandidates(aliceId);

        assertThat(result.get(0).getScore()).isEqualTo(1.50, withPrecision(0.001));
    }

    // ─── enrichissement des skills ────────────────────────────────────────────

    @Test
    void findCandidates_teachSkillsAndLearnSkillsArePopulated() {
        MatchCandidateProjection bob = mockProjection(bobId, "Bob", "I love Java", 4.0, 1L, 1L);
        when(matchingRepository.findCandidatesWithScore(any())).thenReturn(List.of(bob));

        // Bob offre Java, veut Guitar
        UserSkill offeredJava  = mockUserSkill(bobId, "Java",   SkillType.OFFERED);
        UserSkill wantedGuitar = mockUserSkill(bobId, "Guitar", SkillType.WANTED);
        when(userSkillRepository.findByUserIdIn(any())).thenReturn(List.of(offeredJava, wantedGuitar));

        List<MatchCandidateDto> result = matchingService.findCandidates(aliceId);

        assertThat(result.get(0).getTeachSkills()).containsExactly("Java");
        assertThat(result.get(0).getLearnSkills()).containsExactly("Guitar");
    }

    @Test
    void findCandidates_bioIsPopulated() {
        MatchCandidateProjection bob = mockProjection(bobId, "Bob", "I love Java", 4.0, 1L, 1L);
        when(matchingRepository.findCandidatesWithScore(any())).thenReturn(List.of(bob));
        when(userSkillRepository.findByUserIdIn(any())).thenReturn(List.of());

        List<MatchCandidateDto> result = matchingService.findCandidates(aliceId);

        assertThat(result.get(0).getBio()).isEqualTo("I love Java");
    }

    @Test
    void findCandidates_skillsAreIsolatedPerCandidate() {
        MatchCandidateProjection bob   = mockProjection(bobId,   "Bob",   null, 4.0, 1L, 1L);
        MatchCandidateProjection carol = mockProjection(carolId, "Carol", null, 4.0, 1L, 1L);
        when(matchingRepository.findCandidatesWithScore(any())).thenReturn(List.of(bob, carol));

        UserSkill bobSkill   = mockUserSkill(bobId,   "Java",   SkillType.OFFERED);
        UserSkill carolSkill = mockUserSkill(carolId, "Guitar", SkillType.OFFERED);
        when(userSkillRepository.findByUserIdIn(any())).thenReturn(List.of(bobSkill, carolSkill));

        List<MatchCandidateDto> result = matchingService.findCandidates(aliceId);

        MatchCandidateDto bobDto   = result.stream().filter(d -> d.getDisplayName().equals("Bob")).findFirst().orElseThrow();
        MatchCandidateDto carolDto = result.stream().filter(d -> d.getDisplayName().equals("Carol")).findFirst().orElseThrow();

        assertThat(bobDto.getTeachSkills()).containsExactly("Java");
        assertThat(carolDto.getTeachSkills()).containsExactly("Guitar");
    }

    // ─── cas limites ──────────────────────────────────────────────────────────

    @Test
    void findCandidates_returnsEmptyList_whenNoCandidates() {
        when(matchingRepository.findCandidatesWithScore(any())).thenReturn(List.of());

        assertThat(matchingService.findCandidates(aliceId)).isEmpty();
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private MatchCandidateProjection mockProjection(
            UUID id, String name, String bio, Double rating, Long theyTeach, Long iTeach) {
        MatchCandidateProjection p = mock(MatchCandidateProjection.class);
        when(p.getId()).thenReturn(id);
        when(p.getDisplayName()).thenReturn(name);
        when(p.getBio()).thenReturn(bio);
        when(p.getAverageRating()).thenReturn(rating);
        when(p.getTheyTeachMe()).thenReturn(theyTeach);
        when(p.getITeachThem()).thenReturn(iTeach);
        return p;
    }

    private UserSkill mockUserSkill(UUID userId, String skillName, SkillType type) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);

        com.skillswap.skill.Skill skill = mock(com.skillswap.skill.Skill.class);
        when(skill.getName()).thenReturn(skillName);

        UserSkill us = mock(UserSkill.class);
        when(us.getUser()).thenReturn(user);
        when(us.getSkill()).thenReturn(skill);
        when(us.getType()).thenReturn(type);
        return us;
    }

    private static org.assertj.core.data.Offset<Double> withPrecision(double delta) {
        return org.assertj.core.data.Offset.offset(delta);
    }
}
