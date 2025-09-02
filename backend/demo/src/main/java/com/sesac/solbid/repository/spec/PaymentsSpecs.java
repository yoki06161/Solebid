package com.sesac.solbid.repository.spec;

import com.sesac.solbid.domain.Payments;
import com.sesac.solbid.domain.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class PaymentsSpecs {
    private PaymentsSpecs() {}

    public static Specification<Payments> userIdEq(Long userId) {
        return (root, q, cb) -> cb.equal(root.get("user").get("userId"), userId);
    }

    public static Specification<Payments> statusEq(PaymentStatus status) {
        return (root, q, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("paymentStatus"), status);
    }

    public static Specification<Payments> requestedAtBetween(LocalDateTime from, LocalDateTime to) {
        return (root, q, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.get("requestedAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("requestedAt"), from);
            return cb.lessThanOrEqualTo(root.get("requestedAt"), to);
        };
    }
}