package com.buildbetter.plan.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.buildbetter.plan.dto.materials.AddMaterialRequest;
import com.buildbetter.plan.dto.materials.MaterialResponse;
import com.buildbetter.plan.dto.materials.UpdateMaterialRequest;
import com.buildbetter.plan.dto.materials.grouped_material.GroupedMaterialResponse;
import com.buildbetter.plan.service.MaterialService;
import com.buildbetter.shared.dto.ApiResponseMessageAndData;
import com.buildbetter.shared.dto.ApiResponseMessageOnly;
import com.buildbetter.shared.dto.ApiResponseWithData;
import com.buildbetter.shared.security.annotation.IsAdmin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/materials")
@Slf4j
public class MaterialController {
    private final MaterialService materialService;

    @PostMapping(path = "", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @IsAdmin
    public ApiResponseMessageOnly addMaterial(@Valid @ModelAttribute AddMaterialRequest request) {
        log.info("Material Controller : addMaterial");

        materialService.addMaterial(request);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Material added successfully");

        return response;
    }

    @GetMapping("")
    @IsAdmin
    public ApiResponseWithData<?> getAllMaterial(@RequestParam(defaultValue = "false") boolean grouped) {
        log.info("Material Controller : getAllMaterial");

        if (grouped) {
            List<GroupedMaterialResponse> groupedMaterials = materialService.getAllGroupedMaterials();
            return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(), groupedMaterials);
        } else {
            List<MaterialResponse> materials = materialService.getAllMaterials();
            return new ApiResponseWithData<>(HttpStatus.OK.value(), HttpStatus.OK.name(), materials);
        }
    }

    @GetMapping("/{id}")
    @IsAdmin
    public ApiResponseWithData<MaterialResponse> getMaterialById(@PathVariable UUID id) {
        log.info("Material Controller : getMaterialById");

        MaterialResponse material = materialService.getMaterialById(id);

        ApiResponseWithData<MaterialResponse> response = new ApiResponseWithData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(material);

        return response;
    }

    @PatchMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @IsAdmin
    public ApiResponseMessageAndData<MaterialResponse> updateMaterial(
            @PathVariable UUID id,
            @ModelAttribute UpdateMaterialRequest request) {
        log.info("Material Controller : updateMaterial");

        MaterialResponse result = materialService.updateMaterial(id, request);

        ApiResponseMessageAndData<MaterialResponse> response = new ApiResponseMessageAndData<>();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setData(result);
        response.setMessage("Material updated successfully");

        return response;
    }

    @DeleteMapping("/{id}")
    @IsAdmin
    public ApiResponseMessageOnly deleteMaterial(@PathVariable UUID id) {
        log.info("Material Controller : deleteMaterial - " + id);

        materialService.deleteMaterial(id);

        ApiResponseMessageOnly response = new ApiResponseMessageOnly();
        response.setCode(HttpStatus.OK.value());
        response.setStatus(HttpStatus.OK.name());
        response.setMessage("Material deleted successfully");

        return response;
    }

}
