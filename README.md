# Edumerge — Coaching Center Management System

A **complete multi-branch coaching center management platform** built with Spring Boot 3.3, PostgreSQL, and Thymeleaf.

## Features

✅ **Multi-Branch Architecture** — Single system, unlimited branches  
✅ **Role-Based Access Control** — SUPER_ADMIN, BRANCH_MANAGER, STAFF, ACCOUNTANT  
✅ **Student Management** — Admissions, batches, fee tracking  
✅ **Financial Management** — Fee collection, payroll, expenditure tracking  
✅ **Cross-Branch Reporting** — Aggregated financial & operational reports  
✅ **PDF/Excel Export** — Receipts, reports, salary sheets  
✅ **Responsive UI** — Bootstrap 5, mobile-friendly  

## Quick Start

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Maven 3.8+

### Development (PostgreSQL local)
```bash
# Create dev database first
createdb academix

# Run with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Access
# URL: http://localhost:8080
# Username: admin
# Password: admin123
```

### Production (PostgreSQL)
```bash
# Create database
createdb edumerge

# Build JAR
./mvnw clean package -DskipTests

# Run
java -jar target/edumerge-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/edumerge \
  --spring.datasource.username=postgres \
  --spring.datasource.password=yourpassword
```

## Project Structure

```
edumerge/
├── src/
│   ├── main/java/com/edumerge/
│   │   ├── config/              # Security, WebMvc
│   │   ├── context/             # BranchContext, Interceptor
│   │   ├── controller/          # REST controllers
│   │   ├── dto/                 # Data transfer objects
│   │   ├── exception/           # Exception handling
│   │   ├── model/               # JPA entities
│   │   │   └── enums/           # Enums
│   │   ├── repository/          # Data access layer
│   │   ├── service/             # Business logic
│   │   └── specification/       # JPA Specifications
│   ├── resources/
│   │   ├── application.yml      # Main config
│   │   ├── application-dev.yml  # Dev profile (PostgreSQL local)
│   │   ├── application-prod.yml # Prod profile (PostgreSQL)
│   │   ├── db/migration/        # Flyway migrations
│   │   ├── templates/           # Thymeleaf templates
│   │   └── static/              # CSS, JavaScript
│   └── test/                    # Test files
├── pom.xml                      # Maven dependencies
├── IMPLEMENTATION_NOTES.md      # Design recommendations
└── README.md                    # This file
```

## Architecture

### Multi-Branch Pattern
Every record belongs to a branch. `BranchContext` (session-scoped) holds the active branch ID. All queries use `BranchSpec.inBranch()` to filter by branch.

```java
// In any service method:
List<Student> students = studentRepository.findAll(
    BranchSpec.inBranch(branchContext.getActiveBranchId())
);
```

### Request Flow
1. User login → selects branch → session stores `activeBranchId`
2. `BranchContextInterceptor` sets `BranchContext` on each request
3. Service methods read `branchContext.getActiveBranchId()`
4. Repositories filter by branch via `BranchSpec`
5. Null branch ID (SUPER_ADMIN only) = cross-branch view

### Role-Based Access
| Role | Scope | Permissions |
|---|---|---|
| **SUPER_ADMIN** | All branches | Full access everywhere + branch management |
| **BRANCH_MANAGER** | Own branch | Full access within branch |
| **STAFF** | Own branch | Students, batches, fees (read/write) |
| **ACCOUNTANT** | Own branch | Fees, payroll, expenditure (financial only) |

## Key Modules

### 1. **Dashboard**
- KPI cards (students, batches, fees collected, pending)
- Recent admissions list
- Overdue fees alerts
- All filtered by active branch

### 2. **Student Management**
- Admission form + auto-generate student code
- Search/filter by name, phone, batch, status
- Student profile with fee history
- Photo upload support

### 3. **Batch & Teacher Management**
- Create/edit batches with capacity tracking
- Teacher assignment to batches
- Salary management
- Subject-wise organization

### 4. **Fee Collection**
- Record payments (Cash, bKash, Nagad, Bank)
- Auto-generate receipt numbers (e.g., DH-R-1042)
- Mark fees as PAID, PENDING, OVERDUE
- Scheduled task to auto-mark overdue after 30 days

### 5. **Payroll**
- Monthly payroll run for all active teachers
- Base salary + bonus - deduction = net pay
- Disbursement tracking
- Payroll history export

### 6. **Expenditure**
- Categorized expenses (utilities, stationery, maintenance, etc.)
- Voucher tracking
- Monthly summary by category

### 7. **Reporting**
- Student report (admissions, active count, fee collection rate)
- Teacher report (class hours, batch count, salary history)
- Fee collection report (batch-wise, month-wise)
- Payroll report (teacher-wise)
- Expenditure report (category-wise, P&L)
- **Cross-branch comparison** (SUPER_ADMIN only)
- All reports exportable as PDF & Excel

### 8. **Branch Admin** (SUPER_ADMIN only)
- Create/manage branches
- Assign users to branches with roles
- Per-branch KPI cards
- Cross-branch aggregated reports

## Configuration

### application.yml
Main config file (used for all profiles). Includes:
- Jackson serialization settings
- JPA Hibernate dialect
- Flyway migration settings
- Thymeleaf template settings
- Logging levels

### application-dev.yml
Development profile (PostgreSQL local):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/edumerge_dev
    username: ${DB_USER:postgres}
    password: ${DB_PASS:postgres}
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false
```

### application-prod.yml
Production profile (PostgreSQL):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/edumerge
    username: ${DB_USER}
    password: ${DB_PASS}
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
```

## Database Schema

**8 main tables:**
- `branch` — Branch master data
- `app_user` — User credentials
- `user_branch_role` — User-to-branch role mapping
- `teacher` — Teacher master
- `batch` — Class/batch master
- `student` — Student enrollment
- `admission` — Admission records
- `fee_payment` — Monthly fee transactions
- `payroll_entry` — Monthly salary records
- `expenditure` — Office expenses

All transactional tables have `branch_id` FK for isolation.

## Dependencies

### Core
- **Spring Boot 3.3.0** — Web framework
- **Spring Data JPA** — ORM
- **Spring Security 6** — Authentication & authorization
- **Thymeleaf** — Template engine

### Database
- **PostgreSQL Driver** — Production & Development DB
- **Flyway** — Database migrations

### Utilities
- **Lombok** — Reduce boilerplate
- **Validation API** — Bean validation
- **Apache POI** — Excel export
- **OpenPDF** — PDF export

*See `pom.xml` for complete list.*

## Testing

Basic test structure in place. Add tests for:
```java
// src/test/java/com/edumerge/
StudentServiceTest
FeeServiceTest
PayrollServiceTest
DashboardControllerTest
```

## Security

✅ BCrypt password hashing  
✅ CSRF protection (Spring Security default)  
✅ Method-level authorization (`@PreAuthorize`)  
✅ Branch-scoped data access  
✅ Session management  

**Recommended additions:**
- Rate limiting on login
- X-Frame-Options, HSTS headers
- File upload validation

## Troubleshooting

**Q: Flyway migration fails on startup**  
A: Ensure PostgreSQL is running and credentials are correct in application-prod.yml

**Q: Getting "No qualifying bean of type BranchContext"**  
A: Ensure WebMvcConfig is scanning your config package. Check `@ComponentScan` on main class.

**Q: Branch filter not working**  
A: Verify `BranchContextInterceptor` is registered in WebMvcConfig and intercepting requests.

**Q: Login redirects to `/branch/select` but gets 403**  
A: Ensure user has at least one role assigned. Check `user_branch_role` table.

## Next Steps

See `IMPLEMENTATION_NOTES.md` for:
- Design enhancements & recommendations
- Missing features to add
- Testing strategy
- Deployment checklist

## License

This project is provided as-is for internal use.

---

**Last Updated:** May 16, 2026  
**Maintained by:** Development Team
