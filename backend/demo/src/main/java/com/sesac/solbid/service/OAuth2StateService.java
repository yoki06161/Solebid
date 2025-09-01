package com.sesac.solbid.service;

public interface OAuth2StateService {
    String generateState();
    boolean validateState(String state);
    void consumeState(String state);
    void removeState(String state);
    int getStateCount();
}