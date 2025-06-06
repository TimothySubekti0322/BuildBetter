package com.buildbetter.plan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.buildbetter.user.api.UserAPI;

@ExtendWith(MockitoExtension.class)
public class PlanServiceTest {

        @Mock
        private MaterialRepository materialRepository; // unused in addPlan, but required by constructor
        @Mock
        private SuggestionRepository suggestionRepository;
        @Mock
        private PlanRepository planRepository;
        @Mock
        private UserAPI userAPI;

        @InjectMocks
        private PlanService planService;

        private UUID userId;
        private UUID suggestionId;
        private AddPlanRequest request;
        private Suggestion suggestion;
        private Plan plan;

        @BeforeEach
        void setUp() {
                userId = UUID.randomUUID();
                suggestionId = UUID.randomUUID();

                // build a request with all required fields
                request = new AddPlanRequest();
                request.setSuggestionId(suggestionId);
                request.setProvince("ProvinceA");
                request.setCity("CityB");
                request.setLandform("Hill");
                request.setLandArea(120);
                request.setEntranceDirection("North");
                request.setStyle("Modern");
                request.setFloor(2);
                request.setRooms(4);

                // a dummy Suggestion entity to be returned by the repo
                suggestion = new Suggestion();

                userId = UUID.randomUUID();

                // a simple Plan + Suggestion
                suggestion = new Suggestion();
                plan = new Plan();
                plan.setUserId(userId);
                plan.setSuggestion(suggestion);
        }

        @Test
        @DisplayName("addPlan → saves a Plan when user & suggestion exist")
        void addPlan_success() {
                // arrange
                when(userAPI.existsById(userId)).thenReturn(true);
                when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestion));

                // act
                planService.addPlan(userId, request);

                // assert that we checked for user & suggestion
                verify(userAPI).existsById(userId);
                verify(suggestionRepository).findById(suggestionId);

                // capture the Plan that was saved
                ArgumentCaptor<Plan> captor = ArgumentCaptor.forClass(Plan.class);
                verify(planRepository).save(captor.capture());
                Plan saved = captor.getValue();

                // verify all fields were correctly mapped
                assertEquals(userId, saved.getUserId());
                assertEquals("ProvinceA", saved.getProvince());
                assertEquals("CityB", saved.getCity());
                assertEquals("Hill", saved.getLandform());
                assertEquals(120, saved.getLandArea());
                assertEquals("North", saved.getEntranceDirection());
                assertEquals("Modern", saved.getStyle());
                assertEquals(2, saved.getFloor());
                assertEquals(4, saved.getRooms());
                assertSame(suggestion, saved.getSuggestion());
        }

        @Test
        @DisplayName("addPlan → throws NotFoundException when user does not exist")
        void addPlan_userNotFound_throws() {
                when(userAPI.existsById(userId)).thenReturn(false);

                NotFoundException ex = assertThrows(
                                NotFoundException.class,
                                () -> planService.addPlan(userId, request));
                assertEquals("User not found", ex.getMessage());
                verify(userAPI).existsById(userId);
                verifyNoMoreInteractions(suggestionRepository, planRepository);
        }

        @Test
        @DisplayName("addPlan → throws NotFoundException when suggestion not found")
        void addPlan_suggestionNotFound_throws() {
                when(userAPI.existsById(userId)).thenReturn(true);
                when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.empty());

                NotFoundException ex = assertThrows(
                                NotFoundException.class,
                                () -> planService.addPlan(userId, request));
                assertEquals("Suggestion not found", ex.getMessage());
                verify(userAPI).existsById(userId);
                verify(suggestionRepository).findById(suggestionId);
                verifyNoMoreInteractions(planRepository);
        }

        @Test
        @DisplayName("getAllPlans → maps Plan → GetPlansResponse for each plan")
        void getAllPlans_success_returnsResponses() {

                when(userAPI.existsById(userId)).thenReturn(true);

                Plan realPlan = new Plan();
                realPlan.setUserId(userId);

                // We also need a real Suggestion inside the Plan
                Suggestion realSuggestion = new Suggestion();
                realPlan.setSuggestion(realSuggestion);

                // Wrap that real Plan in a spy so we can override any methods if needed
                Plan planSpy = spy(realPlan);

                // Make the repository return our spied Plan
                when(planRepository.findAllByOrderByCreatedAtDesc())
                                .thenReturn(List.of(planSpy));

                UUID matId = UUID.randomUUID();
                Set<UUID> matIds = Set.of(matId);

                // Create a Mockito mock for Material (the service just needs material.getId())
                Material material = mock(Material.class);
                when(material.getId()).thenReturn(matId);

                try (MockedStatic<SuggestionUtils> utils = mockStatic(SuggestionUtils.class)) {
                        // SuggestionUtils.collectMaterialIds(realSuggestion) → matIds
                        utils.when(() -> SuggestionUtils.collectMaterialIds(realSuggestion))
                                        .thenReturn(matIds);

                        // materialRepository.findAllById(matIds) → [material]
                        when(materialRepository.findAllById(eq(matIds)))
                                        .thenReturn(List.of(material));

                        // SuggestionUtils.toGetSuggestionResponse(realSuggestion, {matId→material})
                        SuggestionResponse suggestionResp = new SuggestionResponse();
                        utils.when(() -> SuggestionUtils.toGetSuggestionResponse(realSuggestion,
                                        Map.of(matId, material)))
                                        .thenReturn(suggestionResp);

                        // SuggestionUtils.planToGenerateSuggestionRequest(planSpy, suggestionResp)
                        GenerateSuggestionRequest genReq = new GenerateSuggestionRequest();
                        utils.when(() -> SuggestionUtils.planToGenerateSuggestionRequest(planSpy, suggestionResp))
                                        .thenReturn(genReq);

                        GetPlansResponse[] responses = planService.getAllPlans(userId, "admin");

                        GetPlansResponse single = responses[0];
                        assertSame(suggestionResp, single.getSuggestions());
                        assertSame(genReq, single.getUserInput());

                        verify(userAPI).existsById(userId);
                        verify(planRepository).findAllByOrderByCreatedAtDesc();
                        verify(materialRepository).findAllById(matIds);
                }
        }

        @Test
        @DisplayName("getPlanById → returns a GetPlansResponse with correct suggestion and userInput")
        void getPlanById_success() {
                UUID planId = UUID.randomUUID();

                // Arrange: Plan with a Suggestion
                Suggestion suggestion = new Suggestion();
                Plan plan = new Plan();
                plan.setSuggestion(suggestion);
                when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

                // Prepare material IDs and entities
                Set<UUID> matIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
                List<Material> materials = matIds.stream().map(id -> {
                        Material m = new Material();
                        m.setId(id);
                        return m;
                }).collect(Collectors.toList());

                // Stubbing static methods
                try (MockedStatic<SuggestionUtils> utils = mockStatic(SuggestionUtils.class)) {
                        // 1. collectMaterialIds
                        utils.when(() -> SuggestionUtils.collectMaterialIds(suggestion))
                                        .thenReturn(matIds);
                        // 2. findAllById
                        when(materialRepository.findAllById(matIds)).thenReturn(materials);
                        // 3. toGetSuggestionResponse
                        Map<UUID, Material> matsMap = materials.stream()
                                        .collect(Collectors.toMap(Material::getId, m -> m));
                        SuggestionResponse suggestionResponse = new SuggestionResponse();
                        utils.when(() -> SuggestionUtils.toGetSuggestionResponse(suggestion, matsMap))
                                        .thenReturn(suggestionResponse);
                        // 4. planToGenerateSuggestionRequest
                        GenerateSuggestionRequest genReq = new GenerateSuggestionRequest();
                        utils.when(() -> SuggestionUtils.planToGenerateSuggestionRequest(plan, suggestionResponse))
                                        .thenReturn(genReq);

                        // Act
                        GetPlansResponse response = planService.getPlanById(planId);

                        // Assert interactions
                        verify(planRepository).findById(planId);
                        utils.verify(() -> SuggestionUtils.collectMaterialIds(suggestion), times(1));
                        verify(materialRepository).findAllById(matIds);
                        utils.verify(() -> SuggestionUtils.toGetSuggestionResponse(suggestion, matsMap), times(1));
                        utils.verify(() -> SuggestionUtils.planToGenerateSuggestionRequest(plan, suggestionResponse),
                                        times(1));

                        // Assert returned DTO
                        assertSame(suggestionResponse, response.getSuggestions());
                        assertSame(genReq, response.getUserInput());
                }
        }

        @Test
        @DisplayName("getPlanById → throws NotFoundException when plan not found")
        void getPlanById_notFound() {
                UUID planId = UUID.randomUUID();
                when(planRepository.findById(planId)).thenReturn(Optional.empty());

                NotFoundException ex = assertThrows(
                                NotFoundException.class,
                                () -> planService.getPlanById(planId));
                assertEquals("Plan not found", ex.getMessage());

                verify(planRepository).findById(planId);
                verifyNoInteractions(materialRepository);
        }
}
