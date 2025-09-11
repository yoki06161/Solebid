package com.sesac.solbid.service.auth;

import com.sesac.solbid.domain.SocialLogin;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProviderType;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.SocialLoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialUnlinkService {

    private final SocialLoginRepository socialLoginRepository;

    @Value("${kakao.admin-key:}")
    private String kakaoAdminKey;

    @Value("${kakao.api-host:https://kapi.kakao.com}")
    private String kakaoApiHost;

    private static final String GOOGLE_REVOKE_URL = "https://oauth2.googleapis.com/revoke";

    private RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 주어진 사용자에 연결된 소셜 계정이 있으면 제공자별로 unlink를 시도하고, DB 연동 정보를 제거합니다.
     */
    @Transactional
    public Map<String, Object> unlinkAllForUser(User user) {
        Map<String, Object> result = new HashMap<>();
        Optional<SocialLogin> opt = socialLoginRepository.findByUser(user);
        if (opt.isEmpty()) {
            result.put("unlinked", false);
            result.put("provider", null);
            return result;
        }
        SocialLogin link = opt.get();
        ProviderType provider = link.getProvider();
        boolean remoteOk = true;
        if (provider == ProviderType.Kakao) {
            remoteOk = unlinkKakaoByProviderId(link.getProviderId());
        } else if (provider == ProviderType.Google) {
            remoteOk = revokeGoogle(link.getProviderRefreshToken(), link.getProviderAccessToken());
        }
        socialLoginRepository.delete(link);
        result.put("unlinked", true);
        result.put("provider", provider.name());
        result.put("remoteRevoked", remoteOk);
        return result;
    }

    /**
     * 요청된 provider 이름과 일치할 때만 unlink 수행. 일치하지 않으면 404로 처리합니다.
     */
    @Transactional
    public Map<String, Object> unlinkForUserAndProvider(User user, String providerName) {
        ProviderType requested = normalizeProvider(providerName);
        Optional<SocialLogin> opt = socialLoginRepository.findByUser(user);
        if (opt.isEmpty()) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }
        SocialLogin link = opt.get();
        if (link.getProvider() != requested) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }
        boolean remoteOk = true;
        if (requested == ProviderType.Kakao) {
            remoteOk = unlinkKakaoByProviderId(link.getProviderId());
        } else if (requested == ProviderType.Google) {
            remoteOk = revokeGoogle(link.getProviderRefreshToken(), link.getProviderAccessToken());
        }
        socialLoginRepository.delete(link);
        Map<String, Object> res = new HashMap<>();
        res.put("unlinked", true);
        res.put("provider", requested.name());
        res.put("remoteRevoked", remoteOk);
        if (requested == ProviderType.Google && !remoteOk) {
            res.put("manualRevokeHint", "Google 계정 보안 설정에서 앱 접근 권한을 철회하세요.");
        }
        return res;
    }

    private ProviderType normalizeProvider(String providerName) {
        String normalized = providerName.substring(0,1).toUpperCase() + providerName.substring(1).toLowerCase();
        return ProviderType.valueOf(normalized);
    }

    /** Kakao unlink API 호출 */
    public boolean unlinkKakaoByProviderId(String kakaoUserId) {
        if (kakaoAdminKey == null || kakaoAdminKey.isBlank()) {
            log.warn("kakao.admin-key 미설정: unlink를 건너뜁니다 (providerId={})", kakaoUserId);
            return false;
        }
        String url = kakaoApiHost + "/v1/user/unlink";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("target_id_type", "user_id");
        form.add("target_id", kakaoUserId);

        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
        try {
            ResponseEntity<String> resp = restTemplate().postForEntity(url, req, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                log.info("Kakao unlink 성공: providerId={}", kakaoUserId);
                return true;
            } else {
                log.warn("Kakao unlink 실패: status={}, body={}", resp.getStatusCode(), resp.getBody());
                return false;
            }
        } catch (RestClientException e) {
            log.error("Kakao unlink 호출 예외: providerId={}", kakaoUserId, e);
            return false;
        }
    }

    /** Google revoke API 호출 (refresh_token 우선, 없으면 access_token 사용) */
    public boolean revokeGoogle(String refreshToken, String accessToken) {
        String token = (refreshToken != null && !refreshToken.isBlank()) ? refreshToken : accessToken;
        if (token == null || token.isBlank()) {
            log.warn("Google revoke 불가: 저장된 토큰이 없습니다.");
            return false;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", token);
        HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
        try {
            ResponseEntity<String> resp = restTemplate().postForEntity(GOOGLE_REVOKE_URL, req, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                log.info("Google revoke 성공");
                return true;
            } else {
                log.warn("Google revoke 실패: status={}, body={}", resp.getStatusCode(), resp.getBody());
                return false;
            }
        } catch (RestClientException e) {
            log.error("Google revoke 호출 예외", e);
            return false;
        }
    }
}
