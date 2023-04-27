package com.evcharginstation.backend.persistency.jpa.repository;

import com.evcharginstation.backend.persistency.jpa.entity.EVStation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EVStationRepository extends JpaRepository<EVStation, Long> {
    List<EVStation> findAllByOrderByName(Pageable pageable);
}
