package com.flowforge.config;

import com.flowforge.entity.Department;
import com.flowforge.entity.Role;
import com.flowforge.entity.User;
import com.flowforge.repository.DepartmentRepository;
import com.flowforge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Department engineering = departmentRepository.save(new Department("Engineering"));
        Department sales = departmentRepository.save(new Department("Sales"));
        Department hr = departmentRepository.save(new Department("Human Resources"));

        User admin = new User();
        admin.setName("Ashok Kumar");
        admin.setEmail("admin@flowforge.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setDepartment(engineering);
        userRepository.save(admin);

        User manager = new User();
        manager.setName("Priya Menon");
        manager.setEmail("manager@flowforge.com");
        manager.setPassword(passwordEncoder.encode("manager123"));
        manager.setRole(Role.MANAGER);
        manager.setDepartment(engineering);
        userRepository.save(manager);

        User salesManager = new User();
        salesManager.setName("Karthik Raja");
        salesManager.setEmail("salesmanager@flowforge.com");
        salesManager.setPassword(passwordEncoder.encode("manager123"));
        salesManager.setRole(Role.MANAGER);
        salesManager.setDepartment(sales);
        userRepository.save(salesManager);

        // Department-Based Dynamic Routing: each department is linked to the
        // manager who should automatically receive that department's requests.
        engineering.setManager(manager);
        departmentRepository.save(engineering);
        sales.setManager(salesManager);
        departmentRepository.save(sales);

        User employee = new User();
        employee.setName("Dharshini N");
        employee.setEmail("employee@flowforge.com");
        employee.setPassword(passwordEncoder.encode("employee123"));
        employee.setRole(Role.EMPLOYEE);
        employee.setDepartment(engineering);
        employee.setManager(manager);
        userRepository.save(employee);

        User employee2 = new User();
        employee2.setName("Rahul Sharma");
        employee2.setEmail("rahul@flowforge.com");
        employee2.setPassword(passwordEncoder.encode("employee123"));
        employee2.setRole(Role.EMPLOYEE);
        employee2.setDepartment(sales);
        employee2.setManager(salesManager);
        userRepository.save(employee2);
    }
}
