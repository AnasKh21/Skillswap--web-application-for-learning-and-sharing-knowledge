package com.skillswap.user;

import com.skillswap.common.SkillType;
import com.skillswap.skill.Skill;
import com.skillswap.skill.SkillRepository;
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

// @ExtendWith(MockitoExtension.class) activates Mockito for this test class
// It creates fake (mocked) versions of the repositories so we don't need a real database
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // @Mock creates a fake repository — all its methods return empty/null by default
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSkillRepository userSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    // @InjectMocks creates a real UserService and injects the mocks above into it
    @InjectMocks
    private UserService userService;

    private UUID userId;
    private UUID skillId;
    private User sampleUser;
    private Skill sampleSkill;

    // @BeforeEach runs before every single test method to set up fresh data
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        skillId = UUID.randomUUID();

        sampleUser = new User("alice@example.com", "encoded_password", "Alice");
        sampleUser.setId(userId);
        sampleUser.setAverageRating(0.0);

        sampleSkill = new Skill("Java", "Programming");
        sampleSkill.setId(skillId);
    }

    // ─── createUser ───────────────────────────────────────────────────────────

    @Test
    void createUser_shouldReturnUserWithCorrectEmail() {
        // Arrange: tell the mock what to return when called
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        // Act: call the method under test
        User result = userService.createUser("alice@example.com", "password123", "Alice");

        // Assert: check the result
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getDisplayName()).isEqualTo("Alice");
    }

    @Test
    void createUser_shouldCallSaveOnRepository() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        userService.createUser("alice@example.com", "password123", "Alice");

        // verify that save() was called exactly once
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        // Arrange: simulate an email that is already taken
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        // Assert: expect an exception when trying to create with the same email
        assertThatThrownBy(() -> userService.createUser("alice@example.com", "password", "Alice"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── findUserById ─────────────────────────────────────────────────────────

    @Test
    void findUserById_shouldReturnUser_whenExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findUserById(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
    }

    @Test
    void findUserById_shouldReturnEmpty_whenNotExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserById(userId);

        assertThat(result).isEmpty();
    }

    // ─── findUserByEmail ──────────────────────────────────────────────────────

    @Test
    void findUserByEmail_shouldReturnUser_whenExists() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));

        Optional<User> result = userService.findUserByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@example.com");
    }

    // ─── updateProfile ────────────────────────────────────────────────────────

    @Test
    void updateProfile_shouldUpdateDisplayNameAndBio() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateProfile(userId, "Alice Updated", "New bio");

        assertThat(result.getDisplayName()).isEqualTo("Alice Updated");
        assertThat(result.getBio()).isEqualTo("New bio");
    }

    @Test
    void updateProfile_shouldThrowException_whenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(userId, "Alice", "Bio"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── addSkillToUser ───────────────────────────────────────────────────────

    @Test
    void addSkillToUser_shouldReturnUserSkillWithCorrectFields() {
        UserSkill savedUserSkill = new UserSkill(sampleUser, sampleSkill, SkillType.OFFERED, 3);
        savedUserSkill.setId(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(sampleSkill));
        when(userSkillRepository.existsByUserIdAndSkillId(userId, skillId)).thenReturn(false);
        when(userSkillRepository.save(any(UserSkill.class))).thenReturn(savedUserSkill);

        UserSkill result = userService.addSkillToUser(userId, skillId, SkillType.OFFERED, 3);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(SkillType.OFFERED);
        assertThat(result.getLevel()).isEqualTo(3);
    }

    // ─── getSkillsForUser ─────────────────────────────────────────────────────

    @Test
    void getSkillsForUser_shouldReturnAllSkillsForUser() {
        UserSkill userSkill1 = new UserSkill(sampleUser, sampleSkill, SkillType.OFFERED, 4);
        UserSkill userSkill2 = new UserSkill(sampleUser, sampleSkill, SkillType.WANTED, 1);

        when(userSkillRepository.findByUserId(userId)).thenReturn(List.of(userSkill1, userSkill2));

        List<UserSkill> result = userService.getSkillsForUser(userId);

        assertThat(result).hasSize(2);
    }
}
