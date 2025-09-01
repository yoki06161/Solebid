package com.sesac.solbid.exception;

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

    // 포인트 관련 에러
    POINT_NOT_FOUND(404, "해당 사용자의 포인트 정보가 존재하지 않습니다."),
    INSUFFICIENT_POINT(400, "포인트가 부족합니다."),
    POINT_TRANSACTION_ERROR(500, "포인트 트랜잭션 처리 중 오류가 발생했습니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
