package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailVerificationService 테스트")
class EmailVerificationServiceTest {

    @Mock
    private EmailVerificationTokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private User unverifiedUser;
    private User verifiedUser;
    private final String testEmail = "test@example.com";
    private final String testToken = "test-token-123";

    @BeforeEach
    void setUp() {
        unverifiedUser = User.builder()
                .email(testEmail)
                .password("encodedPassword")
                .nickname("testUser")
                .name("Test User")
                .phone(null)
                .build();

        verifiedUser = User.builder()
                .email(testEmail)
                .password("encodedPassword")
                .nickname("testUser")
                .name("Test User")
                .phone(null)
                .build();
        verifiedUser.verifyEmail(); // 이메일 인증 완료 상태로 설정
    }

    @Test
    @DisplayName("인증 이메일 전송 - 성공")
    void sendVerificationEmail_Success() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(unverifiedUser));
        when(tokenService.createToken(testEmail)).thenReturn(testToken);
        doNothing().when(emailService).sendVerificationEmail(testEmail, testToken);

        // when
        emailVerificationService.sendVerificationEmail(testEmail);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).createToken(testEmail);
        verify(emailService).sendVerificationEmail(testEmail, testToken);
    }

    @Test
    @DisplayName("인증 이메일 전송 - 사용자 없음")
    void sendVerificationEmail_UserNotFound() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> emailVerificationService.sendVerificationEmail(testEmail))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService, never()).createToken(any());
        verify(emailService, never()).sendVerificationEmail(any(), any());
    }

    @Test
    @DisplayName("인증 이메일 전송 - 이미 인증된 사용자")
    void sendVerificationEmail_AlreadyVerified() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(verifiedUser));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.sendVerificationEmail(testEmail))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_VERIFIED);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService, never()).createToken(any());
        verify(emailService, never()).sendVerificationEmail(any(), any());
    }

    @Test
    @DisplayName("이메일 인증 처리 - 성공")
    void verifyEmail_Success() {
        // given
        when(tokenService.consumeToken(testToken)).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(unverifiedUser));

        // when
        String result = emailVerificationService.verifyEmail(testToken);

        // then
        assertThat(result).isEqualTo(testEmail);
        assertThat(unverifiedUser.getEmailVerified()).isTrue();
        assertThat(unverifiedUser.getEmailVerifiedAt()).isNotNull();
        verify(tokenService).consumeToken(testToken);
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("이메일 인증 처리 - 유효하지 않은 토큰")
    void verifyEmail_InvalidToken() {
        // given
        when(tokenService.consumeToken(testToken)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmail(testToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFICATION_TOKEN_INVALID);

        verify(tokenService).consumeToken(testToken);
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    @DisplayName("이메일 인증 처리 - 사용자 없음")
    void verifyEmail_UserNotFound() {
        // given
        when(tokenService.consumeToken(testToken)).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> emailVerificationService.verifyEmail(testToken))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(tokenService).consumeToken(testToken);
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("이메일 인증 처리 - 이미 인증된 사용자")
    void verifyEmail_AlreadyVerified() {
        // given
        when(tokenService.consumeToken(testToken)).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(verifiedUser));

        // when
        String result = emailVerificationService.verifyEmail(testToken);

        // then
        assertThat(result).isEqualTo(testEmail);
        verify(tokenService).consumeToken(testToken);
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("인증 이메일 재전송 - 성공")
    void resendVerificationEmail_Success() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(unverifiedUser));
        when(tokenService.getDailyResendCount(testEmail)).thenReturn(2);
        when(tokenService.getLastResendTime(testEmail)).thenReturn(System.currentTimeMillis() / 1000 - 400); // 6분 전
        when(tokenService.createToken(testEmail)).thenReturn(testToken);
        doNothing().when(tokenService).recordResendRequest(testEmail);
        doNothing().when(emailService).sendVerificationEmail(testEmail, testToken);

        // when
        emailVerificationService.resendVerificationEmail(testEmail);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).getDailyResendCount(testEmail);
        verify(tokenService).getLastResendTime(testEmail);
        verify(tokenService).recordResendRequest(testEmail);
        verify(tokenService).createToken(testEmail);
        verify(emailService).sendVerificationEmail(testEmail, testToken);
    }

    @Test
    @DisplayName("인증 이메일 재전송 - 사용자 없음")
    void resendVerificationEmail_UserNotFound() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> emailVerificationService.resendVerificationEmail(testEmail))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService, never()).getDailyResendCount(any());
    }

    @Test
    @DisplayName("인증 이메일 재전송 - 이미 인증된 사용자")
    void resendVerificationEmail_AlreadyVerified() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(verifiedUser));

        // when & then
        assertThatThrownBy(() -> emailVerificationService.resendVerificationEmail(testEmail))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_VERIFIED);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService, never()).getDailyResendCount(any());
    }

    @Test
    @DisplayName("인증 이메일 재전송 - 일일 재전송 횟수 초과")
    void resendVerificationEmail_DailyLimitExceeded() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(unverifiedUser));
        when(tokenService.getDailyResendCount(testEmail)).thenReturn(5);

        // when & then
        assertThatThrownBy(() -> emailVerificationService.resendVerificationEmail(testEmail))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).getDailyResendCount(testEmail);
        verify(tokenService, never()).getLastResendTime(any());
    }

    @Test
    @DisplayName("인증 이메일 재전송 - 재전송 간격 제한")
    void resendVerificationEmail_TooFrequent() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(unverifiedUser));
        when(tokenService.getDailyResendCount(testEmail)).thenReturn(2);
        when(tokenService.getLastResendTime(testEmail)).thenReturn(System.currentTimeMillis() / 1000 - 30); // 30초 전

        // when & then
        assertThatThrownBy(() -> emailVerificationService.resendVerificationEmail(testEmail))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_VERIFICATION_RESEND_TOO_FREQUENT);

        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).getDailyResendCount(testEmail);
        verify(tokenService).getLastResendTime(testEmail);
        verify(tokenService, never()).recordResendRequest(any());
    }

    @Test
    @DisplayName("인증 이메일 재전송 - 첫 번째 재전송 (마지막 재전송 시간 없음)")
    void resendVerificationEmail_FirstResend() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(unverifiedUser));
        when(tokenService.getDailyResendCount(testEmail)).thenReturn(0);
        when(tokenService.getLastResendTime(testEmail)).thenReturn(-1L); // 재전송 기록 없음
        when(tokenService.createToken(testEmail)).thenReturn(testToken);
        doNothing().when(tokenService).recordResendRequest(testEmail);
        doNothing().when(emailService).sendVerificationEmail(testEmail, testToken);

        // when
        emailVerificationService.resendVerificationEmail(testEmail);

        // then
        verify(userRepository).findByEmail(testEmail);
        verify(tokenService).getDailyResendCount(testEmail);
        verify(tokenService).getLastResendTime(testEmail);
        verify(tokenService).recordResendRequest(testEmail);
        verify(tokenService).createToken(testEmail);
        verify(emailService).sendVerificationEmail(testEmail, testToken);
    }
}