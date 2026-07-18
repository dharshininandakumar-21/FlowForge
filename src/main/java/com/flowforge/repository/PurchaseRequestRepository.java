package com.flowforge.repository;

import com.flowforge.entity.PurchaseRequest;
import com.flowforge.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    List<PurchaseRequest> findByEmployeeId(Long employeeId);
    List<PurchaseRequest> findByManagerId(Long managerId);
    List<PurchaseRequest> findByStatus(RequestStatus status);
    List<PurchaseRequest> findByStatusAndLastAssignedAtBefore(RequestStatus status, LocalDateTime cutoff);
}
