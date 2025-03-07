package com.buildbetter.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.buildbetter.auth.AuthAPI;
import com.buildbetter.auth.dto.VerifiedUserRequest;
import com.buildbetter.auth.exception.OtpException;
import com.buildbetter.auth.model.Otp;
import com.buildbetter.auth.repository.OtpRepository;
import com.buildbetter.auth.service.ratelimit.OtpRateLimiter;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.user.UserAPI;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService implements AuthAPI {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final OtpRateLimiter otpRateLimiter;

    private final UserAPI userAPI;

    @Override
    @Transactional
    public void sendOtp(String userId, String email) {

        if (!otpRateLimiter.canSendOtp(userId)) {
            throw new OtpException("You have reached the maximum number of OTP attempts. Please try again later.");
        }

        // Generate a random 6-digit OTP
        String otpCode = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Hash the OTP for secure storage
        String hashedOtp = BCrypt.hashpw(otpCode, BCrypt.gensalt());

        // Check if there is an existing OTP that is not expired
        Otp existingOtp = otpRepository.findByUserIdAndIsUsedFalse(userId).orElse(null);
        if (existingOtp != null) {

            // Generate a new OTP if the new OTP is the same as the existing OTP
            while (existingOtp.getHashedOtp().equals(hashedOtp)) {
                otpCode = String.valueOf((int) (Math.random() * 900000) + 100000);
                hashedOtp = BCrypt.hashpw(otpCode, BCrypt.gensalt());
            }

            existingOtp.setHashedOtp(hashedOtp);
            existingOtp.setCreatedAt(LocalDateTime.now());
            existingOtp.setExpiredAt(LocalDateTime.now().plusMinutes(5)); // Valid for 5 minutes
            existingOtp.setAttempt(existingOtp.getAttempt() + 1);

            otpRepository.save(existingOtp);

        } else {
            // Create and save the OTP entity
            Otp otp = new Otp();
            otp.setId(UUID.randomUUID());
            otp.setUserId(userId);
            otp.setHashedOtp(hashedOtp);
            otp.setCreatedAt(LocalDateTime.now());
            otp.setExpiredAt(LocalDateTime.now().plusMinutes(5)); // Valid for 5 minutes
            otp.setIsUsed(false);
            otp.setAttempt(0);

            otpRepository.save(otp);
        }

        // Send the OTP via email
        sendOtpEmail(email, otpCode);

        // add OTP attempt
        otpRateLimiter.addOtpAttempt(userId);
    }

    private void sendOtpEmail(String to, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otpCode);
            mailSender.send(message);
        } catch (Exception e) {
            throw new OtpException("Failed to send OTP email: " + e.getMessage());
        }
    }

    public String verifiedUser(VerifiedUserRequest request) {
        String userId = request.getUserId();
        String otpCode = request.getOtp();

        Otp otp = otpRepository.findByUserIdAndIsUsedFalse(userId)
                .orElseThrow(() -> new OtpException("OTP not found or already used."));

        if (!BCrypt.checkpw(otpCode, otp.getHashedOtp())) {
            throw new BadRequestException("Invalid OTP code.");
        }

        otp.setIsUsed(true);
        otpRepository.save(otp);

        userAPI.verifiedUser(null);

        return "User verified successfully.";
    }
}
