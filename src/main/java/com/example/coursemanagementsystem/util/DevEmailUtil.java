package com.example.coursemanagementsystem.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class DevEmailUtil implements EmailUtil {

    public void sendEnrollmentEmail(String to, String subject,
                                    String studentName, String courseTitle,
                                    String instructorName, String enrollmentStatus,
                                    String paymentStatus) {

        System.out.println("\n📧 [DEV MODE] Enrollment Email MOCKED:");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Course: " + courseTitle);
        System.out.println("Instructor: " + instructorName);
        System.out.println("Enrollment Status: " + enrollmentStatus);
        System.out.println("Payment Status: " + paymentStatus);
        System.out.println("----------------------------------------");
    }


    public void sendPaymentConfirmationEmail(String to, String studentName,
                                             String courseTitle, String instructorName,
                                             Double amount, String paymentDate) {

        System.out.println("\n📧 [DEV MODE] Payment Confirmation MOCKED:");
        System.out.println("To: " + to);
        System.out.println("Amount: " + amount);
        System.out.println("Course: " + courseTitle);
        System.out.println("Instructor: " + instructorName);
        System.out.println("Paid On: " + paymentDate);
        System.out.println("----------------------------------------");
    }
}

