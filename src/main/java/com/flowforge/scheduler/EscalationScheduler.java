package com.flowforge.scheduler;

import com.flowforge.entity.LeaveRequest;
import com.flowforge.entity.PurchaseRequest;
import com.flowforge.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EscalationScheduler {

    @Autowired
    private RequestService requestService;

    @Value("${escalation.timeout.minutes:2}")
    private long timeoutMinutes;

    @Scheduled(fixedDelay = 30000)
    public void checkForEscalations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);

        for (LeaveRequest request : requestService.findEscalatableLeaveRequests(cutoff)) {
            requestService.escalateLeave(request);
        }

        for (PurchaseRequest request : requestService.findEscalatablePurchaseRequests(cutoff)) {
            requestService.escalatePurchase(request);
        }
    }
}
