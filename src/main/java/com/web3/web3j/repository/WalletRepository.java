package com.web3.web3j.repository;

import com.web3.web3j.model.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {
    List<WalletEntity> findAllByUserId(Long userId);
    Optional<WalletEntity> findByAddress(String address);
    List<WalletEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
