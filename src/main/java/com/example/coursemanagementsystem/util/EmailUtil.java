package com.example.coursemanagementsystem.util;

public interface EmailUtil {

    void sendEnrollmentEmail(
            String to,
            String subject,
            String studentName,
            String courseTitle,
            String instructorName,
            String enrollmentStatus,
            String paymentStatus
    );

    void sendPaymentConfirmationEmail(
            String to,
            String studentName,
            String courseTitle,
            String instructorName,
            Double amount,
            String paymentDate
    );
}

