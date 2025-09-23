package com.sesac.solbid.service;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserType;
import com.sesac.solbid.dto.auth.response.AuthUrlResponse;
import com.sesac.solbid.dto.auth.response.EmailVerificationResponse;
import com.sesac.solbid.dto.auth.response.LoginSuccessResponse;
import com.sesac.solbid.dto.user.request.SignupRequest;
import com.sesac.solbid.dto.user.response.LoginResponse;
import com.sesac.solbid.dto.user.response.SignupResponse;
import com.sesac.solbid.dto.user.response.NicknameAvailabilityResponse;
import com.sesac.solbid.service.auth.OAuth2Service;
import com.sesac.solbid.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Record DTO와 기존 서비스 레이어 코드의 호환성 테스트
 * 
 * 테스트 범위:
 * - 정적 팩토리 메서드의 정상 동작 확인
 * - 비즈니스 로직 메서드의 정상 동작 확인
 * - 서비스 레이어에서 Record DTO 사용 시 호환성 확인
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecordDtoServiceCompatibilityTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OAuth2Service oAuth2Service;

    @Test
    @DisplayName("SignupRequest Record의 toEntity() 비즈니스 로직 메서드가 정상 동작한다")
    void signupRequest_toEntity_worksCorrectly() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Password123!",
                "testuser",
                "Test User",
                "01012345678"
        );
        String encodedPassword = "encodedPassword123";

        // when
        User user = signupRequest.toEntity(encodedPassword);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("encodedPassword123");
        assertThat(user.getNickname()).isEqualTo("testuser");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getPhone()).isEqualTo("01012345678");
        assertThat(user.getUserType()).isEqualTo(UserType.USER);
    }

    @Test
    @DisplayName("LoginResponse Record의 정적 팩토리 메서드가 정상 동작한다")
    void loginResponse_staticFactoryMethod_worksCorrectly() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .build();
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-456";

        // when
        LoginResponse response = LoginResponse.from(user, accessToken, refreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isNotNull();
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("testuser");
        assertThat(response.userType()).isEqualTo(UserType.USER);
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
    }

    @Test
    @DisplayName("SignupResponse Record의 복잡한 생성자 로직이 정상 동작한다")
    void signupResponse_complexConstructor_worksCorrectly() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .nickname("testuser")
                .name("Test User")
                .phone("01012345678")
                .build();

        // when
        SignupResponse response = new SignupResponse(user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isNotNull();
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("testuser");
        assertThat(response.emailVerified()).isFalse();
        assertThat(response.message()).isEqualTo("회원가입이 완료되었습니다.");
        assertThat(response.nextStep()).contains("이메일 인증");
    }

    @Test
    @DisplayName("AuthUrlResponse Record의 정적 팩토리 메서드가 정상 동작한다")
    void authUrlResponse_staticFactoryMethod_worksCorrectly() {
        // given
        String authUrl = "https://oauth.provider.com/auth?client_id=123";
        String state = "random-state-123";
        String provider = "google";

        // when
        AuthUrlResponse response = AuthUrlResponse.of(authUrl, state, provider);

        // then
        assertThat(response).isNotNull();
        assertThat(response.authUrl()).isEqualTo(authUrl);
        assertThat(response.state()).isEqualTo(state);
        assertThat(response.provider()).isEqualTo(provider);
    }

    @Test
    @DisplayName("EmailVerificationResponse Record의 정적 팩토리 메서드가 정상 동작한다")
    void emailVerificationResponse_staticFactoryMethod_worksCorrectly() {
        // given
        String email = "test@example.com";
        String message = "인증 이메일이 발송되었습니다.";

        // when
        EmailVerificationResponse response = EmailVerificationResponse.success(email, message);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.message()).isEqualTo(message);
    }

    @Test
    @DisplayName("LoginSuccessResponse Record의 복잡한 생성자 로직이 정상 동작한다")
    void loginSuccessResponse_complexConstructor_worksCorrectly() {
        // given
        Long userId = 1L;
        String email = "test@example.com";
        String nickname = "testuser";
        UserType userType = UserType.USER;
        String provider = "google";
        boolean requiresNickname = false;

        // when
        LoginSuccessResponse response = new LoginSuccessResponse(
                userId, email, nickname, userType, provider, requiresNickname
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.nickname()).isEqualTo(nickname);
        assertThat(response.userType()).isEqualTo("USER");
        assertThat(response.provider()).isEqualTo(provider);
        assertThat(response.requiresNickname()).isEqualTo(requiresNickname);
    }

    @Test
    @DisplayName("NicknameAvailabilityResponse Record 생성이 정상 동작한다")
    void nicknameAvailabilityResponse_record_worksCorrectly() {
        // when
        NicknameAvailabilityResponse response = new NicknameAvailabilityResponse(true);

        // then
        assertThat(response).isNotNull();
        assertThat(response.available()).isTrue();
    }

    @Test
    @DisplayName("OAuth2Service에서 AuthUrlResponse Record 생성이 정상 동작한다")
    void oAuth2Service_generateAuthUrl_worksWithRecord() {
        // when
        AuthUrlResponse response = oAuth2Service.generateAuthUrl("google");

        // then
        assertThat(response).isNotNull();
        assertThat(response.authUrl()).isNotBlank();
        assertThat(response.state()).isNotBlank();
        assertThat(response.provider()).isEqualTo("google");
    }

    @Test
    @DisplayName("UserService에서 SignupRequest Record의 toEntity() 메서드 사용이 정상 동작한다")
    void userService_signup_worksWithSignupRequestRecord() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "servicetest@example.com",
                "Password123!",
                "serviceuser",
                "Service User",
                "01087654321"
        );

        // when
        User savedUser = userService.signup(signupRequest);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("servicetest@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("serviceuser");
        assertThat(savedUser.getName()).isEqualTo("Service User");
        assertThat(savedUser.getPhone()).isEqualTo("01087654321");
        assertThat(savedUser.getPassword()).isNotBlank(); // 암호화된 비밀번호
        assertThat(savedUser.getUserType()).isEqualTo(UserType.USER);
    }

    @Test
    @DisplayName("Record DTO의 불변성이 보장된다")
    void recordDto_immutability_isGuaranteed() {
        // given
        SignupRequest original = new SignupRequest(
                "test@example.com",
                "Password123!",
                "testuser",
                "Test User",
                "01012345678"
        );

        // when - Record는 불변이므로 필드 변경 불가능
        // original.email = "changed@example.com"; // 컴파일 에러

        // then
        assertThat(original.email()).isEqualTo("test@example.com");
        assertThat(original.password()).isEqualTo("Password123!");
        assertThat(original.nickname()).isEqualTo("testuser");
        assertThat(original.name()).isEqualTo("Test User");
        assertThat(original.phone()).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("Record DTO의 equals()와 hashCode()가 정상 동작한다")
    void recordDto_equalsAndHashCode_workCorrectly() {
        // given
        SignupRequest request1 = new SignupRequest(
                "test@example.com",
                "Password123!",
                "testuser",
                "Test User",
                "01012345678"
        );
        SignupRequest request2 = new SignupRequest(
                "test@example.com",
                "Password123!",
                "testuser",
                "Test User",
                "01012345678"
        );
        SignupRequest request3 = new SignupRequest(
                "different@example.com",
                "Password123!",
                "testuser",
                "Test User",
                "01012345678"
        );

        // when & then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    @DisplayName("Record DTO의 toString()이 정상 동작한다")
    void recordDto_toString_worksCorrectly() {
        // given
        LoginResponse response = new LoginResponse(
                1L,
                "test@example.com",
                "testuser",
                "테스트이름",
                "010-7777-8888",
                UserType.USER,
                "access-token",
                "refresh-token"
        );

        // when
        String toString = response.toString();

        // then
        assertThat(toString).contains("LoginResponse");
        assertThat(toString).contains("userId=1");
        assertThat(toString).contains("email=test@example.com");
        assertThat(toString).contains("nickname=testuser");
        assertThat(toString).contains("userType=USER");
    }
}