package com.sesac.solbid.service.user;

import com.sesac.solbid.domain.User;
import com.sesac.solbid.dto.user.request.SignupRequest;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class UserServiceSignupTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private EmailVerificationService emailVerificationService;
    
    @InjectMocks
    private UserService userService;
    
    private SignupRequest signupRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest(
                "test@example.com",
                "password123",
                "testuser",
                "Test User",
                "01012345678"
        );
        
        user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("testuser")
                .name("Test User")
                .phone("01012345678")
                .build();
    }
    
    @Test
    @DisplayName("회원가입 성공 - 이메일 인증 메일 전송")
    void signup_Success_SendsVerificationEmail() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);
        doNothing().when(emailVerificationService).sendVerificationEmail(anyString());
        
        // when
        User result = userService.signup(signupRequest);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getEmailVerified()).isFalse(); // 새 사용자는 미인증 상태
        
        then(emailVerificationService).should().sendVerificationEmail("test@example.com");
    }
    
    @Test
    @DisplayName("회원가입 성공 - 이메일 전송 실패해도 회원가입은 성공")
    void signup_Success_EvenWhenEmailSendingFails() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(userRepository.findByNickname(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);
        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailVerificationService).sendVerificationEmail(anyString());
        
        // when
        User result = userService.signup(signupRequest);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getEmailVerified()).isFalse();
        
        then(emailVerificationService).should().sendVerificationEmail("test@example.com");
    }
    
    @Test
    @DisplayName("회원가입 실패 - 중복 이메일")
    void signup_Fail_DuplicateEmail() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        
        // when & then
        assertThatThrownBy(() -> userService.signup(signupRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);
    }
    
    @Test
    @DisplayName("회원가입 실패 - 중복 닉네임")
    void signup_Fail_DuplicateNickname() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(userRepository.findByNickname(anyString())).willReturn(Optional.of(user));
        
        // when & then
        assertThatThrownBy(() -> userService.signup(signupRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
    }
    
    @Test
    @DisplayName("이메일 중복 확인 - 사용 가능한 이메일")
    void isEmailAvailable_Success_AvailableEmail() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        
        // when
        boolean result = userService.isEmailAvailable("test@example.com");
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("이메일 중복 확인 - 이미 사용 중인 이메일")
    void isEmailAvailable_Fail_DuplicateEmail() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
        
        // when
        boolean result = userService.isEmailAvailable("test@example.com");
        
        // then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("이메일 중복 확인 - 잘못된 형식")
    void isEmailAvailable_Fail_InvalidFormat() {
        // when & then
        assertThat(userService.isEmailAvailable("invalid-email")).isFalse();
        assertThat(userService.isEmailAvailable("")).isFalse();
        assertThat(userService.isEmailAvailable(null)).isFalse();
    }
}