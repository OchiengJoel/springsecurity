package com.joe.springsecurity.auth.repo;

import com.joe.springsecurity.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//    User findByName(String username);
    Optional<User> findByUsername(String username);
}
