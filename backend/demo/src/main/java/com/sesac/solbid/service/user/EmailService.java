package com.sesac.solbid.service.user;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.sesac.solbid.exception.CustomException;
import com.sesac.solbid.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

@Slf4j
@Service
public class EmailService {

    private static final String APPLICATION_NAME = "SoleBid";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${google.mail.refresh-token}")
    private String googleRefreshToken;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;



    /**
     * 이메일 인증 메일 전송 (인증번호 방식)
     */
    @Retryable(
        retryFor = {IOException.class, GeneralSecurityException.class, MessagingException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendVerificationEmail(String to, String verificationCode) {
        try {
            Gmail service = createGmailService();
            
            String subject = "[SoleBid] 이메일 인증번호 안내";
            String htmlContent = createVerificationEmailTemplate(verificationCode);
            
            sendEmail(service, to, subject, htmlContent);
            log.info("이메일 인증번호 발송 성공: {}", maskEmail(to));
            
        } catch (Exception e) {
            log.error("이메일 인증번호 발송 실패: {}", maskEmail(to), e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    /**
     * 비밀번호 재설정 OTP 이메일 전송
     */
    @Retryable(
        retryFor = {IOException.class, GeneralSecurityException.class, MessagingException.class},
        maxAttempts = MAX_RETRY_ATTEMPTS,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendPasswordResetOtp(String to, String otp) {
        try {
            Gmail service = createGmailService();
            
            String subject = "[SoleBid] 비밀번호 재설정 인증번호 안내";
            String htmlContent = createPasswordResetOtpTemplate(otp);
            
            sendEmail(service, to, subject, htmlContent);
            log.info("비밀번호 재설정 OTP 발송 성공: {}", maskEmail(to));
            
        } catch (Exception e) {
            log.error("비밀번호 재설정 OTP 발송 실패: {}", maskEmail(to), e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }



    /**
     * Gmail 서비스 클라이언트 생성
     */
    private Gmail createGmailService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(googleClientId, googleClientSecret)
                .build()
                .setRefreshToken(googleRefreshToken);

        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * 이메일 전송 공통 메서드
     */
    private void sendEmail(Gmail service, String to, String subject, String htmlContent) 
            throws MessagingException, IOException {
        MimeMessage mimeMessage = createEmail(to, fromEmail, subject, htmlContent);
        Message message = createMessageWithEmail(mimeMessage);
        service.users().messages().send("me", message).execute();
    }



    /**
     * 이메일 인증번호 메일 템플릿
     */
    private String createVerificationEmailTemplate(String verificationCode) {
        return """
            <div style="max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="background-color: #f8f9fa; padding: 20px; text-align: center; border-bottom: 3px solid #28a745;">
                    <h1 style="color: #28a745; margin: 0;">SoleBid</h1>
                </div>
                <div style="padding: 30px 20px;">
                    <h2 style="color: #333; margin-bottom: 20px;">이메일 인증번호 안내</h2>
                    <p style="margin-bottom: 20px;">안녕하세요!</p>
                    <p style="margin-bottom: 20px;">SoleBid 회원가입을 완료하기 위해 이메일 인증이 필요합니다. 아래 인증번호를 입력하여 이메일 인증을 완료해주세요.</p>
                    <div style="text-align: center; margin: 30px 0; padding: 20px; background-color: #f8f9fa; border: 2px dashed #28a745; border-radius: 10px;">
                        <p style="margin: 0 0 10px 0; font-size: 16px; color: #666;">인증번호</p>
                        <div style="font-size: 32px; font-weight: bold; color: #28a745; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</div>
                    </div>
                    <p style="margin-bottom: 10px; color: #666; font-size: 14px;">⚠️ 이 인증번호는 5분 후에 만료됩니다.</p>
                    <p style="margin-bottom: 20px; color: #666; font-size: 14px;">🔒 보안을 위해 인증번호를 타인과 공유하지 마세요.</p>
                </div>
                <div style="background-color: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #dee2e6; font-size: 12px; color: #666;">
                    <p>© 2025 SoleBid. All rights reserved.</p>
                </div>
            </div>
            """.formatted(verificationCode);
    }

    /**
     * 비밀번호 재설정 OTP 이메일 템플릿
     */
    private String createPasswordResetOtpTemplate(String otp) {
        return """
            <div style="max-width: 600px; margin: 0 auto; font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="background-color: #f8f9fa; padding: 20px; text-align: center; border-bottom: 3px solid #dc3545;">
                    <h1 style="color: #dc3545; margin: 0;">SoleBid</h1>
                </div>
                <div style="padding: 30px 20px;">
                    <h2 style="color: #333; margin-bottom: 20px;">비밀번호 재설정 인증번호 안내</h2>
                    <p style="margin-bottom: 20px;">안녕하세요!</p>
                    <p style="margin-bottom: 20px;">비밀번호 재설정을 요청하셨습니다. 아래 인증번호를 입력하여 새로운 비밀번호를 설정해주세요.</p>
                    <div style="text-align: center; margin: 30px 0; padding: 20px; background-color: #f8f9fa; border: 2px dashed #dc3545; border-radius: 10px;">
                        <p style="margin: 0 0 10px 0; font-size: 16px; color: #666;">인증번호</p>
                        <div style="font-size: 32px; font-weight: bold; color: #dc3545; letter-spacing: 8px; font-family: 'Courier New', monospace;">%s</div>
                    </div>
                    <p style="margin-bottom: 10px; color: #666; font-size: 14px;">⚠️ 이 인증번호는 5분 후에 만료됩니다.</p>
                    <p style="margin-bottom: 10px; color: #666; font-size: 14px;">🔒 보안을 위해 인증번호를 타인과 공유하지 마세요.</p>
                    <p style="margin-bottom: 20px; color: #666; font-size: 14px;">만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시해주세요.</p>
                </div>
                <div style="background-color: #f8f9fa; padding: 20px; text-align: center; border-top: 1px solid #dee2e6; font-size: 12px; color: #666;">
                    <p>© 2025 SoleBid. All rights reserved.</p>
                </div>
            </div>
            """.formatted(otp);
    }



    /**
     * MimeMessage 객체 생성
     */
    private MimeMessage createEmail(String to, String from, String subject, String bodyText) 
            throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from, "SoleBid", "UTF-8"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setContent(bodyText, "text/html; charset=utf-8");
        return email;
    }

    /**
     * MimeMessage를 Gmail API Message 형식으로 변환
     */
    private Message createMessageWithEmail(MimeMessage emailMessage) 
            throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailMessage.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    /**
     * 이메일 주소 마스킹
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) return "**@" + parts[1];
        return parts[0].substring(0, 2) + "****@" + parts[1];
    }
}