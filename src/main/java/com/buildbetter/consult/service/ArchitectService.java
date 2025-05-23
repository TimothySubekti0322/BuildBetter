package com.buildbetter.consult.service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.buildbetter.consult.dto.architect.LoginRequest;
import com.buildbetter.consult.dto.architect.LoginResponse;
import com.buildbetter.consult.dto.architect.RegisterArchitectRequest;
import com.buildbetter.consult.dto.architect.UpdateArchitectRequest;
import com.buildbetter.consult.model.Architect;
import com.buildbetter.consult.repository.ArchitectRepository;
import com.buildbetter.consult.util.ArchitectUtils;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArchitectService {

        private final ArchitectRepository architectRepository;
        private final JwtUtil jwtUtil;

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

                return loginResponse;
        }

        public void updateArchitect(UUID userId, UpdateArchitectRequest request) {
                log.info("Architect Service : updateArchitect");

                Architect existingArchitect = architectRepository.findById(userId).orElseThrow(() -> {
                        throw new BadRequestException("Architect not found");
                });

                existingArchitect.setUsername(
                                request.getUsername() != null ? request.getUsername()
                                                : existingArchitect.getUsername());
                existingArchitect.setProvince(
                                request.getProvince() != null ? request.getProvince()
                                                : existingArchitect.getProvince());
                existingArchitect.setCity(
                                request.getCity() != null ? request.getCity() : existingArchitect.getCity());
                existingArchitect.setPhoto(
                                request.getPhoto() != null ? request.getPhoto() : existingArchitect.getPhoto());
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

                if (request.getPassword() != null) {
                        log.info("Architect Service : Hashing password");
                        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
                        existingArchitect.setPassword(hashedPassword);
                }

                log.info("Architect Service : Saving architect to DB");
                architectRepository.save(existingArchitect);
        }

        public List<Architect> getAllArchitects() {
                log.info("Fetching all architects");
                return architectRepository.findAll();
        }

        public Architect getArchitectById(UUID id) {
                log.info("Fetching architect with id: {}", id);
                return architectRepository.findById(id).orElse(null);
        }

        public void deleteArchitect(UUID id) {
                log.info("Deleting architect with id: {}", id);

                architectRepository.findById(id).orElseThrow(() -> {
                        throw new BadRequestException("Architect not found");
                });

                architectRepository.deleteById(id);
        }
}
