package com.sesac.solbid.service;


import com.sesac.solbid.dto.request.PortOneTokenRequest;
import com.sesac.solbid.dto.response.PortOnePaymentResponse;
import com.sesac.solbid.dto.response.PortOneTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneServiceImpl implements PortOneService {

    private final WebClient portOneWebClient;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.api-secret}")
    private String apiSecret;

    /* 결제 승인 토큰 요청*/
    @Override
    public String getAccessToken() {
        log.info("[PortOne] 액세스 토큰 요청 시작");

        // 요청 DTO 생성
        PortOneTokenRequest requestDto = new PortOneTokenRequest(apiKey, apiSecret);

        // 요청 수행
        PortOneTokenResponse responseDto = portOneWebClient.post()
                .uri("/users/getToken") // baseUrl은 WebClient 설정에 들어감 (application.properties)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(PortOneTokenResponse.class)
                .doOnError(e -> log.error("[PortOne] 토큰 요청 실패: {}", e.getMessage()))
                .block(); // 동기 방식으로 대기 (주의)

        // 유효성 검사
        if (responseDto == null || responseDto.getResponse() == null
                || responseDto.getResponse().getAccessToken() == null) {
            throw new IllegalStateException("PortOne 토큰 응답이 비어있습니다.");
        }

        // 결과 반환
        String token = responseDto.getResponse().getAccessToken();
        log.info("[PortOne] 액세스 토큰 발급 성공: {}", token);
        return token;
    }


    /* 결제 승인 테스트 1 : 토큰 기반
    @Override
    public String approvePayment(String impUid, String accessToken) {
        log.info("[PortOne] 결제 승인 조회 요청 시작, imp_uid={}", impUid);

        PortOneApproveResponse response = portOneWebClient.get()
                .uri("/payments/" + impUid)
                .header("Authorization", accessToken)
                .retrieve()
                .bodyToMono(PortOneApproveResponse.class)
                .doOnError(e -> log.error("[PortOne] 결제 조회 실패: {}", e.getMessage()))
                .block();

        if (response == null || response.getResponse() == null) {
            throw new RuntimeException("결제 승인 응답이 비어 있습니다.");
        }

        PortOneApproveResponse.ApproveData data = response.getResponse();
        log.info("[PortOne] 결제 승인 완료 - orderId={}, amount={}, status={}", data.getOrderId(), data.getAmount(), data.getStatus());

        return data.getStatus(); // 필요시 DB 업데이트도 함께 수행 가능
    }

    */

    // 결제 승인 테스트 2 :imp_uid 기반
    @Override
    public PortOnePaymentResponse.PaymentData approvePayment(String impUid, String accessToken) {
        log.info("[PortOne] 결제 상태 조회 요청: impUid={}", impUid);

        PortOnePaymentResponse fullResponse = portOneWebClient.get()
                .uri("/payments/" + impUid)
                .header("Authorization", accessToken)
                .retrieve()
                .bodyToMono(PortOnePaymentResponse.class)
                .block();

        if (fullResponse == null) {
            log.error("[PortOne] 결제 응답이 null입니다. (impUid={})", impUid);
            throw new IllegalStateException("결제 조회 응답이 null입니다.");
        }

        PortOnePaymentResponse.PaymentData data = fullResponse.getResponse();

        if (data == null) {
            log.error("[PortOne] 결제 상세 응답이 비어있습니다. (impUid={})", impUid);
            throw new IllegalStateException("결제 상세 데이터가 없습니다.");
        }

        // 결제 상태 확인
        String status = data.getStatus();
        if (!"paid".equals(status)) {
            log.warn("[PortOne] 결제가 완료되지 않았습니다. 상태: {}, impUid={}", status, impUid);
            throw new IllegalStateException("결제가 완료되지 않았습니다. 상태: " + status);
        }

        log.info("[PortOne] 결제 승인 성공: orderId={}, amount={}, payMethod={}",
                data.getOrderId(), data.getAmount(), data.getPayMethod());

        return data;
    }





}
