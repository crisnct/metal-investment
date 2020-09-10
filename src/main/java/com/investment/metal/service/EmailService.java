package com.investment.metal.service;

import com.google.common.base.Charsets;
import com.investment.metal.common.MetalType;
import com.investment.metal.common.Util;
import com.investment.metal.database.Alert;
import com.investment.metal.database.Customer;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

@Service
public class EmailService extends AbstractService {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.mail.from}")
    private String emailFrom;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private HttpServletRequest request;

    private final String mailTemplateCode;

    private final String mailTemplateAlert;

    public EmailService() throws IOException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        this.mailTemplateCode = IOUtils.toString(
                Objects.requireNonNull(contextClassLoader.getResourceAsStream("mail-template-code.html")),
                Charsets.UTF_8);
        this.mailTemplateAlert = IOUtils.toString(
                Objects.requireNonNull(contextClassLoader.getResourceAsStream("mail-template-alert-trigger.html")),
                Charsets.UTF_8);
    }

    public void sendMailWithCode(Customer user, int codeGenerated) throws MessagingException {
        final String ip = Util.getClientIpAddress(request);
        final String emailContent = this.mailTemplateCode
                .replace("{user}", user.getUsername())
                .replace("{code}", String.valueOf(codeGenerated))
                .replace("{ip}", ip);
        this.sendMail(user.getEmail(), appName, emailContent);
    }

    public void sendMailWithProfit(UserProfit userInfo, Alert alert) throws MessagingException {
        Customer user = userInfo.getUser();
        MetalType metalType = alert.getMetalType();
        final String emailContent = this.mailTemplateAlert
                .replace("{user}", user.getUsername())
                .replace("{metal}", metalType.name().toLowerCase())
                .replace("{metalSymbol}", metalType.getSymbol())
                .replace("{expression}", alert.getExpression())
                .replace("{amount}", String.valueOf(userInfo.getMetalAmount()))
                .replace("{cost}", String.format("%.2f", userInfo.getOriginalCost()))
                .replace("{costNow}", String.format("%.2f", userInfo.getCurrentCost()))
                .replace("{profit}", String.format("%.2f", userInfo.getProfit()));
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