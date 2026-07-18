package com.flowforge.service;

import com.flowforge.entity.RequestStatus;
import com.flowforge.repository.LeaveRequestRepository;
import com.flowforge.repository.PurchaseRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;

    public Map<String, Object> buildDashboard() {
        Map<String, Object> data = new HashMap<>();

        long totalLeave = leaveRequestRepository.count();
        long totalPurchase = purchaseRequestRepository.count();
        long total = totalLeave + totalPurchase;

        List<RequestStatus> allStatuses = List.of(RequestStatus.values());
        Map<RequestStatus, Long> statusCounts = new HashMap<>();
        for (RequestStatus status : allStatuses) {
            long count = leaveRequestRepository.findByStatus(status).size()
                    + purchaseRequestRepository.findByStatus(status).size();
            statusCounts.put(status, count);
        }

        long pending = statusCounts.getOrDefault(RequestStatus.SUBMITTED, 0L)
                + statusCounts.getOrDefault(RequestStatus.UNDER_REVIEW, 0L);
        long approved = statusCounts.getOrDefault(RequestStatus.APPROVED, 0L)
                + statusCounts.getOrDefault(RequestStatus.COMPLETED, 0L);
        long rejected = statusCounts.getOrDefault(RequestStatus.REJECTED, 0L);
        long escalated = statusCounts.getOrDefault(RequestStatus.ESCALATED, 0L);

        LocalDate today = LocalDate.now();
        long todayCount = leaveRequestRepository.findAll().stream()
                .filter(r -> r.getSubmittedAt().toLocalDate().isEqual(today)).count()
                + purchaseRequestRepository.findAll().stream()
                .filter(r -> r.getSubmittedAt().toLocalDate().isEqual(today)).count();

        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        long monthlyCount = leaveRequestRepository.findAll().stream()
                .filter(r -> r.getSubmittedAt().isAfter(monthStart)).count()
                + purchaseRequestRepository.findAll().stream()
                .filter(r -> r.getSubmittedAt().isAfter(monthStart)).count();

        data.put("total", total);
        data.put("pending", pending);
        data.put("approved", approved);
        data.put("rejected", rejected);
        data.put("escalated", escalated);
        data.put("today", todayCount);
        data.put("monthly", monthlyCount);
        data.put("leaveCount", totalLeave);
        data.put("purchaseCount", totalPurchase);

        double approvalRate = total == 0 ? 0 : (approved * 100.0) / total;
        double escalationRate = total == 0 ? 0 : (escalated * 100.0) / total;
        data.put("approvalRate", Math.round(approvalRate * 10.0) / 10.0);
        data.put("escalationRate", Math.round(escalationRate * 10.0) / 10.0);

        return data;
    }
}
