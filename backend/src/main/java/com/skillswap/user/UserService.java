package com.skillswap.user;

import com.skillswap.common.SkillType;
import com.skillswap.skill.Skill;
import com.skillswap.skill.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final SkillRepository skillRepository;

    public UserService(UserRepository userRepository,
                       UserSkillRepository userSkillRepository,
                       SkillRepository skillRepository) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.skillRepository = skillRepository;
    }

    // Password is stored in plaintext for now; Spring Security + BCrypt encoding comes later.
    @Transactional
    public User createUser(String email, String rawPassword, String displayName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User(email, rawPassword, displayName);
        user.setAverageRating(0.0);
        return userRepository.save(user);
    }

    public Optional<User> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateProfile(UUID userId, String newDisplayName, String newBio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setDisplayName(newDisplayName);
        user.setBio(newBio);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.deleteById(userId);
    }

    // Duplicate check is intentionally skill-level (not per SkillType), so a user
    // cannot register the same skill twice even under different roles (TEACH/LEARN).
    @Transactional
    public UserSkill addSkillToUser(UUID userId, UUID skillId, SkillType type, int level) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        if (userSkillRepository.existsByUserIdAndSkillId(userId, skillId)) {
            throw new IllegalArgumentException("User already has this skill");
        }
        return userSkillRepository.save(new UserSkill(user, skill, type, level));
    }

    @Transactional
    public void removeSkillFromUser(UUID userSkillId) {
        if (!userSkillRepository.existsById(userSkillId)) {
            throw new IllegalArgumentException("UserSkill not found");
        }
        userSkillRepository.deleteById(userSkillId);
    }

    public List<UserSkill> getSkillsForUser(UUID userId) {
        return userSkillRepository.findByUserId(userId);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
