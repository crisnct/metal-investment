package com.investment.metal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Simple email test using turbo-smtp.com.
 * This test can be run independently to verify email connectivity.
 */
public class SimpleEmailTest {

    /**
     * Test email configuration validation.
     * This method validates that the email configuration is correct.
     */
    @Test
    public void testEmailConfiguration() {
        try {
            // Create and configure JavaMailSender
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("pro.turbo-smtp.com");
            mailSender.setPort(587);
            
            // Test connection (this will validate the configuration)
            mailSender.testConnection();
            
            System.out.println("✅ Email configuration is valid");
            assertTrue(true, "Email configuration is valid");
            
        } catch (Exception e) {
            System.err.println("❌ Email configuration test failed: " + e.getMessage());
            e.printStackTrace();
            
            System.err.println("\n🔧 Configuration Issues:");
            System.err.println("- Check turbo-smtp.com host configuration");
            System.err.println("- Verify port 587 is accessible");
            System.err.println("- Ensure SMTP credentials are correct");
            
            fail("Email configuration test failed: " + e.getMessage());
        }
    }

}
