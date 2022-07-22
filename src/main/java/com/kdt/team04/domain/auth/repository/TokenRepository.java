package com.kdt.team04.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kdt.team04.domain.auth.entity.Token;

public interface TokenRepository extends JpaRepository<Token, String> {
}
