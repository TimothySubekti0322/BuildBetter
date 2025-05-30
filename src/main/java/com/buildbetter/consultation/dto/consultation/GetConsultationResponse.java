package com.buildbetter.consultation.dto.consultation;

import com.buildbetter.consultation.model.Consultation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetConsultationResponse {
    private Consultation consultation;
    private String architectName;
    private String architectCity;
    private String userName;
    private String userCity;
}
