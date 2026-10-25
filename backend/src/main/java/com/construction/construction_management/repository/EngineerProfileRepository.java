package com.construction.construction_management.repository;

import com.construction.construction_management.entity.EngineerAvailability;
import com.construction.construction_management.entity.EngineerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EngineerProfileRepository extends JpaRepository<EngineerProfile, Integer> {

    Optional<EngineerProfile> findByUserId(int userId);

    boolean existsByUserId(int userId);

    @EntityGraph(attributePaths = "user")
    @Query("""
            select p from EngineerProfile p
            where (:q is null
                    or lower(p.user.name) like lower(concat('%', :q, '%'))
                    or lower(p.city) like lower(concat('%', :q, '%'))
                    or lower(p.specialty) like lower(concat('%', :q, '%'))
                    or lower(p.skills) like lower(concat('%', :q, '%')))
              and (:city is null or lower(p.city) = lower(:city))
              and (:specialty is null or lower(p.specialty) = lower(:specialty))
              and (:minExperience is null or p.experienceYears >= :minExperience)
              and (:availability is null or p.availability = :availability)
            """)
    Page<EngineerProfile> search(@Param("q") String q,
                                 @Param("city") String city,
                                 @Param("specialty") String specialty,
                                 @Param("minExperience") Integer minExperience,
                                 @Param("availability") EngineerAvailability availability,
                                 Pageable pageable);
}
