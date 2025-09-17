package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.SocialLogin;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.ProviderType;
import com.sesac.solbid.exception.PasswordResetException;
import com.sesac.solbid.repository.SocialLoginRepository;
import com.sesac.solbid.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService 테스트")
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialLoginRepository socialLoginRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationTokenService tokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User normalUser;
    private User socialUser;
    private SocialLogin socialLogin;
    private final String testEmail = "test@example.com";
    private final String testOtp = "123456";
    private final String testPassword = "newPassword123!";
    private final String encodedPassword = "encodedPassword";

    @BeforeEach
    void setUp() {
        normalUser = User.builder()
                .email(testEmail)
                .password("oldEncodedPassword")
                .nickname("testUser")
                .name("Test User")
                .phone(null)
                .build();

        socialUser = User.builder()
                .email("social@example.com")
                .password(null) // 소셜 로그인 사용자는 비밀번호가 없음
                .nickname("socialUser")
                .name("Social User")
                .phone(null)
                .build();

        socialLogin = SocialLogin.builder()
                .user(socialUser)
                .provider(ProviderType.Google)
                .providerId("google123")
                .build();
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 요청 - 성공 (일반 사용자)")
    void requestResetWithOtp_Success_NormalUser() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(socialLoginRepository.findByUser(normalUser)).thenReturn(Optional.empty());
        when(tokenService.createToken(testEmail)).thenReturn(testOtp);
        doNothing().when(emailService).sendPasswordResetOtp(testEmail, testOtp);

        // when
        passwordResetService.requestResetWithOtp(testEmail);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(socialLoginRepository).findByUser(normalUser);
        verify(tokenService).createToken(testEmail);
        verify(emailService).sendPasswordResetOtp(testEmail, testOtp);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 요청 - 존재하지 않는 사용자 (보안상 동일한 응답)")
    void requestResetWithOtp_UserNotFound_SecurityResponse() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // when
        passwordResetService.requestResetWithOtp(testEmail);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(socialLoginRepository, never()).findByUser(any());
        verify(tokenService, never()).createToken(any());
        verify(emailService, never()).sendPasswordResetOtp(any(), any());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 요청 - 소셜 로그인 사용자 (비밀번호 없음)")
    void requestResetWithOtp_SocialUserWithoutPassword() {
        // given
        when(userRepository.findByEmail("social@example.com")).thenReturn(Optional.of(socialUser));
        when(socialLoginRepository.findByUser(socialUser)).thenReturn(Optional.of(socialLogin));

        // when & then
        assertThatThrownBy(() -> passwordResetService.requestResetWithOtp("social@example.com"))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail("social@example.com");
        verify(socialLoginRepository).findByUser(socialUser);
        verify(tokenService, never()).createToken(any());
        verify(emailService, never()).sendPasswordResetOtp(any(), any());
    }

    @Test
    @DisplayName("OTP 검증 및 비밀번호 재설정 - 성공")
    void verifyOtpAndReset_Success() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(tokenService.consumeToken(testOtp)).thenReturn(testEmail);
        when(passwordEncoder.matches(testPassword, normalUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);

        // when
        passwordResetService.verifyOtpAndReset(testEmail, testOtp, testPassword);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).consumeToken(testOtp);
        verify(passwordEncoder).matches(testPassword, normalUser.getPassword());
        verify(passwordEncoder).encode(testPassword);
        assertThat(normalUser.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("OTP 검증 및 비밀번호 재설정 - 사용자 없음")
    void verifyOtpAndReset_UserNotFound() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> passwordResetService.verifyOtpAndReset(testEmail, testOtp, testPassword))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService, never()).consumeToken(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("OTP 검증 및 비밀번호 재설정 - 유효하지 않은 OTP")
    void verifyOtpAndReset_InvalidOtp() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(tokenService.consumeToken(testOtp)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> passwordResetService.verifyOtpAndReset(testEmail, testOtp, testPassword))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).consumeToken(testOtp);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("OTP 검증 및 비밀번호 재설정 - 이메일 불일치")
    void verifyOtpAndReset_EmailMismatch() {
        // given
        String differentEmail = "different@example.com";
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(tokenService.consumeToken(testOtp)).thenReturn(differentEmail);

        // when & then
        assertThatThrownBy(() -> passwordResetService.verifyOtpAndReset(testEmail, testOtp, testPassword))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).consumeToken(testOtp);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("OTP 검증 및 비밀번호 재설정 - 기존 비밀번호와 동일")
    void verifyOtpAndReset_SameAsOldPassword() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(tokenService.consumeToken(testOtp)).thenReturn(testEmail);
        when(passwordEncoder.matches(testPassword, normalUser.getPassword())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> passwordResetService.verifyOtpAndReset(testEmail, testOtp, testPassword))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).consumeToken(testOtp);
        verify(passwordEncoder).matches(testPassword, normalUser.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 성공")
    void resendResetOtp_Success() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(socialLoginRepository.findByUser(normalUser)).thenReturn(Optional.empty());
        when(tokenService.canRequestResend(testEmail)).thenReturn(true);
        when(tokenService.createToken(testEmail)).thenReturn(testOtp);
        doNothing().when(tokenService).recordResendRequest(testEmail);
        doNothing().when(emailService).sendPasswordResetOtp(testEmail, testOtp);

        // when
        passwordResetService.resendResetOtp(testEmail);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(socialLoginRepository).findByUser(normalUser);
        verify(tokenService).recordResendRequest(testEmail);
        verify(tokenService).createToken(testEmail);
        verify(emailService).sendPasswordResetOtp(testEmail, testOtp);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 사용자 없음")
    void resendResetOtp_UserNotFound() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> passwordResetService.resendResetOtp(testEmail))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail(testEmail);
        verify(socialLoginRepository, never()).findByUser(any());
        verify(tokenService, never()).canRequestResend(any());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 소셜 로그인 사용자")
    void resendResetOtp_SocialUser() {
        // given
        when(userRepository.findByEmail("social@example.com")).thenReturn(Optional.of(socialUser));
        when(socialLoginRepository.findByUser(socialUser)).thenReturn(Optional.of(socialLogin));

        // when & then
        assertThatThrownBy(() -> passwordResetService.resendResetOtp("social@example.com"))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail("social@example.com");
        verify(socialLoginRepository).findByUser(socialUser);
        verify(tokenService, never()).canRequestResend(any());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 일일 재전송 횟수 초과")
    void resendResetOtp_DailyLimitExceeded() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(socialLoginRepository.findByUser(normalUser)).thenReturn(Optional.empty());
        when(tokenService.canRequestResend(testEmail)).thenReturn(false);
        when(tokenService.getDailyResendCount(testEmail)).thenReturn(5);

        // when & then
        assertThatThrownBy(() -> passwordResetService.resendResetOtp(testEmail))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail(testEmail);
        verify(socialLoginRepository).findByUser(normalUser);
        verify(tokenService).getDailyResendCount(testEmail);
        verify(tokenService, never()).recordResendRequest(any());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 재전송 간격 제한")
    void resendResetOtp_TooFrequent() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(socialLoginRepository.findByUser(normalUser)).thenReturn(Optional.empty());
        when(tokenService.canRequestResend(testEmail)).thenReturn(false);
        when(tokenService.getDailyResendCount(testEmail)).thenReturn(2);
        when(tokenService.getLastResendTime(testEmail)).thenReturn(System.currentTimeMillis() / 1000 - 30); // 30초 전

        // when & then
        assertThatThrownBy(() -> passwordResetService.resendResetOtp(testEmail))
                .isInstanceOf(PasswordResetException.class);

        verify(userRepository).findByEmail(testEmail);
        verify(socialLoginRepository).findByUser(normalUser);
        verify(tokenService).getDailyResendCount(testEmail);
        verify(tokenService).getLastResendTime(testEmail);
        verify(tokenService, never()).recordResendRequest(any());
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 첫 번째 재전송 (마지막 재전송 시간 없음)")
    void resendResetOtp_FirstResend() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(socialLoginRepository.findByUser(normalUser)).thenReturn(Optional.empty());
        when(tokenService.canRequestResend(testEmail)).thenReturn(false);
        when(tokenService.getDailyResendCount(testEmail)).thenReturn(0);
        when(tokenService.getLastResendTime(testEmail)).thenReturn(-1L); // 재전송 기록 없음
        when(tokenService.createToken(testEmail)).thenReturn(testOtp);
        doNothing().when(tokenService).recordResendRequest(testEmail);
        doNothing().when(emailService).sendPasswordResetOtp(testEmail, testOtp);

        // when
        passwordResetService.resendResetOtp(testEmail);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(socialLoginRepository).findByUser(normalUser);
        verify(tokenService).getDailyResendCount(testEmail);
        verify(tokenService).getLastResendTime(testEmail);
        verify(tokenService).recordResendRequest(testEmail);
        verify(tokenService).createToken(testEmail);
        verify(emailService).sendPasswordResetOtp(testEmail, testOtp);
    }

    @Test
    @DisplayName("비밀번호 재설정 OTP 재전송 - 시간 간격 충족 후 재전송")
    void resendResetOtp_AfterTimeInterval() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(normalUser));
        when(socialLoginRepository.findByUser(normalUser)).thenReturn(Optional.empty());
        when(tokenService.canRequestResend(testEmail)).thenReturn(false);
        when(tokenService.getDailyResendCount(testEmail)).thenReturn(2);
        when(tokenService.getLastResendTime(testEmail)).thenReturn(System.currentTimeMillis() / 1000 - 120); // 2분 전
        when(tokenService.createToken(testEmail)).thenReturn(testOtp);
        doNothing().when(tokenService).recordResendRequest(testEmail);
        doNothing().when(emailService).sendPasswordResetOtp(testEmail, testOtp);

        // when
        passwordResetService.resendResetOtp(testEmail);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(socialLoginRepository).findByUser(normalUser);
        verify(tokenService).getDailyResendCount(testEmail);
        verify(tokenService).getLastResendTime(testEmail);
        verify(tokenService).recordResendRequest(testEmail);
        verify(tokenService).createToken(testEmail);
        verify(emailService).sendPasswordResetOtp(testEmail, testOtp);
    }

    @Test
    @DisplayName("OTP 검증 및 비밀번호 재설정 - 비밀번호가 null인 사용자")
    void verifyOtpAndReset_UserWithNullPassword() {
        // given
        User userWithNullPassword = User.builder()
                .email(testEmail)
                .password(null)
                .nickname("testUser")
                .name("Test User")
                .phone(null)
                .build();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(userWithNullPassword));
        when(tokenService.consumeToken(testOtp)).thenReturn(testEmail);
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);

        // when
        passwordResetService.verifyOtpAndReset(testEmail, testOtp, testPassword);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).consumeToken(testOtp);
        verify(passwordEncoder, never()).matches(any(), any()); // null 비밀번호이므로 matches 호출되지 않음
        verify(passwordEncoder).encode(testPassword);
        assertThat(userWithNullPassword.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    @DisplayName("OTP 검증 및 비밀번호 재설정 - 빈 비밀번호인 사용자")
    void verifyOtpAndReset_UserWithBlankPassword() {
        // given
        User userWithBlankPassword = User.builder()
                .email(testEmail)
                .password("")
                .nickname("testUser")
                .name("Test User")
                .phone(null)
                .build();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(userWithBlankPassword));
        when(tokenService.consumeToken(testOtp)).thenReturn(testEmail);
        when(passwordEncoder.encode(testPassword)).thenReturn(encodedPassword);

        // when
        passwordResetService.verifyOtpAndReset(testEmail, testOtp, testPassword);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).consumeToken(testOtp);
        verify(passwordEncoder, never()).matches(any(), any()); // 빈 비밀번호이므로 matches 호출되지 않음
        verify(passwordEncoder).encode(testPassword);
        assertThat(userWithBlankPassword.getPassword()).isEqualTo(encodedPassword);
    }
}