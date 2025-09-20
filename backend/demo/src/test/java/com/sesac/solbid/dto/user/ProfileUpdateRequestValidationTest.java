package com.sesac.solbid.dto.user;

import com.sesac.solbid.dto.user.request.ProfileUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProfileUpdateRequest DTO 유효성 검사 테스트
 * <p>
 * 일반 프로필 업데이트 요청 DTO의 유효성 검사 규칙을 테스트합니다.
 * 닉네임, 이름 필드의 유효성 검사를 확인합니다.
 * </p>
 */
@DisplayName("ProfileUpdateRequest DTO 유효성 검사 테스트")
class ProfileUpdateRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // === 닉네임 유효성 검사 테스트 ===

    @Test
    @DisplayName("유효한 닉네임 - 검증 통과")
    void nickname_valid_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("유효한닉네임", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("닉네임 null - 검증 통과 (선택적 필드)")
    void nickname_null_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, "홍길동");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("닉네임 최소 길이 (2자) - 검증 통과")
    void nickname_minLength_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("닉네", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("닉네임 최대 길이 (50자) - 검증 통과")
    void nickname_maxLength_shouldPassValidation() {
        // Given
        String longNickname = "a".repeat(50);
        ProfileUpdateRequest request = new ProfileUpdateRequest(longNickname, null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("닉네임 너무 짧음 (1자) - 검증 실패")
    void nickname_tooShort_shouldFailValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("닉", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nickname");
        assertThat(violation.getMessage()).isEqualTo("닉네임은 2-50자 사이여야 합니다");
    }

    @Test
    @DisplayName("닉네임 너무 김 (51자) - 검증 실패")
    void nickname_tooLong_shouldFailValidation() {
        // Given
        String longNickname = "a".repeat(51);
        ProfileUpdateRequest request = new ProfileUpdateRequest(longNickname, null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nickname");
        assertThat(violation.getMessage()).isEqualTo("닉네임은 2-50자 사이여야 합니다");
    }

    @Test
    @DisplayName("닉네임 빈 문자열 - 검증 실패")
    void nickname_empty_shouldFailValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nickname");
        assertThat(violation.getMessage()).isEqualTo("닉네임은 2-50자 사이여야 합니다");
    }

    @Test
    @DisplayName("닉네임 공백만 포함 - 검증 통과 (Size 어노테이션은 공백을 문자로 인식)")
    void nickname_onlySpaces_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("   ", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        // @Size 어노테이션은 공백도 문자로 인식하므로 3자로 계산되어 검증 통과
        assertThat(violations).isEmpty();
    }

    // === 이름 유효성 검사 테스트 ===

    @Test
    @DisplayName("유효한 이름 - 검증 통과")
    void name_valid_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, "홍길동");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름 null - 검증 통과 (선택적 필드)")
    void name_null_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("닉네임", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름 최소 길이 (2자) - 검증 통과")
    void name_minLength_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, "홍길");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름 최대 길이 (50자) - 검증 통과")
    void name_maxLength_shouldPassValidation() {
        // Given
        String longName = "김".repeat(50);
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, longName);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름 너무 짧음 (1자) - 검증 실패")
    void name_tooShort_shouldFailValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, "홍");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("이름은 2-50자 사이여야 합니다");
    }

    @Test
    @DisplayName("이름 너무 김 (51자) - 검증 실패")
    void name_tooLong_shouldFailValidation() {
        // Given
        String longName = "김".repeat(51);
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, longName);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("이름은 2-50자 사이여야 합니다");
    }

    @Test
    @DisplayName("이름 빈 문자열 - 검증 실패")
    void name_empty_shouldFailValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, "");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<ProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
        assertThat(violation.getMessage()).isEqualTo("이름은 2-50자 사이여야 합니다");
    }

    // === 복합 유효성 검사 테스트 ===

    @Test
    @DisplayName("모든 필드 유효 - 검증 통과")
    void allFields_valid_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("새닉네임", "홍길동");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("모든 필드 null - 검증 통과 (모든 필드 선택적)")
    void allFields_null_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("여러 필드 동시 검증 실패 - 모든 오류 반환")
    void multipleFields_invalid_shouldReturnAllErrors() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("닉", "홍");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        
        // 각 필드별 오류 확인
        boolean hasNicknameError = violations.stream()
            .anyMatch(v -> "nickname".equals(v.getPropertyPath().toString()) && 
                          "닉네임은 2-50자 사이여야 합니다".equals(v.getMessage()));
        boolean hasNameError = violations.stream()
            .anyMatch(v -> "name".equals(v.getPropertyPath().toString()) && 
                          "이름은 2-50자 사이여야 합니다".equals(v.getMessage()));
        
        assertThat(hasNicknameError).isTrue();
        assertThat(hasNameError).isTrue();
    }

    // === 경계값 테스트 ===

    @Test
    @DisplayName("닉네임 한글 2자 - 검증 통과")
    void nickname_koreanTwoChars_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("한글", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("닉네임 영문 2자 - 검증 통과")
    void nickname_englishTwoChars_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("ab", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("닉네임 숫자 2자 - 검증 통과")
    void nickname_numberTwoChars_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("12", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("닉네임 특수문자 포함 - 검증 통과")
    void nickname_withSpecialChars_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest("닉네임_123", null);

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름 한글 성명 - 검증 통과")
    void name_koreanFullName_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, "김철수");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이름 영문 성명 - 검증 통과")
    void name_englishFullName_shouldPassValidation() {
        // Given
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, "John Doe");

        // When
        Set<ConstraintViolation<ProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}