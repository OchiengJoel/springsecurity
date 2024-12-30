package com.joe.springsecurity.init;

import org.springframework.stereotype.Service;

@Service
public class InitialDataService {

    private final CompanyRepository companyRepository;

    public InitialDataService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @PostConstruct
    public void initDefaultCompanies() {
        if (companyRepository.count() == 0) {
            Company company1 = new Company();
            company1.setName("Company A");
            companyRepository.save(company1);

            Company company2 = new Company();
            company2.setName("Company B");
            companyRepository.save(company2);

            System.out.println("Default companies added: Company A and Company B");
        } else {
            System.out.println("Companies already exist in the database.");
        }
    }
}
