package com.sesac.solbid.dto.api;

import lombok.Builder;
import lombok.Getter;

/**
 * 공통 API 응답 형식 DTO
 * 기존 컨트롤러의 응답 형식과 일관성 유지
 */
@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String message;

    @Builder
    public ApiResponse(boolean success, T data, String errorCode, String message) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * 성공 응답 생성
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .errorCode(null)
                .message(null)
                .build();
    }

    /**
     * 성공 응답 생성 (메시지 포함)
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .errorCode(null)
                .message(message)
                .build();
    }

    /**
     * 실패 응답 생성
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(null)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    /**
     * 실패 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> error(T data, String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .data(data)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}