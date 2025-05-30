package com.buildbetter.consultation.service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.consultation.dto.architect.ArchitectResponse;
import com.buildbetter.consultation.dto.architect.ChangeArchitectPasswordRequest;
import com.buildbetter.consultation.dto.architect.LoginRequest;
import com.buildbetter.consultation.dto.architect.LoginResponse;
import com.buildbetter.consultation.dto.architect.RegisterArchitectRequest;
import com.buildbetter.consultation.dto.architect.UpdateArchitectRequest;
import com.buildbetter.consultation.model.Architect;
import com.buildbetter.consultation.repository.ArchitectRepository;
import com.buildbetter.consultation.util.ArchitectUtils;
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.util.JwtUtil;
import com.buildbetter.shared.util.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArchitectService {

        private final ArchitectRepository architectRepository;
        private final ConsultationService consultationService;
        private final JwtUtil jwtUtil;
        private final S3Service s3Service;

        public void registerArchitect(RegisterArchitectRequest request) {
                log.info("Architect Service : registerArchitect");

                architectRepository.findByEmail(request.getEmail()).ifPresent(architect -> {
                        throw new BadRequestException("Architect already exists");
                });

                log.info("Architect Service : constructing password from email");
                String password = ArchitectUtils.constructPasswordFromEmail(request.getEmail());

                log.info("Architect Service : Hashing password");
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                Integer rateOnline = 30000;
                Integer rateOffline = 100000;

                // TODO: OMIT THIS CODE BELOW IF REGISTER ARCHITECT IS FINISHED
                String portfolio = "https://issuu.com/erensaratu/docs/architecture_portfolio_by_erensa_ratu_chelsia";
                Random random = new Random();
                Float experience = (float) random.nextInt(15) + 1;

                // Build Random Phone Number
                StringBuilder phoneNumber = new StringBuilder("+62");
                phoneNumber.append("8");
                int numberOfRemainingDigits = 9;

                for (int i = 0; i < numberOfRemainingDigits; i++) {
                        phoneNumber.append(random.nextInt(10)); // Appends a random digit (0-9)
                }

                String randomIndonesianPhoneNumber = phoneNumber.toString();

                Architect architect = Architect.builder().email(request.getEmail()).username(request.getUsername())
                                .province(request.getProvince()).city(request.getCity()).password(hashedPassword)
                                .rateOnline(rateOnline)
                                .rateOffline(rateOffline).portfolio(portfolio).experience(experience)
                                .phoneNumber(randomIndonesianPhoneNumber).build();

                log.info("Architect Service : Saving architect to DB");
                architectRepository.save(architect);
        }

        public LoginResponse loginArchitect(LoginRequest request) {
                log.info("Architect Service : loginArchitect");

                Architect architect = architectRepository.findByEmail(request.getEmail()).orElseThrow(() -> {
                        throw new BadRequestException("Architect not found");
                });

                log.info("Architecture Service : Checking password");
                if (!BCrypt.checkpw(request.getPassword(), architect.getPassword())) {
                        throw new BadRequestException("Invalid password");
                }

                log.info("Architect Service : Generating JWT token");
                String token = jwtUtil.generateToken(
                                architect.getId().toString(), architect.getUsername(), "architect");

                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setEmail(architect.getEmail());
                loginResponse.setToken(token);
                loginResponse.setUserId(architect.getId());
                loginResponse.setUsername(architect.getUsername());

                return loginResponse;
        }

        public void updateArchitect(UUID userId, UpdateArchitectRequest request) {
                log.info("Architect Service : updateArchitect");

                Architect existingArchitect = architectRepository.findById(userId).orElseThrow(() -> {
                        throw new BadRequestException("Architect not found");
                });

                // Update photo profile
                if (request.getPhoto() != null) {
                        log.info("Architect Service : Uploading photo to S3");
                        String folder = S3Folder.CONSULTATIONS + "profile-photos/";
                        String photoUrl = s3Service.uploadFile(request.getPhoto(), folder,
                                        existingArchitect.getId().toString());

                        // Delete the old photo from S3 if it exists
                        if (existingArchitect.getPhoto() != null && !existingArchitect.getPhoto().isBlank()) {
                                log.info("Architect Service : Deleting old photo from S3");
                                s3Service.deleteFile(existingArchitect.getPhoto());
                        }

                        existingArchitect.setPhoto(photoUrl);
                }

                existingArchitect.setUsername(
                                request.getUsername() != null ? request.getUsername()
                                                : existingArchitect.getUsername());
                existingArchitect.setProvince(
                                request.getProvince() != null ? request.getProvince()
                                                : existingArchitect.getProvince());
                existingArchitect.setCity(
                                request.getCity() != null ? request.getCity() : existingArchitect.getCity());
                existingArchitect.setPhoneNumber(
                                request.getPhoneNumber() != null ? request.getPhoneNumber()
                                                : existingArchitect.getPhoneNumber());
                existingArchitect.setExperience(
                                request.getExperience() != null ? request.getExperience()
                                                : existingArchitect.getExperience());
                existingArchitect.setRateOnline(
                                request.getRateOnline() != null ? request.getRateOnline()
                                                : existingArchitect.getRateOnline());
                existingArchitect.setRateOffline(
                                request.getRateOffline() != null ? request.getRateOffline()
                                                : existingArchitect.getRateOffline());
                existingArchitect.setPortfolio(
                                request.getPortfolio() != null ? request.getPortfolio()
                                                : existingArchitect.getPortfolio());

                log.info("Architect Service : Saving architect to DB");
                architectRepository.save(existingArchitect);
        }

        public void changePassword(UUID architectId, ChangeArchitectPasswordRequest request) {
                log.info("Architect Service : changePassword");
                Architect existingArchitect = architectRepository.findById(architectId).orElseThrow(() -> {
                        throw new BadRequestException("Architect not found");
                });

                log.info("Architect Service : Checking old password");
                if (!BCrypt.checkpw(request.getOldPassword(), existingArchitect.getPassword())) {
                        throw new BadRequestException("Invalid old password");
                }

                if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
                        throw new BadRequestException("New password and confirm password do not match");
                }

                log.info("Architect Service : Hashing new password");
                String hashedPassword = BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt());
                existingArchitect.setPassword(hashedPassword);

                log.info("Architect Service : Saving new password to DB");
                architectRepository.save(existingArchitect);
        }

        public List<ArchitectResponse> getAllArchitects(UUID userId, Boolean notContacted, String city) {
                log.info("Fetching all architects");

                List<Architect> architects;
                if (Boolean.TRUE.equals(notContacted) && userId != null) {
                        List<UUID> contacted = consultationService.getAllContactedArchitects(userId);
                        architects = architectRepository.findAllByIdNotIn(contacted);
                } else {
                        architects = architectRepository.findAll();
                }

                if (city != null && !city.isBlank()) {
                        log.info("Filtering architects by city: {}", city);
                        architects = architects.stream()
                                        .filter(architect -> architect.getCity().equalsIgnoreCase(city))
                                        .collect(Collectors.toList());
                }

                return architects.stream()
                                .map(ArchitectUtils::mapToArchitectResponse)
                                .collect(Collectors.toList());
        }

        public ArchitectResponse getArchitectById(UUID id) {
                log.info("Fetching architect with id: {}", id);
                Architect architect = architectRepository.findById(id).orElseThrow(() -> {
                        throw new BadRequestException("Architect not found");
                });
                return ArchitectUtils.mapToArchitectResponse(architect);
        }

        public void deleteArchitect(UUID id) {
                log.info("Deleting architect with id: {}", id);

                architectRepository.findById(id).orElseThrow(() -> {
                        throw new BadRequestException("Architect not found");
                });

                architectRepository.deleteById(id);
        }
}
