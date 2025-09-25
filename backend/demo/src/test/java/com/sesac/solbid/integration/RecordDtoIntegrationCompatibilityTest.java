package com.sesac.solbid.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.domain.User;
import com.sesac.solbid.domain.enums.UserType;
import com.sesac.solbid.dto.user.request.SignupRequest;
import com.sesac.solbid.dto.user.response.LoginResponse;
import com.sesac.solbid.dto.user.response.SignupResponse;
import com.sesac.solbid.repository.user.UserRepository;
import com.sesac.solbid.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Record DTO의 전체 통합 호환성 테스트
 * 
 * 테스트 범위:
 * - Controller → Service → Repository 전체 플로우에서 Record DTO 호환성
 * - 실제 데이터베이스 연동을 통한 end-to-end 테스트
 * - 비즈니스 로직과 Record DTO의 완전한 통합 검증
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class RecordDtoIntegrationCompatibilityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("SignupRequest Record가 전체 회원가입 플로우에서 정상 동작한다")
    void signupRequest_worksInCompleteSignupFlow() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "integration@example.com",
                "Password123!",
                "integrationuser",
                "Integration User",
                "01012345678"
        );
        String requestJson = objectMapper.writeValueAsString(signupRequest);

        // when
        MvcResult result = mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("integrationuser"))
                .andReturn();

        // then - 데이터베이스에 실제로 저장되었는지 확인
        User savedUser = userRepository.findByEmail("integration@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("integration@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("integrationuser");
        assertThat(savedUser.getName()).isEqualTo("Integration User");
        assertThat(savedUser.getPhone()).isEqualTo("01012345678");
        assertThat(savedUser.getPassword()).isNotBlank(); // 암호화되어 저장됨

        // 응답 JSON 구조 검증
        String responseJson = result.getResponse().getContentAsString();
        assertThat(responseJson).contains("\"email\":\"integration@example.com\"");
        assertThat(responseJson).contains("\"nickname\":\"integrationuser\"");
    }

    @Test
    @DisplayName("SignupRequest의 toEntity() 메서드가 서비스 레이어에서 정상 동작한다")
    void signupRequest_toEntity_worksInServiceLayer() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "service@example.com",
                "Password123!",
                "serviceuser",
                "Service User",
                "01087654321"
        );

        // when
        User savedUser = userService.signup(signupRequest);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("service@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("serviceuser");
        assertThat(savedUser.getName()).isEqualTo("Service User");
        assertThat(savedUser.getPhone()).isEqualTo("01087654321");
        assertThat(savedUser.getUserType()).isEqualTo(UserType.USER);

        // 데이터베이스에서 다시 조회하여 확인
        User retrievedUser = userRepository.findByEmail("service@example.com").orElse(null);
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getEmail()).isEqualTo(savedUser.getEmail());
        assertThat(retrievedUser.getNickname()).isEqualTo(savedUser.getNickname());
    }

    @Test
    @DisplayName("SignupResponse Record가 복잡한 생성자 로직과 함께 정상 동작한다")
    void signupResponse_complexConstructor_worksInIntegration() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "response@example.com",
                "Password123!",
                "responseuser",
                "Response User",
                "01011111111"
        );

        // when
        User savedUser = userService.signup(signupRequest);
        SignupResponse response = new SignupResponse(savedUser);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(savedUser.getUserId());
        assertThat(response.email()).isEqualTo("response@example.com");
        assertThat(response.nickname()).isEqualTo("responseuser");
        assertThat(response.emailVerified()).isFalse();
        assertThat(response.message()).isEqualTo("회원가입이 완료되었습니다.");
        assertThat(response.nextStep()).contains("이메일 인증");
    }

    @Test
    @DisplayName("LoginResponse Record의 정적 팩토리 메서드가 실제 사용자 데이터와 함께 정상 동작한다")
    void loginResponse_staticFactory_worksWithRealUserData() {
        // given - 실제 사용자 생성
        SignupRequest signupRequest = new SignupRequest(
                "login@example.com",
                "Password123!",
                "loginuser",
                "Login User",
                "01022222222"
        );
        User savedUser = userService.signup(signupRequest);

        String accessToken = "real-access-token-123";
        String refreshToken = "real-refresh-token-456";

        // when
        LoginResponse response = LoginResponse.from(savedUser, accessToken, refreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(savedUser.getUserId());
        assertThat(response.email()).isEqualTo("login@example.com");
        assertThat(response.nickname()).isEqualTo("loginuser");
        assertThat(response.userType()).isEqualTo(UserType.USER);
        assertThat(response.accessToken()).isEqualTo("real-access-token-123");
        assertThat(response.refreshToken()).isEqualTo("real-refresh-token-456");
    }

    @Test
    @DisplayName("Record DTO의 JSON 직렬화/역직렬화가 전체 플로우에서 호환성을 유지한다")
    void recordDto_jsonCompatibility_maintainedInCompleteFlow() throws Exception {
        // given
        String originalJson = """
                {
                    "email": "json@example.com",
                    "password": "Password123!",
                    "nickname": "jsonuser",
                    "name": "JSON User",
                    "phone": "01033333333"
                }
                """;

        // when - JSON → Record → Service → Database → Response
        MvcResult result = mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(originalJson))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        String responseJson = result.getResponse().getContentAsString();
        
        // 응답 JSON 구조 검증
        assertThat(responseJson).contains("\"email\":\"json@example.com\"");
        assertThat(responseJson).contains("\"nickname\":\"jsonuser\"");

        // 데이터베이스에서 실제 저장된 데이터 확인
        User savedUser = userRepository.findByEmail("json@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("json@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("jsonuser");
        assertThat(savedUser.getName()).isEqualTo("JSON User");
        assertThat(savedUser.getPhone()).isEqualTo("01033333333");
    }

    @Test
    @DisplayName("Record DTO의 불변성이 서비스 레이어에서 보장된다")
    void recordDto_immutability_guaranteedInServiceLayer() {
        // given
        SignupRequest originalRequest = new SignupRequest(
                "immutable@example.com",
                "Password123!",
                "immutableuser",
                "Immutable User",
                "01044444444"
        );

        // when
        User savedUser = userService.signup(originalRequest);

        // then - 원본 Record는 변경되지 않음
        assertThat(originalRequest.email()).isEqualTo("immutable@example.com");
        assertThat(originalRequest.nickname()).isEqualTo("immutableuser");
        
        // 저장된 사용자 데이터는 정상적으로 처리됨
        assertThat(savedUser.getEmail()).isEqualTo("immutable@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("immutableuser");
    }

    @Test
    @DisplayName("여러 Record DTO가 함께 사용되는 복합 시나리오에서 정상 동작한다")
    void multipleRecordDtos_workTogetherInComplexScenario() {
        // given - 회원가입
        SignupRequest signupRequest = new SignupRequest(
                "complex@example.com",
                "Password123!",
                "complexuser",
                "Complex User",
                "01055555555"
        );

        // when - 회원가입 → 응답 생성 → 로그인 응답 생성
        User savedUser = userService.signup(signupRequest);
        SignupResponse signupResponse = new SignupResponse(savedUser);
        LoginResponse loginResponse = LoginResponse.from(savedUser, "access-token", "refresh-token");

        // then - 모든 Record DTO가 일관된 데이터를 유지
        assertThat(signupResponse.email()).isEqualTo(loginResponse.email());
        assertThat(signupResponse.nickname()).isEqualTo(loginResponse.nickname());
        assertThat(signupResponse.userId()).isEqualTo(loginResponse.userId());
    }
}