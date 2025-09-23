package com.sesac.solbid.dto.user.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sesac.solbid.domain.enums.UserType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * LoginResponse Record JSON 직렬화/역직렬화 테스트
 */
class LoginResponseJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testJsonSerialization() throws Exception {
        // Given
        LoginResponse response = new LoginResponse(
                1L, 
                "test@example.com", 
                "testuser", 
                "테스트이름",
                "010-1234-5678",
                UserType.USER, 
                "access-token-123", 
                "refresh-token-456"
        );

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertThat(json).contains("\"userId\":1");
        assertThat(json).contains("\"email\":\"test@example.com\"");
        assertThat(json).contains("\"nickname\":\"testuser\"");
        assertThat(json).contains("\"userType\":\"USER\"");
        assertThat(json).contains("\"accessToken\":\"access-token-123\"");
        assertThat(json).contains("\"refreshToken\":\"refresh-token-456\"");
    }

    @Test
    void testJsonDeserialization() throws Exception {
        // Given
        String json = "{\"userId\":1,\"email\":\"test@example.com\",\"nickname\":\"testuser\"," +
                      "\"userType\":\"USER\",\"accessToken\":\"access-token-123\"," +
                      "\"refreshToken\":\"refresh-token-456\"}";

        // When
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        // Then
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("testuser");
        assertThat(response.userType()).isEqualTo(UserType.USER);
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
    }

    @Test
    void testJsonSerializationWithAdminUserType() throws Exception {
        // Given
        LoginResponse response = new LoginResponse(
                2L, 
                "admin@example.com", 
                "admin", 
                "관리자이름",
                "010-9999-8888",
                UserType.ADMIN, 
                "admin-access-token", 
                "admin-refresh-token"
        );

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertThat(json).contains("\"userType\":\"ADMIN\"");
        assertThat(json).contains("\"email\":\"admin@example.com\"");
    }

    @Test
    void testJsonSerializationWithNullValues() throws Exception {
        // Given
        LoginResponse response = new LoginResponse(
                null, 
                null, 
                null, 
                null, 
                null, 
                null,
                null,
                null
        );

        // When
        String json = objectMapper.writeValueAsString(response);

        // Then
        assertThat(json).contains("\"userId\":null");
        assertThat(json).contains("\"email\":null");
        assertThat(json).contains("\"userType\":null");
    }

    @Test
    void testRoundTripSerialization() throws Exception {
        // Given
        LoginResponse original = new LoginResponse(
                1L, 
                "test@example.com", 
                "testuser", 
                "테스트이름",
                "010-5555-6666",
                UserType.USER, 
                "access-token-123", 
                "refresh-token-456"
        );

        // When
        String json = objectMapper.writeValueAsString(original);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        // Then
        assertThat(deserialized).isEqualTo(original);
        assertThat(deserialized.userId()).isEqualTo(original.userId());
        assertThat(deserialized.email()).isEqualTo(original.email());
        assertThat(deserialized.userType()).isEqualTo(original.userType());
    }
}