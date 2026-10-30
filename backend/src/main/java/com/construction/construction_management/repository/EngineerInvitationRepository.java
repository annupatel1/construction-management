package com.construction.construction_management.repository;

import com.construction.construction_management.entity.EngineerInvitation;
import com.construction.construction_management.entity.InvitationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface EngineerInvitationRepository extends JpaRepository<EngineerInvitation, Integer> {

    boolean existsByProjectIdAndEngineerIdAndStatus(int projectId, int engineerId, InvitationStatus status);

    @EntityGraph(attributePaths = {"project", "inviter", "engineer"})
    Page<EngineerInvitation> findByEngineerId(int engineerId, Pageable pageable);

    @EntityGraph(attributePaths = {"project", "inviter", "engineer"})
    Page<EngineerInvitation> findByInviterId(int inviterId, Pageable pageable);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"project", "inviter", "engineer"})
    Optional<EngineerInvitation> findById(Integer id);
}
