package com.buildbetter.consultation.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.buildbetter.consultation.model.Architect;

@Repository
public interface ArchitectRepository extends JpaRepository<Architect, UUID> {
    Optional<Architect> findByEmail(String email);

    List<Architect> findByCity(String city);

    List<Architect> findAllByIdNotIn(List<UUID> ids);
}
