package com.sesac.solbid.exception;

import lombok.Getter;

@Getter
public class PointNotFoundException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.POINT_NOT_FOUND;

    public PointNotFoundException() {
        super(ErrorCode.POINT_NOT_FOUND.getMessage());
    }

}