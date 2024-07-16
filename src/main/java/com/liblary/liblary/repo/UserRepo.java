package com.liblary.liblary.repo;

import com.liblary.liblary.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<UserEntity, Long>
{
    UserEntity findByToken(String token);
}