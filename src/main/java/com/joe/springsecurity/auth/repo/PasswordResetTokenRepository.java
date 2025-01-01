package com.joe.springsecurity.auth.repo;

import com.joe.springsecurity.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // This method will allow you to find a PasswordResetToken by its token value
    Optional<PasswordResetToken> findByToken(String token);

}
