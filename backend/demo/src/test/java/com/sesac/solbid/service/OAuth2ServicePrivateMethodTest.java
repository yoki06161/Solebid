package com.sesac.solbid.service;

import com.sesac.solbid.service.auth.OAuth2Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2Service Private 메서드 테스트")
class OAuth2ServicePrivateMethodTest {

    @Test
    @DisplayName("이메일 마스킹 처리 테스트")
    void maskEmail_Test() throws Exception {
        // Given
        OAuth2Service service = new OAuth2Service(null, null, null, null, null);
        Method maskEmailMethod = OAuth2Service.class.getDeclaredMethod("maskEmail", String.class);
        maskEmailMethod.setAccessible(true);

        // When & Then
        // 정상적인 이메일
        String result1 = (String) maskEmailMethod.invoke(service, "test@example.com");
        assertThat(result1).isEqualTo("te****@example.com");

        // 짧은 이메일
        String result2 = (String) maskEmailMethod.invoke(service, "a@b.com");
        assertThat(result2).isEqualTo("**@b.com");

        // null 이메일
        String result3 = (String) maskEmailMethod.invoke(service, (String) null);
        assertThat(result3).isEqualTo("****");

        // @ 없는 문자열
        String result4 = (String) maskEmailMethod.invoke(service, "notanemail");
        assertThat(result4).isEqualTo("****");

        // 빈 문자열
        String result5 = (String) maskEmailMethod.invoke(service, "");
        assertThat(result5).isEqualTo("****");

        // 긴 이메일
        String result6 = (String) maskEmailMethod.invoke(service, "verylongemail@example.com");
        assertThat(result6).isEqualTo("ve****@example.com");

        // 특수문자가 포함된 이메일
        String result7 = (String) maskEmailMethod.invoke(service, "test.user+tag@example.com");
        assertThat(result7).isEqualTo("te****@example.com");
    }

    @Test
    @DisplayName("민감한 데이터 마스킹 처리 테스트")
    void maskSensitiveData_Test() throws Exception {
        // Given
        OAuth2Service service = new OAuth2Service(null, null, null, null, null);
        Method maskSensitiveDataMethod = OAuth2Service.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskSensitiveDataMethod.setAccessible(true);

        // When & Then
        // access_token 마스킹
        String jsonWithToken = "{\"access_token\":\"secret-token-123\",\"user\":\"test\"}";
        String result1 = (String) maskSensitiveDataMethod.invoke(service, jsonWithToken);
        assertThat(result1).contains("\"access_token\":\"****\"");
        assertThat(result1).doesNotContain("secret-token-123");

        // refresh_token 마스킹
        String jsonWithRefreshToken = "{\"refresh_token\":\"refresh-secret-456\",\"user\":\"test\"}";
        String result2 = (String) maskSensitiveDataMethod.invoke(service, jsonWithRefreshToken);
        assertThat(result2).contains("\"refresh_token\":\"****\"");
        assertThat(result2).doesNotContain("refresh-secret-456");

        // email 마스킹
        String jsonWithEmail = "{\"email\":\"user@example.com\",\"name\":\"test\"}";
        String result3 = (String) maskSensitiveDataMethod.invoke(service, jsonWithEmail);
        assertThat(result3).contains("\"email\":\"**@example.com\"");
        assertThat(result3).doesNotContain("user@example.com");

        // client_secret 마스킹
        String jsonWithSecret = "{\"client_secret\":\"very-secret-key\",\"client_id\":\"public\"}";
        String result4 = (String) maskSensitiveDataMethod.invoke(service, jsonWithSecret);
        assertThat(result4).contains("\"client_secret\":\"****\"");
        assertThat(result4).doesNotContain("very-secret-key");

        // code 마스킹
        String jsonWithCode = "{\"code\":\"authorization-code-123\",\"state\":\"state-456\"}";
        String result5 = (String) maskSensitiveDataMethod.invoke(service, jsonWithCode);
        assertThat(result5).contains("\"code\":\"****\"");
        assertThat(result5).doesNotContain("authorization-code-123");

        // phone 마스킹
        String jsonWithPhone = "{\"phone\":\"010-1234-5678\",\"name\":\"test\"}";
        String result6 = (String) maskSensitiveDataMethod.invoke(service, jsonWithPhone);
        assertThat(result6).contains("\"phone\":\"****\"");
        assertThat(result6).doesNotContain("010-1234-5678");

        // null 데이터
        String result7 = (String) maskSensitiveDataMethod.invoke(service, (String) null);
        assertThat(result7).isEqualTo("****");

        // 빈 데이터
        String result8 = (String) maskSensitiveDataMethod.invoke(service, "");
        assertThat(result8).isEqualTo("****");
    }

    @Test
    @DisplayName("긴 응답 데이터 잘라내기 테스트")
    void maskSensitiveData_LongResponse_Test() throws Exception {
        // Given
        OAuth2Service service = new OAuth2Service(null, null, null, null, null);
        Method maskSensitiveDataMethod = OAuth2Service.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskSensitiveDataMethod.setAccessible(true);

        // 긴 응답 생성 (500자 초과)
        StringBuilder longResponse = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longResponse.append("This is a very long response data. ");
        }

        // When
        String result = (String) maskSensitiveDataMethod.invoke(service, longResponse.toString());

        // Then
        assertThat(result).hasSizeLessThanOrEqualTo(520); // 500 + "... [truncated]"
        assertThat(result).endsWith("... [truncated]");
    }

    @Test
    @DisplayName("복합 민감 데이터 마스킹 테스트")
    void maskSensitiveData_Complex_Test() throws Exception {
        // Given
        OAuth2Service service = new OAuth2Service(null, null, null, null, null);
        Method maskSensitiveDataMethod = OAuth2Service.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskSensitiveDataMethod.setAccessible(true);

        // 여러 민감 정보가 포함된 JSON
        String complexJson = "{\n" +
                "  \"access_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
                "  \"refresh_token\": \"refresh_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\",\n" +
                "  \"user\": {\n" +
                "    \"email\": \"sensitive@example.com\",\n" +
                "    \"phone\": \"010-1234-5678\",\n" +
                "    \"name\": \"Public Name\"\n" +
                "  },\n" +
                "  \"client_secret\": \"super-secret-key-123\",\n" +
                "  \"code\": \"authorization-code-456\"\n" +
                "}";

        // When
        String result = (String) maskSensitiveDataMethod.invoke(service, complexJson);

        // Then
        assertThat(result).contains("\"access_token\":\"****\"");
        assertThat(result).contains("\"refresh_token\":\"****\"");
        assertThat(result).contains("\"email\":\"**@example.com\"");
        assertThat(result).contains("\"phone\":\"****\"");
        assertThat(result).contains("\"client_secret\":\"****\"");
        assertThat(result).contains("\"code\":\"****\"");
        assertThat(result).contains("\"name\": \"Public Name\""); // 마스킹되지 않는 필드

        // 원본 민감 정보가 포함되지 않았는지 확인
        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");
        assertThat(result).doesNotContain("sensitive@example.com");
        assertThat(result).doesNotContain("010-1234-5678");
        assertThat(result).doesNotContain("super-secret-key-123");
        assertThat(result).doesNotContain("authorization-code-456");
    }

    @Test
    @DisplayName("다양한 이메일 형식 마스킹 테스트")
    void maskEmail_VariousFormats_Test() throws Exception {
        // Given
        OAuth2Service service = new OAuth2Service(null, null, null, null, null);
        Method maskEmailMethod = OAuth2Service.class.getDeclaredMethod("maskEmail", String.class);
        maskEmailMethod.setAccessible(true);

        // When & Then
        // 일반적인 이메일
        String result1 = (String) maskEmailMethod.invoke(service, "john.doe@company.com");
        assertThat(result1).isEqualTo("jo****@company.com");

        // 짧은 로컬 파트
        String result2 = (String) maskEmailMethod.invoke(service, "ab@test.com");
        assertThat(result2).isEqualTo("**@test.com");

        // 한 글자 로컬 파트
        String result3 = (String) maskEmailMethod.invoke(service, "a@test.com");
        assertThat(result3).isEqualTo("**@test.com");

        // 서브도메인이 있는 이메일
        String result4 = (String) maskEmailMethod.invoke(service, "user@mail.google.com");
        assertThat(result4).isEqualTo("us****@mail.google.com");

        // 숫자가 포함된 이메일
        String result5 = (String) maskEmailMethod.invoke(service, "user123@example.com");
        assertThat(result5).isEqualTo("us****@example.com");

        // 특수문자가 포함된 이메일
        String result6 = (String) maskEmailMethod.invoke(service, "user.name+tag@example.com");
        assertThat(result6).isEqualTo("us****@example.com");

        // 매우 긴 로컬 파트
        String result7 = (String) maskEmailMethod.invoke(service, "verylongusernamethatexceedsnormallength@example.com");
        assertThat(result7).isEqualTo("ve****@example.com");
    }

    @Test
    @DisplayName("JSON 응답에서 다양한 패턴 마스킹 테스트")
    void maskSensitiveData_VariousPatterns_Test() throws Exception {
        // Given
        OAuth2Service service = new OAuth2Service(null, null, null, null, null);
        Method maskSensitiveDataMethod = OAuth2Service.class.getDeclaredMethod("maskSensitiveData", String.class);
        maskSensitiveDataMethod.setAccessible(true);

        // When & Then
        // 공백이 있는 JSON
        String jsonWithSpaces = "{ \"access_token\" : \"token-with-spaces\" , \"user\" : \"test\" }";
        String result1 = (String) maskSensitiveDataMethod.invoke(service, jsonWithSpaces);
        assertThat(result1).contains("\"access_token\":\"****\"");

        // 줄바꿈이 있는 JSON
        String jsonWithNewlines = "{\n  \"access_token\": \"multiline-token\",\n  \"user\": \"test\"\n}";
        String result2 = (String) maskSensitiveDataMethod.invoke(service, jsonWithNewlines);
        assertThat(result2).contains("\"access_token\":\"****\"");

        // 중첩된 JSON
        String nestedJson = "{\"data\":{\"access_token\":\"nested-token\",\"user\":{\"email\":\"nested@example.com\"}}}";
        String result3 = (String) maskSensitiveDataMethod.invoke(service, nestedJson);
        assertThat(result3).contains("\"access_token\":\"****\"");
        assertThat(result3).contains("\"email\":\"**@example.com\"");

        // 배열이 포함된 JSON
        String jsonWithArray = "{\"tokens\":[{\"access_token\":\"array-token-1\"},{\"access_token\":\"array-token-2\"}]}";
        String result4 = (String) maskSensitiveDataMethod.invoke(service, jsonWithArray);
        assertThat(result4).contains("\"access_token\":\"****\"");
        assertThat(result4).doesNotContain("array-token-1");
        assertThat(result4).doesNotContain("array-token-2");

        // 특수문자가 포함된 토큰
        String jsonWithSpecialChars = "{\"access_token\":\"token-with-special-chars!@#$%^&*()\",\"user\":\"test\"}";
        String result5 = (String) maskSensitiveDataMethod.invoke(service, jsonWithSpecialChars);
        assertThat(result5).contains("\"access_token\":\"****\"");
        assertThat(result5).doesNotContain("token-with-special-chars!@#$%^&*()");
    }
}