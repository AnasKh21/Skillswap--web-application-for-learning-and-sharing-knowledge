package com.skillswap.skill;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    // Find all skills in a category (e.g. all "Programming" skills)
    List<Skill> findByCategory(String category);

    // Find a skill by its exact name (e.g. "Java")
    Optional<Skill> findByName(String name);

    // Check if a skill with this name already exists (to avoid duplicates)
    boolean existsByName(String name);
}
