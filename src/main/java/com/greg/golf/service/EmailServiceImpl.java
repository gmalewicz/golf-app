package com.greg.golf.service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceImpl {

	@Autowired
	public JavaMailSender javaMailSender;

	public void sendEmail(String to, String title, String content) throws MessagingException {
		MimeMessage mail = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(mail, true);
		helper.setTo(to);

		helper.setSubject(title);
		helper.setText(content, true);

		javaMailSender.send(mail);
	}
}
