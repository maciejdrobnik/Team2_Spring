package com.example.pdp.repositories;

import com.example.pdp.models.TreeElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TreeElementRepository extends JpaRepository<TreeElement, Long> {
    public Optional<TreeElement> findByFileName(String filename);
}
