package com.skillswap.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

// Spring Data JPA generates the SQL automatically from the method names!
// No need to write any SQL yourself for basic operations.
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Spring reads "findByEmail" and generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Spring reads "existsByEmail" and generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);

}
