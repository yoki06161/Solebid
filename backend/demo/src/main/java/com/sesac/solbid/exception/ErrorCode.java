package com.sesac.solbid.exception;

import org.springframework.http.HttpStatus;


public enum ErrorCode {
    INVALID_PARAMETER(400, "매개변수 값이 잘못되었습니다"),
    DB_CONSTRAINT_VIOLATION(409, "DB 제약조건 위반"),
    TRANSACTION_FAILED(500, "트랜잭션 실패"),
    BAD_REQUEST_BODY(400, "요청 값이 잘못되었습니다"),
    PARAMETER_TYPE_MISMATCH(400, "요청 파라미터 타입 오류"),
    VALIDATION_ERROR(400, "유효성 검사 실패"),
    FILE_UPLOAD_FAILED(400, "파일 업로드 실패"),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류"),

    // 회원 가입 에러
    DUPLICATE_EMAIL(400, "이미 사용 중인 이메일 주소입니다."),
    DUPLICATE_NICKNAME(400, "이미 사용 중인 닉네임입니다."),

    // 로그인 에러
    LOGIN_FAILED(401, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INACTIVE_USER(401, "비활성화된 계정입니다."),
    WITHDRAWN_USER(401, "회원 탈퇴 처리된 계정입니다."),
    WITHDRAWAL_GRACE_PERIOD_EXPIRED(401, "탈퇴 유예 기간이 만료된 계정입니다."),

    // OAuth2 소셜로그인 에러
    INVALID_OAUTH2_PROVIDER(400, "지원하지 않는 OAuth2 제공자입니다."),
    OAUTH2_STATE_MISMATCH(400, "OAuth2 state 파라미터가 일치하지 않습니다."),
    OAUTH2_TOKEN_ERROR(400, "OAuth2 액세스 토큰 획득에 실패했습니다."),
    OAUTH2_USER_INFO_ERROR(400, "OAuth2 사용자 정보 획득에 실패했습니다."),
    SOCIAL_ACCOUNT_CONFLICT(409, "이미 다른 소셜 계정으로 연결된 이메일입니다."),

    // 비밀번호 재설정
    PASSWORD_RESET_TOKEN_INVALID(400, "비밀번호 재설정 토큰이 유효하지 않습니다."),
    PASSWORD_RESET_TOKEN_EXPIRED(400, "비밀번호 재설정 토큰이 만료되었습니다."),
    PASSWORD_RESET_SAME_AS_OLD(400, "이전에 사용한 비밀번호와 동일합니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    EMAIL_SEND_FAILED(500, "이메일 발송에 실패했습니다."),
    OAUTH_TOKEN_FAILED(500, "Google OAuth2 Access Token 발급에 실패했습니다."),
    PASSWORD_RESET_NOT_ALLOWED(400, "해당 계정은 비밀번호 재설정을 지원하지 않습니다."),

    // 포인트 관련 에러 (HttpStatus 기반)
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자의 포인트 정보가 존재하지 않습니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    POINT_TRANSACTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "포인트 트랜잭션 처리 중 오류가 발생했습니다."),

    // 상품 관련 에러 (HttpStatus 기반)
    INVALID_SIZE_RANGE(HttpStatus.BAD_REQUEST, "잘못된 사이즈 범위입니다."),
    THUMBNAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "중복된 썸네일입니다."),
    SORT_ORDER_DUPLICATED(HttpStatus.BAD_REQUEST, "정렬 순서가 중복되었습니다."),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "잘못된 이미지 키입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // HttpStatus 생성자
    ErrorCode(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
