package com.bookstore.service.notification;

import com.bookstore.config.AppProperties;
import com.bookstore.dto.notification.request.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final AppProperties appProperties;

    @Async(value = "emailExecutor")
    public void sendVerificationEmail(String name, String email, String token) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("url", appProperties.frontend().url() +
                appProperties.frontend().verificationPath() + "?token=" + token);
        context.setVariable("ttl", appProperties.ttl().auth().verification().toMinutes());

        String html = templateEngine.process("verification", context);
        sendHtmlEmail(new EmailRequest(email, "Verify Your Account", html));
    }

    @Async(value = "emailExecutor")
    public void sendPasswordResetEmail(String name, String email, String token) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("url", appProperties.frontend().url() +
                appProperties.frontend().passwordResetPath() + "?token=" + token);
        context.setVariable("ttl", appProperties.ttl().auth().passwordReset().toMinutes());

        String html = templateEngine.process("password-reset", context);
        sendHtmlEmail(new EmailRequest(email, "Reset Your Password", html));
    }

    private void sendHtmlEmail(EmailRequest request) {
        try {
            log.info("Sending Email to {}", request.to());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(appProperties.settings().admin(), "Online Bookstore");
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(request.body(), true);

            mailSender.send(message);
            log.info("Email sent to {}", request.to());
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {} : {}", request.to(), e.getMessage());
        }
    }
}
