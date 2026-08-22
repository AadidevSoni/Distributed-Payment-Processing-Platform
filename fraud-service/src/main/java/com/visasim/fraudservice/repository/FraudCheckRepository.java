package com.visasim.fraudservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.visasim.fraudservice.model.FraudCheck;

public interface FraudCheckRepository extends JpaRepository<FraudCheck, UUID> {
}