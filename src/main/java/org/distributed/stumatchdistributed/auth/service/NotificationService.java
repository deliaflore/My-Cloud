package org.distributed.stumatchdistributed.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Email notification service for sending OTP codes.
 * Supports both plain text and HTML emails.
 */
@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final Optional<JavaMailSender> mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;

    public NotificationService(@Nullable JavaMailSender mailSender) {
        this.mailSender = Optional.ofNullable(mailSender);
    }

    /**
     * Sends OTP code via email.
     * Falls back to console log if email is not configured.
     */
    public void sendOtpEmail(String to, String code, String purpose) {
        if (mailSender.isEmpty() || fromEmail.isBlank()) {
            log.warn("═══════════════════════════════════════════════════════");
            log.warn("📧 EMAIL NOT CONFIGURED - OTP CODE (for testing only)");
            log.warn("═══════════════════════════════════════════════════════");
            log.warn("To: {}", to);
            log.warn("Purpose: {}", purpose);
            log.warn("OTP Code: {}", code);
            log.warn("Expires in: 5 minutes");
            log.warn("═══════════════════════════════════════════════════════");
            log.warn("To enable email, configure SMTP in application.properties");
            log.warn("See EMAIL_SETUP.md for instructions");
            log.warn("═══════════════════════════════════════════════════════");
            return;
        }

        try {
            // Try HTML email first
            sendHtmlEmail(to, code, purpose);
            log.info("✅ OTP email sent to {}", to);
        } catch (Exception e) {
            // Fallback to plain text
            try {
                sendPlainTextEmail(to, code, purpose);
                log.info("✅ OTP email sent to {} (plain text)", to);
            } catch (MailException ex) {
                log.error("❌ Failed to send OTP email to {}", to, ex);
                // Still log to console as fallback
                log.warn("OTP for {} is {} (email failed, using console fallback)", to, code);
            }
        }
    }

    private void sendHtmlEmail(String to, String code, String purpose) throws MessagingException {
        MimeMessage message = mailSender.get().createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        String from = fromEmail;
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject("Your Verification Code - Distributed Cloud");
        
        String htmlBody = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                              color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .otp-box { background: white; border: 2px dashed #667eea; 
                               padding: 20px; text-align: center; margin: 20px 0; 
                               border-radius: 8px; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #667eea; 
                                letter-spacing: 5px; font-family: 'Courier New', monospace; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .warning { background: #fff3cd; border-left: 4px solid #ffc107; 
                               padding: 15px; margin: 20px 0; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Verification Code</h1>
                    </div>
                    <div class="content">
                        <p>Hello,</p>
                        <p>You requested a verification code for <strong>%s</strong>.</p>
                        
                        <div class="otp-box">
                            <div style="color: #666; margin-bottom: 10px;">Your OTP Code:</div>
                            <div class="otp-code">%s</div>
                        </div>
                        
                        <div class="warning">
                            ⚠️ <strong>Important:</strong> This code expires in <strong>5 minutes</strong>.
                            Do not share this code with anyone.
                        </div>
                        
                        <p>If you didn't request this code, please ignore this email.</p>
                        
                        <div class="footer">
                            <p>This is an automated message from Distributed Cloud Storage System.</p>
                            <p>© 2025 Distributed Cloud Team</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(purpose, code);
        
        helper.setText(htmlBody, true);
        mailSender.get().send(message);
    }

    private void sendPlainTextEmail(String to, String code, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Your Verification Code - Distributed Cloud");
        
        String textBody = """
            Hello,
            
            You requested a verification code for %s.
            
            Your OTP Code: %s
            
            ⚠️ IMPORTANT: This code expires in 5 minutes.
            Do not share this code with anyone.
            
            If you didn't request this code, please ignore this email.
            
            --
            Distributed Cloud Storage System
            © 2025 Distributed Cloud Team
            """.formatted(purpose, code);
        
        message.setText(textBody);
        mailSender.get().send(message);
    }
}

