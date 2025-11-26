package org.thepaddlers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.thepaddlers.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
