package com.duong.salesmanagement.repository;

import com.duong.salesmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    java.util.List<User> findByRole(com.duong.salesmanagement.model.Role role);
    java.util.List<User> findByRoleNot(com.duong.salesmanagement.model.Role role);
}
