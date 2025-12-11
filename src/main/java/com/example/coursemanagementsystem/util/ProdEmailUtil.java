package com.example.coursemanagementsystem.util;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Profile("prod")
@Component
@RequiredArgsConstructor
public class ProdEmailUtil implements EmailUtil {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${mail.sender}")
    private String fromEmail;   // loaded from properties

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    public void sendEnrollmentEmail(String to, String subject,
                                    String studentName, String courseTitle,
                                    String instructorName, String enrollmentStatus,
                                    String paymentStatus) {

        Context context = new Context();
        context.setVariable("studentName", studentName);
        context.setVariable("courseTitle", courseTitle);
        context.setVariable("instructorName", instructorName);
        context.setVariable("enrollmentStatus", enrollmentStatus);
        context.setVariable("paymentStatus", paymentStatus);

        String html = templateEngine.process("email/enrollment-success", context);
        sendEmail(to, subject, html);
    }

    @Override
    public void sendPaymentConfirmationEmail(String to, String studentName,
                                             String courseTitle, String instructorName,
                                             Double amount, String paymentDate) {

        Context context = new Context();
        context.setVariable("studentName", studentName);
        context.setVariable("courseTitle", courseTitle);
        context.setVariable("instructorName", instructorName);
        context.setVariable("amount", amount);
        context.setVariable("paymentDate", paymentDate);

        String html = templateEngine.process("email/payment-confirmation", context);
        sendEmail(to, "Payment Confirmation - " + courseTitle, html);
    }
}

