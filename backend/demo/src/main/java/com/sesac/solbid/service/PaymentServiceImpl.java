package com.sesac.solbid.service;


import com.sesac.solbid.domain.Payments;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.PaymentStatus;
import com.sesac.solbid.dto.request.PaymentPrepareRequest;
import com.sesac.solbid.dto.response.PaymentPrepareResponse;
import com.sesac.solbid.repository.PaymentsRepository;
import com.sesac.solbid.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PortOneService portOneService; // 추후 결제 승인 시 사용 예정
    private final WebClient portOneWebClient;
    private final UserRepository userRepository;
    private final PaymentsRepository paymentsRepository;

    @Value("${portone.base-url}")
    private String portOneBaseUrl;

    /**
     * 결제 준비 요청 처리
     * - orderId 생성
     * - 결제 정보 DB 저장
     * - 클라이언트에 orderId & redirectUrl 반환
     */
    @Override
    @Transactional
    public PaymentPrepareResponse preparePayment(PaymentPrepareRequest request) {
        String orderId = UUID.randomUUID().toString();
        String redirectUrl = request.getRedirectUrl(); // 향후 변경 가능

        // 더미 유저 (로그인 미구현)
        User dummyUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("테스트용 유저가 없습니다."));

        // debug
        System.out.println("==== DEBUG ====");
        System.out.println("amount = " + request.getAmount());
        System.out.println("method = " + request.getPaymentMethod());
        System.out.println("redirectUrl = " + redirectUrl);
        System.out.println("==== DEBUG ====");


        log.info("[결제 준비] orderId={}, amount={}, method={}, redirectUrl={}",
                orderId,
                String.valueOf(request.getAmount()),
                String.valueOf(request.getPaymentMethod()),
                String.valueOf(redirectUrl)
        );

        // 결제 정보 생성 및 저장
        Payments payment = new Payments();
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setOrderId(orderId);
        payment.setPaymentStatus(PaymentStatus.WAITING);
        payment.setConvertedPoint(0);
        payment.setIsCharged(false);
        payment.setUser(dummyUser);
        payment.setRequestedAt(LocalDateTime.now());


        paymentsRepository.save(payment);

        log.info("[결제 준비 완료] orderId={} → redirectUrl={}", orderId, redirectUrl);

        return new PaymentPrepareResponse(orderId, redirectUrl);




    }

    /*결제 승인*/
    @Override
    @Transactional
    public String handlePaymentSuccess(String impUid, String accessToken) {
        log.info("[결제 승인 요청] impUid={}", impUid);


        //PortOnePaymentResponse paymentResponse = portOneService.approvePayment(impUid, accessToken);
        PortOnePaymentResponse.PaymentData paymentData = portOneService.approvePayment(impUid, accessToken);


        int amount = paymentData.getAmount();

        Payments payment = paymentsRepository.findByOrderId(paymentData.getOrderId())
                .orElseThrow(() -> new RuntimeException("해당 주문이 없습니다."));

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(impUid);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setIsCharged(true);
        payment.setConvertedPoint(amount);

        User user = payment.getUser();
        user.setPoint(user.getPoint().add(BigDecimal.valueOf(amount)));

        log.info("[포인트 전환 완료] userId={}, amount={}", user.getUserId(), amount);

        return "결제 승인 및 포인트 적립 완료";
    }



}
