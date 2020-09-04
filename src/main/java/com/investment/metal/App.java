package com.investment.metal;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class App
{
   public static void main(String[] args)
   {
      final String to = "susan.atkinson1234@gmail.com";
      final String from = "nelucristian2005@yahoo.com";

      String host = "smtp.mail.yahoo.com";
      Properties properties = System.getProperties();

      properties.put("mail.smtp.host", host);
      properties.put("mail.smtp.port", "587");
      properties.put("mail.smtp.starttls.enable", "true");
      properties.put("mail.smtp.auth", "true");


      Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication("nelucristian2005", "mefypbhrvtsbfrzs");
          }
      });

      session.setDebug(true);
      try {
          MimeMessage message = new MimeMessage(session);

          message.setFrom(new InternetAddress(from));
          message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
          message.setSubject("This is the Subject Line!");
          message.setText("This is actual message");

          System.out.println("sending...");
          Transport.send(message);
          System.out.println("Sent message successfully....");
      } catch (MessagingException mex) {
          mex.printStackTrace();
      }
   }
}