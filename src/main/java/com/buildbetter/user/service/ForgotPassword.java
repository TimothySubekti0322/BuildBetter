package com.buildbetter.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.exception.InternalServerErrorException;
import com.buildbetter.user.dto.ResetPasswordRequest;
import com.buildbetter.user.model.ResetPasswordToken;
import com.buildbetter.user.model.User;
import com.buildbetter.user.repository.ResetPasswordRepository;
import com.buildbetter.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPassword {

    private final ResetPasswordRepository resetPasswordRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final RateLimiterService rateLimiterService;

    // Forgot Password
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BadRequestException("User not registered"));

        if (!user.getIsVerified()) {
            throw new BadRequestException("User not verified");
        }

        if (!rateLimiterService.canSendOtp(email, RateLimiterService.PREFIX_FORGOT_PASSWORD_RATE_LIMIT)) {
            throw new BadRequestException("You have reached the maximum number of attempts. Please try again later.");
        }

        // Generate Token
        String token = UUID.randomUUID().toString();

        // Hash Token
        String hashedToken = BCrypt.hashpw(token, BCrypt.gensalt());

        // Check if there is an existing token that is not used
        ResetPasswordToken existingToken = resetPasswordRepository.findByUserIdAndIsUsedFalse(user.getId())
                .orElse(null);

        if (existingToken != null) {

            existingToken.setHashedToken(hashedToken);
            existingToken.setCreatedAt(LocalDateTime.now());
            existingToken.setExpiredAt(LocalDateTime.now().plusHours(1));

            resetPasswordRepository.save(existingToken);

        } else {

            // Save Token
            ResetPasswordToken resetPasswordToken = new ResetPasswordToken();
            resetPasswordToken.setId(UUID.randomUUID());
            resetPasswordToken.setUserId(user.getId());
            resetPasswordToken.setHashedToken(hashedToken);
            resetPasswordToken.setCreatedAt(LocalDateTime.now());
            resetPasswordToken.setExpiredAt(LocalDateTime.now().plusHours(1));
            resetPasswordToken.setIsUsed(false);

            resetPasswordRepository.save(resetPasswordToken);
        }

        // Send email
        sendChangePasswordLink(email, token);

        // Update send email attempts
        rateLimiterService.addOtpAttempt(email, RateLimiterService.PREFIX_FORGOT_PASSWORD_RATE_LIMIT);

        return "Change Password link sent to email";
    }

    // Send Change-Password Link to Email
    public void sendChangePasswordLink(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Change Password Link");
            message.setText("Click on the link to change your password: http://localhost:8080/reset-password?token="
                    + token + "&email=" + to);
            mailSender.send(message);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to send OTP email: " + e.getMessage());
        }
    }

    // Reset Password
    public String resetPassword(ResetPasswordRequest request) {

        // Check if user exists
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            throw new BadRequestException("User not registered");
        } else if (!user.getIsVerified()) {
            throw new BadRequestException("User not verified");
        }

        // Check if OTP is correct
        ResetPasswordToken resetPasswordToken = resetPasswordRepository
                .findByUserIdAndIsUsedFalse(user.getId())
                .orElseThrow(() -> new BadRequestException("No Token Found"));

        if (!BCrypt.checkpw(request.getToken(), resetPasswordToken.getHashedToken())) {
            throw new BadRequestException("Invalid Token");
        }

        // Hash password
        String hashedPassword = BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt());

        // Update User Password
        user.setPassword(hashedPassword);

        userRepository.save(user);

        // Update Token
        resetPasswordToken.setIsUsed(true);

        resetPasswordRepository.save(resetPasswordToken);

        rateLimiterService.resetOtpAttempts(request.getEmail(), RateLimiterService.PREFIX_FORGOT_PASSWORD_RATE_LIMIT);

        return "Password Changed Successfully";
    }
}
