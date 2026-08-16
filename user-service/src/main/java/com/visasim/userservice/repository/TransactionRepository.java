package com.visasim.userservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.visasim.userservice.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
 boolean existsByFromWalletIdOrToWalletId(UUID fromWalletId, UUID toWalletId);
}