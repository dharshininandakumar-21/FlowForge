package com.flowforge.controller;

import com.flowforge.entity.*;
import com.flowforge.repository.UserRepository;
import com.flowforge.service.RequestService;
import com.flowforge.service.WorkflowHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestService requestService;

    @Autowired
    private WorkflowHistoryService workflowHistoryService;

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = currentUser(authentication);
        List<LeaveRequest> leaveRequests = requestService.getManagerLeaveRequests(user.getId());
        List<PurchaseRequest> purchaseRequests = requestService.getManagerPurchaseRequests(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("leaveRequests", leaveRequests);
        model.addAttribute("purchaseRequests", purchaseRequests);
        return "manager/dashboard";
    }

    @GetMapping("/leave/{id}")
    public String viewLeave(@PathVariable Long id, Authentication authentication, Model model) {
        requestService.markLeaveViewed(id, currentUser(authentication));
        LeaveRequest request = requestService.getLeaveRequest(id);
        model.addAttribute("request", request);
        model.addAttribute("timeline", workflowHistoryService.getTimeline(RequestType.LEAVE, id));
        return "manager/leave-detail";
    }

    @GetMapping("/purchase/{id}")
    public String viewPurchase(@PathVariable Long id, Authentication authentication, Model model) {
        requestService.markPurchaseViewed(id, currentUser(authentication));
        PurchaseRequest request = requestService.getPurchaseRequest(id);
        model.addAttribute("request", request);
        model.addAttribute("timeline", workflowHistoryService.getTimeline(RequestType.PURCHASE, id));
        return "manager/purchase-detail";
    }

    @PostMapping("/leave/{id}/decide")
    public String decideLeave(@PathVariable Long id, Authentication authentication,
                               @RequestParam String decision, @RequestParam(required = false) String comments) {
        requestService.decideLeave(id, currentUser(authentication), "approve".equals(decision), comments);
        return "redirect:/manager/dashboard";
    }

    @PostMapping("/purchase/{id}/decide")
    public String decidePurchase(@PathVariable Long id, Authentication authentication,
                                  @RequestParam String decision, @RequestParam(required = false) String comments) {
        requestService.decidePurchase(id, currentUser(authentication), "approve".equals(decision), comments);
        return "redirect:/manager/dashboard";
    }
}
