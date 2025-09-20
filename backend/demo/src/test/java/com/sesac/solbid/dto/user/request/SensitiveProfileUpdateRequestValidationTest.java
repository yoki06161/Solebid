package com.sesac.solbid.dto.user.request;

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
 * SensitiveProfileUpdateRequest DTO 유효성 검사 테스트
 * <p>
 * 민감한 프로필 업데이트 요청 DTO의 유효성 검사 규칙을 테스트합니다.
 * 현재 비밀번호, 이메일, 전화번호 필드의 유효성 검사를 확인합니다.
 * </p>
 */
@DisplayName("SensitiveProfileUpdateRequest DTO 유효성 검사 테스트")
class SensitiveProfileUpdateRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // === 현재 비밀번호 유효성 검사 테스트 ===

    @Test
    @DisplayName("유효한 현재 비밀번호 - 검증 통과")
    void currentPassword_valid_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "validPassword123!", null, null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("현재 비밀번호 null - 검증 실패")
    void currentPassword_null_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            null, "test@example.com", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("currentPassword");
        assertThat(violation.getMessage()).isEqualTo("현재 비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("현재 비밀번호 빈 문자열 - 검증 실패")
    void currentPassword_empty_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "", "test@example.com", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("currentPassword");
        assertThat(violation.getMessage()).isEqualTo("현재 비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("현재 비밀번호 공백만 포함 - 검증 실패")
    void currentPassword_onlySpaces_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "   ", "test@example.com", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("currentPassword");
        assertThat(violation.getMessage()).isEqualTo("현재 비밀번호는 필수입니다");
    }

    // === 이메일 유효성 검사 테스트 ===

    @Test
    @DisplayName("유효한 이메일 - 검증 통과")
    void email_valid_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "test@example.com", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이메일 null - 검증 통과 (선택적 필드)")
    void email_null_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "010-1234-5678");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이메일 빈 문자열 - 검증 통과 (선택적 필드)")
    void email_empty_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "", "010-1234-5678");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("잘못된 이메일 형식 - @ 없음 - 검증 실패")
    void email_invalidFormat_noAtSign_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "testexample.com", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(violation.getMessage()).isEqualTo("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("잘못된 이메일 형식 - 도메인 없음 - 검증 실패")
    void email_invalidFormat_noDomain_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "test@", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(violation.getMessage()).isEqualTo("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("잘못된 이메일 형식 - 로컬 부분 없음 - 검증 실패")
    void email_invalidFormat_noLocalPart_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "@example.com", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
        assertThat(violation.getMessage()).isEqualTo("올바른 이메일 형식이 아닙니다");
    }

    @Test
    @DisplayName("복잡한 유효한 이메일 - 검증 통과")
    void email_complexValid_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "user.name+tag@example-domain.co.kr", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    // === 전화번호 유효성 검사 테스트 ===

    @Test
    @DisplayName("유효한 전화번호 - 검증 통과")
    void phone_valid_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "010-1234-5678");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("전화번호 null - 검증 통과 (선택적 필드)")
    void phone_null_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "test@example.com", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("전화번호 빈 문자열 - 검증 실패 (@Pattern은 빈 문자열도 검증)")
    void phone_empty_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "test@example.com", "");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("phone");
        assertThat(violation.getMessage()).isEqualTo("전화번호 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("잘못된 전화번호 형식 - 하이픈 없음 - 검증 실패")
    void phone_invalidFormat_noHyphen_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "01012345678");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("phone");
        assertThat(violation.getMessage()).isEqualTo("전화번호 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("잘못된 전화번호 형식 - 010으로 시작하지 않음 - 검증 실패")
    void phone_invalidFormat_notStartWith010_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "011-1234-5678");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("phone");
        assertThat(violation.getMessage()).isEqualTo("전화번호 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("잘못된 전화번호 형식 - 중간 번호 길이 틀림 - 검증 실패")
    void phone_invalidFormat_wrongMiddleLength_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "010-123-5678");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("phone");
        assertThat(violation.getMessage()).isEqualTo("전화번호 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("잘못된 전화번호 형식 - 마지막 번호 길이 틀림 - 검증 실패")
    void phone_invalidFormat_wrongLastLength_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "010-1234-567");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("phone");
        assertThat(violation.getMessage()).isEqualTo("전화번호 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("잘못된 전화번호 형식 - 문자 포함 - 검증 실패")
    void phone_invalidFormat_withLetters_shouldFailValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "010-abcd-5678");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<SensitiveProfileUpdateRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("phone");
        assertThat(violation.getMessage()).isEqualTo("전화번호 형식이 올바르지 않습니다");
    }

    // === hasUpdates() 메서드 테스트 ===

    @Test
    @DisplayName("hasUpdates() - 이메일만 있음 - true 반환")
    void hasUpdates_emailOnly_shouldReturnTrue() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "test@example.com", null);

        // When & Then
        assertThat(request.hasUpdates()).isTrue();
    }

    @Test
    @DisplayName("hasUpdates() - 전화번호만 있음 - true 반환")
    void hasUpdates_phoneOnly_shouldReturnTrue() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "010-1234-5678");

        // When & Then
        assertThat(request.hasUpdates()).isTrue();
    }

    @Test
    @DisplayName("hasUpdates() - 이메일과 전화번호 모두 있음 - true 반환")
    void hasUpdates_bothEmailAndPhone_shouldReturnTrue() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "test@example.com", "010-1234-5678");

        // When & Then
        assertThat(request.hasUpdates()).isTrue();
    }

    @Test
    @DisplayName("hasUpdates() - 이메일과 전화번호 모두 null - false 반환")
    void hasUpdates_bothNull_shouldReturnFalse() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, null);

        // When & Then
        assertThat(request.hasUpdates()).isFalse();
    }

    @Test
    @DisplayName("hasUpdates() - 이메일과 전화번호 모두 빈 문자열 - false 반환")
    void hasUpdates_bothEmpty_shouldReturnFalse() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "", "");

        // When & Then
        assertThat(request.hasUpdates()).isFalse();
    }

    @Test
    @DisplayName("hasUpdates() - 이메일과 전화번호 모두 공백만 포함 - false 반환")
    void hasUpdates_bothBlank_shouldReturnFalse() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "   ", "   ");

        // When & Then
        assertThat(request.hasUpdates()).isFalse();
    }

    @Test
    @DisplayName("hasUpdates() - 이메일 공백, 전화번호 유효 - true 반환")
    void hasUpdates_emailBlankPhoneValid_shouldReturnTrue() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "   ", "010-1234-5678");

        // When & Then
        assertThat(request.hasUpdates()).isTrue();
    }

    // === 복합 유효성 검사 테스트 ===

    @Test
    @DisplayName("모든 필드 유효 - 검증 통과")
    void allFields_valid_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "test@example.com", "010-1234-5678");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("여러 필드 동시 검증 실패 - 모든 오류 반환")
    void multipleFields_invalid_shouldReturnAllErrors() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "", "invalid-email", "invalid-phone");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3);
        
        // 각 필드별 오류 확인
        boolean hasPasswordError = violations.stream()
            .anyMatch(v -> "currentPassword".equals(v.getPropertyPath().toString()) && 
                          "현재 비밀번호는 필수입니다".equals(v.getMessage()));
        boolean hasEmailError = violations.stream()
            .anyMatch(v -> "email".equals(v.getPropertyPath().toString()) && 
                          "올바른 이메일 형식이 아닙니다".equals(v.getMessage()));
        boolean hasPhoneError = violations.stream()
            .anyMatch(v -> "phone".equals(v.getPropertyPath().toString()) && 
                          "전화번호 형식이 올바르지 않습니다".equals(v.getMessage()));
        
        assertThat(hasPasswordError).isTrue();
        assertThat(hasEmailError).isTrue();
        assertThat(hasPhoneError).isTrue();
    }

    // === 경계값 테스트 ===

    @Test
    @DisplayName("전화번호 경계값 - 정확히 13자 - 검증 통과")
    void phone_exactLength_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "010-0000-0000");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("전화번호 다양한 숫자 조합 - 검증 통과")
    void phone_variousNumbers_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", null, "010-9999-1111");

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이메일 다양한 도메인 - 검증 통과")
    void email_variousDomains_shouldPassValidation() {
        // Given
        SensitiveProfileUpdateRequest request = new SensitiveProfileUpdateRequest(
            "password123", "user@subdomain.example.org", null);

        // When
        Set<ConstraintViolation<SensitiveProfileUpdateRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}