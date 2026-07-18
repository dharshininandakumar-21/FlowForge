package com.flowforge.controller;

import com.flowforge.entity.User;
import com.flowforge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return switch (user.getRole()) {
            case EMPLOYEE -> "redirect:/employee/dashboard";
            case MANAGER -> "redirect:/manager/dashboard";
            case ADMIN -> "redirect:/admin/dashboard";
        };
    }
}
