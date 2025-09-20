package com.sesac.solbid.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 비밀번호 변경 요청 DTO
 * <p>
 * 사용자의 비밀번호 변경 요청 시 필요한 정보를 담는 DTO입니다.
 * 현재 비밀번호 확인과 새 비밀번호 설정이 필요합니다.
 * </p>
 * 
 * @param currentPassword 현재 비밀번호
 * @param newPassword 새로운 비밀번호 (8~20자, 영문 대소문자, 숫자, 특수문자 포함)
 * @param confirmPassword 새로운 비밀번호 확인
 */
public record PasswordChangeRequest(
    @NotBlank(message = "현재 비밀번호는 필수입니다")
    String currentPassword,
    
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
        message = "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다"
    )
    String newPassword,
    
    @NotBlank(message = "비밀번호 확인은 필수입니다")
    String confirmPassword
) {
    /**
     * 새 비밀번호와 확인 비밀번호가 일치하는지 확인합니다.
     * 
     * @return 비밀번호가 일치하면 true, 그렇지 않으면 false
     */
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
    
    /**
     * 현재 비밀번호와 새 비밀번호가 동일한지 확인합니다.
     * 
     * @return 현재 비밀번호와 새 비밀번호가 동일하면 true
     */
    public boolean isSameAsCurrentPassword() {
        return currentPassword != null && currentPassword.equals(newPassword);
    }
}