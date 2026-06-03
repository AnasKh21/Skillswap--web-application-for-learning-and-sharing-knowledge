package com.skillswap.user;

import com.skillswap.common.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {

    // Get all skills for a given user
    List<UserSkill> findByUserId(UUID userId);

    // Get only the skills a user offers, or only the ones they want
    List<UserSkill> findByUserIdAndType(UUID userId, SkillType type);

    // Check if a user already has a specific skill registered
    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);
}
