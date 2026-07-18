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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

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
        List<LeaveRequest> leaveRequests = requestService.getEmployeeLeaveRequests(user.getId());
        List<PurchaseRequest> purchaseRequests = requestService.getEmployeePurchaseRequests(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("leaveRequests", leaveRequests);
        model.addAttribute("purchaseRequests", purchaseRequests);
        return "employee/dashboard";
    }

    @GetMapping("/leave/new")
    public String newLeaveForm(Model model) {
        model.addAttribute("leaveRequest", new LeaveRequest());
        return "employee/new-leave";
    }

    @PostMapping("/leave/new")
    public String submitLeave(Authentication authentication,
                               @RequestParam String leaveType,
                               @RequestParam String reason,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               @RequestParam(required = false) MultipartFile attachment) {
        User user = currentUser(authentication);
        LeaveRequest request = new LeaveRequest();
        request.setEmployee(user);
        request.setLeaveType(leaveType);
        request.setReason(reason);
        request.setStartDate(java.time.LocalDate.parse(startDate));
        request.setEndDate(java.time.LocalDate.parse(endDate));
        if (attachment != null && !attachment.isEmpty()) {
            request.setAttachmentName(attachment.getOriginalFilename());
        }
        requestService.submitLeaveRequest(request);
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/purchase/new")
    public String newPurchaseForm(Model model) {
        model.addAttribute("purchaseRequest", new PurchaseRequest());
        return "employee/new-purchase";
    }

    @PostMapping("/purchase/new")
    public String submitPurchase(Authentication authentication,
                                  @RequestParam String itemName,
                                  @RequestParam Integer quantity,
                                  @RequestParam java.math.BigDecimal estimatedCost,
                                  @RequestParam String justification,
                                  @RequestParam(required = false) MultipartFile attachment) {
        User user = currentUser(authentication);
        PurchaseRequest request = new PurchaseRequest();
        request.setEmployee(user);
        request.setItemName(itemName);
        request.setQuantity(quantity);
        request.setEstimatedCost(estimatedCost);
        request.setJustification(justification);
        if (attachment != null && !attachment.isEmpty()) {
            request.setAttachmentName(attachment.getOriginalFilename());
        }
        requestService.submitPurchaseRequest(request);
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/leave/{id}")
    public String viewLeave(@PathVariable Long id, Model model) {
        LeaveRequest request = requestService.getLeaveRequest(id);
        model.addAttribute("request", request);
        model.addAttribute("timeline", workflowHistoryService.getTimeline(RequestType.LEAVE, id));
        return "employee/request-detail";
    }

    @GetMapping("/purchase/{id}")
    public String viewPurchase(@PathVariable Long id, Model model) {
        PurchaseRequest request = requestService.getPurchaseRequest(id);
        model.addAttribute("request", request);
        model.addAttribute("timeline", workflowHistoryService.getTimeline(RequestType.PURCHASE, id));
        return "employee/purchase-detail";
    }

    @PostMapping("/leave/{id}/cancel")
    public String cancelLeave(@PathVariable Long id, Authentication authentication) {
        requestService.cancelLeave(id, currentUser(authentication));
        return "redirect:/employee/dashboard";
    }

    @PostMapping("/purchase/{id}/cancel")
    public String cancelPurchase(@PathVariable Long id, Authentication authentication) {
        requestService.cancelPurchase(id, currentUser(authentication));
        return "redirect:/employee/dashboard";
    }
}
