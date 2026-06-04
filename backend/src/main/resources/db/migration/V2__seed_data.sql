-- ============================================================
-- V2: Seed data — fake users and skills for development/testing
-- ============================================================
-- These rows let you test the app immediately without having to
-- register via the API first. UUIDs are hardcoded so they're
-- always the same — you can paste them directly into Swagger.
--
-- ⚠️  PASSWORDS: stored as plaintext for now (Phase 2).
--     When Phase 3 (BCrypt) is done, run:
--         UPDATE users SET password = '<bcrypt_hash>' WHERE email = '...';
--     or create a V3__hash_passwords.sql migration.
-- ============================================================

-- ── Skills (global catalogue) ────────────────────────────────
INSERT INTO skills (id, name, category) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'Java',         'Programming'),
    ('b0000000-0000-0000-0000-000000000002', 'Guitar',       'Music'),
    ('b0000000-0000-0000-0000-000000000003', 'Spanish',      'Language'),
    ('b0000000-0000-0000-0000-000000000004', 'Python',       'Programming'),
    ('b0000000-0000-0000-0000-000000000005', 'Photography',  'Art');

-- ── Users ────────────────────────────────────────────────────
-- Tip: copy these UUIDs into Swagger "userId" fields to test endpoints
INSERT INTO users (id, email, password, display_name, bio, average_rating, created_at) VALUES
    ('a0000000-0000-0000-0000-000000000001',
     'alice@test.com', 'password123', 'Alice',
     'Senior Java dev looking to learn guitar in exchange for coding lessons.',
     4.5, NOW()),

    ('a0000000-0000-0000-0000-000000000002',
     'bob@test.com', 'password123', 'Bob',
     'Guitar teacher happy to trade music lessons for programming help.',
     3.8, NOW()),

    ('a0000000-0000-0000-0000-000000000003',
     'charlie@test.com', 'password123', 'Charlie',
     'Spanish native speaker learning Python for data science.',
     4.2, NOW());

-- ── User skills ──────────────────────────────────────────────
-- Alice: OFFERS Java (level 5), WANTS Guitar (level 1)
INSERT INTO user_skills (id, user_id, skill_id, type, level) VALUES
    ('c0000000-0000-0000-0000-000000000001',
     'a0000000-0000-0000-0000-000000000001',
     'b0000000-0000-0000-0000-000000000001', 'OFFERED', 5),
    ('c0000000-0000-0000-0000-000000000002',
     'a0000000-0000-0000-0000-000000000001',
     'b0000000-0000-0000-0000-000000000002', 'WANTED', 1);

-- Bob: OFFERS Guitar (level 4), WANTS Java (level 2)
-- → Alice and Bob are a perfect bilateral match!
INSERT INTO user_skills (id, user_id, skill_id, type, level) VALUES
    ('c0000000-0000-0000-0000-000000000003',
     'a0000000-0000-0000-0000-000000000002',
     'b0000000-0000-0000-0000-000000000002', 'OFFERED', 4),
    ('c0000000-0000-0000-0000-000000000004',
     'a0000000-0000-0000-0000-000000000002',
     'b0000000-0000-0000-0000-000000000001', 'WANTED', 2);

-- Charlie: OFFERS Spanish (level 5), WANTS Python (level 1)
INSERT INTO user_skills (id, user_id, skill_id, type, level) VALUES
    ('c0000000-0000-0000-0000-000000000005',
     'a0000000-0000-0000-0000-000000000003',
     'b0000000-0000-0000-0000-000000000003', 'OFFERED', 5),
    ('c0000000-0000-0000-0000-000000000006',
     'a0000000-0000-0000-0000-000000000003',
     'b0000000-0000-0000-0000-000000000004', 'WANTED', 1);
