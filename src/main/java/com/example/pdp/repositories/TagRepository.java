package com.example.pdp.repositories;

import com.example.pdp.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository  extends JpaRepository<Tag, Long> {
    public Tag findByName(String value);
}
