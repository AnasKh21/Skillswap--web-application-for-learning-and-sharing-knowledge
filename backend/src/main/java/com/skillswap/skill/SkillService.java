package com.skillswap.skill;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Transactional
    public Skill createSkill(String name, String category) {
        if (skillRepository.existsByName(name)) {
            throw new IllegalArgumentException("Skill already exists: " + name);
        }
        return skillRepository.save(new Skill(name, category));
    }

    public List<Skill> findAllSkills() {
        return skillRepository.findAll();
    }

    public Optional<Skill> findSkillById(UUID id) {
        return skillRepository.findById(id);
    }

    public List<Skill> findSkillsByCategory(String category) {
        return skillRepository.findByCategory(category);
    }
}
