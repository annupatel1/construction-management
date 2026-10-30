package com.construction.construction_management.repository;

import com.construction.construction_management.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findByLabourAssignmentId(int labourAssignmentId);
    boolean existsByLabourAssignmentIdAndDate(int labourAssignmentId, LocalDate date);
}
