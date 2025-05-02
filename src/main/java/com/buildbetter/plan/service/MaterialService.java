package com.buildbetter.plan.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {

        private final MaterialRepository materialRepository;
        private final S3Service s3Service;

        public void addMaterial(AddMaterialRequest request) {
                log.info("Material Service : addMaterial");

                // Construct the folder path based on the category
                String folder = S3Folder.MATERIALS + request.getCategory() + "/" + request.getSubCategory() + "/";

                // Upload the image to S3 and get the URL
                log.info("Material Service : addMaterial - Upload File to S3");
                String imageUrl = s3Service.uploadFile(request.getImage(), folder);

                Material material = Material.builder()
                                .name(request.getName())
                                .category(request.getCategory())
                                .subCategory(request.getSubCategory())
                                .image(imageUrl).build();

                log.info("Material Service : addMaterial - Save material to DB");
                materialRepository.save(material);
        }

        public List<MaterialResponse> getAllMaterials() {
                log.info("Material Service : getAllMaterials");

                return materialRepository.findAll()
                                .stream()
                                .map(MaterialUtils::toMaterialResponse)
                                .collect(Collectors.toList());
        }

        public List<GroupedMaterialResponse> getAllGroupedMaterials() {
                log.info("Material Service : getAllGroupedMaterials");

                List<Material> materials = materialRepository.findAll(
                                Sort.by("category").ascending()
                                                .and(Sort.by("subCategory").ascending())
                                                .and(Sort.by("name").ascending()));

                // 2. Group category → subCategory → List<entity>
                log.info("Material Service : getAllGroupedMaterials - Grouping materials");
                Map<String, Map<String, List<Material>>> grouped = materials.stream()
                                .collect(Collectors.groupingBy(
                                                Material::getCategory,
                                                LinkedHashMap::new,
                                                Collectors.groupingBy(
                                                                Material::getSubCategory,
                                                                LinkedHashMap::new,
                                                                Collectors.toList())));

                // 3. Convert to DTOs
                log.info("Material Service : getAllGroupedMaterials - Converting to DTOs");
                return grouped.entrySet().stream()
                                .map(catEntry -> {
                                        SubCategory[] subCategories = catEntry.getValue()
                                                        .entrySet().stream()
                                                        .map(subEntry -> {
                                                                MaterialItem[] mats = subEntry.getValue().stream()
                                                                                .map(m -> new MaterialItem(
                                                                                                m.getId(),
                                                                                                m.getName(),
                                                                                                m.getCategory(),
                                                                                                m.getSubCategory(),
                                                                                                m.getImage()))
                                                                                .toArray(MaterialItem[]::new);

                                                                return new SubCategory(subEntry.getKey(), mats);
                                                        })
                                                        .toArray(SubCategory[]::new);

                                        return GroupedMaterialResponse.builder()
                                                        .category(catEntry.getKey())
                                                        .subCategories(subCategories)
                                                        .build();
                                })
                                .toList();
        }

        public MaterialResponse getMaterialById(UUID id) {
                log.info("Service : Get material by id");

                Material material = materialRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Material not found"));

                return MaterialUtils.toMaterialResponse(material);
        }

        @Transactional
        public MaterialResponse updateMaterial(UUID id, UpdateMaterialRequest request) {
                log.info("Material Service : updateMaterial");

                // Check if the material exists
                Material existingMaterial = materialRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Material not found"));

                // If the image is not null, upload it to S3 and get the URL
                if (request.getImage() != null) {

                        log.info("Material Service : updateMaterial - Upload File to S3");
                        String folder = S3Folder.MATERIALS + request.getCategory() + "/";
                        String imageUrl = s3Service.uploadFile(request.getImage(), folder);

                        // Delete the old image from S3
                        log.info("Material Service : updateMaterial - Delete old image from S3");
                        s3Service.deleteFile(existingMaterial.getImage());

                        existingMaterial.setImage(imageUrl);
                } else {
                        existingMaterial.setImage(existingMaterial.getImage());
                }

                // Update the other fields
                existingMaterial.setName(request.getName() != null ? request.getName() : existingMaterial.getName());
                existingMaterial
                                .setCategory(request.getCategory() != null ? request.getCategory()
                                                : existingMaterial.getCategory());
                existingMaterial.setSubCategory(
                                request.getSubCategory() != null ? request.getSubCategory()
                                                : existingMaterial.getSubCategory());

                // Save the updated material
                log.info("Material Service : updateMaterial - Save updated material to DB");
                materialRepository.save(existingMaterial);
                return MaterialUtils.toMaterialResponse(existingMaterial);
        }

        public void deleteMaterial(UUID id) {
                log.info("Material Service : deleteMaterial");
                Material existingMaterial = materialRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Material not found"));

                // Delete the old image from S3
                log.info("Material Service : deleteMaterial - Delete old image from S3");
                s3Service.deleteFile(existingMaterial.getImage());

                // Delete the material from the database
                log.info("Material Service : deleteMaterial - Delete material from DB");
                materialRepository.deleteById(id);
        }
}
