package com.visasim.userservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.visasim.userservice.model.FraudCheck;

public interface FraudCheckRepository extends JpaRepository<FraudCheck, UUID> {
}