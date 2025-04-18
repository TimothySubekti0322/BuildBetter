package com.buildbetter.plan.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildbetter.plan.model.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {

    List<Material> findByCategory(String category);

    List<Material> findBySubCategory(String subCategory);

    List<Material> findByCategoryAndSubCategory(String category, String subCategory);
}