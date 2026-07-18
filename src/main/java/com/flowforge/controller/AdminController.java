package com.flowforge.controller;

import com.flowforge.entity.*;
import com.flowforge.repository.DepartmentRepository;
import com.flowforge.repository.UserRepository;
import com.flowforge.service.AnalyticsService;
import com.flowforge.service.RequestService;
import com.flowforge.service.WorkflowHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RequestService requestService;

    @Autowired
    private WorkflowHistoryService workflowHistoryService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow();
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", analyticsService.buildDashboard());
        model.addAttribute("recentActivity", workflowHistoryService.getRecentActivity());
        List<LeaveRequest> escalatedLeave = requestService.getAllLeaveRequests().stream()
                .filter(r -> r.getStatus() == RequestStatus.ESCALATED).toList();
        List<PurchaseRequest> escalatedPurchase = requestService.getAllPurchaseRequests().stream()
                .filter(r -> r.getStatus() == RequestStatus.ESCALATED).toList();
        model.addAttribute("escalatedLeave", escalatedLeave);
        model.addAttribute("escalatedPurchase", escalatedPurchase);
        return "admin/dashboard";
    }

    @GetMapping("/requests")
    public String requests(@RequestParam(required = false) String status,
                            @RequestParam(required = false) String type,
                            Model model) {
        List<LeaveRequest> leaveRequests = requestService.getAllLeaveRequests();
        List<PurchaseRequest> purchaseRequests = requestService.getAllPurchaseRequests();

        if (status != null && !status.isEmpty()) {
            RequestStatus filterStatus = RequestStatus.valueOf(status);
            leaveRequests = leaveRequests.stream().filter(r -> r.getStatus() == filterStatus).toList();
            purchaseRequests = purchaseRequests.stream().filter(r -> r.getStatus() == filterStatus).toList();
        }

        if ("LEAVE".equals(type)) {
            purchaseRequests = List.of();
        } else if ("PURCHASE".equals(type)) {
            leaveRequests = List.of();
        }

        model.addAttribute("leaveRequests", leaveRequests);
        model.addAttribute("purchaseRequests", purchaseRequests);
        model.addAttribute("statuses", RequestStatus.values());
        return "admin/requests";
    }

    @GetMapping("/leave/{id}")
    public String viewLeave(@PathVariable Long id, Model model) {
        LeaveRequest request = requestService.getLeaveRequest(id);
        model.addAttribute("request", request);
        model.addAttribute("timeline", workflowHistoryService.getTimeline(RequestType.LEAVE, id));
        return "admin/leave-detail";
    }

    @GetMapping("/purchase/{id}")
    public String viewPurchase(@PathVariable Long id, Model model) {
        PurchaseRequest request = requestService.getPurchaseRequest(id);
        model.addAttribute("request", request);
        model.addAttribute("timeline", workflowHistoryService.getTimeline(RequestType.PURCHASE, id));
        return "admin/purchase-detail";
    }

    @PostMapping("/leave/{id}/decide")
    public String decideLeave(@PathVariable Long id, Authentication authentication,
                               @RequestParam String decision, @RequestParam(required = false) String comments) {
        requestService.decideLeave(id, currentUser(authentication), "approve".equals(decision), comments);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/purchase/{id}/decide")
    public String decidePurchase(@PathVariable Long id, Authentication authentication,
                                  @RequestParam String decision, @RequestParam(required = false) String comments) {
        requestService.decidePurchase(id, currentUser(authentication), "approve".equals(decision), comments);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("managers", userRepository.findByRole(Role.MANAGER));
        return "admin/users";
    }

    @PostMapping("/users/new")
    public String createUser(@RequestParam String name, @RequestParam String email,
                              @RequestParam String password, @RequestParam Role role,
                              @RequestParam(required = false) Long departmentId,
                              @RequestParam(required = false) Long managerId) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        if (departmentId != null) {
            departmentRepository.findById(departmentId).ifPresent(user::setDepartment);
        }
        if (managerId != null) {
            userRepository.findById(managerId).ifPresent(user::setManager);
        }
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/assign-manager")
    public String assignManager(@PathVariable Long id, @RequestParam Long managerId) {
        User user = userRepository.findById(id).orElseThrow();
        User manager = userRepository.findById(managerId).orElseThrow();
        user.setManager(manager);
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @GetMapping("/departments")
    public String departments(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("managers", userRepository.findByRole(Role.MANAGER));
        return "admin/departments";
    }

    @PostMapping("/departments/new")
    public String createDepartment(@RequestParam String name,
                                    @RequestParam(required = false) Long managerId) {
        Department department = new Department(name);
        if (managerId != null) {
            userRepository.findById(managerId).ifPresent(department::setManager);
        }
        departmentRepository.save(department);
        return "redirect:/admin/departments";
    }

    @PostMapping("/departments/{id}/assign-manager")
    public String assignDepartmentManager(@PathVariable Long id,
                                           @RequestParam(required = false) Long managerId) {
        Department department = departmentRepository.findById(id).orElseThrow();
        if (managerId == null) {
            department.setManager(null);
        } else {
            User manager = userRepository.findById(managerId).orElseThrow();
            department.setManager(manager);
        }
        departmentRepository.save(department);
        return "redirect:/admin/departments";
    }
}
