-- V4 : supprime les utilisateurs du seed V2 (mots de passe en clair)
-- Les vrais comptes se créent via POST /api/auth/register (BCrypt)

DELETE FROM user_skills  WHERE user_id IN (
    'a0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000003'
);

DELETE FROM users WHERE id IN (
    'a0000000-0000-0000-0000-000000000001',
    'a0000000-0000-0000-0000-000000000002',
    'a0000000-0000-0000-0000-000000000003'
);
