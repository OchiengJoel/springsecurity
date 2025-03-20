package com.joe.springsecurity.auth.repo;

import com.joe.springsecurity.auth.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("select t from Token t inner join User u on t.user.id = u.id where t.user.id = :userId and t.loggedOut = false")
    List<Token> findAllAccessTokensByUser(Long userId);

    Optional<Token> findByAccessToken(String token);

    Optional<Token > findByRefreshToken(String token);

    // Add this method to delete expired or logged-out tokens
    @Modifying
    @Transactional
    @Query("delete from Token t where t.loggedOut = true or t.refreshToken is null or t.expirationDate < :currentDate")
    void deleteExpiredOrLoggedOutTokens(@Param("currentDate") Date currentDate);

}
