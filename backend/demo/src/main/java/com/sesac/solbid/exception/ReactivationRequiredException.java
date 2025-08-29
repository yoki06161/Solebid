package com.sesac.solbid.exception;

public class ReactivationRequiredException extends CustomException {
    private final String email;

    public ReactivationRequiredException(String email) {
        super(ErrorCode.WITHDRAWN_USER);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}

