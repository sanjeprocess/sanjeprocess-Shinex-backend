package com.hsb.hris.repository;

import com.hsb.hris.entity.LoginUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface LoginUserRepository extends JpaRepository<LoginUser, String> {
    @Query("select u from LoginUser u where trim(u.loginName) = trim(:loginName)")
    Optional<LoginUser> findByLoginName(@Param("loginName") String loginName);
}
