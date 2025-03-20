package com.joe.springsecurity.auth.config;


import com.joe.springsecurity.auth.repo.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Date;

@Component
@EnableScheduling
public class TokenCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupTask.class);
    private final TokenRepository tokenRepository;

    public TokenCleanupTask(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // Daily cleanup
    @Transactional
    public void cleanupExpiredTokens() {
        logger.info("Starting token cleanup task...");
        Date currentDate = new Date();
        tokenRepository.deleteExpiredOrLoggedOutTokens(currentDate);
        logger.info("Token cleanup completed.");
    }
}
