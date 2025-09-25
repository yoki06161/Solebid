package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserStatus;
import com.sesac.solbid.domain.enums.UserType;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import com.sesac.solbid.repository.auth.SocialLoginRepository;
import com.sesac.solbid.repository.user.UserRepository;
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
 * UserService 프로필 업데이트 관련 단위 테스트
 * <p>
 * updateProfileForEmail 메서드의 다양한 시나리오를 테스트합니다.
 * 정상적인 업데이트, 중복 검사, 예외 상황 등을 확인합니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 프로필 업데이트 단위 테스트")
class UserServiceProfileUpdateTest {

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

    private User existingUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        // 기존 사용자 설정
        existingUser = User.builder()
                .email("test@example.com")
                .nickname("기존닉네임")
                .name("홍길동")
                .password("encoded-password")
                .phone("010-1234-5678")
                .build();
        setUserId(existingUser, 1L);
        setUserStatus(existingUser, UserStatus.ACTIVE);
        setUserType(existingUser, UserType.USER);
        setTemperature(existingUser, new BigDecimal("36.5"));

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

    // === 일반 프로필 업데이트 테스트 ===

    @Test
    @DisplayName("모든 필드 업데이트 - 성공")
    void updateProfileForEmail_allFields_success() {
        // Given
        String email = "test@example.com";
        String newNickname = "새닉네임";
        String newName = "새이름";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByNickname(newNickname)).thenReturn(Optional.empty());

        // When
        User result = userService.updateProfileForEmail(email, newNickname, newName);

        // Then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getNickname()).isEqualTo(newNickname);
        assertThat(result.getName()).isEqualTo(newName);

        verify(userRepository).findByEmail(email);
        verify(userRepository).findByNickname(newNickname);
    }

    @Test
    @DisplayName("닉네임만 업데이트 - 성공")
    void updateProfileForEmail_nicknameOnly_success() {
        // Given
        String email = "test@example.com";
        String newNickname = "새닉네임";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByNickname(newNickname)).thenReturn(Optional.empty());

        // When
        User result = userService.updateProfileForEmail(email, newNickname, null);

        // Then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getNickname()).isEqualTo(newNickname);
        assertThat(result.getName()).isEqualTo("홍길동"); // 기존 값 유지

        verify(userRepository).findByEmail(email);
        verify(userRepository).findByNickname(newNickname);
    }

    @Test
    @DisplayName("이름만 업데이트 - 성공")
    void updateProfileForEmail_nameOnly_success() {
        // Given
        String email = "test@example.com";
        String newName = "새이름";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // When
        User result = userService.updateProfileForEmail(email, null, newName);

        // Then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getNickname()).isEqualTo("기존닉네임"); // 기존 값 유지
        assertThat(result.getName()).isEqualTo(newName);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).findByNickname(any());
    }

    @Test
    @DisplayName("모든 필드 null - 변경 없음")
    void updateProfileForEmail_allFieldsNull_noChange() {
        // Given
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // When
        User result = userService.updateProfileForEmail(email, null, null);

        // Then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getNickname()).isEqualTo("기존닉네임"); // 기존 값 유지
        assertThat(result.getName()).isEqualTo("홍길동"); // 기존 값 유지

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).findByNickname(any());
    }

    // === 예외 상황 테스트 ===

    @Test
    @DisplayName("존재하지 않는 사용자 - LOGIN_FAILED 예외")
    void updateProfileForEmail_userNotFound_throwsException() {
        // Given
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateProfileForEmail(email, "새닉네임", "새이름"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).findByNickname(any());
    }

    @Test
    @DisplayName("중복된 닉네임 - DUPLICATE_NICKNAME 예외")
    void updateProfileForEmail_duplicateNickname_throwsException() {
        // Given
        String email = "test@example.com";
        String duplicateNickname = "다른닉네임"; // 다른 사용자가 사용 중

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByNickname(duplicateNickname)).thenReturn(Optional.of(anotherUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateProfileForEmail(email, duplicateNickname, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);

        verify(userRepository).findByEmail(email);
        verify(userRepository).findByNickname(duplicateNickname);
    }

    // === 민감한 프로필 업데이트 테스트 ===

    @Test
    @DisplayName("민감한 정보 업데이트 - 성공")
    void updateSensitiveProfileForEmail_success() {
        // Given
        String email = "test@example.com";
        String currentPassword = "currentPassword";
        String newEmail = "new@example.com";
        String newPhone = "010-1111-2222";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(currentPassword, existingUser.getPassword())).thenReturn(true);
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.findByPhone(newPhone)).thenReturn(Optional.empty());

        // When
        User result = userService.updateSensitiveProfileForEmail(email, currentPassword, newEmail, newPhone);

        // Then
        assertThat(result).isEqualTo(existingUser);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(currentPassword, existingUser.getPassword());
    }

    @Test
    @DisplayName("민감한 정보 업데이트 - 잘못된 현재 비밀번호")
    void updateSensitiveProfileForEmail_wrongPassword_throwsException() {
        // Given
        String email = "test@example.com";
        String wrongPassword = "wrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(wrongPassword, existingUser.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.updateSensitiveProfileForEmail(email, wrongPassword, "new@example.com", null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGIN_FAILED);

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(wrongPassword, existingUser.getPassword());
    }

    // === 비밀번호 변경 테스트 ===

    @Test
    @DisplayName("비밀번호 변경 - 성공")
    void changePasswordForEmail_success() {
        // Given
        String email = "test@example.com";
        String currentPassword = "currentPassword";
        String newPassword = "newPassword123!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(currentPassword, existingUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(newPassword, existingUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("encoded-new-password");

        // When
        User result = userService.changePasswordForEmail(email, currentPassword, newPassword);

        // Then
        assertThat(result).isEqualTo(existingUser);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(currentPassword, existingUser.getPassword());
        verify(passwordEncoder).matches(newPassword, existingUser.getPassword());
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    @DisplayName("비밀번호 변경 - 현재 비밀번호와 동일한 새 비밀번호")
    void changePasswordForEmail_samePassword_throwsException() {
        // Given
        String email = "test@example.com";
        String currentPassword = "currentPassword";
        String samePassword = "currentPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(currentPassword, existingUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches(samePassword, existingUser.getPassword())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.changePasswordForEmail(email, currentPassword, samePassword))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_RESET_SAME_AS_OLD);

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(currentPassword, existingUser.getPassword());
        verify(passwordEncoder).matches(samePassword, existingUser.getPassword());
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