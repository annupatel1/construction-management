package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @EntityGraph(attributePaths = {"project"})
    Page<Notification> findByRecipientIdOrderByCreatedAtDescIdDesc(int recipientId, Pageable pageable);

    long countByRecipientIdAndReadAtIsNull(int recipientId);

    Optional<Notification> findByIdAndRecipientId(int id, int recipientId);

    @Modifying
    @Query("""
            update Notification n
            set n.readAt = :readAt
            where n.recipient.id = :recipientId and n.readAt is null
            """)
    int markAllRead(@Param("recipientId") int recipientId,
                    @Param("readAt") LocalDateTime readAt);
}
