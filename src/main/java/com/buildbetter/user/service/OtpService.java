package com.buildbetter.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.exception.ForbiddenException;
import com.buildbetter.shared.exception.InternalServerErrorException;
import com.buildbetter.shared.exception.NotFoundException;
import com.buildbetter.shared.exception.TooManyRequestException;
import com.buildbetter.user.dto.auth.SendOTPRequest;
import com.buildbetter.user.model.Otp;
import com.buildbetter.user.model.User;
import com.buildbetter.user.repository.OtpRepository;
import com.buildbetter.user.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final RateLimiterService rateLimiterService;

    // Send OTP
    public void sendOtp(SendOTPRequest request) {
        log.info("Otp Service : sendOtp");

        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UUID userId = user.getId();

        if (user.getIsVerified()) {
            throw new BadRequestException("User already verified");
        }

        log.info("Otp Service : Checking if user is rate limited");
        if (!rateLimiterService.canSendOtp(userId.toString(), RateLimiterService.PREFIX_OTP_RATE_LIMIT)) {
            throw new TooManyRequestException(
                    "You have reached the maximum number of OTP attempts. Please try again later.");
        }

        // Generate a random 6-digit OTP
        log.info("Otp Service : Generating OTP");
        String otpCode = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Hash the OTP for secure storage
        log.info("Otp Service : Hashing OTP");
        String hashedOtp = BCrypt.hashpw(otpCode, BCrypt.gensalt());

        // Check if there is an existing OTP that is not expired
        Otp existingOtp = otpRepository.findByUserIdAndIsUsedFalse(userId).orElse(null);
        if (existingOtp != null) {

            // Generate a new OTP if the new OTP is the same as the existing OTP
            while (existingOtp.getHashedOtp().equals(hashedOtp)) {
                log.info("Otp Service : Regenerating OTP");
                otpCode = String.valueOf((int) (Math.random() * 900000) + 100000);
                hashedOtp = BCrypt.hashpw(otpCode, BCrypt.gensalt());
            }

            existingOtp.setHashedOtp(hashedOtp);
            existingOtp.setCreatedAt(LocalDateTime.now());
            existingOtp.setExpiredAt(LocalDateTime.now().plusMinutes(5)); // Valid for 5 minutes
            existingOtp.setAttempt(existingOtp.getAttempt() + 1);

            log.info("Otp Service : Update existing OTP in DB");
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

            log.info("Otp Service : Saving new OTP to DB");
            otpRepository.save(otp);
        }

        // Send OTP via email
        log.info("Otp Service : Sending OTP to email");
        sendOtpToEmail(email, otpCode);

        // Update OTP Attempts
        log.info("Otp Service : Updating OTP attempts");
        rateLimiterService.addOtpAttempt(userId.toString(), RateLimiterService.PREFIX_OTP_RATE_LIMIT);
    }

    // Send OTP via email
    private void sendOtpToEmail(String to, String otpCode) {
        log.info("Otp Service : sendOtpToEmail");
        try {
            log.info("Otp Service : Constructing OTP email message");
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🔒 Verifikasi Alamat Email");

            String message = """
                    <body style="font-family: Arial, sans-serif">
                        <div
                        style="padding: 40px 40px; background-color: #30534b; border-radius: 36px"
                        >
                            <div
                                style="
                                text-align: center;
                                background-color: #cae1db;
                                border-radius: 36px;
                                padding: 20px;
                                "
                            >
                                <h1 style="color: #1d322d">Verifikasi Email</h1>
                                <p style="color:#3f473d;">
                                Mohon masukkan kode OTP berikut kedalam aplikasi BuildBetter untuk memverifikasi alamat email Anda.
                                </p>
                                <p
                                style="
                                    text-align: center;
                                    padding: 16px 24px;
                                    background-color: #3f473d;
                                    color: #cae1db;
                                    font-weight: 600;
                                    font-size: 1.6rem;
                                    margin: 8px auto;
                                    border-radius: 8px;
                                    width: fit-content;
                                "
                                >
                                %s %s %s %s %s %s
                                </p>
                            </div>
                        </div>
                    </body>
                    """
                    .formatted(otpCode.charAt(0), otpCode.charAt(1), otpCode.charAt(2), otpCode.charAt(3),
                            otpCode.charAt(4), otpCode.charAt(5));

            helper.setText(message, true);

            log.info("Otp Service : Sending email to " + to);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to send OTP email: " + e.getMessage());
        }
    }

    // Verify OTP
    public Boolean verifyOtp(UUID userId, String otpCode) {
        log.info("Otp Service : verifyOtp");

        Otp otp = otpRepository.findByUserIdAndIsUsedFalse(userId)
                .orElseThrow(() -> new NotFoundException("OTP not recognized"));

        log.info("Otp Service : Checking if OTP is valid");
        if (!BCrypt.checkpw(otpCode, otp.getHashedOtp())) {
            throw new BadRequestException("Invalid OTP code");
        }

        log.info("Otp Service : Checking if OTP is expired");
        if (otp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ForbiddenException("OTP code has expired");
        }

        otp.setIsUsed(true);

        log.info("Otp Service : Marking OTP as used in DB");
        otpRepository.save(otp);

        log.info("Otp Service : Reset OTP attempts");
        rateLimiterService.resetOtpAttempts(userId.toString(), RateLimiterService.PREFIX_OTP_RATE_LIMIT);

        return true;
    }

}
