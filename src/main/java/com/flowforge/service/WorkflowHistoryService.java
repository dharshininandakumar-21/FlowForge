package com.flowforge.service;

import com.flowforge.entity.*;
import com.flowforge.repository.WorkflowHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowHistoryService {

    @Autowired
    private WorkflowHistoryRepository workflowHistoryRepository;

    public void log(RequestType type, Long requestId, String performedBy, Role role,
                     RequestStatus oldStatus, RequestStatus newStatus, String comments) {
        WorkflowHistory history = new WorkflowHistory();
        history.setRequestType(type);
        history.setRequestId(requestId);
        history.setPerformedBy(performedBy);
        history.setRole(role);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setComments(comments);
        workflowHistoryRepository.save(history);
    }

    public List<WorkflowHistory> getTimeline(RequestType type, Long requestId) {
        return workflowHistoryRepository.findByRequestTypeAndRequestIdOrderByTimestampAsc(type, requestId);
    }

    public List<WorkflowHistory> getRecentActivity() {
        return workflowHistoryRepository.findTop10ByOrderByTimestampDesc();
    }
}
