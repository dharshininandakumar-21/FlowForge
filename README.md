# FlowForge - Enterprise Request Approval & Workflow Management System

A Spring Boot web application that automates Leave Request and Purchase Request
approvals with role based access, auto escalation, audit logging and an
analytics dashboard.

## Tech Stack
Java 17+, Spring Boot 3.3, Spring Security, Spring Data JPA, Hibernate,
Thymeleaf, Bootstrap 5, Maven.

The project runs out of the box on an embedded H2 file database, so no
database installation is required to try it out. A MySQL profile is also
included for production style deployment.

## How To Run

### Option 1: Run with the bundled H2 database (fastest, no setup)

```
mvn spring-boot:run
```

Open http://localhost:8080 in your browser. Data is stored in a local file
under `./data/flowforgedb.mv.db` so it persists between restarts.

### Option 2: Run with MySQL

1. Make sure MySQL is running locally and update the credentials in
   `src/main/resources/application-mysql.properties` if needed.
2. Run:

```
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

The schema (`flowforge_db`) is created automatically on first run.

### Building a runnable jar

```
mvn clean package
java -jar target/flowforge.jar
```

## Demo Accounts

| Role     | Email                       | Password    |
|----------|------------------------------|-------------|
| Admin    | admin@flowforge.com          | admin123    |
| Manager  | manager@flowforge.com        | manager123  |
| Manager  | salesmanager@flowforge.com   | manager123  |
| Employee | employee@flowforge.com       | employee123 |
| Employee | rahul@flowforge.com          | employee123 |

These accounts are created automatically the first time the app starts
(see `DataSeeder.java`).

## Core Workflow

1. Employee logs in and submits a Leave Request or Purchase Request.
2. The request is automatically assigned to the employee's Manager.
3. The Manager views, then Approves or Rejects the request with comments.
4. If the Manager does not act within the configured timeout
   (`escalation.timeout.minutes=2` in application.properties, set low for
   demo purposes), a background scheduler automatically escalates the
   request to the Admin.
5. Every state change is written to the Workflow History table and shown
   as a timeline on the request detail page.
6. The Admin dashboard shows live analytics: total/pending/approved/
   rejected/escalated counts, today's and monthly volumes, approval rate,
   escalation rate, and recent activity.

## Testing Auto Escalation

1. Log in as employee@flowforge.com and submit a Leave Request.
2. Do not act on it as the manager.
3. Wait 2 minutes (the scheduler runs every 30 seconds).
4. Log in as admin@flowforge.com — the request will now appear under
   "Escalated Requests Needing Admin Action" on the dashboard, and can be
   approved or rejected there.

## Project Structure

```
src/main/java/com/flowforge
  config/          Spring Security config and startup data seeder
  controller/      Employee, Manager, Admin and Login controllers
  entity/          JPA entities: User, Department, LeaveRequest,
                    PurchaseRequest, WorkflowHistory
  repository/      Spring Data JPA repositories
  scheduler/        Background job for auto escalation
  service/         Business logic: RequestService, AnalyticsService,
                    WorkflowHistoryService, UserDetailsServiceImpl
src/main/resources
  templates/       Thymeleaf views (login, employee, manager, admin)
  static/css/      Custom styling on top of Bootstrap 5
  application.properties         H2 config (default)
  application-mysql.properties   MySQL config (optional profile)
```

## Notes

- Passwords are stored using BCrypt hashing.
- Access to `/employee/**`, `/manager/**`, `/admin/**` is restricted by
  role using Spring Security.
- File attachments are accepted on the leave and purchase forms; the file
  name is stored against the request as a lightweight demonstration of
  attachment handling (no binary storage layer is wired up).
- This project was built and reviewed in a sandboxed environment without
  outbound access to Maven Central, so an automated `mvn compile` could not
  be executed here. All files were manually checked for structural
  correctness (balanced braces, matching Thymeleaf view names, consistent
  entity/repository/service method signatures). Please run `mvn clean
  package` on first use to fetch dependencies and confirm the build on your
  machine.
