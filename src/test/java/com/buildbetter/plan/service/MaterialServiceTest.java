package com.buildbetter.plan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;

import com.buildbetter.plan.dto.materials.AddMaterialRequest;
import com.buildbetter.plan.dto.materials.MaterialResponse;
import com.buildbetter.plan.dto.materials.UpdateMaterialRequest;
import com.buildbetter.plan.dto.materials.grouped_material.GroupedMaterialResponse;
import com.buildbetter.plan.dto.materials.grouped_material.MaterialItem;
import com.buildbetter.plan.dto.materials.grouped_material.SubCategory;
import com.buildbetter.plan.model.Material;
import com.buildbetter.plan.repository.MaterialRepository;
import com.buildbetter.plan.util.MaterialUtils;
import com.buildbetter.shared.constant.S3Folder;
import com.buildbetter.shared.exception.NotFoundException;
import com.buildbetter.shared.util.S3Service;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private MaterialService materialService;

    @Test
    @DisplayName("addMaterial → uploads image to S3 with correct folder and saves Material")
    void addMaterial_success() {
        // given: a request with name, category, subCategory, and image
        AddMaterialRequest req = new AddMaterialRequest();
        req.setName("Roof Tile");
        req.setCategory("Building");
        req.setSubCategory("Roof");
        MockMultipartFile image = new MockMultipartFile(
                "image", "tile.png", "image/png", "dummy".getBytes());
        req.setImage(image);

        // stub S3 upload
        String expectedFolder = S3Folder.MATERIALS + "Building" + "/" + "Roof" + "/";
        when(s3Service.uploadFile(image, expectedFolder, ""))
                .thenReturn("https://s3/build_better/materials/Building/Roof/tile.png");

        // when
        materialService.addMaterial(req);

        // then: verify S3 upload called with correct folder
        verify(s3Service).uploadFile(image, expectedFolder, "");

        // capture saved Material
        ArgumentCaptor<Material> captor = ArgumentCaptor.forClass(Material.class);
        verify(materialRepository).save(captor.capture());
        Material saved = captor.getValue();

        // assert fields on saved entity
        assertEquals("Roof Tile", saved.getName());
        assertEquals("Building", saved.getCategory());
        assertEquals("Roof", saved.getSubCategory());
        assertEquals("https://s3/build_better/materials/Building/Roof/tile.png", saved.getImage());

        // no more interactions
        verifyNoMoreInteractions(s3Service, materialRepository);
    }

    @Test
    @DisplayName("getAllMaterials → should fetch all Materials and map to DTOs")
    void getAllMaterials_mapsEverything() {
        // prepare two dummy Material entities
        Material m1 = new Material();
        m1.setId(UUID.randomUUID());
        Material m2 = new Material();
        m2.setId(UUID.randomUUID());

        // stub repository to return them
        when(materialRepository.findAll()).thenReturn(List.of(m1, m2));

        // stub the static mapper
        MaterialResponse dto1 = new MaterialResponse();
        MaterialResponse dto2 = new MaterialResponse();
        try (MockedStatic<MaterialUtils> utils = mockStatic(MaterialUtils.class)) {
            utils.when(() -> MaterialUtils.toMaterialResponse(m1)).thenReturn(dto1);
            utils.when(() -> MaterialUtils.toMaterialResponse(m2)).thenReturn(dto2);

            // execute
            List<MaterialResponse> result = materialService.getAllMaterials();

            // verify repository call
            verify(materialRepository).findAll();

            // verify static mapper calls
            utils.verify(() -> MaterialUtils.toMaterialResponse(m1), times(1));
            utils.verify(() -> MaterialUtils.toMaterialResponse(m2), times(1));

            // assert the returned list is exactly the two DTOs in order
            assertEquals(2, result.size());
            assertSame(dto1, result.get(0));
            assertSame(dto2, result.get(1));
        }
    }

    @Test
    @DisplayName("getAllGroupedMaterials → groups by category/subCategory and returns DTOs in sorted order")
    void getAllGroupedMaterials_groupsAndMapsCorrectly() {
        // --- Prepare some Materials (any initial order; service will sort them) ---
        Material m1 = new Material();
        m1.setId(UUID.randomUUID());
        m1.setCategory("CatA");
        m1.setSubCategory("Sub1");
        m1.setName("Apple");
        m1.setImage("img1");

        Material m2 = new Material();
        m2.setId(UUID.randomUUID());
        m2.setCategory("CatA");
        m2.setSubCategory("Sub1");
        m2.setName("Banana");
        m2.setImage("img2");

        Material m3 = new Material();
        m3.setId(UUID.randomUUID());
        m3.setCategory("CatA");
        m3.setSubCategory("Sub2");
        m3.setName("Avocado");
        m3.setImage("img3");

        Material m4 = new Material();
        m4.setId(UUID.randomUUID());
        m4.setCategory("CatB");
        m4.setSubCategory("Sub1");
        m4.setName("Cherry");
        m4.setImage("img4");

        List<Material> allMats = List.of(m1, m2, m3, m4);

        // Stub the repository to return our list when any Sort is passed
        when(materialRepository.findAll(any(Sort.class)))
                .thenReturn(allMats);

        // --- Execute ---
        List<GroupedMaterialResponse> grouped = materialService.getAllGroupedMaterials();

        // --- Verify Sort parameter ---
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(materialRepository).findAll(sortCaptor.capture());
        Sort sort = sortCaptor.getValue();
        // We expect three ascending orders: category, subCategory, name
        assertEquals(3, sort.stream().count());
        assertTrue(sort.getOrderFor("category").isAscending());
        assertTrue(sort.getOrderFor("subCategory").isAscending());
        assertTrue(sort.getOrderFor("name").isAscending());

        // --- Verify grouping structure ---
        // Top level: 2 categories
        assertEquals(2, grouped.size());

        // Category "CatA"
        GroupedMaterialResponse catA = grouped.get(0);
        assertEquals("CatA", catA.getCategory());
        SubCategory[] subCatsA = catA.getSubCategories();
        assertEquals(2, subCatsA.length);

        // Sub-category "Sub1" under CatA
        assertEquals("Sub1", subCatsA[0].getSubCategory());
        MaterialItem[] itemsSub1 = subCatsA[0].getMaterials();
        assertEquals(2, itemsSub1.length);
        assertEquals("Apple", itemsSub1[0].getName());
        assertEquals(m1.getId(), itemsSub1[0].getId());
        assertEquals("img1", itemsSub1[0].getImage());
        assertEquals("Banana", itemsSub1[1].getName());
        assertEquals(m2.getId(), itemsSub1[1].getId());

        // Sub-category "Sub2" under CatA
        assertEquals("Sub2", subCatsA[1].getSubCategory());
        MaterialItem[] itemsSub2 = subCatsA[1].getMaterials();
        assertEquals(1, itemsSub2.length);
        assertEquals("Avocado", itemsSub2[0].getName());
        assertEquals(m3.getId(), itemsSub2[0].getId());

        // Category "CatB"
        GroupedMaterialResponse catB = grouped.get(1);
        assertEquals("CatB", catB.getCategory());
        SubCategory[] subCatsB = catB.getSubCategories();
        assertEquals(1, subCatsB.length);

        // Sub-category "Sub1" under CatB
        assertEquals("Sub1", subCatsB[0].getSubCategory());
        MaterialItem[] itemsB = subCatsB[0].getMaterials();
        assertEquals(1, itemsB.length);
        assertEquals("Cherry", itemsB[0].getName());
        assertEquals(m4.getId(), itemsB[0].getId());
    }

    @Test
    @DisplayName("getMaterialById → returns DTO when material exists")
    void getMaterialById_success() {
        // given
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(id);
        when(materialRepository.findById(id)).thenReturn(Optional.of(material));

        MaterialResponse expectedDto = new MaterialResponse();
        try (MockedStatic<MaterialUtils> utils = mockStatic(MaterialUtils.class)) {
            utils.when(() -> MaterialUtils.toMaterialResponse(material)).thenReturn(expectedDto);

            // when
            MaterialResponse actual = materialService.getMaterialById(id);

            // then
            verify(materialRepository).findById(id);
            utils.verify(() -> MaterialUtils.toMaterialResponse(material), times(1));
            assertSame(expectedDto, actual);
        }
    }

    @Test
    @DisplayName("getMaterialById → throws NotFoundException when material not found")
    void getMaterialById_notFound() {
        // given
        UUID id = UUID.randomUUID();
        when(materialRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> materialService.getMaterialById(id));
        assertEquals("Material not found", ex.getMessage());
        verify(materialRepository).findById(id);
    }

    @Test
    @DisplayName("updateMaterial → with new image uploads to S3, deletes old image, saves, and returns DTO")
    void updateMaterial_withImage_success() {
        UUID id = UUID.randomUUID();

        // existing entity
        Material existing = new Material();
        existing.setId(id);
        existing.setName("OldName");
        existing.setCategory("OldCat");
        existing.setSubCategory("OldSub");
        existing.setImage("old-url.png");
        when(materialRepository.findById(id)).thenReturn(Optional.of(existing));

        // prepare update request with image + new fields
        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new.png", "image/png", "bytes".getBytes());
        UpdateMaterialRequest req = new UpdateMaterialRequest();
        req.setImage(newImage);
        req.setName("NewName");
        req.setCategory("NewCat");
        req.setSubCategory("NewSub");

        // stub S3 upload
        String expectedFolder = S3Folder.MATERIALS + "NewCat" + "/";
        when(s3Service.uploadFile(newImage, expectedFolder, "")).thenReturn("new-url.png");

        // stub mapper
        MaterialResponse expectedDto = new MaterialResponse();
        try (MockedStatic<MaterialUtils> utils = mockStatic(MaterialUtils.class)) {
            utils.when(() -> MaterialUtils.toMaterialResponse(existing))
                    .thenReturn(expectedDto);

            // execute
            MaterialResponse actual = materialService.updateMaterial(id, req);

            // verify S3 interactions
            verify(s3Service).uploadFile(newImage, expectedFolder, "");
            verify(s3Service).deleteFile("old-url.png");

            // verify repository save
            ArgumentCaptor<Material> captor = ArgumentCaptor.forClass(Material.class);
            verify(materialRepository).save(captor.capture());
            Material saved = captor.getValue();
            assertEquals("new-url.png", saved.getImage());
            assertEquals("NewName", saved.getName());
            assertEquals("NewCat", saved.getCategory());
            assertEquals("NewSub", saved.getSubCategory());

            // verify mapper and result
            utils.verify(() -> MaterialUtils.toMaterialResponse(existing), times(1));
            assertSame(expectedDto, actual);
        }
    }

    @Test
    @DisplayName("updateMaterial → without image retains old image, updates fields, saves, and returns DTO")
    void updateMaterial_withoutImage_success() {
        UUID id = UUID.randomUUID();

        Material existing = new Material();
        existing.setId(id);
        existing.setName("OldName");
        existing.setCategory("OldCat");
        existing.setSubCategory("OldSub");
        existing.setImage("old-url.png");
        when(materialRepository.findById(id)).thenReturn(Optional.of(existing));

        UpdateMaterialRequest req = new UpdateMaterialRequest();
        req.setImage(null); // no new image
        req.setName("NewName");
        req.setCategory("NewCat");
        req.setSubCategory("NewSub");

        MaterialResponse expectedDto = new MaterialResponse();
        try (MockedStatic<MaterialUtils> utils = mockStatic(MaterialUtils.class)) {
            utils.when(() -> MaterialUtils.toMaterialResponse(existing))
                    .thenReturn(expectedDto);

            MaterialResponse actual = materialService.updateMaterial(id, req);

            // no S3 uploads/deletes
            verify(s3Service, never()).uploadFile(any(), any(), any());
            verify(s3Service, never()).deleteFile(any());

            // save and field updates
            ArgumentCaptor<Material> captor = ArgumentCaptor.forClass(Material.class);
            verify(materialRepository).save(captor.capture());
            Material saved = captor.getValue();
            assertEquals("old-url.png", saved.getImage()); // unchanged
            assertEquals("NewName", saved.getName());
            assertEquals("NewCat", saved.getCategory());
            assertEquals("NewSub", saved.getSubCategory());

            utils.verify(() -> MaterialUtils.toMaterialResponse(existing), times(1));
            assertSame(expectedDto, actual);
        }
    }

    @Test
    @DisplayName("updateMaterial → throws NotFoundException when material not found")
    void updateMaterial_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(materialRepository.findById(id)).thenReturn(Optional.empty());

        UpdateMaterialRequest req = new UpdateMaterialRequest();
        req.setImage(null);
        req.setName("X");
        req.setCategory("Y");
        req.setSubCategory("Z");

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> materialService.updateMaterial(id, req));
        assertEquals("Material not found", ex.getMessage());

        verify(materialRepository).findById(id);
        verifyNoMoreInteractions(s3Service, materialRepository);
    }

    @Test
    @DisplayName("deleteMaterial → deletes image from S3 and record from DB when material exists")
    void deleteMaterial_success() {
        // given
        UUID id = UUID.randomUUID();
        Material existing = new Material();
        existing.setId(id);
        existing.setImage("old-image.png");
        when(materialRepository.findById(id)).thenReturn(Optional.of(existing));

        // when
        materialService.deleteMaterial(id);

        // then: S3 deletion
        verify(s3Service).deleteFile("old-image.png");
        // then: repository delete
        verify(materialRepository).deleteById(id);
    }

    @Test
    @DisplayName("deleteMaterial → throws NotFoundException when material not found")
    void deleteMaterial_notFound() {
        // given
        UUID id = UUID.randomUUID();
        when(materialRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> materialService.deleteMaterial(id));
        assertEquals("Material not found", ex.getMessage());

        // and: no S3 or deleteById calls
        verify(s3Service, never()).deleteFile(anyString());
        verify(materialRepository, never()).deleteById(any());
    }
}
