package com.sesac.solbid.exception;

import com.sesac.solbid.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 표준 스키마: { success, errorCode, message, data }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_PARAMETER", "매개변수 값이 잘못되었습니다: " + e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DB_CONSTRAINT_VIOLATION", "DB 제약조건 위반: " + e.getMessage()));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransaction(TransactionSystemException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("TRANSACTION_FAILED", "트랜잭션 실패: " + e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessage(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_INPUT_VALUE", "요청 값이 잘못되었습니다: " + e.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error("UNSUPPORTED_MEDIA_TYPE", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("PARAMETER_TYPE_MISMATCH", "요청 파라미터 타입이 잘못되었습니다: " + e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("유효성 검사 실패");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_INPUT_VALUE", errorMessage));
    }

    // JWT/서명 관련 예외는 401로 응답
    @ExceptionHandler({io.jsonwebtoken.JwtException.class, io.jsonwebtoken.security.SecurityException.class})
    public ResponseEntity<ApiResponse<Void>> handleJwtException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", "유효하지 않은 토큰입니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(HttpServletRequest request, Exception e) throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith("/files/")) {
            throw e;
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }

    // 이메일 인증 관련 예외
    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmailVerificationException(EmailVerificationException e) {
        ErrorCode code = e.getErrorCode();
        
        // 이메일 인증 관련 예외는 추가 로깅
        String maskedEmail = e.getMaskedEmail();
        String additionalInfo = e.getAdditionalInfo();
        
        if (additionalInfo != null) {
            log.warn("이메일 인증 예외 발생: {} - {} ({})", code.name(), maskedEmail, additionalInfo);
        } else {
            log.warn("이메일 인증 예외 발생: {} - {}", code.name(), maskedEmail);
        }
        
        // 재전송 제한 관련 예외의 경우 추가 정보 제공
        if (code == ErrorCode.EMAIL_VERIFICATION_RESEND_TOO_FREQUENT || 
            code == ErrorCode.EMAIL_VERIFICATION_RESEND_LIMIT_EXCEEDED) {
            
            Object data = null;
            if (additionalInfo != null) {
                // 추가 정보가 있는 경우 (예: 남은 시간, 재시도 가능 시간 등)
                data = java.util.Map.of("additionalInfo", additionalInfo);
            }
            
            return ResponseEntity.status(code.getStatus())
                    .body(ApiResponse.error(data, code.name(), code.getMessage()));
        }
        
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.error(code.name(), code.getMessage()));
    }

    // 비밀번호 재설정 관련 예외
    @ExceptionHandler(PasswordResetException.class)
    public ResponseEntity<ApiResponse<Object>> handlePasswordResetException(PasswordResetException e) {
        ErrorCode code = e.getErrorCode();
        
        // 비밀번호 재설정 관련 예외는 추가 로깅
        String maskedEmail = e.getMaskedEmail();
        String additionalInfo = e.getAdditionalInfo();
        
        if (additionalInfo != null) {
            log.warn("비밀번호 재설정 예외 발생: {} - {} ({})", code.name(), maskedEmail, additionalInfo);
        } else {
            log.warn("비밀번호 재설정 예외 발생: {} - {}", code.name(), maskedEmail);
        }
        
        // 재전송 제한 관련 예외의 경우 추가 정보 제공
        if (code == ErrorCode.PASSWORD_RESET_RESEND_TOO_FREQUENT || 
            code == ErrorCode.PASSWORD_RESET_RESEND_LIMIT_EXCEEDED) {
            
            Object data = null;
            if (additionalInfo != null) {
                // 추가 정보가 있는 경우 (예: 남은 시간, 재시도 가능 시간 등)
                data = java.util.Map.of("additionalInfo", additionalInfo);
            }
            
            return ResponseEntity.status(code.getStatus())
                    .body(ApiResponse.error(data, code.name(), code.getMessage()));
        }
        
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.error(code.name(), code.getMessage()));
    }

    // 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.error(code.name(), code.getMessage()));
    }
}
