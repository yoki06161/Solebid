package com.sesac.solbid.service.user;

import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Set up test properties
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");
        ReflectionTestUtils.setField(emailService, "googleClientId", "test-client-id");
        ReflectionTestUtils.setField(emailService, "googleClientSecret", "test-client-secret");
        ReflectionTestUtils.setField(emailService, "googleRefreshToken", "test-refresh-token");
        ReflectionTestUtils.setField(emailService, "frontendBaseUrl", "http://localhost:5173");
    }

    @Test
    void testMaskEmail() {
        // Use reflection to access private method for testing
        String result1 = invokePrivateMethod("maskEmail", "test@example.com");
        assertEquals("te****@example.com", result1);

        String result2 = invokePrivateMethod("maskEmail", "a@example.com");
        assertEquals("**@example.com", result2);

        String result3 = invokePrivateMethod("maskEmail", "ab@example.com");
        assertEquals("**@example.com", result3);

        String result4 = invokePrivateMethodWithNull("maskEmail");
        assertEquals("****", result4);

        String result5 = invokePrivateMethod("maskEmail", "invalid-email");
        assertEquals("****", result5);
    }

    @Test
    void testCreateVerificationEmailTemplate() {
        String verificationCode = "123456";
        String template = invokePrivateMethod("createVerificationEmailTemplate", verificationCode);
        
        assertNotNull(template);
        assertTrue(template.contains("이메일 인증번호 안내"));
        assertTrue(template.contains(verificationCode));
        assertTrue(template.contains("SoleBid"));
        assertTrue(template.contains("5분 후에 만료"));
    }





    // Note: Integration tests for actual email sending would require Gmail API credentials
    // and should be run separately with proper test configuration

    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethod(String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : String.class;
            }
            
            var method = EmailService.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (T) method.invoke(emailService, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method: " + methodName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethodWithNull(String methodName) {
        try {
            var method = EmailService.class.getDeclaredMethod(methodName, String.class);
            method.setAccessible(true);
            return (T) method.invoke(emailService, (String) null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method: " + methodName, e);
        }
    }
}