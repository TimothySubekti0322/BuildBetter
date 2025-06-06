package com.buildbetter.plan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.buildbetter.plan.dto.suggestions.AddSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.AddSuggestionUrlRequest;
import com.buildbetter.plan.dto.suggestions.SuggestionResponse;
import com.buildbetter.plan.dto.suggestions.UpdateSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.UploadFloorPlans;
import com.buildbetter.plan.dto.suggestions.UploadHouseFileRequest;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionRequest;
import com.buildbetter.plan.dto.suggestions.generate.GenerateSuggestionResponse;
import com.buildbetter.plan.model.Material;
import com.buildbetter.plan.model.Suggestion;
import com.buildbetter.plan.repository.MaterialRepository;
import com.buildbetter.plan.repository.SuggestionRepository;
import com.buildbetter.plan.util.SuggestionUtils; // We will use actual SuggestionUtils for transformation logic where possible or mock it if it becomes too complex for service unit test
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.exception.BadRequestException;
import com.buildbetter.shared.exception.NotFoundException;
import com.buildbetter.shared.util.S3Service;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private SuggestionRepository suggestionRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private SuggestionService suggestionService;

    @Captor
    private ArgumentCaptor<Suggestion> suggestionArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<String>> stringListArgumentCaptor;

    private UUID testSuggestionId;
    private Suggestion testSuggestion;

    @BeforeEach
    void setUp() {
        testSuggestionId = UUID.randomUUID();
        testSuggestion = new Suggestion();
        testSuggestion.setId(testSuggestionId);
        testSuggestion.setHouseNumber("H123");
        // Initialize other fields if needed for general setup
    }

    @Nested
    @DisplayName("addSuggestion Tests")
    class AddSuggestionTests {
        @Test
        @DisplayName("Should add suggestion and return its ID")
        void addSuggestion_shouldSaveSuggestionAndReturnId() {
            // Given
            AddSuggestionRequest request = new AddSuggestionRequest();
            request.setHouseNumber("HN001");
            request.setWindDirection(List.of("North"));
            request.setLandArea(200);
            request.setBuildingArea(150);
            request.setStyle("Modern");
            request.setFloor(2);
            request.setRooms(5);
            request.setBuildingHeight(10);
            request.setDesigner("Jane Doe");
            request.setDefaultBudget(0);
            request.setBudgetMin(List.of(80000, 100000, 120000));
            request.setBudgetMax(List.of(120000, 150000, 180000));
            List<UUID> materialIds = List.of(UUID.randomUUID(), UUID.randomUUID());
            request.setMaterials0(materialIds);
            request.setMaterials1(new ArrayList<>());
            request.setMaterials2(new ArrayList<>());

            Suggestion savedSuggestion = new Suggestion();
            UUID generatedId = UUID.randomUUID();
            savedSuggestion.setId(generatedId);
            // Set other properties on savedSuggestion if they are read back or used

            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

            // When
            UUID resultId = suggestionService.addSuggestion(request);

            // Then
            assertEquals(generatedId, resultId);
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            Suggestion capturedSuggestion = suggestionArgumentCaptor.getValue();

            assertEquals(request.getHouseNumber(), capturedSuggestion.getHouseNumber());
            assertEquals(request.getWindDirection(), capturedSuggestion.getWindDirection());
            assertEquals(request.getLandArea(), capturedSuggestion.getLandArea());
            assertEquals(request.getBuildingArea(), capturedSuggestion.getBuildingArea());
            assertEquals(request.getStyle(), capturedSuggestion.getStyle());
            assertEquals(request.getFloor(), capturedSuggestion.getFloor());
            assertEquals(request.getRooms(), capturedSuggestion.getRooms());
            assertEquals(request.getBuildingHeight(), capturedSuggestion.getBuildingHeight());
            assertEquals(request.getDesigner(), capturedSuggestion.getDesigner());
            assertEquals(request.getDefaultBudget(), capturedSuggestion.getDefaultBudget());
            assertEquals(request.getBudgetMin(), capturedSuggestion.getBudgetMin());
            assertEquals(request.getBudgetMax(), capturedSuggestion.getBudgetMax());
            assertEquals(request.getMaterials0(), capturedSuggestion.getMaterials0());
            assertEquals(request.getMaterials1(), capturedSuggestion.getMaterials1());
            assertEquals(request.getMaterials2(), capturedSuggestion.getMaterials2());
        }
    }

    @Nested
    @DisplayName("uploadFloorPlans Tests")
    class UploadFloorPlansTests {
        @Test
        @DisplayName("Should upload floor plans when suggestion exists and has no existing plans")
        void uploadFloorPlans_suggestionExists_noExistingPlans() {
            // Given
            UUID suggestionId = testSuggestionId;
            testSuggestion.setFloorplans(null); // Or new ArrayList<>()
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            MockMultipartFile file1 = new MockMultipartFile("files", "plan1.jpg", "image/jpeg",
                    "plan1_content".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("files", "plan2.png", "image/png",
                    "plan2_content".getBytes());
            UploadFloorPlans request = new UploadFloorPlans();
            request.setId(suggestionId);
            request.setFiles(new MultipartFile[] { file1, file2 });

            String expectedUrl1 = "s3://bucket/suggestions/H123/plan1.jpg";
            String expectedUrl2 = "s3://bucket/suggestions/H123/plan2.png";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";

            when(s3Service.uploadFile(eq(file1), eq(expectedFolder), anyString())).thenReturn(expectedUrl1);
            when(s3Service.uploadFile(eq(file2), eq(expectedFolder), anyString())).thenReturn(expectedUrl2);

            // When
            suggestionService.uploadFloorPlans(request);

            // Then
            verify(suggestionRepository).findById(suggestionId);
            verify(s3Service).uploadFile(eq(file1), eq(expectedFolder), anyString());
            verify(s3Service).uploadFile(eq(file2), eq(expectedFolder), anyString());
            verify(s3Service, never()).deleteFile(anyString());
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());

            Suggestion savedSuggestion = suggestionArgumentCaptor.getValue();
            assertNotNull(savedSuggestion.getFloorplans());
            assertEquals(2, savedSuggestion.getFloorplans().size());
            assertTrue(savedSuggestion.getFloorplans().containsAll(List.of(expectedUrl1, expectedUrl2)));
        }

        @Test
        @DisplayName("Should upload floor plans and delete old ones when suggestion exists with existing plans")
        void uploadFloorPlans_suggestionExists_withExistingPlans() {
            // Given
            UUID suggestionId = testSuggestionId;
            String oldPlanUrl1 = "s3://bucket/suggestions/H123/old_plan1.jpg";
            String oldPlanUrl2 = "s3://bucket/suggestions/H123/old_plan2.jpg";
            testSuggestion.setFloorplans(new ArrayList<>(List.of(oldPlanUrl1, oldPlanUrl2)));

            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            MockMultipartFile newFile = new MockMultipartFile("files", "new_plan.jpg", "image/jpeg",
                    "new_plan_content".getBytes());
            UploadFloorPlans request = new UploadFloorPlans();
            request.setId(suggestionId);
            request.setFiles(new MultipartFile[] { newFile });

            String newPlanUrl = "s3://bucket/suggestions/H123/new_plan.jpg";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";

            when(s3Service.uploadFile(eq(newFile), eq(expectedFolder), anyString())).thenReturn(newPlanUrl);

            // When
            suggestionService.uploadFloorPlans(request);

            // Then
            verify(suggestionRepository).findById(suggestionId);
            verify(s3Service).uploadFile(eq(newFile), eq(expectedFolder), anyString());
            verify(s3Service).deleteFile(oldPlanUrl1);
            verify(s3Service).deleteFile(oldPlanUrl2);
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());

            Suggestion savedSuggestion = suggestionArgumentCaptor.getValue();
            assertNotNull(savedSuggestion.getFloorplans());
            assertEquals(1, savedSuggestion.getFloorplans().size());
            assertTrue(savedSuggestion.getFloorplans().contains(newPlanUrl));
        }

        @Test
        @DisplayName("Should skip empty files during upload")
        void uploadFloorPlans_skipEmptyFiles() {
            // Given
            UUID suggestionId = testSuggestionId;
            testSuggestion.setFloorplans(null);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            MockMultipartFile emptyFile = new MockMultipartFile("files", "empty.jpg", "image/jpeg", new byte[0]);
            MockMultipartFile validFile = new MockMultipartFile("files", "valid.jpg", "image/jpeg",
                    "valid_content".getBytes());
            UploadFloorPlans request = new UploadFloorPlans();
            request.setId(suggestionId);
            request.setFiles(new MultipartFile[] { emptyFile, validFile });

            String expectedValidUrl = "s3://bucket/suggestions/H123/valid.jpg";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";
            when(s3Service.uploadFile(eq(validFile), eq(expectedFolder), anyString())).thenReturn(expectedValidUrl);

            // When
            suggestionService.uploadFloorPlans(request);

            // Then
            verify(s3Service, never()).uploadFile(eq(emptyFile), anyString(), anyString());
            verify(s3Service).uploadFile(eq(validFile), eq(expectedFolder), anyString());
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            Suggestion savedSuggestion = suggestionArgumentCaptor.getValue();
            assertEquals(1, savedSuggestion.getFloorplans().size());
            assertEquals(expectedValidUrl, savedSuggestion.getFloorplans().get(0));
        }

        @Test
        @DisplayName("Should throw NotFoundException if suggestion does not exist")
        void uploadFloorPlans_suggestionNotFound_throwsNotFoundException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            UploadFloorPlans request = new UploadFloorPlans();
            request.setId(nonExistentId);
            request.setFiles(new MultipartFile[] {
                    new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes()) });

            when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                suggestionService.uploadFloorPlans(request);
            });
            assertEquals("Suggestion not found", exception.getMessage());
            verify(s3Service, never()).uploadFile(any(MultipartFile.class), anyString(), anyString());
            verify(s3Service, never()).deleteFile(anyString());
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }
    }

    @Nested
    @DisplayName("uploadHouseFile Tests")
    class UploadHouseFileTests {
        private UploadHouseFileRequest createUploadHouseFileRequest(UUID id, String type, MockMultipartFile file) {
            UploadHouseFileRequest request = new UploadHouseFileRequest();
            request.setId(id);
            request.setType(type);
            request.setFile(file);
            return request;
        }

        @Test
        @DisplayName("Should upload HOUSE_IMAGE_FRONT and save URL, no existing image")
        void uploadHouseFile_houseImageFront_noExisting() {
            // Given
            UUID suggestionId = testSuggestionId;
            testSuggestion.setHouseImageFront(null);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            MockMultipartFile file = new MockMultipartFile("file", "front.jpg", "image/jpeg",
                    "front_content".getBytes());
            UploadHouseFileRequest request = createUploadHouseFileRequest(suggestionId, "HOUSE_IMAGE_FRONT", file);

            String expectedUrl = "s3://bucket/suggestions/H123/front.jpg";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";
            when(s3Service.uploadFile(eq(file), eq(expectedFolder), anyString())).thenReturn(expectedUrl);

            // When
            suggestionService.uploadHouseFile(request);

            // Then
            verify(s3Service).uploadFile(eq(file), eq(expectedFolder), anyString());
            verify(s3Service, never()).deleteFile(anyString());
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            assertEquals(expectedUrl, suggestionArgumentCaptor.getValue().getHouseImageFront());
        }

        @Test
        @DisplayName("Should upload HOUSE_IMAGE_FRONT and delete old one")
        void uploadHouseFile_houseImageFront_withExisting() {
            // Given
            UUID suggestionId = testSuggestionId;
            String oldImageUrl = "s3://bucket/suggestions/H123/old_front.jpg";
            testSuggestion.setHouseImageFront(oldImageUrl);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            MockMultipartFile file = new MockMultipartFile("file", "new_front.jpg", "image/jpeg",
                    "new_front_content".getBytes());
            UploadHouseFileRequest request = createUploadHouseFileRequest(suggestionId, "HOUSE_IMAGE_FRONT", file);

            String newImageUrl = "s3://bucket/suggestions/H123/new_front.jpg";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";
            when(s3Service.uploadFile(eq(file), eq(expectedFolder), anyString())).thenReturn(newImageUrl);

            // When
            suggestionService.uploadHouseFile(request);

            // Then
            verify(s3Service).uploadFile(eq(file), eq(expectedFolder), anyString());
            verify(s3Service).deleteFile(oldImageUrl);
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            assertEquals(newImageUrl, suggestionArgumentCaptor.getValue().getHouseImageFront());
        }

        // Similar tests for HOUSE_IMAGE_BACK, HOUSE_IMAGE_SIDE, HOUSE_OBJECT, PDF
        @Test
        @DisplayName("Should upload HOUSE_IMAGE_BACK and save URL")
        void uploadHouseFile_houseImageBack() {
            UUID suggestionId = testSuggestionId;
            testSuggestion.setHouseImageBack(null);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));
            MockMultipartFile file = new MockMultipartFile("file", "back.jpg", "image/jpeg", "back_content".getBytes());
            UploadHouseFileRequest request = createUploadHouseFileRequest(suggestionId, "HOUSE_IMAGE_BACK", file);
            String expectedUrl = "s3://bucket/suggestions/H123/back.jpg";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";
            when(s3Service.uploadFile(eq(file), eq(expectedFolder), anyString())).thenReturn(expectedUrl);

            suggestionService.uploadHouseFile(request);

            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            assertEquals(expectedUrl, suggestionArgumentCaptor.getValue().getHouseImageBack());
        }

        @Test
        @DisplayName("Should upload HOUSE_OBJECT and save URL")
        void uploadHouseFile_houseObject() {
            UUID suggestionId = testSuggestionId;
            testSuggestion.setObject(null);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));
            MockMultipartFile file = new MockMultipartFile("file", "object.glb", "model/gltf-binary",
                    "object_content".getBytes());
            UploadHouseFileRequest request = createUploadHouseFileRequest(suggestionId, "HOUSE_OBJECT", file);
            String expectedUrl = "s3://bucket/suggestions/H123/object.glb";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";
            when(s3Service.uploadFile(eq(file), eq(expectedFolder), anyString())).thenReturn(expectedUrl);

            suggestionService.uploadHouseFile(request);

            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            assertEquals(expectedUrl, suggestionArgumentCaptor.getValue().getObject());
        }

        @Test
        @DisplayName("Should upload PDF and save URL, deleting old PDF if present")
        void uploadHouseFile_pdf_withExisting() {
            UUID suggestionId = testSuggestionId;
            String oldPdfUrl = "s3://bucket/suggestions/H123/old_document.pdf";
            testSuggestion.setPdf(oldPdfUrl); // Set an existing PDF
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf",
                    "pdf_content".getBytes());
            UploadHouseFileRequest request = createUploadHouseFileRequest(suggestionId, "PDF", file);

            String newPdfUrl = "s3://bucket/suggestions/H123/document.pdf";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";
            when(s3Service.uploadFile(eq(file), eq(expectedFolder), anyString())).thenReturn(newPdfUrl);

            // When
            suggestionService.uploadHouseFile(request);

            // Then
            verify(s3Service).uploadFile(eq(file), eq(expectedFolder), anyString());
            verify(s3Service).deleteFile(oldPdfUrl); // Verify old PDF is deleted
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            assertEquals(newPdfUrl, suggestionArgumentCaptor.getValue().getPdf());
        }

        @Test
        @DisplayName("Should throw NotFoundException if suggestion does not exist")
        void uploadHouseFile_suggestionNotFound_throwsNotFoundException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
            UploadHouseFileRequest request = createUploadHouseFileRequest(nonExistentId, "HOUSE_IMAGE_FRONT", file);

            when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                suggestionService.uploadHouseFile(request);
            });
            assertEquals("Suggestion not found", exception.getMessage());
            verify(s3Service, never()).uploadFile(any(MultipartFile.class), anyString(), anyString());
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid HouseFileType")
        void uploadHouseFile_invalidType_throwsIllegalArgumentException() {
            // Given
            UUID suggestionId = testSuggestionId;
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
            UploadHouseFileRequest request = createUploadHouseFileRequest(suggestionId, "INVALID_TYPE", file);

            String expectedUrl = "s3://bucket/suggestions/H123/test.jpg";
            String expectedFolder = S3Folder.SUGGESTIONS + testSuggestion.getHouseNumber() + "/";
            when(s3Service.uploadFile(eq(file), eq(expectedFolder), anyString())).thenReturn(expectedUrl);

            // When & Then
            assertThrows(BadRequestException.class, () -> {
                suggestionService.uploadHouseFile(request);
            });
            verify(suggestionRepository, never()).save(any(Suggestion.class)); // Ensure save is not called if type is
                                                                               // invalid
        }
    }

    @Nested
    @DisplayName("addSuggestionUrl Tests")
    class AddSuggestionUrlTests {
        private AddSuggestionUrlRequest createAddSuggestionUrlRequest(UUID id, String type, String url) {
            AddSuggestionUrlRequest request = new AddSuggestionUrlRequest();
            request.setId(id);
            request.setType(type);
            request.setUrl(url);
            return request;
        }

        @Test
        @DisplayName("Should add HOUSE_IMAGE_FRONT URL, no existing URL")
        void addSuggestionUrl_houseImageFront_noExisting() {
            // Given
            UUID suggestionId = testSuggestionId;
            testSuggestion.setHouseImageFront(null);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            String newUrl = "http://example.com/image_front.jpg";
            AddSuggestionUrlRequest request = createAddSuggestionUrlRequest(suggestionId, "HOUSE_IMAGE_FRONT", newUrl);

            // When
            suggestionService.addSugesstionUrl(request); // Corrected method name in test to match service

            // Then
            verify(s3Service, never()).deleteFile(anyString());
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            assertEquals(newUrl, suggestionArgumentCaptor.getValue().getHouseImageFront());
        }

        @Test
        @DisplayName("Should add HOUSE_IMAGE_FRONT URL and delete old one from S3 if it was an S3 URL")
        void addSuggestionUrl_houseImageFront_withExistingS3Url() {
            // Given
            UUID suggestionId = testSuggestionId;
            String oldS3Url = "s3://bucket/suggestions/H123/old_front.jpg"; // Assume this is an S3 URL
            testSuggestion.setHouseImageFront(oldS3Url);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            String newUrl = "http://example.com/new_image_front.jpg";
            AddSuggestionUrlRequest request = createAddSuggestionUrlRequest(suggestionId, "HOUSE_IMAGE_FRONT", newUrl);

            // When
            suggestionService.addSugesstionUrl(request);

            // Then
            verify(s3Service).deleteFile(oldS3Url); // Expect delete because old URL is an S3 one
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            assertEquals(newUrl, suggestionArgumentCaptor.getValue().getHouseImageFront());
        }

        @Test
        @DisplayName("Should add PDF URL and delete old PDF if present")
        void addSuggestionUrl_pdf_withExisting() {
            UUID suggestionId = testSuggestionId;
            String oldPdfUrl = "s3://bucket/suggestions/H123/old_doc.pdf";
            testSuggestion.setPdf(oldPdfUrl);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            String newUrl = "http://example.com/new_doc.pdf";
            AddSuggestionUrlRequest request = createAddSuggestionUrlRequest(suggestionId, "PDF", newUrl);

            suggestionService.addSugesstionUrl(request);

            verify(s3Service).deleteFile(oldPdfUrl);
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            assertEquals(newUrl, suggestionArgumentCaptor.getValue().getPdf());
        }

        @Test
        @DisplayName("Should throw NotFoundException if suggestion does not exist")
        void addSuggestionUrl_suggestionNotFound_throwsNotFoundException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            AddSuggestionUrlRequest request = createAddSuggestionUrlRequest(nonExistentId, "HOUSE_IMAGE_FRONT",
                    "http://example.com/image.jpg");
            when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                suggestionService.addSugesstionUrl(request);
            });
            assertEquals("Suggestion not found", exception.getMessage());
            verify(s3Service, never()).deleteFile(anyString());
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for invalid HouseFileType")
        void addSuggestionUrl_invalidType_throwsIllegalArgumentException() {
            // Given
            UUID suggestionId = testSuggestionId;
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));
            AddSuggestionUrlRequest request = createAddSuggestionUrlRequest(suggestionId, "INVALID_TYPE",
                    "http://example.com/image.jpg");

            // When & Then
            assertThrows(BadRequestException.class, () -> {
                suggestionService.addSugesstionUrl(request);
            });
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }
    }

    @Nested
    @DisplayName("getAllSuggestions Tests")
    class GetAllSuggestionsTests {
        @Test
        @DisplayName("Should return all suggestions mapped to responses")
        void getAllSuggestions_shouldReturnMappedResponses() {
            // For SuggestionUtils.toGetSuggestionResponse, we will need to mock its static
            // call
            // or ensure it works correctly with the provided data, and verify the outcome.
            // Using Mockito.mockStatic for this.

            // Given
            Suggestion suggestion1 = new Suggestion();
            suggestion1.setId(UUID.randomUUID());
            suggestion1.setHouseNumber("S1");
            UUID mat1Id = UUID.randomUUID();
            UUID mat2Id = UUID.randomUUID();
            suggestion1.setMaterials0(List.of(mat1Id));

            Suggestion suggestion2 = new Suggestion();
            suggestion2.setId(UUID.randomUUID());
            suggestion2.setHouseNumber("S2");
            suggestion2.setMaterials1(List.of(mat2Id));

            List<Suggestion> suggestions = List.of(suggestion1, suggestion2);
            when(suggestionRepository.findAll()).thenReturn(suggestions);

            Material material1 = new Material();
            material1.setId(mat1Id);
            material1.setName("Brick");
            Material material2 = new Material();
            material2.setId(mat2Id);
            material2.setName("Wood");
            Map<UUID, Material> materialMap = Map.of(mat1Id, material1, mat2Id, material2);

            // Mocking materialRepository to return these specific materials when queried
            // with their IDs
            // The service calls findAllById with a Set of IDs.
            Set<UUID> allMaterialIds = Stream
                    .of(suggestion1.getMaterials0(), suggestion1.getMaterials1(), suggestion1.getMaterials2(),
                            suggestion2.getMaterials0(), suggestion2.getMaterials1(), suggestion2.getMaterials2())
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            when(materialRepository.findAllById(allMaterialIds)).thenReturn(List.of(material1, material2));

            // Expected responses
            SuggestionResponse resp1 = new SuggestionResponse();
            resp1.setId(suggestion1.getId());
            resp1.setHouseNumber("S1"); // Simplified
            SuggestionResponse resp2 = new SuggestionResponse();
            resp2.setId(suggestion2.getId());
            resp2.setHouseNumber("S2"); // Simplified

            // If SuggestionUtils.toGetSuggestionResponse is complex, mock it.
            // For this test, we'll assume it correctly maps.
            // If SuggestionUtils is a class you wrote, it should have its own unit tests.
            // Here we focus on the SuggestionService's orchestration.

            try (MockedStatic<SuggestionUtils> mockedUtils = Mockito.mockStatic(SuggestionUtils.class)) {
                mockedUtils.when(() -> SuggestionUtils.toGetSuggestionResponse(eq(suggestion1), anyMap()))
                        .thenReturn(resp1);
                mockedUtils.when(() -> SuggestionUtils.toGetSuggestionResponse(eq(suggestion2), anyMap()))
                        .thenReturn(resp2);

                // When
                List<SuggestionResponse> result = suggestionService.getAllSuggestions();

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());
                assertTrue(result.containsAll(List.of(resp1, resp2)));

                verify(suggestionRepository).findAll();
                verify(materialRepository).findAllById(allMaterialIds);
                mockedUtils.verify(() -> SuggestionUtils.toGetSuggestionResponse(eq(suggestion1), anyMap()), times(1));
                mockedUtils.verify(() -> SuggestionUtils.toGetSuggestionResponse(eq(suggestion2), anyMap()), times(1));
            }
        }

        @Test
        @DisplayName("Should return empty list if no suggestions exist")
        void getAllSuggestions_noSuggestions_returnsEmptyList() {
            // Given
            when(suggestionRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<SuggestionResponse> result = suggestionService.getAllSuggestions();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getSuggestionById Tests")
    class GetSuggestionByIdTests {
        @Test
        @DisplayName("Should return suggestion response when suggestion found")
        void getSuggestionById_suggestionFound_returnsResponse() {
            // Given
            UUID suggestionId = testSuggestionId;
            testSuggestion.setHouseNumber("TestHouse");
            UUID matId = UUID.randomUUID();
            testSuggestion.setMaterials0(List.of(matId));

            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(testSuggestion));

            Material material = new Material();
            material.setId(matId);
            material.setName("Steel");
            Map<UUID, Material> materialMap = Map.of(matId, material);
            when(materialRepository.findAllById(Set.of(matId))).thenReturn(List.of(material));

            SuggestionResponse expectedResponse = new SuggestionResponse();
            expectedResponse.setId(suggestionId);
            expectedResponse.setHouseNumber("TestHouse");
            // ... other fields if SuggestionUtils.toGetSuggestionResponse is not mocked

            try (MockedStatic<SuggestionUtils> mockedUtils = Mockito.mockStatic(SuggestionUtils.class)) {
                mockedUtils.when(() -> SuggestionUtils.toGetSuggestionResponse(eq(testSuggestion), anyMap()))
                        .thenReturn(expectedResponse);

                // When
                SuggestionResponse result = suggestionService.getSuggestionById(suggestionId);

                // Then
                assertNotNull(result);
                assertEquals(expectedResponse.getId(), result.getId());
                assertEquals(expectedResponse.getHouseNumber(), result.getHouseNumber());

                verify(suggestionRepository).findById(suggestionId);
                verify(materialRepository).findAllById(Set.of(matId));
                mockedUtils.verify(() -> SuggestionUtils.toGetSuggestionResponse(eq(testSuggestion), anyMap()),
                        times(1));
            }
        }

        @Test
        @DisplayName("Should throw NotFoundException when suggestion not found")
        void getSuggestionById_suggestionNotFound_throwsNotFoundException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                suggestionService.getSuggestionById(nonExistentId);
            });
            assertEquals("Suggestion not found", exception.getMessage());
            verify(materialRepository, never()).findAllById(anyCollection());
        }
    }

    @Nested
    @DisplayName("updateSuggestion Tests")
    class UpdateSuggestionTests {
        @Test
        @DisplayName("Should update suggestion fields correctly")
        void updateSuggestion_suggestionExists_updatesFields() {
            // Given
            UUID suggestionId = testSuggestionId;
            Suggestion existingSuggestion = new Suggestion();
            existingSuggestion.setId(suggestionId);
            existingSuggestion.setHouseNumber("OldHouse");
            existingSuggestion.setLandArea(100);
            existingSuggestion.setBudgetMin(List.of(50000, 60000, 70000));
            // ... set other old values

            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(existingSuggestion));
            // No need to mock save for void method verification, but ArgumentCaptor will
            // capture the object

            UpdateSuggestionRequest request = new UpdateSuggestionRequest();
            request.setHouseNumber("NewHouse"); // Update this
            request.setLandArea(0); // Should not update landArea as it's 0
            request.setBudgetMin(List.of(60000, 70000, 80000)); // Update this
            // Other fields in request are null or default, so existing values should be
            // kept

            // When
            suggestionService.updateSuggestion(suggestionId, request);

            // Then
            verify(suggestionRepository).findById(suggestionId);
            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            Suggestion updatedSuggestion = suggestionArgumentCaptor.getValue();

            assertEquals(suggestionId, updatedSuggestion.getId());
            assertEquals("NewHouse", updatedSuggestion.getHouseNumber()); // Updated
            assertEquals(100, updatedSuggestion.getLandArea()); // Kept old value
            assertEquals(List.of(60000, 70000, 80000), updatedSuggestion.getBudgetMin()); // Updated
            // Assert other fields remain unchanged or are updated as per request logic
        }

        @Test
        @DisplayName("Should update all fields if request provides all values")
        void updateSuggestion_allFieldsProvided() {
            UUID suggestionId = testSuggestionId;
            Suggestion existingSuggestion = new Suggestion(); // empty initial
            existingSuggestion.setId(suggestionId);
            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(existingSuggestion));

            UpdateSuggestionRequest request = new UpdateSuggestionRequest();
            request.setHouseNumber("UpdatedHouse");
            request.setWindDirection(List.of("South"));
            request.setLandArea(250);
            request.setBuildingArea(180);
            request.setStyle("Contemporary");
            request.setFloor(3);
            request.setRooms(6);
            request.setBuildingHeight(12);
            request.setDesigner("John Smith");
            request.setDefaultBudget(150000);
            request.setBudgetMin(List.of(120000, 140000, 160000));
            request.setBudgetMax(List.of(180000, 200000, 220000));
            List<UUID> newMaterialIds = List.of(UUID.randomUUID());
            request.setMaterials0(newMaterialIds);
            request.setMaterials1(new ArrayList<>());
            request.setMaterials2(new ArrayList<>());

            suggestionService.updateSuggestion(suggestionId, request);

            verify(suggestionRepository).save(suggestionArgumentCaptor.capture());
            Suggestion updatedSuggestion = suggestionArgumentCaptor.getValue();

            assertEquals(request.getHouseNumber(), updatedSuggestion.getHouseNumber());
            assertEquals(request.getWindDirection(), updatedSuggestion.getWindDirection());
            assertEquals(request.getLandArea(), updatedSuggestion.getLandArea());
            // ... and so on for all fields
            assertEquals(request.getMaterials0(), updatedSuggestion.getMaterials0());
        }

        @Test
        @DisplayName("Should throw NotFoundException if suggestion to update does not exist")
        void updateSuggestion_suggestionNotFound_throwsNotFoundException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            UpdateSuggestionRequest request = new UpdateSuggestionRequest();
            request.setHouseNumber("AnyHouse");
            when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                suggestionService.updateSuggestion(nonExistentId, request);
            });
            assertEquals("Suggestion not found", exception.getMessage());
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }
    }

    @Nested
    @DisplayName("deleteSuggestion Tests")
    class DeleteSuggestionTests {
        @Test
        @DisplayName("Should delete suggestion and associated S3 files")
        void deleteSuggestion_suggestionExists_deletesSuggestionAndFiles() {
            // Given
            UUID suggestionId = testSuggestionId;
            Suggestion suggestionToDelete = new Suggestion();
            suggestionToDelete.setId(suggestionId);
            suggestionToDelete.setFloorplans(List.of("s3://bucket/plan1.jpg", "s3://bucket/plan2.jpg"));
            suggestionToDelete.setHouseImageFront("s3://bucket/front.jpg");
            suggestionToDelete.setHouseImageBack("s3://bucket/back.jpg");
            suggestionToDelete.setHouseImageSide(null); // Test null case
            suggestionToDelete.setObject("s3://bucket/object.glb");
            suggestionToDelete.setPdf("s3://bucket/doc.pdf");

            when(suggestionRepository.findById(suggestionId)).thenReturn(Optional.of(suggestionToDelete));
            doNothing().when(suggestionRepository).deleteById(suggestionId);
            // s3Service.deleteFile will be called for each non-null URL

            // When
            suggestionService.deleteSuggestion(suggestionId);

            // Then
            verify(suggestionRepository).findById(suggestionId);
            verify(s3Service).deleteFile("s3://bucket/plan1.jpg");
            verify(s3Service).deleteFile("s3://bucket/plan2.jpg");
            verify(s3Service).deleteFile("s3://bucket/front.jpg");
            verify(s3Service).deleteFile("s3://bucket/back.jpg");
            verify(s3Service, never()).deleteFile(null); // Ensure it handles null gracefully (though code checks)
            verify(s3Service).deleteFile("s3://bucket/object.glb");
            verify(s3Service).deleteFile("s3://bucket/doc.pdf");
            verify(suggestionRepository).deleteById(suggestionId);
        }

        @Test
        @DisplayName("Should throw NotFoundException if suggestion to delete does not exist")
        void deleteSuggestion_suggestionNotFound_throwsNotFoundException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            when(suggestionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(NotFoundException.class, () -> {
                suggestionService.deleteSuggestion(nonExistentId);
            });
            assertEquals("Suggestion not found", exception.getMessage());
            verify(s3Service, never()).deleteFile(anyString());
            verify(suggestionRepository, never()).deleteById(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("generateSuggestion Tests")
    class GenerateSuggestionTests {
        private GenerateSuggestionRequest createGenerateRequest(String style, int landArea, int floor) {
            GenerateSuggestionRequest req = new GenerateSuggestionRequest();
            req.setStyle(style);
            req.setLandArea(landArea);
            req.setFloor(floor);
            return req;
        }

        private Suggestion createSuggestionForGeneration(UUID id, String style, int landArea, int floor,
                List<UUID> materialIds) {
            Suggestion s = new Suggestion();
            s.setId(id);
            s.setStyle(style);
            s.setLandArea(landArea);
            s.setFloor(floor);
            if (materialIds != null) {
                s.setMaterials0(materialIds); // Assuming materials go into materials0 for simplicity
            }
            return s;
        }

        @Test
        @DisplayName("Rule 1: Exact match")
        void generateSuggestion_rule1_exactMatch() {
            GenerateSuggestionRequest req = createGenerateRequest("Modern", 200, 2);
            Suggestion s1 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 200, 2,
                    List.of(UUID.randomUUID()));
            when(suggestionRepository.findByStyleIgnoreCase("Modern")).thenReturn(List.of(s1));

            Material m1 = new Material();
            m1.setId(s1.getMaterials0().get(0));
            when(materialRepository.findAllById(anySet())).thenReturn(List.of(m1));

            SuggestionResponse sr1 = new SuggestionResponse();
            sr1.setId(s1.getId());

            try (MockedStatic<SuggestionUtils> mockedUtils = Mockito.mockStatic(SuggestionUtils.class)) {
                mockedUtils.when(() -> SuggestionUtils.collectMaterialIds(anyList()))
                        .thenReturn(Set.copyOf(s1.getMaterials0()));
                mockedUtils.when(() -> SuggestionUtils.toGetSuggestionResponse(eq(s1), anyMap())).thenReturn(sr1);

                GenerateSuggestionResponse response = suggestionService.generateSuggestion(req);

                assertEquals(1, response.getSuggestions().length);
                assertEquals(s1.getId(), response.getSuggestions()[0].getId());
            }
        }

        @Test
        @DisplayName("Rule 2: Same floor, closest smaller landArea")
        void generateSuggestion_rule2_sameFloorClosestSmallerLandArea() {
            GenerateSuggestionRequest req = createGenerateRequest("Modern", 200, 2); // User wants 200m2
            Suggestion s1 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 150, 2, null); // Available 150m2
            Suggestion s2 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 180, 2,
                    List.of(UUID.randomUUID())); // Available 180m2 (closer)
            Suggestion s3 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 220, 2, null); // Larger, not
                                                                                                      // picked by this
                                                                                                      // rule
            when(suggestionRepository.findByStyleIgnoreCase("Modern")).thenReturn(List.of(s1, s2, s3));

            Material m2 = new Material();
            m2.setId(s2.getMaterials0().get(0));
            when(materialRepository.findAllById(anySet())).thenReturn(List.of(m2));

            SuggestionResponse sr2 = new SuggestionResponse();
            sr2.setId(s2.getId());

            try (MockedStatic<SuggestionUtils> mockedUtils = Mockito.mockStatic(SuggestionUtils.class)) {
                // collectMaterialIds will be called with the selected suggestion (s2)
                mockedUtils.when(() -> SuggestionUtils.collectMaterialIds(List.of(s2)))
                        .thenReturn(Set.copyOf(s2.getMaterials0()));
                mockedUtils.when(() -> SuggestionUtils.toGetSuggestionResponse(eq(s2), anyMap())).thenReturn(sr2);

                GenerateSuggestionResponse response = suggestionService.generateSuggestion(req);

                assertEquals(1, response.getSuggestions().length);
                assertEquals(s2.getId(), response.getSuggestions()[0].getId()); // s2 is 180m2, closer to 200m2 than s1
                                                                                // (150m2)
            }
        }

        @Test
        @DisplayName("Rule 3: Same landArea, different floor")
        void generateSuggestion_rule3_sameLandAreaDifferentFloor() {
            GenerateSuggestionRequest req = createGenerateRequest("Modern", 200, 2); // User: 200m2, 2 floors
            Suggestion s1 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 200, 1,
                    List.of(UUID.randomUUID())); // 200m2, floor=1
            Suggestion s2 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 200, 3,
                    List.of(UUID.randomUUID())); // 200m2, floor=3
            // Omit any “floor=2, landArea < 200” entry so Rule 2 cannot fire
            when(suggestionRepository.findByStyleIgnoreCase("Modern")).thenReturn(List.of(s1, s2));

            Material m1 = new Material();
            m1.setId(s1.getMaterials0().get(0));
            Material m2 = new Material();
            m2.setId(s2.getMaterials0().get(0));
            when(materialRepository.findAllById(anySet())).thenReturn(List.of(m1, m2));

            SuggestionResponse sr1 = new SuggestionResponse();
            sr1.setId(s1.getId());
            SuggestionResponse sr2 = new SuggestionResponse();
            sr2.setId(s2.getId());

            try (MockedStatic<SuggestionUtils> mockedUtils = Mockito.mockStatic(SuggestionUtils.class)) {
                mockedUtils.when(() -> SuggestionUtils.collectMaterialIds(List.of(s1, s2)))
                        .thenReturn(Set.copyOf(Stream.concat(s1.getMaterials0().stream(), s2.getMaterials0().stream())
                                .collect(Collectors.toList())));
                mockedUtils.when(() -> SuggestionUtils.toGetSuggestionResponse(eq(s1), anyMap())).thenReturn(sr1);
                mockedUtils.when(() -> SuggestionUtils.toGetSuggestionResponse(eq(s2), anyMap())).thenReturn(sr2);

                GenerateSuggestionResponse response = suggestionService.generateSuggestion(req);

                assertEquals(2, response.getSuggestions().length);
                assertTrue(Arrays.stream(response.getSuggestions()).anyMatch(s -> s.getId().equals(s1.getId())));
                assertTrue(Arrays.stream(response.getSuggestions()).anyMatch(s -> s.getId().equals(s2.getId())));
            }
        }

        @Test
        @DisplayName("Rule 4: Smaller landArea, different floor")
        void generateSuggestion_rule4_smallerLandAreaDifferentFloor() {
            GenerateSuggestionRequest req = createGenerateRequest("Modern", 200, 2); // User: 200m2, 2 floors
            Suggestion s1 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 150, 1, null); // 150m2, 1 floor
            Suggestion s2 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 180, 3,
                    List.of(UUID.randomUUID())); // 180m2, 3 floors (closest smaller area, different floor)
            Suggestion s3 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 220, 1, null); // Larger area,
                                                                                                      // not picked
            when(suggestionRepository.findByStyleIgnoreCase("Modern")).thenReturn(List.of(s1, s2, s3));

            Material m2 = new Material();
            m2.setId(s2.getMaterials0().get(0));
            when(materialRepository.findAllById(anySet())).thenReturn(List.of(m2));

            SuggestionResponse sr2 = new SuggestionResponse();
            sr2.setId(s2.getId());

            try (MockedStatic<SuggestionUtils> mockedUtils = Mockito.mockStatic(SuggestionUtils.class)) {
                mockedUtils.when(() -> SuggestionUtils.collectMaterialIds(List.of(s2)))
                        .thenReturn(Set.copyOf(s2.getMaterials0()));
                mockedUtils.when(() -> SuggestionUtils.toGetSuggestionResponse(eq(s2), anyMap())).thenReturn(sr2);

                GenerateSuggestionResponse response = suggestionService.generateSuggestion(req);

                assertEquals(1, response.getSuggestions().length);
                assertEquals(s2.getId(), response.getSuggestions()[0].getId());
            }
        }

        @Test
        @DisplayName("No suggestions found by style")
        void generateSuggestion_noStyleMatch_returnsEmpty() {
            GenerateSuggestionRequest req = createGenerateRequest("Rustic", 300, 3);
            when(suggestionRepository.findByStyleIgnoreCase("Rustic")).thenReturn(Collections.emptyList());

            GenerateSuggestionResponse response = suggestionService.generateSuggestion(req);

            assertEquals(0, response.getSuggestions().length);
            assertEquals(req, response.getUserInput());
            verify(materialRepository, never()).findAllById(anySet());
        }

        @Test
        @DisplayName("No rules matched after initial style filter")
        void generateSuggestion_styleMatchButNoRulesHit_returnsEmpty() {
            GenerateSuggestionRequest req = createGenerateRequest("Modern", 100, 1); // User wants 100m2, 1 floor
            // Available suggestions are all larger or don't fit other criteria for rules
            // 2,3,4
            Suggestion s1 = createSuggestionForGeneration(UUID.randomUUID(), "Modern", 300, 3, null);
            when(suggestionRepository.findByStyleIgnoreCase("Modern")).thenReturn(List.of(s1));

            GenerateSuggestionResponse response = suggestionService.generateSuggestion(req);

            assertEquals(0, response.getSuggestions().length);
            assertEquals(req, response.getUserInput());
            verify(materialRepository, never()).findAllById(anySet());
        }
    }
}