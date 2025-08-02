

package com.example.demo.repository;

import com.example.demo.model.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface IncidentTypeRepository extends JpaRepository<IncidentType, Long> {


    Optional<IncidentType> findByTitle(String title);
    Optional<IncidentType> findByCode(String code);

}
