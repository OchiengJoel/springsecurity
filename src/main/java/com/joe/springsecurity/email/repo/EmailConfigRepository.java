package com.joe.springsecurity.email.repo;

import com.joe.springsecurity.company.model.Company;
import com.joe.springsecurity.email.model.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailConfigRepository extends JpaRepository<EmailConfig, Long> {

    EmailConfig findByCompanyId(Long companyId);

    Optional<EmailConfig> findByCompany(Company company);
}
