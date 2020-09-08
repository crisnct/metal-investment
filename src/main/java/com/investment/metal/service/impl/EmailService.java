package com.investment.metal.service.impl;

import com.google.common.base.Charsets;
import com.investment.metal.database.Customer;
import com.investment.metal.service.AbstractService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;

@Service
public class EmailService extends AbstractService {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.mail.from}")
    private String emailFrom;

    @Autowired
    private JavaMailSender mailSender;

    private final String mailTemplateCode;

    public EmailService() throws IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("mail-template-code.html");
        this.mailTemplateCode = IOUtils.toString(is, Charsets.UTF_8);
    }

    public void sendMailWithCode(Customer user, int codeGenerated) throws MessagingException {
        final String emailContent = this.mailTemplateCode
                .replace("{user}", user.getUsername())
                .replace("{code}", String.valueOf(codeGenerated));
        this.sendMail(user.getEmail(), appName, emailContent);
    }

    private void sendMail(String toEmail, String subject, String message) throws MessagingException {
        MimeMessage msg = this.mailSender.createMimeMessage();
        MimeMessageHelper mailMessage = new MimeMessageHelper(msg, true);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message, true);
        mailMessage.setFrom(emailFrom);
        this.mailSender.send(msg);
    }


}