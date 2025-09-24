package com.sesac.solbid.repository;

import com.sesac.solbid.domain.PointTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointTransactionRepository  extends JpaRepository<PointTransaction, Long> {
}
