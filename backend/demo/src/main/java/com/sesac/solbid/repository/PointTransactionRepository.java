package com.sesac.solbid.repository;

import com.sesac.solbid.domain.PointTransaction;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointTransactionRepository {

    Optional<PointTransaction> findLatestByUserId(Long userId);
}
