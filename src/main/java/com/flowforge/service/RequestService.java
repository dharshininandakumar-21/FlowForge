package com.flowforge.service;

import com.flowforge.entity.*;
import com.flowforge.repository.LeaveRequestRepository;
import com.flowforge.repository.PurchaseRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;

    @Autowired
    private WorkflowHistoryService workflowHistoryService;

    /**
     * Department-Based Dynamic Routing.
     * Resolves the correct approver for a request purely from business rules:
     * 1. Look up the employee's department.
     * 2. Look up the manager assigned to that department.
     * 3. If no department manager is configured, fall back to the employee's
     *    directly-assigned manager (kept for backward compatibility with
     *    existing data / manual overrides).
     * The employee never selects or knows the manager - this happens
     * automatically in the background.
     */
    private User resolveApprover(User employee) {
        if (employee.getDepartment() != null && employee.getDepartment().getManager() != null) {
            return employee.getDepartment().getManager();
        }
        return employee.getManager();
    }

    private String routingExplanation(User employee, User resolvedManager) {
        if (employee.getDepartment() != null && employee.getDepartment().getManager() != null
                && employee.getDepartment().getManager().getId().equals(resolvedManager.getId())) {
            return "Auto-routed to " + resolvedManager.getName() + " (Manager of "
                    + employee.getDepartment().getName() + " department)";
        }
        return "Assigned to manager " + resolvedManager.getName();
    }

    public LeaveRequest submitLeaveRequest(LeaveRequest request) {
        User approver = resolveApprover(request.getEmployee());
        request.setManager(approver);
        request.setStatus(RequestStatus.SUBMITTED);
        request.setSubmittedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        request.setLastAssignedAt(LocalDateTime.now());
        LeaveRequest saved = leaveRequestRepository.save(request);
        workflowHistoryService.log(RequestType.LEAVE, saved.getId(), saved.getEmployee().getName(),
                Role.EMPLOYEE, null, RequestStatus.SUBMITTED, "Leave request submitted");
        if (saved.getManager() != null) {
            workflowHistoryService.log(RequestType.LEAVE, saved.getId(), "System",
                    Role.EMPLOYEE, RequestStatus.SUBMITTED, RequestStatus.SUBMITTED,
                    routingExplanation(saved.getEmployee(), saved.getManager()));
        }
        return saved;
    }

    public PurchaseRequest submitPurchaseRequest(PurchaseRequest request) {
        User approver = resolveApprover(request.getEmployee());
        request.setManager(approver);
        request.setStatus(RequestStatus.SUBMITTED);
        request.setSubmittedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        request.setLastAssignedAt(LocalDateTime.now());
        PurchaseRequest saved = purchaseRequestRepository.save(request);
        workflowHistoryService.log(RequestType.PURCHASE, saved.getId(), saved.getEmployee().getName(),
                Role.EMPLOYEE, null, RequestStatus.SUBMITTED, "Purchase request submitted");
        if (saved.getManager() != null) {
            workflowHistoryService.log(RequestType.PURCHASE, saved.getId(), "System",
                    Role.EMPLOYEE, RequestStatus.SUBMITTED, RequestStatus.SUBMITTED,
                    routingExplanation(saved.getEmployee(), saved.getManager()));
        }
        return saved;
    }

    public void markLeaveViewed(Long id, User viewer) {
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow();
        if (request.getStatus() == RequestStatus.SUBMITTED) {
            request.setStatus(RequestStatus.UNDER_REVIEW);
            request.setUpdatedAt(LocalDateTime.now());
            leaveRequestRepository.save(request);
            workflowHistoryService.log(RequestType.LEAVE, id, viewer.getName(), viewer.getRole(),
                    RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW, "Viewed by " + viewer.getName());
        }
    }

    public void markPurchaseViewed(Long id, User viewer) {
        PurchaseRequest request = purchaseRequestRepository.findById(id).orElseThrow();
        if (request.getStatus() == RequestStatus.SUBMITTED) {
            request.setStatus(RequestStatus.UNDER_REVIEW);
            request.setUpdatedAt(LocalDateTime.now());
            purchaseRequestRepository.save(request);
            workflowHistoryService.log(RequestType.PURCHASE, id, viewer.getName(), viewer.getRole(),
                    RequestStatus.SUBMITTED, RequestStatus.UNDER_REVIEW, "Viewed by " + viewer.getName());
        }
    }

    private String buildDecisionMessage(boolean approve, User decidedBy, String comments) {
        String verb = approve ? "Approved" : "Rejected";
        String base = verb + " by " + decidedBy.getName();
        if (comments != null && !comments.isBlank()) {
            base = base + " - " + comments.trim();
        }
        return base;
    }

    public void decideLeave(Long id, User decidedBy, boolean approve, String comments) {
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow();
        RequestStatus oldStatus = request.getStatus();
        RequestStatus newStatus = approve ? RequestStatus.APPROVED : RequestStatus.REJECTED;
        request.setStatus(newStatus);
        request.setUpdatedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
        workflowHistoryService.log(RequestType.LEAVE, id, decidedBy.getName(), decidedBy.getRole(),
                oldStatus, newStatus, buildDecisionMessage(approve, decidedBy, comments));
        workflowHistoryService.log(RequestType.LEAVE, id, "System", decidedBy.getRole(),
                newStatus, RequestStatus.COMPLETED, "Workflow completed");
        request.setStatus(RequestStatus.COMPLETED);
        leaveRequestRepository.save(request);
    }

    public void decidePurchase(Long id, User decidedBy, boolean approve, String comments) {
        PurchaseRequest request = purchaseRequestRepository.findById(id).orElseThrow();
        RequestStatus oldStatus = request.getStatus();
        RequestStatus newStatus = approve ? RequestStatus.APPROVED : RequestStatus.REJECTED;
        request.setStatus(newStatus);
        request.setUpdatedAt(LocalDateTime.now());
        purchaseRequestRepository.save(request);
        workflowHistoryService.log(RequestType.PURCHASE, id, decidedBy.getName(), decidedBy.getRole(),
                oldStatus, newStatus, buildDecisionMessage(approve, decidedBy, comments));
        workflowHistoryService.log(RequestType.PURCHASE, id, "System", decidedBy.getRole(),
                newStatus, RequestStatus.COMPLETED, "Workflow completed");
        request.setStatus(RequestStatus.COMPLETED);
        purchaseRequestRepository.save(request);
    }

    public void cancelLeave(Long id, User employee) {
        LeaveRequest request = leaveRequestRepository.findById(id).orElseThrow();
        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalStateException("Not authorized to cancel this request");
        }
        RequestStatus oldStatus = request.getStatus();
        request.setStatus(RequestStatus.REJECTED);
        leaveRequestRepository.save(request);
        workflowHistoryService.log(RequestType.LEAVE, id, employee.getName(), Role.EMPLOYEE,
                oldStatus, RequestStatus.REJECTED, "Cancelled by employee");
    }

    public void cancelPurchase(Long id, User employee) {
        PurchaseRequest request = purchaseRequestRepository.findById(id).orElseThrow();
        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalStateException("Not authorized to cancel this request");
        }
        RequestStatus oldStatus = request.getStatus();
        request.setStatus(RequestStatus.REJECTED);
        purchaseRequestRepository.save(request);
        workflowHistoryService.log(RequestType.PURCHASE, id, employee.getName(), Role.EMPLOYEE,
                oldStatus, RequestStatus.REJECTED, "Cancelled by employee");
    }

    public List<LeaveRequest> getEmployeeLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeId(employeeId);
    }

    public List<PurchaseRequest> getEmployeePurchaseRequests(Long employeeId) {
        return purchaseRequestRepository.findByEmployeeId(employeeId);
    }

    public List<LeaveRequest> getManagerLeaveRequests(Long managerId) {
        return leaveRequestRepository.findByManagerId(managerId);
    }

    public List<PurchaseRequest> getManagerPurchaseRequests(Long managerId) {
        return purchaseRequestRepository.findByManagerId(managerId);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }

    public List<PurchaseRequest> getAllPurchaseRequests() {
        return purchaseRequestRepository.findAll();
    }

    public LeaveRequest getLeaveRequest(Long id) {
        return leaveRequestRepository.findById(id).orElseThrow();
    }

    public PurchaseRequest getPurchaseRequest(Long id) {
        return purchaseRequestRepository.findById(id).orElseThrow();
    }

    public List<LeaveRequest> findEscalatableLeaveRequests(LocalDateTime cutoff) {
        return leaveRequestRepository.findByStatusAndLastAssignedAtBefore(RequestStatus.SUBMITTED, cutoff);
    }

    public List<PurchaseRequest> findEscalatablePurchaseRequests(LocalDateTime cutoff) {
        return purchaseRequestRepository.findByStatusAndLastAssignedAtBefore(RequestStatus.SUBMITTED, cutoff);
    }

    public void escalateLeave(LeaveRequest request) {
        RequestStatus oldStatus = request.getStatus();
        request.setStatus(RequestStatus.ESCALATED);
        request.setUpdatedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
        workflowHistoryService.log(RequestType.LEAVE, request.getId(), "System", Role.ADMIN,
                oldStatus, RequestStatus.ESCALATED, "Manager did not respond in time, escalated to Admin");
    }

    public void escalatePurchase(PurchaseRequest request) {
        RequestStatus oldStatus = request.getStatus();
        request.setStatus(RequestStatus.ESCALATED);
        request.setUpdatedAt(LocalDateTime.now());
        purchaseRequestRepository.save(request);
        workflowHistoryService.log(RequestType.PURCHASE, request.getId(), "System", Role.ADMIN,
                oldStatus, RequestStatus.ESCALATED, "Manager did not respond in time, escalated to Admin");
    }
}
