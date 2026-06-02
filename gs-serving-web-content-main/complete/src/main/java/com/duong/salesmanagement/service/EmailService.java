package com.duong.salesmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Gửi email phục vụ xác minh tài khoản, quên mật khẩu...
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:duongngoctund2004@gmail.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Gửi mã OTP 6 chữ số đến email người dùng để xác minh đăng ký.
     */
    public void sendVerificationCode(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[Food Delivery] Mã xác minh tài khoản của bạn");
        message.setText(
            "Xin chào!\n\n" +
            "Mã xác minh tài khoản của bạn là:\n\n" +
            "    " + otp + "\n\n" +
            "Mã có hiệu lực trong 15 phút.\n" +
            "Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.\n\n" +
            "Trân trọng,\nFood Delivery System"
        );
        mailSender.send(message);
    }

    /**
     * Gửi mã OTP 6 chữ số để lấy lại mật khẩu.
     */
    public void sendPasswordResetCode(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[Food Delivery] Lấy lại mật khẩu tài khoản");
        message.setText(
            "Xin chào!\n\n" +
            "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n" +
            "Mã OTP xác nhận của bạn là:\n\n" +
            "    " + otp + "\n\n" +
            "Mã có hiệu lực trong 15 phút.\n" +
            "NẾU BẠN KHÔNG YÊU CẦU: Vui lòng bỏ qua email này, tài khoản của bạn vẫn an toàn.\n\n" +
            "Trân trọng,\nFood Delivery System"
        );
        mailSender.send(message);
    }
}
