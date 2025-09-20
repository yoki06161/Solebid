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
 * PasswordChangeRequest DTO 유효성 검사 테스트
 * <p>
 * 비밀번호 변경 요청 DTO의 유효성 검사 규칙을 테스트합니다.
 * 현재 비밀번호, 새 비밀번호, 확인 비밀번호 필드의 유효성 검사를 확인합니다.
 * </p>
 */
@DisplayName("PasswordChangeRequest DTO 유효성 검사 테스트")
class PasswordChangeRequestValidationTest {

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
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("현재 비밀번호 null - 검증 실패")
    void currentPassword_null_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            null, "NewPassword123!", "NewPassword123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("currentPassword");
        assertThat(violation.getMessage()).isEqualTo("현재 비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("현재 비밀번호 빈 문자열 - 검증 실패")
    void currentPassword_empty_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "", "NewPassword123!", "NewPassword123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("currentPassword");
        assertThat(violation.getMessage()).isEqualTo("현재 비밀번호는 필수입니다");
    }

    @Test
    @DisplayName("현재 비밀번호 공백만 포함 - 검증 실패")
    void currentPassword_onlySpaces_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "   ", "NewPassword123!", "NewPassword123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("currentPassword");
        assertThat(violation.getMessage()).isEqualTo("현재 비밀번호는 필수입니다");
    }

    // === 새 비밀번호 유효성 검사 테스트 ===

    @Test
    @DisplayName("유효한 새 비밀번호 - 모든 조건 만족 - 검증 통과")
    void newPassword_valid_shouldPassValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("새 비밀번호 null - 검증 통과 (@Pattern은 null 값을 검증하지 않음)")
    void newPassword_null_shouldPassValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", null, "NewPassword123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("새 비밀번호 너무 짧음 (7자) - 검증 실패")
    void newPassword_tooShort_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "Pass1!", "Pass1!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("newPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다");
    }

    @Test
    @DisplayName("새 비밀번호 너무 김 (21자) - 검증 실패")
    void newPassword_tooLong_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "VeryLongPassword123!@", "VeryLongPassword123!@");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("newPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다");
    }

    @Test
    @DisplayName("새 비밀번호 소문자 없음 - 검증 실패")
    void newPassword_noLowercase_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "PASSWORD123!", "PASSWORD123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("newPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다");
    }

    @Test
    @DisplayName("새 비밀번호 대문자 없음 - 검증 실패")
    void newPassword_noUppercase_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "password123!", "password123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("newPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다");
    }

    @Test
    @DisplayName("새 비밀번호 숫자 없음 - 검증 실패")
    void newPassword_noDigit_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "Password!", "Password!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("newPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다");
    }

    @Test
    @DisplayName("새 비밀번호 특수문자 없음 - 검증 실패")
    void newPassword_noSpecialChar_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "Password123", "Password123");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("newPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다");
    }

    @Test
    @DisplayName("새 비밀번호 최소 길이 (8자) - 검증 통과")
    void newPassword_minLength_shouldPassValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "Pass123!", "Pass123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("새 비밀번호 최대 길이 (20자) - 검증 통과")
    void newPassword_maxLength_shouldPassValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "VeryLongPassword123!", "VeryLongPassword123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("새 비밀번호 다양한 특수문자 - 검증 통과")
    void newPassword_variousSpecialChars_shouldPassValidation() {
        // Given
        String[] specialChars = {"@", "$", "!", "%", "*", "?", "&"};
        
        for (String specialChar : specialChars) {
            PasswordChangeRequest request = new PasswordChangeRequest(
                "currentPassword123!", "Password123" + specialChar, "Password123" + specialChar);

            // When
            Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Test
    @DisplayName("새 비밀번호 허용되지 않는 특수문자 - 검증 실패")
    void newPassword_invalidSpecialChar_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "Password123#", "Password123#");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("newPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다");
    }

    // === 확인 비밀번호 유효성 검사 테스트 ===

    @Test
    @DisplayName("확인 비밀번호 null - 검증 실패")
    void confirmPassword_null_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", null);

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("confirmPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호 확인은 필수입니다");
    }

    @Test
    @DisplayName("확인 비밀번호 빈 문자열 - 검증 실패")
    void confirmPassword_empty_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("confirmPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호 확인은 필수입니다");
    }

    @Test
    @DisplayName("확인 비밀번호 공백만 포함 - 검증 실패")
    void confirmPassword_onlySpaces_shouldFailValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "   ");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<PasswordChangeRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("confirmPassword");
        assertThat(violation.getMessage()).isEqualTo("비밀번호 확인은 필수입니다");
    }

    // === isPasswordMatching() 메서드 테스트 ===

    @Test
    @DisplayName("isPasswordMatching() - 비밀번호 일치 - true 반환")
    void isPasswordMatching_matching_shouldReturnTrue() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        // When & Then
        assertThat(request.isPasswordMatching()).isTrue();
    }

    @Test
    @DisplayName("isPasswordMatching() - 비밀번호 불일치 - false 반환")
    void isPasswordMatching_notMatching_shouldReturnFalse() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "DifferentPassword123!");

        // When & Then
        assertThat(request.isPasswordMatching()).isFalse();
    }

    @Test
    @DisplayName("isPasswordMatching() - 새 비밀번호 null - false 반환")
    void isPasswordMatching_newPasswordNull_shouldReturnFalse() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", null, "NewPassword123!");

        // When & Then
        assertThat(request.isPasswordMatching()).isFalse();
    }

    @Test
    @DisplayName("isPasswordMatching() - 확인 비밀번호 null - false 반환")
    void isPasswordMatching_confirmPasswordNull_shouldReturnFalse() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", null);

        // When & Then
        assertThat(request.isPasswordMatching()).isFalse();
    }

    @Test
    @DisplayName("isPasswordMatching() - 둘 다 null - false 반환")
    void isPasswordMatching_bothNull_shouldReturnFalse() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", null, null);

        // When & Then
        assertThat(request.isPasswordMatching()).isFalse();
    }

    @Test
    @DisplayName("isPasswordMatching() - 빈 문자열 일치 - true 반환")
    void isPasswordMatching_bothEmpty_shouldReturnTrue() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "", "");

        // When & Then
        assertThat(request.isPasswordMatching()).isTrue();
    }

    // === isSameAsCurrentPassword() 메서드 테스트 ===

    @Test
    @DisplayName("isSameAsCurrentPassword() - 현재 비밀번호와 동일 - true 반환")
    void isSameAsCurrentPassword_same_shouldReturnTrue() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "SamePassword123!", "SamePassword123!", "SamePassword123!");

        // When & Then
        assertThat(request.isSameAsCurrentPassword()).isTrue();
    }

    @Test
    @DisplayName("isSameAsCurrentPassword() - 현재 비밀번호와 다름 - false 반환")
    void isSameAsCurrentPassword_different_shouldReturnFalse() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        // When & Then
        assertThat(request.isSameAsCurrentPassword()).isFalse();
    }

    @Test
    @DisplayName("isSameAsCurrentPassword() - 현재 비밀번호 null - false 반환")
    void isSameAsCurrentPassword_currentPasswordNull_shouldReturnFalse() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            null, "NewPassword123!", "NewPassword123!");

        // When & Then
        assertThat(request.isSameAsCurrentPassword()).isFalse();
    }

    @Test
    @DisplayName("isSameAsCurrentPassword() - 새 비밀번호 null - false 반환")
    void isSameAsCurrentPassword_newPasswordNull_shouldReturnFalse() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", null, "NewPassword123!");

        // When & Then
        assertThat(request.isSameAsCurrentPassword()).isFalse();
    }

    @Test
    @DisplayName("isSameAsCurrentPassword() - 둘 다 null - false 반환")
    void isSameAsCurrentPassword_bothNull_shouldReturnFalse() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            null, null, "NewPassword123!");

        // When & Then
        assertThat(request.isSameAsCurrentPassword()).isFalse();
    }

    // === 복합 유효성 검사 테스트 ===

    @Test
    @DisplayName("모든 필드 유효 - 검증 통과")
    void allFields_valid_shouldPassValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "NewPassword123!", "NewPassword123!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("여러 필드 동시 검증 실패 - 모든 오류 반환")
    void multipleFields_invalid_shouldReturnAllErrors() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "", "weak", "");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(3);
        
        // 각 필드별 오류 확인
        boolean hasCurrentPasswordError = violations.stream()
            .anyMatch(v -> "currentPassword".equals(v.getPropertyPath().toString()) && 
                          "현재 비밀번호는 필수입니다".equals(v.getMessage()));
        boolean hasNewPasswordError = violations.stream()
            .anyMatch(v -> "newPassword".equals(v.getPropertyPath().toString()) && 
                          "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다".equals(v.getMessage()));
        boolean hasConfirmPasswordError = violations.stream()
            .anyMatch(v -> "confirmPassword".equals(v.getPropertyPath().toString()) && 
                          "비밀번호 확인은 필수입니다".equals(v.getMessage()));
        
        assertThat(hasCurrentPasswordError).isTrue();
        assertThat(hasNewPasswordError).isTrue();
        assertThat(hasConfirmPasswordError).isTrue();
    }

    // === 경계값 테스트 ===

    @Test
    @DisplayName("새 비밀번호 복잡한 조합 - 검증 통과")
    void newPassword_complexCombination_shouldPassValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "MyC0mpl3x@P4ssw0rd!", "MyC0mpl3x@P4ssw0rd!");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("새 비밀번호 모든 허용 특수문자 포함 - 검증 통과")
    void newPassword_allAllowedSpecialChars_shouldPassValidation() {
        // Given
        PasswordChangeRequest request = new PasswordChangeRequest(
            "currentPassword123!", "Pass123@$!%*?&", "Pass123@$!%*?&");

        // When
        Set<ConstraintViolation<PasswordChangeRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}