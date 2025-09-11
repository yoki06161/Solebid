package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserStatus;
import com.sesac.solbid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawnUserPurgeScheduler {

    private static final long GRACE_DAYS = 30L;

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    // 매일 03:00에 실행 (서버 로컬 시간 기준)
    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void purgeExpiredWithdrawnUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(GRACE_DAYS);
        List<User> targets = em.createQuery(
                "select u from User u where u.userStatus = :st and u.withdrawnAt < :cutoff",
                User.class
        ).setParameter("st", UserStatus.WITHDRAWN)
         .setParameter("cutoff", cutoff)
         .setMaxResults(500) // 1회 최대 500명 처리
         .getResultList();

        if (targets.isEmpty()) {
            log.debug("[Purge] 대상 없음: 기준={}일 전", GRACE_DAYS);
            return;
        }

        AtomicInteger deleted = new AtomicInteger(0);
        AtomicInteger anonymized = new AtomicInteger(0);

        for (User user : targets) {
            try {
                boolean hasReferences = hasStrongReferences(user);

                // 공통: 약한 연관(세션/알림/위시/카트/소셜링크)은 먼저 정리
                deleteSoftLinks(user);

                if (!hasReferences) {
                    // 안전 삭제 경로
                    userRepository.delete(user);
                    deleted.incrementAndGet();
                } else {
                    // 삭제 불가: 결제/주문/입찰/상품 등 강한 참조 존재 → PII 익명화
                    anonymizeUser(user);
                    anonymized.incrementAndGet();
                }
            } catch (Exception e) {
                log.warn("[Purge] 사용자 정리 실패 userId={}", user.getUserId(), e);
            }
        }

        log.info("[Purge] 만료 탈퇴 계정 정리 완료 - 삭제: {}, 익명화: {}, 대상: {}", deleted.get(), anonymized.get(), targets.size());
    }

    private boolean hasStrongReferences(User user) {
        Long orderCnt = em.createQuery(
                "select count(o) from OrderInfo o where o.seller = :u or o.winner = :u",
                Long.class
        ).setParameter("u", user).getSingleResult();

        Long productCnt = em.createQuery(
                "select count(p) from Product p where p.seller = :u",
                Long.class
        ).setParameter("u", user).getSingleResult();

        Long bidCnt = em.createQuery(
                "select count(b) from Bid b where b.bidder = :u",
                Long.class
        ).setParameter("u", user).getSingleResult();

        Long payCnt = em.createQuery(
                "select count(p) from Payments p where p.user = :u",
                Long.class
        ).setParameter("u", user).getSingleResult();

        Long ptCnt = em.createQuery(
                "select count(t) from PointTransaction t where t.user = :u",
                Long.class
        ).setParameter("u", user).getSingleResult();

        return (orderCnt + productCnt + bidCnt + payCnt + ptCnt) > 0;
    }

    private void deleteSoftLinks(User user) {
        // SocialLogin 제거
        em.createQuery("delete from SocialLogin s where s.user = :u")
                .setParameter("u", user)
                .executeUpdate();
        // WishList 제거
        em.createQuery("delete from WishList w where w.user = :u")
                .setParameter("u", user)
                .executeUpdate();
        // Carts 제거
        em.createQuery("delete from Carts c where c.user = :u")
                .setParameter("u", user)
                .executeUpdate();
        // Notification 제거
        em.createQuery("delete from Notification n where n.user = :u")
                .setParameter("u", user)
                .executeUpdate();
    }

    private void anonymizeUser(User user) {
        // 개인정보 익명화 (unique 제약 보호)
        String marker = "deleted_" + user.getUserId() + "_" + System.currentTimeMillis();
        setField(user, "email", marker + "@deleted.local");
        setField(user, "nickname", "deleted_user_" + user.getUserId());
        setField(user, "name", null);
        setField(user, "phone", null);
        // 상태/탈퇴일 유지하여 추적성 보장
        em.flush(); // 즉시 반영
    }

    private void setField(User user, String field, Object value) {
        try {
            Field f = User.class.getDeclaredField(field);
            f.setAccessible(true);
            f.set(user, value);
        } catch (Exception ignored) { }
    }
}

