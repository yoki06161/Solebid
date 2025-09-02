package com.sesac.solbid.repository;

import com.sesac.solbid.domain.Payments;
import com.sesac.solbid.domain.enums.PaymentStatus;
import com.sesac.solbid.dto.response.PaymentRecordItemResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentsRepository extends JpaRepository<Payments, Long>, JpaSpecificationExecutor<Payments> {

    Optional<Payments> findByProviderAndOrderId(String provider, String orderId);

    //멱등키로 조회 + lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payments p where p.provider=:provider and p.orderId=:orderId")
    Optional<Payments> findByProviderAndOrderIdForUpdate(@Param("provider") String provider,
                                                         @Param("orderId") String orderId);
    //웹훅, 재시도 등 항상 같은 결제 식별, 멀티 인스턴스, 동시 요청 시 같은 키로 같은 로우 집기
    //동시성 레이스를 DB 수준에서 차단


    Optional<Payments> findByOrderId(String orderId);

    // 결제 내역 목록 조회 시 user 함께 로딩 (Lazy 예방)
    @Override
    @EntityGraph(attributePaths = {"user"})
    Page<Payments> findAll(Specification<Payments> spec, Pageable pageable);


    //포인트 전환건만 조회, 필터 활성 user 예약어 이슈 회피
    @Query(value = """
        SELECT new com.sesac.solbid.dto.response.PaymentRecordItemResponse(
            p.paymentId, u.userId, p.orderId, p.transactionId, p.amount,
            p.paymentMethod, p.provider, p.paymentStatus, p.charged,
            p.convertedPoint, p.requestedAt, p.confirmedAt
        )
        FROM Payments p
        JOIN p.user u
        WHERE u.userId = :userId
          AND (:status IS NULL OR p.paymentStatus = :status)
          AND (:fromAt IS NULL OR p.requestedAt >= :fromAt)
          AND (:toAt   IS NULL OR p.requestedAt <= :toAt)
          AND (:convertedOnly = FALSE OR (p.charged = TRUE OR p.convertedPoint > 0))
        """,
            countQuery = """
        SELECT COUNT(p)
        FROM Payments p
        JOIN p.user u
        WHERE u.userId = :userId
          AND (:status IS NULL OR p.paymentStatus = :status)
          AND (:fromAt IS NULL OR p.requestedAt >= :fromAt)
          AND (:toAt   IS NULL OR p.requestedAt <= :toAt)
          AND (:convertedOnly = FALSE OR (p.charged = TRUE OR p.convertedPoint > 0))
        """)
    Page<PaymentRecordItemResponse> findRecordsByUserId(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            @Param("fromAt") LocalDateTime fromAt,
            @Param("toAt") LocalDateTime toAt,
            @Param("convertedOnly") boolean convertedOnly,
            Pageable pageable
    );

}
