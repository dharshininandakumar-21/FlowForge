package com.flowforge.repository;

import com.flowforge.entity.RequestType;
import com.flowforge.entity.WorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, Long> {
    List<WorkflowHistory> findByRequestTypeAndRequestIdOrderByTimestampAsc(RequestType requestType, Long requestId);
    List<WorkflowHistory> findTop10ByOrderByTimestampDesc();
}
