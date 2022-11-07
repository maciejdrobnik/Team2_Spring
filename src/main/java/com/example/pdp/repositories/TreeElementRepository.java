package com.example.pdp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.pdp.models.TreeElement;

@Repository
public interface TreeElementRepository extends JpaRepository<TreeElement, Long> {
    TreeElement findById(long id);
}
