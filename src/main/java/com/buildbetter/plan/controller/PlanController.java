package com.buildbetter.plan.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.plan.dto.plans.AddPlanRequest;
import com.buildbetter.plan.dto.plans.GetPlansResponse;
import com.buildbetter.plan.service.PlanService;
import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.security.JwtAuthentication;
import com.buildbetter.shared.security.annotation.Authenticated;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/plans")
@Slf4j
public class PlanController {

    private final PlanService planService;

    @PostMapping("")
    @Authenticated
    public ApiResponseMessageOnly addPlans(Authentication auth, @Valid @RequestBody AddPlanRequest request) {

        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        planService.AddPlan(userId, request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Plan added successfully");

        return response;
    }

    @GetMapping("")
    @Authenticated
    public ApiResponseMessageAndData<GetPlansResponse[]> getAllPlans(Authentication auth) {
        JwtAuthentication jwt = (JwtAuthentication) auth;
        UUID userId = UUID.fromString(jwt.claim("id"));

        GetPlansResponse[] plansResponse = planService.getAllPlans(userId);

        ApiResponseMessageAndData<GetPlansResponse[]> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Plans fetched successfully");
        response.setData(plansResponse);
        return response;
    }

    @GetMapping("/{id}")
    @Authenticated
    public ApiResponseMessageAndData<GetPlansResponse> getPlanById(@PathVariable String id) {

        UUID planUuid = UUID.fromString(id);
        GetPlansResponse planResponse = planService.getPlanById(planUuid);

        ApiResponseMessageAndData<GetPlansResponse> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Plan fetched successfully");
        response.setData(planResponse);
        return response;
    }
}
