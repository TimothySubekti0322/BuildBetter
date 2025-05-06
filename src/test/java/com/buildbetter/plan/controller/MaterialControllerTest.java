package com.buildbetter.plan.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.buildbetter.plan.dto.materials.MaterialResponse;
import com.buildbetter.plan.dto.materials.grouped_material.GroupedMaterialResponse;
import com.buildbetter.plan.service.MaterialService;

@WebMvcTest(MaterialController.class)
@AutoConfigureMockMvc
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private MaterialService materialService;

    /* ---------- POST /api/v1/materials ---------------------------------- */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("addMaterial → 200 + success message")
    void addMaterial() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "image", "genting.png", "image/png",
                "🏗️".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/materials")
                .file(file)
                .param("name", "Genting")
                .param("category", "Atap")
                .param("subCategory", "Atap")
                .contentType(MediaType.MULTIPART_FORM_DATA).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Material added successfully"));

        verify(materialService).addMaterial(any());
    }

    /* ---------- GET /api/v1/materials[?grouped] ------------------------- */
    @Nested
    @WithMockUser(roles = "ADMIN")
    class GetAllMaterial {

        @Test
        void ungrouped() throws Exception {

            when(materialService.getAllMaterials())
                    .thenReturn(List.of(new MaterialResponse()));

            mockMvc.perform(get("/api/v1/materials").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());

            verify(materialService).getAllMaterials();
        }

        @Test
        void grouped() throws Exception {

            when(materialService.getAllGroupedMaterials())
                    .thenReturn(List.of(new GroupedMaterialResponse()));

            mockMvc.perform(get("/api/v1/materials").param("grouped", "true").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());

            verify(materialService).getAllGroupedMaterials();
        }
    }

    /* ---------- GET /api/v1/materials/{id} ------------------------------ */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getMaterialById() throws Exception {

        UUID id = UUID.randomUUID();
        when(materialService.getMaterialById(id)).thenReturn(new MaterialResponse());

        mockMvc.perform(get("/api/v1/materials/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());

        verify(materialService).getMaterialById(id);
    }

    /* ---------- PATCH /api/v1/materials/{id} ---------------------------- */
    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMaterial() throws Exception {

        UUID id = UUID.randomUUID();
        when(materialService.updateMaterial(eq(id), any())).thenReturn(new MaterialResponse());

        MockMultipartFile file = new MockMultipartFile(
                "file", "new.png", "image/png",
                "🪨".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/materials/{id}", id)
                .file(file)
                .param("name", "Updated cement")
                .with(req -> {
                    req.setMethod(HttpMethod.PATCH.name());
                    return req;
                }).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Material updated successfully"));

        verify(materialService).updateMaterial(eq(id), any());
    }

    /* ---------- DELETE /api/v1/materials/{id} --------------------------- */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMaterial() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/materials/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Material deleted successfully"));

        verify(materialService).deleteMaterial(id);
    }
}
