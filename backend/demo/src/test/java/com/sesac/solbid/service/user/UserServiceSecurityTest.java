package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserStatus;
import com.sesac.solbid.domain.enums.UserType;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.SocialLoginRepository;
import com.sesac.solbid.repository.UserRepository;
import com.sesac.solbid.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 보안 관련 단위 테스트
 * <p>
 * 스텝업 인증, 비밀번호 변경, 민감한 프로필 업데이트 등
 * 보안이 강화된 기능들의 다양한 시나리오를 테스트합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 보안 관련 단위 테스트")
class UserServiceSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialLoginRepository socialLoginRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User anotherUser;
    private String testEmail;
    private String currentPassword;
    private String encodedCurrentPassword;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        currentPassword = "currentPassword123!";
        encodedCurrentPassword = "encoded-current-password";

        // 테스트용 사용자 설정
        testUser = User.builder()
                .email(testEmail)
                .nickname("테스트닉네임")
                .name("홍길동")
                .password(encodedCurrentPassword)
                .phone("010-1234-5678")
                .build();
        setUserId(testUser, 1L);
        setUserStatus(testUser, UserStatus.ACTIVE);
        setUserType(testUser, UserType.USER);
        setTemperature(testUser, new BigDecimal("36.5"));

        // 다른 사용자 설정 (중복 검사용)
        anotherUser = User.builder()
                .email("another@example.com")
                .nickname("다른닉네임")
                .name("김철수")
                .password("encoded-password")
                .phone("010-9876-5432")
                .build();
        setUserId(anotherUser, 2L);
    }

    // === 민감한 프로필 업데이트 테스트 ===

    @Test
    @DisplayName("민감한 프로필 업데이트 - 이메일과 전화번호 모두 업데이트 - 성공")
    void updateSensitiveProfileForEmail_bothEmailAndPhone_success() {
        // Given
        String newEmail = "new@example.com";
        String newPhone = "010-1111-2222";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.findByPhone(newPhone)).thenReturn(Optional.empty());

        // When
        User result = userService.updateSensitiveProfileForEmail(testEmail, currentPassword, newEmail, newPhone);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).findByPhone(newPhone);
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 이메일만 업데이트 - 성공")
    void updateSensitiveProfileForEmail_emailOnly_success() {
        // Given
        String newEmail = "new@example.com";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());

        // When
        User result = userService.updateSensitiveProfileForEmail(testEmail, currentPassword, newEmail, null);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository, never()).findByPhone(any());
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 전화번호만 업데이트 - 성공")
    void updateSensitiveProfileForEmail_phoneOnly_success() {
        // Given
        String newPhone = "010-1111-2222";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(userRepository.findByPhone(newPhone)).thenReturn(Optional.empty());

        // When
        User result = userService.updateSensitiveProfileForEmail(testEmail, currentPassword, null, newPhone);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        // null 이메일은 중복 검사하지 않음
        verify(userRepository, times(1)).findByEmail(testEmail); // 사용자 조회만
        verify(userRepository).findByPhone(newPhone);
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 기존 이메일과 동일 - 중복 검사 생략")
    void updateSensitiveProfileForEmail_sameEmail_skipDuplicateCheck() {
        // Given
        String sameEmail = testEmail; // 기존 이메일과 동일

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);

        // When
        User result = userService.updateSensitiveProfileForEmail(testEmail, currentPassword, sameEmail, null);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        // 동일한 이메일이므로 중복 검사 수행하지 않음
        verify(userRepository, times(1)).findByEmail(testEmail); // 사용자 조회만
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 기존 전화번호와 동일 - 중복 검사 생략")
    void updateSensitiveProfileForEmail_samePhone_skipDuplicateCheck() {
        // Given
        String samePhone = "010-1234-5678"; // 기존 전화번호와 동일

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);

        // When
        User result = userService.updateSensitiveProfileForEmail(testEmail, currentPassword, null, samePhone);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        // 동일한 전화번호이므로 중복 검사 수행하지 않음
        verify(userRepository, never()).findByPhone(any());
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 존재하지 않는 사용자 - LOGIN_FAILED 예외")
    void updateSensitiveProfileForEmail_userNotFound_throwsException() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";

        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateSensitiveProfileForEmail(
                nonExistentEmail, currentPassword, "new@example.com", null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        verify(userRepository).findByEmail(nonExistentEmail);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 잘못된 현재 비밀번호 - LOGIN_FAILED 예외")
    void updateSensitiveProfileForEmail_wrongCurrentPassword_throwsException() {
        // Given
        String wrongPassword = "wrongPassword123!";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, encodedCurrentPassword)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.updateSensitiveProfileForEmail(
                testEmail, wrongPassword, "new@example.com", null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(wrongPassword, encodedCurrentPassword);
        verify(userRepository, never()).findByEmail("new@example.com");
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 중복된 이메일 - DUPLICATE_EMAIL 예외")
    void updateSensitiveProfileForEmail_duplicateEmail_throwsException() {
        // Given
        String duplicateEmail = "another@example.com";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(userRepository.findByEmail(duplicateEmail)).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateSensitiveProfileForEmail(
                testEmail, currentPassword, duplicateEmail, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        verify(userRepository).findByEmail(duplicateEmail);
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 중복된 전화번호 - DUPLICATE_PHONE 예외")
    void updateSensitiveProfileForEmail_duplicatePhone_throwsException() {
        // Given
        String duplicatePhone = "010-9876-5432";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(userRepository.findByPhone(duplicatePhone)).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateSensitiveProfileForEmail(
                testEmail, currentPassword, null, duplicatePhone))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_PHONE);

        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        verify(userRepository).findByPhone(duplicatePhone);
    }

    // === 비밀번호 변경 테스트 ===

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void changePasswordForEmail_success() {
        // Given
        String newPassword = "NewPassword123!";
        String encodedNewPassword = "encoded-new-password";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, encodedCurrentPassword)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // When
        User result = userService.changePasswordForEmail(testEmail, currentPassword, newPassword);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        verify(passwordEncoder).matches(newPassword, encodedCurrentPassword);
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    @DisplayName("비밀번호 변경 - 존재하지 않는 사용자 - LOGIN_FAILED 예외")
    void changePasswordForEmail_userNotFound_throwsException() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";

        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.changePasswordForEmail(
                nonExistentEmail, currentPassword, "NewPassword123!"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        verify(userRepository).findByEmail(nonExistentEmail);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("비밀번호 변경 - 잘못된 현재 비밀번호 - LOGIN_FAILED 예외")
    void changePasswordForEmail_wrongCurrentPassword_throwsException() {
        // Given
        String wrongPassword = "wrongPassword123!";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, encodedCurrentPassword)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.changePasswordForEmail(
                testEmail, wrongPassword, "NewPassword123!"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(wrongPassword, encodedCurrentPassword);
        verify(passwordEncoder, never()).matches(eq("NewPassword123!"), any());
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호와 동일한 새 비밀번호 - PASSWORD_RESET_SAME_AS_OLD 예외")
    void changePasswordForEmail_sameAsCurrentPassword_throwsException() {
        // Given
        String samePassword = "currentPassword123!";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(passwordEncoder.matches(samePassword, encodedCurrentPassword)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.changePasswordForEmail(
                testEmail, currentPassword, samePassword))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_RESET_SAME_AS_OLD);

        verify(userRepository).findByEmail(testEmail);
        // passwordEncoder.matches는 현재 비밀번호 검증과 새 비밀번호 중복 검사에서 총 2번 호출됨
        verify(passwordEncoder, times(2)).matches(anyString(), eq(encodedCurrentPassword));
        verify(passwordEncoder, never()).encode(any());
    }

    // === 현재 비밀번호 검증 테스트 ===

    @Test
    @DisplayName("현재 비밀번호 검증 - 올바른 비밀번호 - true 반환")
    void validateCurrentPassword_correctPassword_returnsTrue() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);

        // When
        boolean result = userService.validateCurrentPassword(testEmail, currentPassword);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
    }

    @Test
    @DisplayName("현재 비밀번호 검증 - 잘못된 비밀번호 - false 반환")
    void validateCurrentPassword_wrongPassword_returnsFalse() {
        // Given
        String wrongPassword = "wrongPassword123!";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(wrongPassword, encodedCurrentPassword)).thenReturn(false);

        // When
        boolean result = userService.validateCurrentPassword(testEmail, wrongPassword);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(wrongPassword, encodedCurrentPassword);
    }

    @Test
    @DisplayName("현재 비밀번호 검증 - 존재하지 않는 사용자 - LOGIN_FAILED 예외")
    void validateCurrentPassword_userNotFound_throwsException() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";

        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.validateCurrentPassword(nonExistentEmail, currentPassword))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        verify(userRepository).findByEmail(nonExistentEmail);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    // === 경계값 및 특수 케이스 테스트 ===

    @Test
    @DisplayName("민감한 프로필 업데이트 - null 값들로 업데이트 - 변경 없음")
    void updateSensitiveProfileForEmail_nullValues_noChange() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);

        // When
        User result = userService.updateSensitiveProfileForEmail(testEmail, currentPassword, null, null);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        // null 값은 중복 검사를 수행하지 않음
        verify(userRepository, times(1)).findByEmail(testEmail); // 사용자 조회만
        verify(userRepository, never()).findByPhone(any());
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 빈 문자열로 업데이트 - 중복 검사 수행")
    void updateSensitiveProfileForEmail_emptyStrings_duplicateCheckPerformed() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());
        when(userRepository.findByPhone("")).thenReturn(Optional.empty());

        // When
        User result = userService.updateSensitiveProfileForEmail(testEmail, currentPassword, "", "");

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        // 빈 문자열은 기존 값과 다르므로 중복 검사 수행됨
        verify(userRepository).findByEmail("");
        verify(userRepository).findByPhone("");
    }

    @Test
    @DisplayName("비밀번호 변경 - 복잡한 새 비밀번호 - 성공")
    void changePasswordForEmail_complexNewPassword_success() {
        // Given
        String complexNewPassword = "MyC0mpl3x@P4ssw0rd!";
        String encodedComplexPassword = "encoded-complex-password";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(passwordEncoder.matches(complexNewPassword, encodedCurrentPassword)).thenReturn(false);
        when(passwordEncoder.encode(complexNewPassword)).thenReturn(encodedComplexPassword);

        // When
        User result = userService.changePasswordForEmail(testEmail, currentPassword, complexNewPassword);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        verify(passwordEncoder).matches(complexNewPassword, encodedCurrentPassword);
        verify(passwordEncoder).encode(complexNewPassword);
    }

    @Test
    @DisplayName("민감한 프로필 업데이트 - 이메일과 전화번호 모두 중복 - 첫 번째 중복에서 예외")
    void updateSensitiveProfileForEmail_bothDuplicate_throwsFirstException() {
        // Given
        String duplicateEmail = "another@example.com";
        String duplicatePhone = "010-9876-5432";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(userRepository.findByEmail(duplicateEmail)).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateSensitiveProfileForEmail(
                testEmail, currentPassword, duplicateEmail, duplicatePhone))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
        verify(userRepository).findByEmail(duplicateEmail);
        // 이메일 중복에서 예외가 발생하므로 전화번호 중복 검사는 수행되지 않음
        verify(userRepository, never()).findByPhone(any());
    }

    // === 헬퍼 메서드 ===

    /**
     * 리플렉션을 사용하여 User 엔티티의 userId를 설정하는 헬퍼 메서드
     */
    private void setUserId(User user, Long userId) {
        try {
            java.lang.reflect.Field userIdField = User.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(user, userId);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }

    /**
     * 리플렉션을 사용하여 User 엔티티의 userStatus를 설정하는 헬퍼 메서드
     */
    private void setUserStatus(User user, UserStatus userStatus) {
        try {
            java.lang.reflect.Field userStatusField = User.class.getDeclaredField("userStatus");
            userStatusField.setAccessible(true);
            userStatusField.set(user, userStatus);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }

    /**
     * 리플렉션을 사용하여 User 엔티티의 userType을 설정하는 헬퍼 메서드
     */
    private void setUserType(User user, UserType userType) {
        try {
            java.lang.reflect.Field userTypeField = User.class.getDeclaredField("userType");
            userTypeField.setAccessible(true);
            userTypeField.set(user, userType);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }

    /**
     * 리플렉션을 사용하여 User 엔티티의 temperature를 설정하는 헬퍼 메서드
     */
    private void setTemperature(User user, BigDecimal temperature) {
        try {
            java.lang.reflect.Field temperatureField = User.class.getDeclaredField("temperature");
            temperatureField.setAccessible(true);
            temperatureField.set(user, temperature);
        } catch (Exception e) {
            // 리플렉션 실패 시 무시
        }
    }
}