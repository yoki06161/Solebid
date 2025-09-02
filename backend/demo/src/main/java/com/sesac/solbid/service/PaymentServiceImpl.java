package com.sesac.solbid.service;

import com.sesac.solbid.domain.Payments;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.PaymentStatus;
import com.sesac.solbid.dto.payment.request.PaymentPrepareRequest;
import com.sesac.solbid.dto.payment.response.PaymentPrepareResponse;
import com.sesac.solbid.dto.payment.response.PortOnePaymentResponse;
import com.sesac.solbid.repository.PaymentsRepository;
import com.sesac.solbid.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PortOneService portOneService; // 결제 승인 요청에 사용
    private final WebClient portOneWebClient;    // 사용 계획 없으면 제거 가능
    private final UserRepository userRepository;
    private final PaymentsRepository paymentsRepository;

    @Value("${portone.base-url}")
    private String portoneBaseUrl;

    @PersistenceContext //엔티티수정 없이
    private EntityManager em;


    /** 결제 준비 */
    @Override
    @Transactional
    public PaymentPrepareResponse preparePayment(PaymentPrepareRequest request) {
        String orderId = UUID.randomUUID().toString();
        String redirectUrl = request.getRedirectUrl();

        // TODO: 실제 로그인 연동되면 인증 사용자 사용
        User dummyUser = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("테스트용 유저가 없습니다."));

        log.info("[결제 준비] orderId={}, amount={}, method={}, redirectUrl={}",
                orderId, request.getAmount(), request.getPaymentMethod(), redirectUrl);

        Payments payment = Payments.builder()
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .orderId(orderId)
                .paymentStatus(PaymentStatus.WAITING)
                .convertedPoint(0)
                .charged(false)
                .user(dummyUser)
                .build();

        paymentsRepository.save(payment);

        log.info("[결제 준비 완료] orderId={} → redirectUrl={}", orderId, redirectUrl);
        return new PaymentPrepareResponse(orderId, redirectUrl);
    }

    /** 결제 승인(성공) */
    @Override
    //@Transactional
    @org.springframework.transaction.annotation.Transactional
    public String handlePaymentSuccess(String impUid, String accessToken) {
        log.info("[결제 승인 요청] impUid={}", impUid);

        // PortOne 승인
        PortOnePaymentResponse.PaymentData paymentData =
                portOneService.approvePayment(impUid, accessToken);

        int amount = paymentData.getAmount();
        String orderId = paymentData.getOrderId();
        log.debug("[결제 승인 데이터] orderId={}, impUid={}, amount={}", orderId, impUid, amount);

        //결제 엔티티 조회
        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("해당 주문이 없습니다. orderId=" + orderId));

        //중복 승인 차단
        if (payment.isCharged()) {
            log.warn("[중복 승인 차단] orderId={}", orderId);
            return "이미 처리된 결제입니다.";
        }

        //승인 금액 검증
        if (payment.getAmount() != amount) { // amount 타입에 맞춰 비교 (int/BigDecimal)
            throw new IllegalStateException("결제 금액 불일치: expected=" + payment.getAmount() + ", actual=" + amount);
        }
        //승인 데이터 디버깅
        log.debug("[결제 승인 데이터] orderId={}, impUid={}, amount={}", orderId, impUid, amount);


        //결제 상태 반영
        payment.approve(impUid, amount);
        //결제 상태 반영 디버깅
        log.debug("[Payment 엔티티 상태] id={}, charged={}, status={}, userId={}",
                payment.getPaymentId(), payment.isCharged(), payment.getPaymentStatus(), payment.getUser().getUserId());


        // 결제 변경분 flush -> 안정화
        em.flush();

        //사용자 포인트 적립
        // user point 증분(user 엔티티 수정 없는 버전-JPQL)
        Long userId = payment.getUser().getUserId();

        // BEFORE
        BigDecimal pointBefore = em.createQuery(
                        "select u.point from User u where u.userId = :id", BigDecimal.class)
                .setParameter("id", userId)
                .getSingleResult();
        log.info("[포인트 BEFORE] userId={}, point={}", userId, pointBefore);

        // UPDATE (NULL 안전: COALESCE)
        int rows = em.createQuery(
                        "update User u set u.point = COALESCE(u.point,0) + :delta where u.userId = :id")
                .setParameter("delta", BigDecimal.valueOf(amount))
                .setParameter("id", userId)
                .executeUpdate();
        log.info("[포인트 UPDATE jpql] updatedRows={}", rows);

        if (rows != 1) {
            log.error("[포인트 UPDATE 실패] userId={}, rows={}", userId, rows);
            throw new IllegalStateException("포인트 업데이트 실패 userId=" + userId);
        }

        // AFTER (벌크업데이트는 1차캐시 우회 → 동기화 후 확인)
        em.clear();
        BigDecimal pointAfter = em.createQuery(
                        "select u.point from User u where u.userId = :id", BigDecimal.class)
                .setParameter("id", userId)
                .getSingleResult();
        log.info("[포인트 AFTER]  userId={}, before={}, delta={}, after={}",
                userId, pointBefore, amount, pointAfter);

        log.info("[포인트 전환 완료] userId={}, amount={}, orderId={}", userId, amount, orderId);
        return "결제 승인 및 포인트 적립 완료";
    }
}
