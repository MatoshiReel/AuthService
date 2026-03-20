package reel.ru.AuthService.service.security.otp;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class OtpMailSender {
    private final JavaMailSender mailSender;

    public OtpMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reel - OTP-code");
        message.setText(otp);
        mailSender.send(message);
    }
}
