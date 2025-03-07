// package com.buildbetter.plan.service;

// import java.util.UUID;

// import org.springframework.stereotype.Service;

// import com.buildbetter.plan.repository.PlanRepository;
// import com.buildbetter.user.UserAPI;

// @Service
// public class PlanService {
//     private final PlanRepository planRepository;
//     private final UserAPI userAPI;

//     public PlanService(PlanRepository planRepository, UserAPI userAPI) {
//         this.planRepository = planRepository;
//         this.userAPI = userAPI;
//     }

//     public boolean existsById(UUID planId) {
//         return userAPI.existsById(planId);
//     }

// }
