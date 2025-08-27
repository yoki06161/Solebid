package com.sesac.solbid.repository;

import com.sesac.solbid.domain.Payments;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<Payments, Long> {

    Optional<Payments> findByProviderAndOrderId(String provider, String orderId);

    //멱등키로 조회 + lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payments p where p.provider=:provider and p.orderId=:orderId")
    Optional<Payments> findByProviderAndOrderIdForUpdate(@Param("provider") String provider,
                                                         @Param("orderId") String orderId);
    //웹훅, 재시도 등 항상 같은 결제 식별, 멀티 인스턴스, 동시 요청 시 같은 키로 같은 로우 집기
    //동시성 레이스를 DB 수준에서 차단


    Optional<Payments> findByOrderId(String orderId);

}
