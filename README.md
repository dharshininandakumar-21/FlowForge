# FlowForge – Enterprise Workflow Management System

FlowForge is a Spring Boot-based enterprise workflow management system that automates organizational approval processes through role-based workflows, secure authentication, workflow history tracking, and analytics dashboards.

The application enables employees to submit requests, managers to review and approve them, and administrators to monitor workflows while ensuring transparency through audit logs and automated escalation.

---

## Features

- Role-Based Authentication & Authorization
- Employee, Manager, and Admin Dashboards
- Leave Request Management
- Purchase Request Management
- Dynamic Workflow Routing
- Multi-Level Approval Process
- Workflow History & Audit Trail
- Automatic Request Escalation
- Analytics Dashboard
- Department Management
- User Management
- Secure Password Encryption using Spring Security
- Responsive UI with Thymeleaf & Bootstrap

---

## Technology Stack

- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- Thymeleaf
- Bootstrap 5
- Maven
- H2 Database
- MySQL (Optional)

---

## Project Architecture

```
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

The application follows a layered architecture with proper separation of concerns using Spring Boot best practices.

---

## Project Structure

```
src
 ├── main
 │   ├── java
 │   │   ├── config
 │   │   ├── controller
 │   │   ├── entity
 │   │   ├── repository
 │   │   ├── scheduler
 │   │   └── service
 │   └── resources
 │       ├── static
 │       ├── templates
 │       └── application.properties
```

---

## Workflow

1. Employee submits a Leave or Purchase Request.
2. The request is assigned to the respective Manager.
3. Manager reviews and approves or rejects the request.
4. If the request is not processed within the configured time, it is automatically escalated.
5. Every workflow action is recorded in the workflow history.
6. Administrators can monitor requests and analytics through the dashboard.

---

## Security

- Spring Security Authentication
- Role-Based Authorization
- BCrypt Password Encryption
- Session Management

---

## Key Modules

- Authentication & Authorization
- Employee Management
- Department Management
- Leave Management
- Purchase Request Management
- Workflow Engine
- Workflow History
- Analytics Dashboard
- Automatic Escalation Scheduler

---

## Build & Run

Clone the repository:

```bash
git clone <repository-url>
```

Navigate to the project:

```bash
cd FlowForge
```

Run the application:

```bash
mvn spring-boot:run
```

Or build the project:

```bash
mvn clean package
```

---

## Future Enhancements

- Email Notifications
- REST API Integration
- File Storage Service
- Dashboard Reports
- Multi-Level Approval Configuration
- Docker Deployment
- Cloud Deployment

---

## Author

**Dharshini N**

MCA Student | Java Full Stack Developer | Spring Boot Enthusiast
