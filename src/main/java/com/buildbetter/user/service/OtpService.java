package com.buildbetter.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.exception.ForbiddenException;
import com.buildbetter.shared.exception.InternalServerErrorException;
import com.buildbetter.shared.exception.NotFoundException;
import com.buildbetter.shared.exception.TooManyRequestException;
import com.buildbetter.user.dto.SendOTPRequest;
import com.buildbetter.user.model.Otp;
import com.buildbetter.user.model.User;
import com.buildbetter.user.repository.OtpRepository;
import com.buildbetter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final RateLimiterService rateLimiterService;

    // Send OTP
    public void sendOtp(SendOTPRequest request) {

        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UUID userId = user.getId();

        if (user.getIsVerified()) {
            throw new BadRequestException("User already verified");
        }

        if (!rateLimiterService.canSendOtp(userId.toString(), RateLimiterService.PREFIX_OTP_RATE_LIMIT)) {
            throw new TooManyRequestException(
                    "You have reached the maximum number of OTP attempts. Please try again later.");
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
            Otp otp = new Otp();
            otp.setId(UUID.randomUUID());
            otp.setUserId(user.getId());
            otp.setHashedOtp(hashedOtp);
            otp.setCreatedAt(LocalDateTime.now());
            otp.setExpiredAt(LocalDateTime.now().plusMinutes(5)); // Valid for 5 minutes
            otp.setIsUsed(false);
            otp.setAttempt(0);

            otpRepository.save(otp);
        }

        // Send OTP via email
        sendOtpToEmail(email, otpCode);

        // Update OTP Attempts
        rateLimiterService.addOtpAttempt(userId.toString(), RateLimiterService.PREFIX_OTP_RATE_LIMIT);
    }

    // Send OTP via email
    private void sendOtpToEmail(String to, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otpCode);
            mailSender.send(message);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to send OTP email: " + e.getMessage());
        }
    }

    // Verify OTP
    public Boolean verifyOtp(UUID userId, String otpCode) {
        Otp otp = otpRepository.findByUserIdAndIsUsedFalse(userId)
                .orElseThrow(() -> new NotFoundException("OTP not recognized"));

        if (!BCrypt.checkpw(otpCode, otp.getHashedOtp())) {
            throw new BadRequestException("Invalid OTP code");
        }

        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ForbiddenException("OTP code has expired");
        }

        otp.setIsUsed(true);

        otpRepository.save(otp);

        rateLimiterService.resetOtpAttempts(userId.toString(), RateLimiterService.PREFIX_OTP_RATE_LIMIT);

        return true;
    }

}
