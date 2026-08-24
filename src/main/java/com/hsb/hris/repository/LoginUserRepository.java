package com.hsb.hris.repository;

import com.hsb.hris.entity.LoginUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginUserRepository extends JpaRepository<LoginUser, String> {
    Optional<LoginUser> findByLoginName(String loginName);
}
