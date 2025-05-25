package com.buildbetter.consultation.util;

import com.buildbetter.consultation.dto.architect.ArchitectResponse;
import com.buildbetter.consultation.model.Architect;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ArchitectUtils {
    public static String constructPasswordFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }

        int atIndex = email.indexOf('@');

        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    public static ArchitectResponse mapToArchitectResponse(Architect architect) {
        return ArchitectResponse.builder()
                .id(architect.getId())
                .email(architect.getEmail())
                .username(architect.getUsername())
                .photo(architect.getPhoto())
                .province(architect.getProvince())
                .city(architect.getCity())
                .phoneNumber(architect.getPhoneNumber())
                .experience(architect.getExperience())
                .rateOnline(architect.getRateOnline())
                .rateOffline(architect.getRateOffline())
                .portfolio(architect.getPortfolio())
                .createdAt(architect.getCreatedAt())
                .build();
    }
}
