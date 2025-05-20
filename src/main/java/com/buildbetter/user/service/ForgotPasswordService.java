package com.buildbetter.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.exception.InternalServerErrorException;
import com.buildbetter.user.dto.auth.ResetPasswordRequest;
import com.buildbetter.user.model.ResetPasswordToken;
import com.buildbetter.user.model.User;
import com.buildbetter.user.repository.ResetPasswordRepository;
import com.buildbetter.user.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordService {

  private final ResetPasswordRepository resetPasswordRepository;
  private final UserRepository userRepository;
  private final JavaMailSender mailSender;
  private final RateLimiterService rateLimiterService;

  // Forgot Password
  public String forgotPassword(String email) {
    log.info("Forgot Password Service : forgotPassword");

    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new BadRequestException("User not registered"));

    if (!user.getIsVerified()) {
      throw new BadRequestException("User not verified");
    }

    log.info("Forgot Password Service : Checking if user is rate limited");
    if (!rateLimiterService.canSendOtp(email, RateLimiterService.PREFIX_FORGOT_PASSWORD_RATE_LIMIT)) {
      throw new BadRequestException("You have reached the maximum number of attempts. Please try again later.");
    }

    // Generate Token
    String token = UUID.randomUUID().toString();

    // Hash Token
    log.info("Forgot Password Service : Hashing token");
    String hashedToken = BCrypt.hashpw(token, BCrypt.gensalt());

    // Check if there is an existing token that is not used
    ResetPasswordToken existingToken = resetPasswordRepository.findByUserIdAndIsUsedFalse(user.getId())
        .orElse(null);

    if (existingToken != null) {

      existingToken.setHashedToken(hashedToken);
      existingToken.setCreatedAt(LocalDateTime.now());
      existingToken.setExpiredAt(LocalDateTime.now().plusHours(1));

      log.info("Forgot Password Service : Update existing token in DB");
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

      log.info("Forgot Password Service : Saving new token to DB");
      resetPasswordRepository.save(resetPasswordToken);
    }

    // Send email
    log.info("Forgot Password Service : Sending Change Password link to email");
    sendChangePasswordLink(email, token);

    // Update send email attempts
    log.info("Forgot Password Service : Updating send email attempts");
    rateLimiterService.addOtpAttempt(email, RateLimiterService.PREFIX_FORGOT_PASSWORD_RATE_LIMIT);

    return "Change Password link sent to email";
  }

  // Send Change-Password Link to Email
  public void sendChangePasswordLink(String to, String token) {
    log.info("Forgot Password Service : sendChangePasswordLink");
    try {
      log.info("Forgot Password Service : Constructing Change Password email message");
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

      helper.setTo(to);
      helper.setSubject("🔒 Reset your password");

      String html = """
            <html>
              <body style="font-family:Arial,sans-serif">
                <div style="padding:40px 50px;background-color:#30534b;border-radius:36px;">
                  <div
                    style="
                      text-align:center;
                      background-color:#eece7c;
                      border-radius:36px;
                      padding:20px;
                    "
                  >
                    <h1 style="color:#1d322d;">Reset Your Password</h1>
                    <p>Click the button below to choose a new password:</p>
                    <p style="text-align:center;">
                      <a
                        href="https://build-better.site/api/v1/reset-password-redirect?token=%s&email=%s"
                        style="
                          display:inline-block;
                          padding:16px 32px;
                          background-color:#3f473d;
                          color:#cae1db;
                          text-decoration:none;
                          border-radius:8px;
                          margin:4px 0px;
                        "
                      >
                        Reset Password
                      </a>
                    </p>
                    <p style="font-size:1rem;color:#EE7766;margin:6px 0px">
                      If you didn’t request a password reset, just ignore this email.
                    </p>
                  </div>
                </div>
              </body>
            </html>
          """
          .formatted(token, to, token, to, token, to);

      helper.setText(html, true);
      log.info("Forgot Password Service : Sending HTML email to {}", to);
      mailSender.send(mimeMessage);
    } catch (Exception e) {
      throw new InternalServerErrorException("Failed to send change-password email: " + e.getMessage());
    }
  }

  // Reset Password
  public String resetPassword(ResetPasswordRequest request) {
    log.info("Forgot Password Service : resetPassword");

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

    log.info("Forgot Password Service : Checking if token is valid");
    if (!BCrypt.checkpw(request.getToken(), resetPasswordToken.getHashedToken())) {
      throw new BadRequestException("Invalid Token");
    }

    // Hash password
    log.info("Forgot Password Service : Hashing password");
    String hashedPassword = BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt());

    // Update User Password
    user.setPassword(hashedPassword);

    log.info("Forgot Password Service : Updating user password in DB");
    userRepository.save(user);

    // Update Token
    resetPasswordToken.setIsUsed(true);

    log.info("Forgot Password Service : mark token as used in DB");
    resetPasswordRepository.save(resetPasswordToken);

    log.info("Forgot Password Service : Resetting OTP attempts");
    rateLimiterService.resetOtpAttempts(request.getEmail(), RateLimiterService.PREFIX_FORGOT_PASSWORD_RATE_LIMIT);

    return "Password Changed Successfully";
  }
}
