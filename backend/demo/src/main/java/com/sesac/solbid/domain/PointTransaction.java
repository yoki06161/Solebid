package com.sesac.solbid.domain;
import com.sesac.solbid.domain.enums.TransEnum;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_transaction")
public class PointTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "trans_enum")    // (선택) 컬럼명 명시
    private TransEnum transEnum;

    @Column(name = "balance_after", nullable = false, precision = 38, scale = 2) // ★ 정밀도 일치 +unique 충돌 위험, 제거
    private BigDecimal balanceAfter;

    @Column(length = 225)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY) // FK: payments.payment_id
    @JoinColumn(name = "payment_id")
    private Payments payments;

    @Column(name = "created_at", nullable = false) //unique 충돌 위험, 제거 + name = "created_at" 추가
    private LocalDateTime createdAt;

    @Column(name = "point", nullable = false)
    private int point;

    //충돌 방지
    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}