package com.greg.golf.service;

import jakarta.mail.MessagingException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender javaMailSender;

	public void sendEmail(String[] to, String title, String content) throws MessagingException {
		var mail = javaMailSender.createMimeMessage();

		var helper = new MimeMessageHelper(mail, true);
		helper.setBcc(to);

		helper.setSubject(title);
		helper.setText(content, true);

		javaMailSender.send(mail);
	}
}
