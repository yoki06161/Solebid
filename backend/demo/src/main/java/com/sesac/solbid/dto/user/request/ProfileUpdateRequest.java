package com.sesac.solbid.dto.user.request;

import jakarta.validation.constraints.Size;

/**
 * 일반 프로필 업데이트 요청 DTO
 * <p>
 * 사용자의 일반적인 프로필 정보 업데이트 요청 시 필요한 정보를 담는 DTO입니다.
 * 민감하지 않은 정보(닉네임, 이름)만 포함하며, 추가 인증이 필요하지 않습니다.
 * 모든 필드는 선택적이며, null이 아닌 값만 업데이트됩니다.
 * </p>
 * 
 * @param nickname 새로운 닉네임 (선택적, 2~50자)
 * @param name 새로운 이름 (선택적, 2~50자)
 */
public record ProfileUpdateRequest(
    @Size(min = 2, max = 50, message = "닉네임은 2-50자 사이여야 합니다")
    String nickname,
    
    @Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다")
    String name
) {
    /**
     * 업데이트할 필드가 있는지 확인합니다.
     * 
     * @return 닉네임 또는 이름 중 하나라도 업데이트할 값이 있으면 true
     */
    public boolean hasUpdates() {
        return (nickname != null && !nickname.isBlank()) || 
               (name != null && !name.isBlank());
    }
}