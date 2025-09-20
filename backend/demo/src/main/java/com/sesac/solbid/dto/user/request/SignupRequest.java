package com.sesac.solbid.dto.user.request;

import com.sesac.solbid.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 사용자 회원가입 요청 DTO
 * <p>
 * 사용자의 회원가입 요청 시 필요한 정보를 담는 DTO입니다.
 * 각 필드에 대한 유효성 검증을 포함합니다.
 * </p>
 * 
 * @param email 사용자 이메일 주소 (필수, 이메일 형식 검증)
 * @param password 사용자 비밀번호 (필수, 8~20자 영문 대소문자, 숫자, 특수문자 포함)
 * @param nickname 사용자 닉네임 (필수, 2~10자)
 * @param name 사용자 실명 (필수)
 * @param phone 사용자 전화번호 (필수, 한국 휴대폰 번호 형식)
 */
public record SignupRequest(
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'`<>,.?/|~])(?=\\S+$).{8,20}$",
            message = "비밀번호는 8~20자 영문 대소문자, 숫자, 특수문자를 모두 포함하세요.")
    String password,

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
    String nickname,

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    String name,

    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(010\\d{8}|02\\d{7,8}|0[3-6]\\d{8,9}|1[5-8]\\d{6})$", 
             message = "올바른 전화번호 형식이 아닙니다")
    String phone
) {
    /**
     * SignupRequest를 User 엔티티로 변환합니다.
     * 
     * @param encodedPassword 암호화된 비밀번호
     * @return 생성된 User 엔티티
     */
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(this.email)
                .password(encodedPassword)
                .nickname(this.nickname)
                .name(this.name)
                .phone(this.phone)
                .build();
    }
}
