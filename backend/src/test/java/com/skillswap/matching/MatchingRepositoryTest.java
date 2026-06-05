package com.skillswap.matching;

import com.skillswap.common.SkillType;
import com.skillswap.skill.Skill;
import com.skillswap.skill.SkillRepository;
import com.skillswap.user.User;
import com.skillswap.user.UserRepository;
import com.skillswap.user.UserSkill;
import com.skillswap.user.UserSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest charge uniquement la couche JPA (entités + repositories)
// sans démarrer tout le contexte Spring (pas de controllers, pas de services)
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MatchingRepositoryTest {

    @Autowired private MatchingRepository    matchingRepository;
    @Autowired private UserRepository        userRepository;
    @Autowired private SkillRepository       skillRepository;
    @Autowired private UserSkillRepository   userSkillRepository;

    private User alice;
    private User bob;
    private User carol;
    private Skill java;
    private Skill guitar;
    private Skill python;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(new User("alice@test.com", "pass", "Alice"));
        bob   = userRepository.save(new User("bob@test.com",   "pass", "Bob"));
        carol = userRepository.save(new User("carol@test.com", "pass", "Carol"));

        java   = skillRepository.save(new Skill("Java",   "Programming"));
        guitar = skillRepository.save(new Skill("Guitar", "Music"));
        python = skillRepository.save(new Skill("Python", "Programming"));
    }

    // ─── condition de base ────────────────────────────────────────────────────

    @Test
    void returnsCandidate_whenTheyOfferWhatIWant() {
        bob.setBio("I love Java");
        userRepository.save(bob);
        userSkillRepository.save(new UserSkill(alice, java,   SkillType.WANTED,  2));
        userSkillRepository.save(new UserSkill(bob,   java,   SkillType.OFFERED, 4));

        List<MatchCandidateProjection> result =
                matchingRepository.findCandidatesWithScore(alice.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDisplayName()).isEqualTo("Bob");
        assertThat(result.get(0).getBio()).isEqualTo("I love Java");
    }

    @Test
    void excludesCandidate_whenNoSkillInCommon() {
        userSkillRepository.save(new UserSkill(alice, java,   SkillType.WANTED,  2));
        userSkillRepository.save(new UserSkill(carol, guitar, SkillType.OFFERED, 3));

        List<MatchCandidateProjection> result =
                matchingRepository.findCandidatesWithScore(alice.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void excludesMe_fromResults() {
        // Alice offre Java et veut Guitar — elle ne doit pas s'apparaître elle-même
        userSkillRepository.save(new UserSkill(alice, java,   SkillType.OFFERED, 4));
        userSkillRepository.save(new UserSkill(alice, guitar, SkillType.WANTED,  1));
        userSkillRepository.save(new UserSkill(bob,   guitar, SkillType.OFFERED, 3));

        List<MatchCandidateProjection> result =
                matchingRepository.findCandidatesWithScore(alice.getId());

        assertThat(result).noneMatch(p -> "Alice".equals(p.getDisplayName()));
    }

    // ─── comptage de l'intersection ───────────────────────────────────────────

    @Test
    void countsTheyTeachMeCorrectly_whenCandidateOffersMultipleWantedSkills() {
        // Alice veut Java et Guitar — Bob offre les deux
        userSkillRepository.save(new UserSkill(alice, java,   SkillType.WANTED,  1));
        userSkillRepository.save(new UserSkill(alice, guitar, SkillType.WANTED,  1));
        userSkillRepository.save(new UserSkill(bob,   java,   SkillType.OFFERED, 4));
        userSkillRepository.save(new UserSkill(bob,   guitar, SkillType.OFFERED, 5));

        List<MatchCandidateProjection> result =
                matchingRepository.findCandidatesWithScore(alice.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTheyTeachMe()).isEqualTo(2L);
    }

    @Test
    void countsITeachThemCorrectly_whenIOfferWhatCandidateWants() {
        // Alice offre Java et Python — Bob veut les deux, offre Guitar qu'Alice veut
        userSkillRepository.save(new UserSkill(alice, java,   SkillType.OFFERED, 4));
        userSkillRepository.save(new UserSkill(alice, python, SkillType.OFFERED, 3));
        userSkillRepository.save(new UserSkill(alice, guitar, SkillType.WANTED,  1));
        userSkillRepository.save(new UserSkill(bob,   java,   SkillType.WANTED,  2));
        userSkillRepository.save(new UserSkill(bob,   python, SkillType.WANTED,  1));
        userSkillRepository.save(new UserSkill(bob,   guitar, SkillType.OFFERED, 5));

        List<MatchCandidateProjection> result =
                matchingRepository.findCandidatesWithScore(alice.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getITeachThem()).isEqualTo(2L);
    }

    @Test
    void theyTeachMeIsZero_whenCandidateWantsButDoesNotOffer() {
        // Bob veut Java (comme Alice) mais n'offre rien qu'Alice veuille
        // → Bob offre Guitar mais Alice ne veut pas Guitar
        userSkillRepository.save(new UserSkill(alice, java,   SkillType.WANTED,  2));
        userSkillRepository.save(new UserSkill(bob,   java,   SkillType.WANTED,  1));
        userSkillRepository.save(new UserSkill(bob,   guitar, SkillType.OFFERED, 3));

        List<MatchCandidateProjection> result =
                matchingRepository.findCandidatesWithScore(alice.getId());

        // Bob n'offre rien qu'Alice veut → HAVING filtre Bob → liste vide
        assertThat(result).isEmpty();
    }

    // ─── plusieurs candidats ──────────────────────────────────────────────────

    @Test
    void returnsMultipleCandidates_whenSeveralMatch() {
        // Alice veut Java
        // Bob offre Java (et veut Guitar qu'Alice offre)
        // Carol offre Java
        userSkillRepository.save(new UserSkill(alice, java,   SkillType.WANTED,  2));
        userSkillRepository.save(new UserSkill(alice, guitar, SkillType.OFFERED, 4));
        userSkillRepository.save(new UserSkill(bob,   java,   SkillType.OFFERED, 4));
        userSkillRepository.save(new UserSkill(bob,   guitar, SkillType.WANTED,  1));
        userSkillRepository.save(new UserSkill(carol, java,   SkillType.OFFERED, 3));

        List<MatchCandidateProjection> result =
                matchingRepository.findCandidatesWithScore(alice.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MatchCandidateProjection::getDisplayName)
                .containsExactlyInAnyOrder("Bob", "Carol");
    }

    @Test
    void iTeachThemIsZero_whenCandidateWantsNothingICanOffer() {
        // Alice veut Java et offre Guitar
        // Bob offre Java (il peut apprendre à Alice) mais ne veut pas Guitar
        userSkillRepository.save(new UserSkill(alice, java,   SkillType.WANTED,  2));
        userSkillRepository.save(new UserSkill(alice, guitar, SkillType.OFFERED, 4));
        userSkillRepository.save(new UserSkill(bob,   java,   SkillType.OFFERED, 4));
        userSkillRepository.save(new UserSkill(bob,   python, SkillType.WANTED,  1));

        List<MatchCandidateProjection> result =
                matchingRepository.findCandidatesWithScore(alice.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getITeachThem()).isEqualTo(0L);
    }
}
