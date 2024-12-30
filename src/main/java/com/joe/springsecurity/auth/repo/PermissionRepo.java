package com.joe.springsecurity.auth.repo;

import com.joe.springsecurity.auth.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepo extends JpaRepository<Permission, Long> {

    Permission findByName(String name);
}
