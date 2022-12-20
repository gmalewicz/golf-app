package com.greg.golf.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendEmail(String to, String subject, String content) throws MessagingException;
}
