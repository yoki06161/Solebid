package com.sesac.solbid.service;

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
public class ResetMailService {

    private static final String APPLICATION_NAME = "SoleBid";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // application.properties에서 OAuth 2.0 관련 값들을 주입받습니다.
    @Value("${spring.mail.username}")
    private String fromEmail; // 여기서는 yoki04189+solbid@gmail.com

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${google.mail.refresh-token}")
    private String googleRefreshToken;


    public void sendPasswordResetMail(String to, String link) {
        try {
            // 1. HTTP Transport와 Credential 객체를 생성합니다.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = new GoogleCredential.Builder()
                    .setTransport(HTTP_TRANSPORT)
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(googleClientId, googleClientSecret)
                    .build()
                    .setRefreshToken(googleRefreshToken);

            // 2. Gmail 서비스 클라이언트를 빌드합니다.
            Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // 3. 이메일 메시지를 생성합니다. (RFC 2822 형식)
            MimeMessage mimeMessage = createEmail(to, fromEmail, "[SoleBid] 비밀번호 재설정 안내",
                    "<h3>[Solebid] 비밀번호 재설정 안내</h3>"
                            + "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요. (유효시간: 15분)</p>"
                            + "<a href=\"" + link + "\">비밀번호 재설정 링크</a>");

            // 4. MimeMessage를 Gmail API가 요구하는 Message 객체로 변환합니다.
            Message message = createMessageWithEmail(mimeMessage);

            // 5. Gmail API를 통해 메시지를 전송합니다. "me"는 인증된 사용자를 의미합니다.
            service.users().messages().send("me", message).execute();

            log.info("비밀번호 재설정 메일 발송 성공 (Gmail API): {}", maskEmail(to));

        } catch (GeneralSecurityException | IOException | MessagingException e) {
            log.error("비밀번호 재설정 메일 발송 실패 (Gmail API): {}", to, e);
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    // MimeMessage 객체를 생성하는 헬퍼 메서드
    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from, "SoleBid", "UTF-8"));

        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(to));
        email.setSubject(subject);
        email.setContent(bodyText, "text/html; charset=utf-8");
        return email;
    }

    // MimeMessage를 Base64로 인코딩하여 Gmail API의 Message 형식으로 변환하는 헬퍼 메서드
    private Message createMessageWithEmail(MimeMessage emailMessage) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailMessage.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    // 이메일 주소 마스킹을 위한 유틸리티 메서드
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) return "**@" + parts[1];
        return parts[0].substring(0, 2) + "****@" + parts[1];
    }
}