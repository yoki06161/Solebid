// src/main/java/com/sesac/solbid/service/UserService.java

package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.SocialLogin;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProviderType;
import com.sesac.solbid.domain.enums.UserStatus;
import com.sesac.solbid.dto.user.request.SignupRequest;
import com.sesac.solbid.dto.user.request.LoginRequest;
import com.sesac.solbid.dto.user.response.LoginResponse;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.exception.OAuth2Exception;
import com.sesac.solbid.exception.ReactivationRequiredException;
import com.sesac.solbid.repository.SocialLoginRepository;
import com.sesac.solbid.repository.UserRepository;
import com.sesac.solbid.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final long WITHDRAWAL_GRACE_DAYS = 30L;

    @Transactional
    public User signup(SignupRequest requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.findByNickname(requestDto.getNickname()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        User user = requestDto.toEntity(encodedPassword);
        return userRepository.save(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest requestDto) {
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            if (isWithinGrace(user)) {
                throw new ReactivationRequiredException(user.getEmail());
            } else {
                throw new CustomException(ErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED);
            }
        }

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new CustomException(ErrorCode.INACTIVE_USER);
        }

        final String accessToken = jwtUtil.generateToken(user.getEmail());
        final String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return LoginResponse.from(user, accessToken, refreshToken);
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));
    }

    // 이메일로 사용자 조회 (현재 사용자 조회용)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));
    }

    // 닉네임 가용성 확인
    public boolean isNicknameAvailable(String nickname) {
        if (nickname == null || nickname.isBlank()) return false;
        if (nickname.length() < 2 || nickname.length() > 10) return false;
        // 임시 접두 사용 방지 권장
        if (nickname.startsWith("user_")) return false;
        return userRepository.findByNickname(nickname).isEmpty();
    }

    // 이메일 기준으로 닉네임 변경
    @Transactional
    public User updateNicknameForEmail(String email, String newNickname) {
        if (!isNicknameAvailable(newNickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));
        user.updateNickname(newNickname);
        return user;
    }

    @Transactional
    public User saveOrUpdate(String providerName, Map<String, Object> userAttributes) {
        // Provider 이름을 적절한 형태로 변환 (첫 글자만 대문자)
        String normalizedProviderName = providerName.substring(0, 1).toUpperCase() + providerName.substring(1).toLowerCase();
        ProviderType provider = ProviderType.valueOf(normalizedProviderName);
        
        String providerId = getProviderId(provider, userAttributes);
        String email = getEmail(provider, userAttributes);
        // provider가 제공하는 이름은 name 으로만 사용하고, nickname 은 사용자가 직접 설정하도록 유도
        String displayName = getDisplayName(provider, userAttributes);

        Optional<SocialLogin> socialLoginOptional = socialLoginRepository.findByProviderAndProviderId(provider, providerId);

        User user;
        if (socialLoginOptional.isPresent()) {
            // 기존 소셜 로그인 사용자: 닉네임은 자동 동기화하지 않음
            user = socialLoginOptional.get().getUser();
            // 상태 체크: ACTIVE/BLOCKED/WITHDRAWN 분기
            if (user.getUserStatus() == UserStatus.WITHDRAWN) {
                if (isWithinGrace(user)) {
                    throw new ReactivationRequiredException(user.getEmail());
                } else {
                    throw new CustomException(ErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED);
                }
            }
            if (user.getUserStatus() != UserStatus.ACTIVE) {
                throw new CustomException(ErrorCode.INACTIVE_USER);
            }
            // 사용자의 name 은 비어있을 때만 보수적으로 채움
            if (user.getName() == null || user.getName().isBlank()) {
                if (displayName != null && !displayName.isBlank()) {
                    // 엔티티에 name setter가 없으므로 신규 생성 외에는 유지
                }
            }
        } else {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                user = userOptional.get();
                // 상태 체크: ACTIVE/BLOCKED/WITHDRAWN 분기
                if (user.getUserStatus() == UserStatus.WITHDRAWN) {
                    if (isWithinGrace(user)) {
                        throw new ReactivationRequiredException(user.getEmail());
                    } else {
                        throw new CustomException(ErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED);
                    }
                }
                if (user.getUserStatus() != UserStatus.ACTIVE) {
                    throw new CustomException(ErrorCode.INACTIVE_USER);
                }
                // 다른 소셜 계정으로 이미 연결된 경우 충돌 처리
                Optional<SocialLogin> existingLogin = socialLoginRepository.findByUser(user);
                if (existingLogin.isPresent() && existingLogin.get().getProvider() != provider) {
                    throw new OAuth2Exception(ErrorCode.SOCIAL_ACCOUNT_CONFLICT);
                }
                // 동일 사용자에 새로운 소셜 계정 연결 (사용자 프로필은 변경하지 않음)
                SocialLogin socialLogin = SocialLogin.builder()
                        .user(user)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                socialLoginRepository.save(socialLogin);
            } else {
                // 새로운 유저 생성 시, provider가 준 display name을 name 컬럼에 저장
                // nickname 은 임시값(고유)으로 생성하여 저장하고, 이후 사용자 입력으로 변경하도록 유도
                String tempNickname = generateTemporaryNickname();
                user = User.builder()
                        .email(email)
                        .password(null)
                        .nickname(tempNickname)
                        .name(displayName)
                        .phone(null)
                        .build();
                userRepository.save(user);

                SocialLogin socialLogin = SocialLogin.builder()
                        .user(user)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                socialLoginRepository.save(socialLogin);
            }
        }
        return user;
    }

    @Transactional
    public User saveOrUpdate(String providerName, Map<String, Object> userAttributes, String providerAccessToken, String providerRefreshToken) {
        String normalizedProviderName = providerName.substring(0, 1).toUpperCase() + providerName.substring(1).toLowerCase();
        ProviderType provider = ProviderType.valueOf(normalizedProviderName);

        String providerId = getProviderId(provider, userAttributes);
        String email = getEmail(provider, userAttributes);
        String displayName = getDisplayName(provider, userAttributes);

        Optional<SocialLogin> socialLoginOptional = socialLoginRepository.findByProviderAndProviderId(provider, providerId);

        User user;
        if (socialLoginOptional.isPresent()) {
            SocialLogin link = socialLoginOptional.get();
            user = link.getUser();
            if (user.getUserStatus() == UserStatus.WITHDRAWN) {
                if (isWithinGrace(user)) {
                    throw new ReactivationRequiredException(user.getEmail());
                } else {
                    throw new CustomException(ErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED);
                }
            }
            if (user.getUserStatus() != UserStatus.ACTIVE) {
                throw new CustomException(ErrorCode.INACTIVE_USER);
            }
            if (providerAccessToken != null || providerRefreshToken != null) {
                link.updateProviderTokens(providerAccessToken, providerRefreshToken);
            }
        } else {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                user = userOptional.get();
                if (user.getUserStatus() == UserStatus.WITHDRAWN) {
                    if (isWithinGrace(user)) {
                        throw new ReactivationRequiredException(user.getEmail());
                    } else {
                        throw new CustomException(ErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED);
                    }
                }
                if (user.getUserStatus() != UserStatus.ACTIVE) {
                    throw new CustomException(ErrorCode.INACTIVE_USER);
                }
                Optional<SocialLogin> existingLogin = socialLoginRepository.findByUser(user);
                if (existingLogin.isPresent() && existingLogin.get().getProvider() != provider) {
                    throw new OAuth2Exception(ErrorCode.SOCIAL_ACCOUNT_CONFLICT);
                }
                SocialLogin socialLogin = SocialLogin.builder()
                        .user(user)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                socialLogin.updateProviderTokens(providerAccessToken, providerRefreshToken);
                socialLoginRepository.save(socialLogin);
            } else {
                String tempNickname = generateTemporaryNickname();
                user = User.builder()
                        .email(email)
                        .password(null)
                        .nickname(tempNickname)
                        .name(displayName)
                        .phone(null)
                        .build();
                userRepository.save(user);

                SocialLogin socialLogin = SocialLogin.builder()
                        .user(user)
                        .provider(provider)
                        .providerId(providerId)
                        .build();
                socialLogin.updateProviderTokens(providerAccessToken, providerRefreshToken);
                socialLoginRepository.save(socialLogin);
            }
        }
        return user;
    }

    private String getProviderId(ProviderType provider, Map<String, Object> attributes) {
        if (provider == ProviderType.Google) {
            Object sub = attributes.get("sub");
            Object id = attributes.get("id");
            Object value = (sub != null) ? sub : id; // Google: sub 우선, 없으면 id 사용
            if (value == null) {
                throw new IllegalArgumentException("Google 사용자 정보에 식별자가 없습니다.");
            }
            return String.valueOf(value);
        }
        if (provider == ProviderType.Kakao) {
            return String.valueOf(attributes.get("id"));
        }
        throw new IllegalArgumentException("Unsupported Provider: " + provider);
    }

    private String getEmail(ProviderType provider, Map<String, Object> attributes) {
        if (provider == ProviderType.Google) {
            return (String) attributes.get("email");
        }
        if (provider == ProviderType.Kakao) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        }
        throw new IllegalArgumentException("Unsupported Provider: " + provider);
    }

    // provider가 제공하는 이름 (표시용)
    private String getDisplayName(ProviderType provider, Map<String, Object> attributes) {
        if (provider == ProviderType.Google) {
            // Google은 일반적으로 "name"(full name) 또는 "given_name" 등을 제공
            String name = (String) attributes.get("name");
            if (name != null && !name.isBlank()) return name;
            String given = (String) attributes.get("given_name");
            if (given != null && !given.isBlank()) return given;
            return (String) attributes.get("email");
        }
        if (provider == ProviderType.Kakao) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                Object profileObj = kakaoAccount.get("profile");
                if (profileObj instanceof Map<?, ?> profile) {
                    Object nickname = profile.get("nickname");
                    if (nickname != null) return String.valueOf(nickname);
                }
            }
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            if (properties != null) {
                Object nickname = properties.get("nickname");
                if (nickname != null) return String.valueOf(nickname);
            }
            return (kakaoAccount != null) ? (String) kakaoAccount.get("email") : null;
        }
        throw new IllegalArgumentException("Unsupported Provider: " + provider);
    }

    // 임시 닉네임 생성: user_ + 10자리 영소문자/숫자, 중복 회피
    private String generateTemporaryNickname() {
        final String prefix = "user_";
        final String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < 10; i++) {
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            String candidate = sb.toString();
            if (userRepository.findByNickname(candidate).isEmpty()) {
                return candidate;
            }
        }
        // 매우 드문 경우, UUID 일부 사용
        return prefix + Long.toHexString(System.nanoTime());
    }

    // 회원 탈퇴(소프트 삭제)
    @Transactional
    public void withdrawByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));
        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            return;
        }
        user.withdraw();
    }

    // 계정 재활성화 (유예 기간 내에만 가능)
    @Transactional
    public User reactivateByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));
        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            if (!isWithinGrace(user)) {
                throw new CustomException(ErrorCode.WITHDRAWAL_GRACE_PERIOD_EXPIRED);
            }
            user.reactivate();
        }
        return user;
    }

    private boolean isWithinGrace(User user) {
        if (user == null || user.getUserStatus() != UserStatus.WITHDRAWN) return false;
        LocalDateTime at = user.getWithdrawnAt();
        if (at == null) return false; // 과거 데이터: 안전하게 유예기간 만료로 간주
        return LocalDateTime.now().isBefore(at.plusDays(WITHDRAWAL_GRACE_DAYS));
    }

    private void userStatusToActive(User user) {
        try {
            java.lang.reflect.Field f = User.class.getDeclaredField("userStatus");
            f.setAccessible(true);
            f.set(user, UserStatus.ACTIVE);
        } catch (Exception ignored) { }
    }
}
