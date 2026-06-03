package com.skillswap.skill;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    private UUID skillId;
    private Skill javaSkill;
    private Skill guitarSkill;

    @BeforeEach
    void setUp() {
        skillId = UUID.randomUUID();

        javaSkill = new Skill("Java", "Programming");
        javaSkill.setId(skillId);

        guitarSkill = new Skill("Guitar", "Music");
        guitarSkill.setId(UUID.randomUUID());
    }

    // ─── createSkill ──────────────────────────────────────────────────────────

    @Test
    void createSkill_shouldReturnSkillWithCorrectFields() {
        when(skillRepository.existsByName("Java")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(javaSkill);

        Skill result = skillService.createSkill("Java", "Programming");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Java");
        assertThat(result.getCategory()).isEqualTo("Programming");
    }

    @Test
    void createSkill_shouldThrowException_whenSkillAlreadyExists() {
        when(skillRepository.existsByName("Java")).thenReturn(true);

        assertThatThrownBy(() -> skillService.createSkill("Java", "Programming"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createSkill_shouldCallSaveOnRepository() {
        when(skillRepository.existsByName(any())).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(javaSkill);

        skillService.createSkill("Java", "Programming");

        verify(skillRepository, times(1)).save(any(Skill.class));
    }

    // ─── findAllSkills ────────────────────────────────────────────────────────

    @Test
    void findAllSkills_shouldReturnAllSkills() {
        when(skillRepository.findAll()).thenReturn(List.of(javaSkill, guitarSkill));

        List<Skill> result = skillService.findAllSkills();

        assertThat(result).hasSize(2);
        assertThat(result).contains(javaSkill, guitarSkill);
    }

    @Test
    void findAllSkills_shouldReturnEmptyList_whenNoSkillsExist() {
        when(skillRepository.findAll()).thenReturn(List.of());

        List<Skill> result = skillService.findAllSkills();

        assertThat(result).isEmpty();
    }

    // ─── findSkillById ────────────────────────────────────────────────────────

    @Test
    void findSkillById_shouldReturnSkill_whenExists() {
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(javaSkill));

        Optional<Skill> result = skillService.findSkillById(skillId);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Java");
    }

    @Test
    void findSkillById_shouldReturnEmpty_whenNotExists() {
        when(skillRepository.findById(skillId)).thenReturn(Optional.empty());

        Optional<Skill> result = skillService.findSkillById(skillId);

        assertThat(result).isEmpty();
    }

    // ─── findSkillsByCategory ─────────────────────────────────────────────────

    @Test
    void findSkillsByCategory_shouldReturnOnlySkillsInThatCategory() {
        when(skillRepository.findByCategory("Programming")).thenReturn(List.of(javaSkill));

        List<Skill> result = skillService.findSkillsByCategory("Programming");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Programming");
    }
}
