package com.buildbetter.plan.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.buildbetter.plan.dto.plans.AddPlanRequest;
import com.buildbetter.plan.dto.plans.GetPlansResponse;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionRequest;
import com.buildbetter.plan.model.Material;
import com.buildbetter.plan.model.Plan;
import com.buildbetter.plan.model.Suggestion;
import com.buildbetter.plan.repository.MaterialRepository;
import com.buildbetter.plan.repository.PlanRepository;
import com.buildbetter.plan.repository.SuggestionRepository;
import com.buildbetter.plan.util.SuggestionUtils;
import com.buildbetter.shared.exception.NotFoundException;
import com.buildbetter.user.UserAPI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanService {

        private final MaterialRepository materialRepository;
        private final SuggestionRepository suggestionRepository;
        private final PlanRepository planRepository;
        private final UserAPI userAPI;

        public void addPlan(UUID userId, AddPlanRequest request) {
                log.info("Plan Service : addPlan");

                // Check if user exists
                if (!userAPI.existsById(userId)) {
                        throw new NotFoundException("User not found");
                }

                // Check Suggestion Id
                Suggestion suggestion = suggestionRepository.findById(request.getSuggestionId())
                                .orElseThrow(() -> new NotFoundException("Suggestion not found"));

                // Build Plan
                Plan plan = Plan.builder()
                                .userId(userId)
                                .province(request.getProvince())
                                .city(request.getCity())
                                .landform(request.getLandform())
                                .landArea(request.getLandArea())
                                .entranceDirection(request.getEntranceDirection())
                                .style(request.getStyle())
                                .floor(request.getFloor())
                                .rooms(request.getRooms())
                                .suggestion(suggestion).build();

                log.info("Plan Service : addPlan - Save plan to DB");
                planRepository.save(plan);
        }

        public GetPlansResponse[] getAllPlans(UUID userId) {
                log.info("Plan Service : getAllPlans");

                // Check if user exists
                if (!userAPI.existsById(userId)) {
                        throw new NotFoundException("User not found");
                }

                // Get All Plans
                List<Plan> plans = planRepository.findByUserIdOrderByCreatedAtDesc(userId);
                if (plans.isEmpty()) {
                        return new GetPlansResponse[0];
                }

                log.info("Plan Service : getAllPlans - Iterate through plans and create GetPlansResponse");
                GetPlansResponse[] plansResponse = new GetPlansResponse[plans.size()];

                // Iterate thrpugh plans and create GetPlansResponse
                for (int i = 0; i < plans.size(); i++) {
                        Plan plan = plans.get(i);
                        Suggestion suggestion = plan.getSuggestion();

                        Set<UUID> matIds = SuggestionUtils.collectMaterialIds(suggestion);
                        Map<UUID, Material> mats = materialRepository.findAllById(matIds)
                                        .stream()
                                        .collect(Collectors.toMap(Material::getId, m -> m));

                        SuggestionResponse suggestionResponse = SuggestionUtils.toGetSuggestionResponse(suggestion,
                                        mats);
                        GenerateSuggestionRequest userInput = SuggestionUtils.planToGenerateSuggestionRequest(plan,
                                        suggestionResponse);

                        plansResponse[i] = new GetPlansResponse(userInput, suggestionResponse);
                }

                return plansResponse;
        }

        public GetPlansResponse getPlanById(UUID planId) {
                log.info("Plan Service : getPlanById");

                // Get Plan by Id
                Plan plan = planRepository.findById(planId)
                                .orElseThrow(() -> new NotFoundException("Plan not found"));

                // Get Suggestion
                Suggestion suggestion = plan.getSuggestion();

                // Get Materials
                log.info("Plan Service : getPlanById - Get Materials");
                Set<UUID> matIds = SuggestionUtils.collectMaterialIds(suggestion);
                Map<UUID, Material> mats = materialRepository.findAllById(matIds)
                                .stream()
                                .collect(Collectors.toMap(Material::getId, m -> m));

                // Prepare Response
                log.info("Plan Service : getPlanById - Prepare Response");
                SuggestionResponse suggestionResponse = SuggestionUtils.toGetSuggestionResponse(suggestion, mats);
                GenerateSuggestionRequest userInput = SuggestionUtils.planToGenerateSuggestionRequest(plan,
                                suggestionResponse);

                GetPlansResponse response = new GetPlansResponse();
                response.setUserInput(userInput);
                response.setSuggestions(suggestionResponse);

                return response;
        }

}
