package com.sesac.solbid.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // 공통
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "매개변수 값이 잘못되었습니다"),
    DB_CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, "DB 제약조건 위반"),
    TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "트랜잭션 실패"),
    BAD_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 값이 잘못되었습니다"),
    PARAMETER_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "요청 파라미터 타입 오류"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "유효성 검사 실패"),
    FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "파일 업로드 실패"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류"),

    // 회원 가입 에러
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일 주소입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),

    // 로그인 에러
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INACTIVE_USER(HttpStatus.UNAUTHORIZED, "비활성화된 계정입니다."),
    WITHDRAWN_USER(HttpStatus.UNAUTHORIZED, "회원 탈퇴 처리된 계정입니다."),
    WITHDRAWAL_GRACE_PERIOD_EXPIRED(HttpStatus.UNAUTHORIZED, "탈퇴 유예 기간이 만료된 계정입니다."),

    // OAuth2 소셜로그인 에러
    INVALID_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다."),
    OAUTH2_STATE_MISMATCH(HttpStatus.BAD_REQUEST, "OAuth2 state 파라미터가 일치하지 않습니다."),
    OAUTH2_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "OAuth2 액세스 토큰 획득에 실패했습니다."),
    OAUTH2_USER_INFO_ERROR(HttpStatus.BAD_REQUEST, "OAuth2 사용자 정보 획득에 실패했습니다."),
    SOCIAL_ACCOUNT_CONFLICT(HttpStatus.CONFLICT, "이미 다른 소셜 계정으로 연결된 이메일입니다."),

    // 비밀번호 재설정 (OTP 방식)
    PASSWORD_RESET_OTP_INVALID(HttpStatus.BAD_REQUEST, "비밀번호 재설정 인증번호가 유효하지 않습니다."),
    PASSWORD_RESET_OTP_EXPIRED(HttpStatus.BAD_REQUEST, "비밀번호 재설정 인증번호가 만료되었습니다."),
    PASSWORD_RESET_OTP_ATTEMPTS_EXCEEDED(HttpStatus.BAD_REQUEST, "인증번호 입력 횟수를 초과했습니다."),
    PASSWORD_RESET_RESEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "재전송 횟수를 초과했습니다."),
    PASSWORD_RESET_RESEND_TOO_FREQUENT(HttpStatus.TOO_MANY_REQUESTS, "인증번호 재전송은 1분 후에 가능합니다."),
    PASSWORD_RESET_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "이전에 사용한 비밀번호와 동일합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    OAUTH_TOKEN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Google OAuth2 Access Token 발급에 실패했습니다."),
    PASSWORD_RESET_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "해당 계정은 비밀번호 재설정을 지원하지 않습니다."),

    // 이메일 인증 관련
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 필요합니다."),
    EMAIL_VERIFICATION_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 토큰입니다."),
    EMAIL_VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "인증 토큰이 만료되었습니다."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "이미 인증된 이메일입니다."),
    EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증 이메일 재전송 횟수를 초과했습니다."),
    EMAIL_VERIFICATION_RESEND_TOO_FREQUENT(HttpStatus.TOO_MANY_REQUESTS, "인증번호 재전송은 1분 후에 가능합니다."),

    // 포인트 관련 에러
    POINT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자의 포인트 정보가 존재하지 않습니다."),
    INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    POINT_TRANSACTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "포인트 트랜잭션 처리 중 오류가 발생했습니다."),

    // 상품 관련 에러
    INVALID_SIZE_RANGE(HttpStatus.BAD_REQUEST, "잘못된 사이즈 범위입니다."),
    THUMBNAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "중복된 썸네일입니다."),
    SORT_ORDER_DUPLICATED(HttpStatus.BAD_REQUEST, "정렬 순서가 중복되었습니다."),
    INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "잘못된 이미지 키입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    FORBIDDEN(HttpStatus.UNAUTHORIZED, "해당 상품에 대한 권한이 없습니다."),

    // S3 관련 에러
    S3_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3 입출력 처리 중 오류가 발생했습니다."),
    UNSUPPORTED_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 Content-Type 입니다. (image/jpeg, image/png만 허용)"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "요청 값이 잘못되었습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),

    // 경매 관련
    AUCTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 진행 중인 경매가 있습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // 정수 코드가 필요한 레거시 사용처 호환용
    public int getStatusValue() {
        return status.value();
    }

    public String getMessage() {
        return message;
    }
}
