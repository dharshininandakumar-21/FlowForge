package com.flowforge.repository;

import com.flowforge.entity.LeaveRequest;
import com.flowforge.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByManagerId(Long managerId);
    List<LeaveRequest> findByStatus(RequestStatus status);
    List<LeaveRequest> findByStatusAndLastAssignedAtBefore(RequestStatus status, LocalDateTime cutoff);
}
