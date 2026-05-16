# Academix

### Coaching Center Management System — Full Design Document

**Stack:** Java 17 · Spring Boot 3.x · Thymeleaf · Bootstrap 5 · Spring Security · Spring Data JPA · Flyway · PostgreSQL (dev & prod)

---

## Table of Contents

1. [System Overview](#1-system-overview)  
2. [Multi-Branch Architecture](#2-multi-branch-architecture)  
3. [System Architecture Layers](#3-system-architecture-layers)  
4. [Database Schema (ERD)](#4-database-schema-erd)  
5. [Entity Models](#5-entity-models)  
6. [Role & Access Control](#6-role--access-control)  
7. [Branch Context — Request Flow](#7-branch-context--request-flow)  
8. [Module Breakdown](#8-module-breakdown)  
9. [Controller & Service Layer](#9-controller--service-layer)  
10. [Thymeleaf Layout & UI Structure](#10-thymeleaf-layout--ui-structure)  
11. [Security Configuration](#11-security-configuration)  
12. [Application Properties](#12-application-properties)  
13. [Database Migration (Flyway)](#13-database-migration-flyway)  
14. [URL Route Reference](#14-url-route-reference)  
15. [Project Folder Structure](#15-project-folder-structure)  
16. [pom.xml Dependencies](#16-pomxml-dependencies)  
17. [Build & Run](#17-build--run)  
18. [Implementation Roadmap](#18-implementation-roadmap)

---

## 1\. System Overview

Academix is a web-based Academix management platform that handles every operational aspect of a multi-branch coaching business — from student admissions and batch scheduling to fee collection, payroll, expenditure tracking, and cross-branch reporting.

```
┌─────────────────────────────────────────────────────────────┐
│                     Academix HQ                       │
│              (Super-Admin · Cross-branch view)               │
└────────────┬──────────────────┬──────────────────┬──────────┘
             │                  │                  │
     ┌───────▼──────┐  ┌───────▼──────┐  ┌───────▼──────┐
     │  Branch A    │  │  Branch B    │  │  Branch C    │
     │ Dhanmondi    │  │   Mirpur     │  │   Uttara     │
     └──────────────┘  └──────────────┘  └──────────────┘
     Each branch owns: Batches · Teachers · Students
                       Fees · Payroll · Expenditure
```

---

## 2\. Multi-Branch Architecture

### Ownership Hierarchy

Every record in the system is owned by a branch. The `branch_id` foreign key is present on all transactional tables.

```
Academix HQ
└── Branch  (branch_code, branch_name, address)
    ├── Batch  (branch_id FK)
    │   └── Student  (batch_id FK + branch_id FK)
    │       └── FeePayment  (student_id FK + branch_id FK)
    ├── Teacher  (branch_id FK)
    │   └── PayrollEntry  (teacher_id FK + branch_id FK)
    └── Expenditure  (branch_id FK)
```

### Branch Context Flow

```
HTTP Request
     │
     ▼
BranchContextInterceptor
     │  reads ?branchId param or session
     ▼
BranchContext  (@SessionScope bean)
     │  holds activeBranchId
     ▼
Service Layer
     │  passes activeBranchId to BranchSpec.inBranch()
     ▼
JPA Specification  → WHERE branch_id = ?  (null = all, SUPER_ADMIN only)
     │
     ▼
Repository → Database
```

### Branch Switcher (UI)

After login, users with access to multiple branches see a branch selector page. A dropdown in the sidebar lets them switch branch at any time without logging out. Super-admins have an extra **"All branches (HQ)"** option that disables the branch filter and shows aggregated data.

---

## 3\. System Architecture Layers

```
┌──────────────────────────────────────────────────────────────┐
│                    Browser Client                             │
│         Thymeleaf · Bootstrap 5 · jQuery · Chart.js          │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTP
┌───────────────────────────▼──────────────────────────────────┐
│              Spring Boot (Embedded Tomcat)                    │
│    Spring Security · Session · CSRF · BranchContextFilter    │
├──────────────────────────────────────────────────────────────┤
│                    @Controller Layer                          │
│  Dashboard  Student  Batch  Teacher  Admission  Fee          │
│  Payroll    Expenditure     Report   Branch(Admin)            │
├──────────────────────────────────────────────────────────────┤
│                    @Service Layer                             │
│  StudentService   BatchService     TeacherService            │
│  FeeService       PayrollService   ExpenditureService        │
│  ReportService    BranchService    AdmissionService          │
├──────────────────────────────────────────────────────────────┤
│               Spring Data JPA Repositories                    │
│  + JpaSpecificationExecutor (BranchSpec for all entities)    │
├──────────────────────────────────────────────────────────────┤
│  Cross-cutting: Validation · Logging · Exception Handling    │
│                 PDF Export (iText 7) · Excel (Apache POI)    │
├──────────────────────────────────────────────────────────────┤
│    PostgreSQL (academix_dev / academix_prod)                  │
│              Flyway migration management                       │
└──────────────────────────────────────────────────────────────┘
```

---

## 4\. Database Schema (ERD)

```
┌──────────────────┐
│     branch       │
├──────────────────┤
│ id  PK           │
│ branch_code      │◄────────────────────────────────────┐
│ branch_name      │                                     │
│ address          │                                     │
│ phone            │                                     │
│ email            │                                     │
│ manager_name     │                                     │
│ status           │                                     │
│ opened_date      │                                     │
└──────────────────┘                                     │
                                                         │ branch_id FK (on all tables below)
┌──────────────────┐     ┌──────────────────┐            │
│     teacher      │     │      batch       │            │
├──────────────────┤     ├──────────────────┤            │
│ id  PK           │     │ id  PK           │            │
│ teacher_code     │     │ batch_code       │            │
│ full_name        │◄─┐  │ batch_name       │            │
│ phone            │  │  │ subject          │            │
│ email            │  │  │ schedule         │            │
│ subject          │  │  │ capacity         │            │
│ qualification    │  │  │ monthly_fee      │            │
│ base_salary      │  │  │ teacher_id  FK──►│            │
│ joining_date     │  │  │ status           │            │
│ status           │  │  │ start_date       │            │
│ branch_id   FK───┼──┼──┼►branch_id  FK────┼────────────┤
└──────────────────┘  │  └────────┬─────────┘            │
                      │           │                      │
┌──────────────────┐  │  ┌────────▼─────────┐            │
│  payroll_entry   │  │  │     student      │            │
├──────────────────┤  │  ├──────────────────┤            │
│ id  PK           │  │  │ id  PK           │            │
│ teacher_id  FK───┼──┘  │ student_code     │            │
│ month            │     │ full_name        │            │
│ year             │     │ phone            │            │
│ base_salary      │     │ guardian_name    │            │
│ bonus            │     │ guardian_phone   │            │
│ deduction        │     │ address          │            │
│ net_pay          │     │ date_of_birth    │            │
│ status           │     │ batch_id    FK───┼────────────┘
│ disbursed_date   │     │ admission_date   │
│ branch_id   FK───┼─────┼►branch_id  FK    │
└──────────────────┘     │ status           │
                         │ photo_path       │
                         └────────┬─────────┘
                                  │
                     ┌────────────▼──────────┐
                     │      fee_payment      │
                     ├───────────────────────┤
                     │ id  PK                │
                     │ receipt_number        │
                     │ student_id  FK        │
                     │ batch_id    FK        │
                     │ month                 │
                     │ year                  │
                     │ amount                │
                     │ payment_mode          │
                     │ status                │
                     │ paid_date             │
                     │ branch_id   FK        │
                     └───────────────────────┘

┌──────────────────────┐     ┌──────────────────────┐
│     expenditure      │     │   user_branch_role   │
├──────────────────────┤     ├──────────────────────┤
│ id  PK               │     │ id  PK               │
│ description          │     │ user_id    FK        │
│ amount               │     │ branch_id  FK        │
│ category             │     │ role                 │
│ date                 │     └──────────────────────┘
│ voucher_number       │
│ paid_to              │     ┌──────────────────────┐
│ approved_by          │     │       app_user       │
│ branch_id   FK       │     ├──────────────────────┤
└──────────────────────┘     │ id  PK               │
                             │ username             │
                             │ password  (BCrypt)   │
                             │ full_name            │
                             │ enabled              │
                             └──────────────────────┘
```

---

## 5\. Entity Models

### Branch.java

```java
@Entity
@Getter @Setter @NoArgsConstructor
public class Branch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String branchCode;       // e.g. BR-01
    private String branchName;       // e.g. "Dhanmondi Branch"
    private String address;
    private String phone;
    private String email;
    private String managerName;

    @Enumerated(EnumType.STRING)
    private BranchStatus status;     // ACTIVE, INACTIVE

    private LocalDate openedDate;

    @OneToMany(mappedBy = "branch")
    private List<Teacher> teachers;

    @OneToMany(mappedBy = "branch")
    private List<Batch> batches;
}
```

### Student.java

```java
@Entity
@Getter @Setter @NoArgsConstructor
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentCode;      // e.g. S-001
    private String fullName;
    private String phone;
    private String guardianName;
    private String guardianPhone;
    private String address;
    private LocalDate dateOfBirth;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    private StudentStatus status;    // ACTIVE, INACTIVE, GRADUATED

    private LocalDate admissionDate;
    private String photoPath;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<FeePayment> feePayments;
}
```

### Batch.java

```java
@Entity
@Getter @Setter @NoArgsConstructor
public class Batch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String batchCode;        // e.g. HSC-SCI-A
    private String batchName;
    private String subject;
    private String schedule;         // e.g. "Mon/Wed/Fri 7:00–9:00am"
    private Integer capacity;
    private BigDecimal monthlyFee;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    private BatchStatus status;      // ACTIVE, UPCOMING, COMPLETED

    private LocalDate startDate;
    private LocalDate endDate;

    @OneToMany(mappedBy = "batch")
    private List<Student> students;
}
```

### Teacher.java

```java
@Entity
@Getter @Setter @NoArgsConstructor
public class Teacher {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teacherCode;      // e.g. T-001
    private String fullName;
    private String phone;
    private String email;
    private String subject;
    private String qualification;
    private BigDecimal baseSalary;
    private LocalDate joiningDate;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    private TeacherStatus status;    // ACTIVE, INACTIVE, PROBATION

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    private List<PayrollEntry> payrollEntries;
}
```

### FeePayment.java

```java
@Entity
@Getter @Setter @NoArgsConstructor
public class FeePayment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String receiptNumber;    // e.g. DH-R-1042  (branch-prefixed)

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    private Batch batch;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    private Integer month;           // 1–12
    private Integer year;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode; // CASH, BKASH, NAGAD, BANK

    @Enumerated(EnumType.STRING)
    private FeeStatus status;        // PAID, PENDING, OVERDUE

    private LocalDate paidDate;
    private String notes;
}
```

### PayrollEntry.java

```java
@Entity
@Getter @Setter @NoArgsConstructor
public class PayrollEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    private Integer month;
    private Integer year;
    private BigDecimal baseSalary;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal netPay;       // base + bonus - deduction

    @Enumerated(EnumType.STRING)
    private PayrollStatus status;    // PENDING, DISBURSED

    private LocalDate disbursedDate;
    private String remarks;
}
```

### Expenditure.java

```java
@Entity
@Getter @Setter @NoArgsConstructor
public class Expenditure {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category; // UTILITIES, STATIONERY, MAINTENANCE, MARKETING, OTHER

    private LocalDate date;
    private String voucherNumber;
    private String paidTo;
    private String approvedBy;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
}
```

### User.java \+ UserBranchRole.java

```java
@Entity
@Table(name = "app_user")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;         // BCrypt
    private String fullName;
    private boolean enabled;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UserBranchRole> branchRoles;

    public boolean isSuperAdmin() {
        return branchRoles.stream()
            .anyMatch(r -> r.getRole() == BranchRole.SUPER_ADMIN);
    }

    public List<Branch> getAccessibleBranches() {
        return branchRoles.stream()
            .map(UserBranchRole::getBranch)
            .distinct()
            .collect(Collectors.toList());
    }
}

@Entity
@Getter @Setter @NoArgsConstructor
public class UserBranchRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Enumerated(EnumType.STRING)
    private BranchRole role;         // SUPER_ADMIN, BRANCH_MANAGER, STAFF, ACCOUNTANT
}
```

---

## 6\. Role & Access Control

### Role Definitions

| Role | Scope | Description |
| :---- | :---- | :---- |
| `SUPER_ADMIN` | All branches | Full access everywhere; can manage branches and users |
| `BRANCH_MANAGER` | Own branch only | Full access within their branch |
| `STAFF` | Own branch only | Students, batches (read), fees — no finance/payroll |
| `ACCOUNTANT` | Own branch only | Fees, payroll, expenditure — no student/batch editing |

### Permissions Matrix

| Module | SUPER\_ADMIN | BRANCH\_MANAGER | STAFF | ACCOUNTANT |
| :---- | :---: | :---: | :---: | :---: |
| Branch admin | R/W | — | — | — |
| Students | R/W | R/W | R/W | R |
| Batches | R/W | R/W | R | R |
| Teachers | R/W | R/W | R | R |
| Admissions | R/W | R/W | R/W | — |
| Fees | R/W | R/W | R/W | R/W |
| Payroll | R/W | R/W | — | R/W |
| Expenditure | R/W | R/W | — | R/W |
| Own-branch rpt | R/W | R/W | R | R |
| Cross-branch | R/W | — | — | — |

---

## 7\. Branch Context — Request Flow

### BranchContext.java  *(session-scoped bean)*

```java
@Component
@SessionScope
public class BranchContext {
    private Long activeBranchId;    // null = all branches (SUPER_ADMIN only)
    public Long getActiveBranchId() { return activeBranchId; }
    public void setActiveBranchId(Long id) { this.activeBranchId = id; }
}
```

### BranchContextInterceptor.java

```java
@Component
@RequiredArgsConstructor
public class BranchContextInterceptor implements HandlerInterceptor {
    private final BranchContext branchContext;
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest req,
                             HttpServletResponse res, Object handler) {
        String param = req.getParameter("branchId");
        if (param != null) {
            Long requestedId = Long.parseLong(param);
            User current = userService.currentUser();
            boolean allowed = current.isSuperAdmin() ||
                current.getAccessibleBranches().stream()
                    .anyMatch(b -> b.getId().equals(requestedId));
            if (allowed) branchContext.setActiveBranchId(requestedId);
        }
        if (branchContext.getActiveBranchId() == null) {
            User current = userService.currentUser();
            if (!current.isSuperAdmin()) {
                List<Branch> branches = current.getAccessibleBranches();
                if (branches.size() == 1)
                    branchContext.setActiveBranchId(branches.get(0).getId());
            }
        }
        return true;
    }
}
```

### BranchSpec.java  *(reusable JPA specification)*

```java
public class BranchSpec {
    /** null branchId → no filter (SUPER_ADMIN cross-branch view) */
    public static <T> Specification<T> inBranch(Long branchId) {
        return (root, query, cb) ->
            branchId == null
                ? cb.conjunction()
                : cb.equal(root.get("branch").get("id"), branchId);
    }
}
```

Usage in every service method:

```java
public List<Student> search(String name, Long batchId) {
    Long branchId = branchContext.getActiveBranchId();
    return studentRepo.findAll(
        Specification
            .where(BranchSpec.inBranch(branchId))
            .and(StudentSpec.nameLike(name))
            .and(StudentSpec.inBatch(batchId))
    );
}
```

---

## 8\. Module Breakdown

### Dashboard

- KPI cards: total students, active batches, fee collected this month, pending fees  
- Monthly fee collection bar chart (Chart.js)  
- Recent admissions list  
- Overdue fees alert table  
- All stats filtered by `activeBranchId`; SUPER\_ADMIN sees cross-branch totals when `activeBranchId` is null

### Student Management

- List with search (name, phone), filter (batch, status, branch)  
- Add / Edit / View student profile  
- Student card shows: personal info, current batch, fee history, admission date  
- Auto-generate `studentCode` (`S-001` per branch)  
- Photo upload stored in `/uploads/{branchCode}/students/`

### Batch Management

- Card grid view of all batches (subject, teacher, schedule, capacity, enrolled count)  
- Create / edit batch; assign teacher; set monthly fee  
- Capacity enforcement — warn when batch is full  
- Status: `ACTIVE`, `UPCOMING`, `COMPLETED`

### Teacher Management

- List with search and status filter  
- Add / Edit teacher: name, subject, qualification, base salary, joining date  
- View assigned batches per teacher  
- Auto-generate `teacherCode` (`T-001` per branch)

### Admission

- Admission form: student details \+ batch selection \+ admission fee  
- On submit: creates `Student` \+ `Admission` record \+ initial `FeePayment` for admission fee  
- Receipt generated immediately (printable / PDF download)  
- Admission fee distinct from monthly tuition

### Student Fee Collection

- List fees with filters: batch, month/year, status (`PAID`, `PENDING`, `OVERDUE`)  
- Record payment: student lookup → amount → payment mode (Cash / bKash / Nagad / Bank)  
- Auto-mark fees as `OVERDUE` if unpaid past 30th of the due month (scheduled task)  
- Receipt number format: `{BRANCH_CODE}-R-{sequence}` e.g. `DH-R-1042`  
- Download receipt as PDF

### Payroll

- Monthly payroll run scoped to active branch  
- Per teacher: base salary \+ bonus \+ deduction → net pay  
- Disbursement status: `PENDING` → `DISBURSED`  
- Payroll history by teacher or by month  
- Export payroll sheet as Excel

### Expenditure

- Add expense: description, category, amount, date, voucher number, paid to  
- Categories: `UTILITIES`, `STATIONERY`, `MAINTENANCE`, `MARKETING`, `RENT`, `OTHER`  
- Monthly summary by category  
- Export as PDF/Excel

### Reporting

- **Student report** — admissions, active/inactive count, fee collection rate per student  
- **Teacher report** — class hours, batch count, payroll summary  
- **Fee collection report** — batch-wise, month-wise collected vs pending  
- **Payroll report** — teacher-wise salary history  
- **Expenditure report** — category-wise, month-wise with P\&L  
- **Cross-branch comparison** (SUPER\_ADMIN only) — all branches side-by-side with totals  
- All reports exportable as PDF (iText 7\) and Excel (Apache POI)

### Branch Admin  *(SUPER\_ADMIN only)*

- Create / edit branches  
- Assign users to branches with roles  
- Per-branch KPI summary card  
- Cross-branch aggregated financial report

---

## 9\. Controller & Service Layer

### DashboardController.java

```java
@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final StudentService studentService;
    private final FeeService feeService;
    private final BatchService batchService;
    private final BranchContext branchContext;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        Long bid = branchContext.getActiveBranchId();
        model.addAttribute("stats", DashboardStatsDto.builder()
            .totalStudents(studentService.countActive(bid))
            .activeBatches(batchService.countActive(bid))
            .feeCollectedThisMonth(feeService.totalCollectedThisMonth(bid))
            .pendingFees(feeService.totalPending(bid))
            .build());
        model.addAttribute("recentAdmissions", studentService.getRecentAdmissions(5, bid));
        model.addAttribute("overdueFees",      feeService.getOverdueFees(bid));
        model.addAttribute("monthlyFeeData",   feeService.getMonthlyData(6, bid));
        return "dashboard/index";
    }
}
```

### StudentController.java

```java
@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;
    private final BatchService batchService;
    private final BranchContext branchContext;

    @GetMapping
    public String list(@RequestParam(required=false) String name,
                       @RequestParam(required=false) Long batchId,
                       @RequestParam(required=false) String status,
                       Model model) {
        model.addAttribute("students", studentService.search(name, batchId, status));
        model.addAttribute("batches",  batchService.findAllActive(branchContext.getActiveBranchId()));
        return "student/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("student", new StudentDto());
        model.addAttribute("batches", batchService.findAllActive(branchContext.getActiveBranchId()));
        return "student/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute StudentDto dto,
                       BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "student/form";
        studentService.save(dto, branchContext.getActiveBranchId());
        ra.addFlashAttribute("success", "Student saved successfully.");
        return "redirect:/students";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.findById(id));
        return "student/view";
    }
}
```

### FeeController.java

```java
@Controller
@RequestMapping("/fees")
@RequiredArgsConstructor
public class FeeController {
    private final FeeService feeService;
    private final StudentService studentService;
    private final BatchService batchService;
    private final BranchContext branchContext;

    @GetMapping
    public String list(@RequestParam(required=false) Long batchId,
                       @RequestParam(required=false) String status,
                       @RequestParam(required=false) Integer month,
                       Model model) {
        Long bid = branchContext.getActiveBranchId();
        model.addAttribute("fees",    feeService.search(batchId, status, month, bid));
        model.addAttribute("batches", batchService.findAllActive(bid));
        return "fee/list";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute FeePaymentDto dto,
                       BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) return "fee/form";
        FeePayment saved = feeService.save(dto, branchContext.getActiveBranchId());
        ra.addFlashAttribute("success", "Payment recorded. Receipt# " + saved.getReceiptNumber());
        return "redirect:/fees";
    }

    @GetMapping("/receipt/{id}/pdf")
    public void downloadReceipt(@PathVariable Long id,
                                HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
            "attachment; filename=receipt-" + id + ".pdf");
        feeService.generateReceiptPdf(id, response.getOutputStream());
    }
}
```

### ReportController.java

```java
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final BranchContext branchContext;

    @GetMapping
    public String index() { return "report/index"; }

    @GetMapping("/financial")
    public String financial(@RequestParam(defaultValue="0") int month,
                            @RequestParam(defaultValue="0") int year,
                            Model model) {
        model.addAttribute("summary",
            reportService.getFinancialSummary(month, year,
                branchContext.getActiveBranchId()));
        return "report/financial";
    }

    @GetMapping("/cross-branch")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String crossBranch(@RequestParam(required=false) Integer month,
                              @RequestParam(required=false) Integer year,
                              Model model) {
        model.addAttribute("summaries",
            reportService.getCrossBranchSummary(month, year));
        return "report/cross-branch";
    }

    @GetMapping("/{type}/export")
    public void export(@PathVariable String type,
                       @RequestParam String format,
                       HttpServletResponse response) throws IOException {
        Long bid = branchContext.getActiveBranchId();
        if ("pdf".equals(format)) {
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                "attachment; filename=" + type + "-report.pdf");
            reportService.exportPdf(type, bid, response.getOutputStream());
        } else {
            response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                "attachment; filename=" + type + "-report.xlsx");
            reportService.exportExcel(type, bid, response.getOutputStream());
        }
    }
}
```

### PayrollService.java  *(key logic)*

```java
@Service
@Transactional
@RequiredArgsConstructor
public class PayrollService {
    private final TeacherRepository teacherRepo;
    private final PayrollRepository payrollRepo;
    private final BranchContext branchContext;

    /** Run payroll for all active teachers in the active branch for a given month/year */
    public List<PayrollEntry> runPayroll(int month, int year) {
        Long bid = branchContext.getActiveBranchId();
        List<Teacher> teachers = teacherRepo.findAll(
            Specification.where(BranchSpec.inBranch(bid))
                .and(TeacherSpec.isActive()));

        return teachers.stream().map(teacher -> {
            PayrollEntry entry = new PayrollEntry();
            entry.setTeacher(teacher);
            entry.setBranch(teacher.getBranch());
            entry.setMonth(month);
            entry.setYear(year);
            entry.setBaseSalary(teacher.getBaseSalary());
            entry.setBonus(BigDecimal.ZERO);
            entry.setDeduction(BigDecimal.ZERO);
            entry.setNetPay(teacher.getBaseSalary());
            entry.setStatus(PayrollStatus.PENDING);
            return payrollRepo.save(entry);
        }).collect(Collectors.toList());
    }

    public PayrollEntry disburse(Long entryId) {
        PayrollEntry entry = payrollRepo.findById(entryId)
            .orElseThrow(() -> new ResourceNotFoundException("Payroll entry not found"));
        entry.setStatus(PayrollStatus.DISBURSED);
        entry.setDisbursedDate(LocalDate.now());
        return payrollRepo.save(entry);
    }
}
```

---

## 10\. Thymeleaf Layout & UI Structure

### templates/layout/base.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title th:text="${pageTitle} + ' — Academix'">Academix</title>
    <link rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" th:href="@{/css/custom.css}">
</head>
<body>
<div class="d-flex" style="min-height:100vh">

  <!-- ===== SIDEBAR ===== -->
  <nav class="bg-white border-end d-flex flex-column"
       style="width:220px;min-height:100vh">

    <div class="p-3 border-bottom">
      <span class="fw-semibold">
        <i class="bi bi-mortarboard-fill text-primary"></i> Academix
      </span>
    </div>

    <!-- Branch switcher (shown when user has multiple branches) -->
    <div class="px-3 py-2 border-bottom"
         th:if="${#lists.size(session.currentUser.accessibleBranches) > 1}">
      <form th:action="@{/branch/select}" method="post">
        <select name="branchId" class="form-select form-select-sm"
                onchange="this.form.submit()">
          <option th:if="${session.currentUser.superAdmin}"
                  th:value="''"
                  th:selected="${activeBranchId == null}">All branches (HQ)</option>
          <option th:each="b : ${session.currentUser.accessibleBranches}"
                  th:value="${b.id}"
                  th:text="${b.branchName}"
                  th:selected="${activeBranchId == b.id}"></option>
        </select>
      </form>
    </div>

    <ul class="nav flex-column px-2 pt-2 small flex-grow-1">
      <li><span class="text-muted px-2 d-block"
                style="font-size:10px;text-transform:uppercase;padding:10px 0 4px">
          Main</span></li>
      <li class="nav-item">
        <a th:href="@{/dashboard}" class="nav-link rounded"
           th:classappend="${activeMenu=='dashboard'}?' active bg-primary-subtle':' text-secondary'">
          <i class="bi bi-speedometer2 me-2"></i>Dashboard
        </a>
      </li>
      <li><span class="text-muted px-2 d-block"
                style="font-size:10px;text-transform:uppercase;padding:10px 0 4px">
          Academic</span></li>
      <li class="nav-item">
        <a th:href="@{/students}" class="nav-link rounded"
           th:classappend="${activeMenu=='students'}?' active bg-primary-subtle':' text-secondary'">
          <i class="bi bi-people me-2"></i>Students
        </a>
      </li>
      <li class="nav-item">
        <a th:href="@{/batches}" class="nav-link rounded"
           th:classappend="${activeMenu=='batches'}?' active bg-primary-subtle':' text-secondary'">
          <i class="bi bi-calendar3 me-2"></i>Batches
        </a>
      </li>
      <li class="nav-item">
        <a th:href="@{/teachers}" class="nav-link rounded"
           th:classappend="${activeMenu=='teachers'}?' active bg-primary-subtle':' text-secondary'">
          <i class="bi bi-person-badge me-2"></i>Teachers
        </a>
      </li>
      <li><span class="text-muted px-2 d-block"
                style="font-size:10px;text-transform:uppercase;padding:10px 0 4px">
          Finance</span></li>
      <li class="nav-item">
        <a th:href="@{/admissions/new}" class="nav-link rounded"
           th:classappend="${activeMenu=='admission'}?' active bg-primary-subtle':' text-secondary'">
          <i class="bi bi-file-earmark-plus me-2"></i>Admission
        </a>
      </li>
      <li class="nav-item">
        <a th:href="@{/fees}" class="nav-link rounded"
           th:classappend="${activeMenu=='fees'}?' active bg-primary-subtle':' text-secondary'">
          <i class="bi bi-coin me-2"></i>Student Fees
        </a>
      </li>
      <li class="nav-item">
        <a th:href="@{/payroll}" class="nav-link rounded"
           th:classappend="${activeMenu=='payroll'}?' active bg-primary-subtle':' text-secondary'"
           sec:authorize="hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER','ACCOUNTANT')">
          <i class="bi bi-cash-stack me-2"></i>Payroll
        </a>
      </li>
      <li class="nav-item">
        <a th:href="@{/expenditures}" class="nav-link rounded"
           th:classappend="${activeMenu=='expenditures'}?' active bg-primary-subtle':' text-secondary'"
           sec:authorize="hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER','ACCOUNTANT')">
          <i class="bi bi-receipt me-2"></i>Expenditure
        </a>
      </li>
      <li><span class="text-muted px-2 d-block"
                style="font-size:10px;text-transform:uppercase;padding:10px 0 4px">
          Insights</span></li>
      <li class="nav-item">
        <a th:href="@{/reports}" class="nav-link rounded"
           th:classappend="${activeMenu=='reports'}?' active bg-primary-subtle':' text-secondary'">
          <i class="bi bi-bar-chart me-2"></i>Reports
        </a>
      </li>
      <li class="nav-item" sec:authorize="hasRole('SUPER_ADMIN')">
        <a th:href="@{/admin/branches}" class="nav-link rounded"
           th:classappend="${activeMenu=='branches'}?' active bg-primary-subtle':' text-secondary'">
          <i class="bi bi-buildings me-2"></i>Branches
        </a>
      </li>
    </ul>

    <div class="p-3 border-top small text-muted">
      <sec:authentication property="name" var="uname"/>
      <i class="bi bi-person-circle me-1"></i>
      <span th:text="${uname}"></span>
      <a th:href="@{/logout}" class="ms-2 text-danger text-decoration-none">Logout</a>
    </div>
  </nav>

  <!-- ===== MAIN ===== -->
  <div class="flex-grow-1 d-flex flex-column">
    <header class="bg-white border-bottom px-4 py-2 d-flex align-items-center gap-3">
      <h6 class="mb-0 flex-grow-1 fw-medium" th:text="${pageTitle}">Dashboard</h6>
      <span th:if="${activeBranchName}" class="badge bg-primary-subtle text-primary"
            th:text="${activeBranchName}"></span>
    </header>

    <main class="flex-grow-1 p-4 bg-light">
      <div th:if="${success}"
           class="alert alert-success alert-dismissible fade show">
        <span th:text="${success}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      </div>
      <div th:if="${error}"
           class="alert alert-danger alert-dismissible fade show">
        <span th:text="${error}"></span>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      </div>
      <th:block th:replace="${content}"></th:block>
    </main>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.2/dist/chart.umd.min.js"></script>
<script th:src="@{/js/app.js}"></script>
</body>
</html>
```

### templates/branch/select.html  *(post-login branch picker)*

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Select Branch — Academix</title>
    <link rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">
<div class="d-flex align-items-center justify-content-center"
     style="min-height:100vh">
  <div class="card shadow-sm" style="width:420px">
    <div class="card-body p-4">
      <h5 class="fw-semibold mb-1">
        <i class="bi bi-buildings text-primary"></i> Select Branch
      </h5>
      <p class="text-muted small mb-4">Choose the branch you want to work in.</p>
      <form th:action="@{/branch/select}" method="post">
        <div class="d-grid gap-2">
          <button th:if="${showAll}" name="branchId" value=""
                  class="btn btn-outline-primary text-start">
            <i class="bi bi-globe me-2"></i>
            All branches (HQ view)
          </button>
          <button th:each="b : ${branches}"
                  th:name="branchId" th:value="${b.id}"
                  class="btn btn-outline-secondary text-start">
            <i class="bi bi-building me-2"></i>
            <span th:text="${b.branchName}"></span>
            <small class="text-muted ms-1" th:text="'· ' + ${b.address}"></small>
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
</body>
</html>
```

---

## 11\. Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/branches/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/reports/cross-branch").hasRole("SUPER_ADMIN")
                .requestMatchers("/payroll/**")
                    .hasAnyRole("SUPER_ADMIN","BRANCH_MANAGER","ACCOUNTANT")
                .requestMatchers("/expenditures/**")
                    .hasAnyRole("SUPER_ADMIN","BRANCH_MANAGER","ACCOUNTANT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/branch/select", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .userDetailsService(userDetailsService);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 12\. Application Properties

### application.properties  *(common)*

```
spring.application.name=academix
spring.thymeleaf.cache=false
spring.jpa.open-in-view=false
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate

# File uploads
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
app.upload.dir=uploads/

# Application settings
app.center.name=Academix Academix
app.academic.year=2025
```

### application-dev.properties

```
spring.datasource.url=jdbc:postgresql://localhost:5432/academix_dev
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USER:academix}
spring.datasource.password=${DB_PASS:academix}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.flyway.enabled=false
spring.jpa.hibernate.ddl-auto=create-drop
```

### application-prod.properties

```
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/academix_prod
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.thymeleaf.cache=true
logging.level.root=WARN
# Prod DB: CREATE DATABASE academix_prod OWNER academix_user;
# Set timezone: ALTER DATABASE academix_prod SET timezone TO 'Asia/Dhaka';
```

---

## 13\. Database Migration (Flyway)

### V1\_\_initial\_schema.sql

```sql
CREATE TABLE branch (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    branch_code   VARCHAR(20)  NOT NULL UNIQUE,
    branch_name   VARCHAR(100) NOT NULL,
    address       VARCHAR(255),
    phone         VARCHAR(20),
    email         VARCHAR(100),
    manager_name  VARCHAR(100),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    opened_date   DATE
);

CREATE TABLE app_user (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(100),
    enabled    BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_branch_role (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES app_user(id),
    branch_id  BIGINT      NOT NULL REFERENCES branch(id),
    role       VARCHAR(30) NOT NULL,
    UNIQUE (user_id, branch_id, role)
);

CREATE TABLE teacher (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    teacher_code   VARCHAR(20)    NOT NULL,
    full_name      VARCHAR(100)   NOT NULL,
    phone          VARCHAR(20),
    email          VARCHAR(100),
    subject        VARCHAR(100),
    qualification  VARCHAR(200),
    base_salary    DECIMAL(12,2)  NOT NULL DEFAULT 0,
    joining_date   DATE,
    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    branch_id      BIGINT         NOT NULL REFERENCES branch(id)
);

CREATE TABLE batch (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_code   VARCHAR(20)   NOT NULL,
    batch_name   VARCHAR(100)  NOT NULL,
    subject      VARCHAR(100),
    schedule     VARCHAR(200),
    capacity     INT,
    monthly_fee  DECIMAL(10,2) NOT NULL DEFAULT 0,
    teacher_id   BIGINT        REFERENCES teacher(id),
    status       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    start_date   DATE,
    end_date     DATE,
    branch_id    BIGINT        NOT NULL REFERENCES branch(id)
);

CREATE TABLE student (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_code    VARCHAR(20)  NOT NULL,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    guardian_name   VARCHAR(100),
    guardian_phone  VARCHAR(20),
    address         VARCHAR(255),
    date_of_birth   DATE,
    batch_id        BIGINT       REFERENCES batch(id),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    admission_date  DATE,
    photo_path      VARCHAR(255),
    branch_id       BIGINT       NOT NULL REFERENCES branch(id)
);

CREATE TABLE fee_payment (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    receipt_number  VARCHAR(30)   NOT NULL UNIQUE,
    student_id      BIGINT        NOT NULL REFERENCES student(id),
    batch_id        BIGINT        REFERENCES batch(id),
    month           SMALLINT,
    year            SMALLINT,
    amount          DECIMAL(10,2) NOT NULL,
    payment_mode    VARCHAR(20)   NOT NULL DEFAULT 'CASH',
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    paid_date       DATE,
    notes           VARCHAR(500),
    branch_id       BIGINT        NOT NULL REFERENCES branch(id)
);

CREATE TABLE payroll_entry (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    teacher_id      BIGINT        NOT NULL REFERENCES teacher(id),
    month           SMALLINT      NOT NULL,
    year            SMALLINT      NOT NULL,
    base_salary     DECIMAL(12,2) NOT NULL,
    bonus           DECIMAL(12,2) NOT NULL DEFAULT 0,
    deduction       DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_pay         DECIMAL(12,2) NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    disbursed_date  DATE,
    remarks         VARCHAR(500),
    branch_id       BIGINT        NOT NULL REFERENCES branch(id)
);

CREATE TABLE expenditure (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description     VARCHAR(255)  NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    category        VARCHAR(50)   NOT NULL,
    date            DATE          NOT NULL,
    voucher_number  VARCHAR(50),
    paid_to         VARCHAR(100),
    approved_by     VARCHAR(100),
    branch_id       BIGINT        NOT NULL REFERENCES branch(id)
);
```

### V2\_\_seed\_data.sql

```sql
-- Default main branch
INSERT INTO branch (branch_code, branch_name, status, opened_date)
VALUES ('BR-HQ', 'Main Branch', 'ACTIVE', CURRENT_DATE);

-- Default super-admin  (password: admin123  BCrypt rounds=12)
INSERT INTO app_user (username, password, full_name, enabled)
VALUES ('admin',
        '$2a$12$oRX.oa6VVIZfE6g4ypV5tOeQJSmkOFn3N0FHC6PkF3UcmJyv.0SBK',
        'System Admin', TRUE);

INSERT INTO user_branch_role (user_id, branch_id, role)
VALUES (1, 1, 'SUPER_ADMIN');
```

---

## 14\. URL Route Reference

### Public

| URL | Method | Description |
| :---- | :---- | :---- |
| `/login` | GET | Login page |
| `/login` | POST | Authenticate |
| `/logout` | POST | Logout |

### Branch selection

| URL | Method | Description |
| :---- | :---- | :---- |
| `/branch/select` | GET | Branch picker (post-login) |
| `/branch/select` | POST | Set active branch in session |

### Dashboard

| URL | Method | Description |
| :---- | :---- | :---- |
| `/dashboard` | GET | Stats, charts, overdue alerts |

### Students

| URL | Method | Description |
| :---- | :---- | :---- |
| `/students` | GET | List with search/filter |
| `/students/new` | GET | New student form |
| `/students/save` | POST | Save student |
| `/students/{id}` | GET | View student profile |
| `/students/{id}/edit` | GET | Edit form |
| `/students/{id}/edit` | POST | Update student |

### Batches

| URL | Method | Description |
| :---- | :---- | :---- |
| `/batches` | GET | Card grid view |
| `/batches/new` | GET | Create batch form |
| `/batches/save` | POST | Save batch |
| `/batches/{id}/edit` | GET | Edit batch |

### Teachers

| URL | Method | Description |
| :---- | :---- | :---- |
| `/teachers` | GET | List |
| `/teachers/new` | GET | New teacher form |
| `/teachers/save` | POST | Save teacher |
| `/teachers/{id}/edit` | GET | Edit teacher |

### Admissions

| URL | Method | Description |
| :---- | :---- | :---- |
| `/admissions/new` | GET | Admission form |
| `/admissions/save` | POST | Create student \+ admission record \+ fee record |

### Fees

| URL | Method | Description |
| :---- | :---- | :---- |
| `/fees` | GET | List with filters |
| `/fees/new` | GET | Record payment form |
| `/fees/save` | POST | Save payment |
| `/fees/receipt/{id}` | GET | View receipt |
| `/fees/receipt/{id}/pdf` | GET | Download receipt PDF |

### Payroll

| URL | Method | Description |
| :---- | :---- | :---- |
| `/payroll` | GET | Monthly payroll list |
| `/payroll/run` | GET | Run payroll form |
| `/payroll/run` | POST | Execute payroll for month |
| `/payroll/{id}/disburse` | POST | Mark as disbursed |
| `/payroll/export` | GET | Export sheet (xlsx) |

### Expenditure

| URL | Method | Description |
| :---- | :---- | :---- |
| `/expenditures` | GET | List |
| `/expenditures/new` | GET | Add expense form |
| `/expenditures/save` | POST | Save expense |
| `/expenditures/{id}/edit` | GET | Edit expense |

### Reports

| URL | Method | Description |
| :---- | :---- | :---- |
| `/reports` | GET | Reports index |
| `/reports/students` | GET | Student report |
| `/reports/teachers` | GET | Teacher report |
| `/reports/fees` | GET | Fee collection report |
| `/reports/payroll` | GET | Payroll summary |
| `/reports/financial` | GET | Expenditure / P\&L |
| `/reports/cross-branch` | GET | All-branches comparison (SUPER\_ADMIN) |
| `/reports/{type}/export` | GET | Export PDF or Excel `?format=pdf/xlsx` |

### Branch Admin  *(SUPER\_ADMIN only)*

| URL | Method | Description |
| :---- | :---- | :---- |
| `/admin/branches` | GET | All branches list |
| `/admin/branches/new` | GET | Create branch |
| `/admin/branches/save` | POST | Save branch |
| `/admin/branches/{id}/edit` | GET | Edit branch |
| `/admin/branches/{id}/users` | GET | Manage users for branch |
| `/admin/branches/{id}/users` | POST | Assign user \+ role |

---

## 15\. Project Folder Structure

```
academix/
├── src/
│   ├── main/
│   │   ├── java/com/academix/
│   │   │   ├── AcademixApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── WebMvcConfig.java          ← registers interceptor
│   │   │   │
│   │   │   ├── context/
│   │   │   │   ├── BranchContext.java          ← @SessionScope bean
│   │   │   │   └── BranchContextInterceptor.java
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Branch.java
│   │   │   │   ├── Student.java
│   │   │   │   ├── Batch.java
│   │   │   │   ├── Teacher.java
│   │   │   │   ├── Admission.java
│   │   │   │   ├── FeePayment.java
│   │   │   │   ├── PayrollEntry.java
│   │   │   │   ├── Expenditure.java
│   │   │   │   ├── User.java
│   │   │   │   ├── UserBranchRole.java
│   │   │   │   └── enums/
│   │   │   │       ├── BranchRole.java         ← SUPER_ADMIN, BRANCH_MANAGER, STAFF, ACCOUNTANT
│   │   │   │       ├── StudentStatus.java
│   │   │   │       ├── BatchStatus.java
│   │   │   │       ├── TeacherStatus.java
│   │   │   │       ├── FeeStatus.java
│   │   │   │       ├── PaymentMode.java
│   │   │   │       ├── PayrollStatus.java
│   │   │   │       └── ExpenseCategory.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── BranchRepository.java
│   │   │   │   ├── StudentRepository.java
│   │   │   │   ├── BatchRepository.java
│   │   │   │   ├── TeacherRepository.java
│   │   │   │   ├── FeePaymentRepository.java
│   │   │   │   ├── PayrollRepository.java
│   │   │   │   ├── ExpenditureRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   │
│   │   │   ├── specification/
│   │   │   │   ├── BranchSpec.java             ← inBranch(Long branchId)
│   │   │   │   ├── StudentSpec.java
│   │   │   │   ├── TeacherSpec.java
│   │   │   │   └── FeeSpec.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── BranchService.java
│   │   │   │   ├── StudentService.java
│   │   │   │   ├── BatchService.java
│   │   │   │   ├── TeacherService.java
│   │   │   │   ├── AdmissionService.java
│   │   │   │   ├── FeeService.java
│   │   │   │   ├── PayrollService.java
│   │   │   │   ├── ExpenditureService.java
│   │   │   │   ├── ReportService.java
│   │   │   │   └── UserService.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── BranchSelectController.java
│   │   │   │   ├── StudentController.java
│   │   │   │   ├── BatchController.java
│   │   │   │   ├── TeacherController.java
│   │   │   │   ├── AdmissionController.java
│   │   │   │   ├── FeeController.java
│   │   │   │   ├── PayrollController.java
│   │   │   │   ├── ExpenditureController.java
│   │   │   │   ├── ReportController.java
│   │   │   │   └── admin/
│   │   │   │       └── BranchAdminController.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── StudentDto.java
│   │   │   │   ├── FeePaymentDto.java
│   │   │   │   ├── DashboardStatsDto.java
│   │   │   │   └── BranchSummaryDto.java       ← cross-branch report
│   │   │   │
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── ResourceNotFoundException.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       │
│   │       ├── db/migration/
│   │       │   ├── V1__initial_schema.sql
│   │       │   └── V2__seed_data.sql
│   │       │
│   │       ├── static/
│   │       │   ├── css/custom.css
│   │       │   └── js/app.js
│   │       │
│   │       └── templates/
│   │           ├── layout/
│   │           │   ├── base.html               ← sidebar + topbar
│   │           │   └── fragments.html
│   │           ├── branch/
│   │           │   └── select.html             ← post-login picker
│   │           ├── dashboard/index.html
│   │           ├── student/
│   │           │   ├── list.html
│   │           │   ├── form.html
│   │           │   └── view.html
│   │           ├── batch/
│   │           │   ├── list.html
│   │           │   └── form.html
│   │           ├── teacher/
│   │           │   ├── list.html
│   │           │   └── form.html
│   │           ├── admission/form.html
│   │           ├── fee/
│   │           │   ├── list.html
│   │           │   ├── form.html
│   │           │   └── receipt.html
│   │           ├── payroll/
│   │           │   ├── list.html
│   │           │   └── run.html
│   │           ├── expenditure/
│   │           │   ├── list.html
│   │           │   └── form.html
│   │           ├── report/
│   │           │   ├── index.html
│   │           │   ├── students.html
│   │           │   ├── teachers.html
│   │           │   ├── fees.html
│   │           │   ├── financial.html
│   │           │   └── cross-branch.html
│   │           └── admin/
│   │               ├── branches/list.html
│   │               ├── branches/form.html
│   │               └── branches/users.html
│   │
│   └── test/
│       └── java/com/academix/
│           ├── service/
│           │   ├── StudentServiceTest.java
│           │   ├── FeeServiceTest.java
│           │   └── PayrollServiceTest.java
│           └── controller/
│               └── DashboardControllerTest.java
│
├── pom.xml
└── README.md
```

---

## 16\. pom.xml Dependencies

```xml
<properties>
    <java.version>17</java.version>
    <java.version>17</java.version>
    <spring-boot.version>3.3.0</spring-boot.version>
</properties>

<dependencies>
    <!-- Web + Thymeleaf -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Flyway -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- PDF export -->
    <dependency>
        <groupId>com.itextpdf</groupId>
        <artifactId>itext7-core</artifactId>
        <version>7.2.5</version>
        <type>pom</type>
    </dependency>

    <!-- Excel export -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.5</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 17\. Build & Run

```shell
# ── Development (PostgreSQL local) ──────────────────────────────────────
# Prereq: createdb academix_dev  (or: psql -c "CREATE DATABASE academix_dev;")
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# ── Production build ───────────────────────────────────────────────────
./mvnw clean package -DskipTests

# ── Run production JAR ─────────────────────────────────────────────────
java -jar target/academix-1.0.0.jar \
  --spring.profiles.active=prod \
  --DB_USER=root \
  --DB_PASS=yourpassword

# ── Docker (optional) ─────────────────────────────────────────────────
# Dockerfile:
#   FROM eclipse-temurin:17-jre
#   COPY target/academix-1.0.0.jar app.jar
#   ENTRYPOINT ["java","-jar","/app.jar"]

# Default login after V2 seed:
#   URL:      http://localhost:8080/login
#   Username: admin
#   Password: admin123
```

---

## 18\. Implementation Roadmap

| Week | Focus |
| :---- | :---- |
| 1 | Project scaffold · `Branch` \+ `UserBranchRole` entities · Flyway V1/V2 |
| 2 | Spring Security · login flow · branch selector page · `BranchContext` |
| 3 | Student \+ Batch \+ Teacher CRUD (all `BranchSpec`\-scoped) |
| 4 | Admission form · Fee collection · PDF receipt (iText 7\) |
| 5 | Payroll module · Expenditure tracking |
| 6 | Dashboard KPIs \+ Chart.js bar charts · overdue fee scheduler (`@Scheduled`) |
| 7 | Reports: per-branch PDF/Excel export · cross-branch comparison |
| 8 | Branch admin panel (SUPER\_ADMIN) · user management · UI polish |
| 9 | Validation messages · integration tests · performance review |

### Key implementation rules

1. Every service method passes `branchContext.getActiveBranchId()` to `BranchSpec.inBranch()`. A `null` branch id is only valid for `SUPER_ADMIN`.  
2. Receipt and code sequences are per-branch. Prefix with branch code: `DH-R-001`, `MP-T-001`.  
3. Payroll runs are always scoped to the active branch — no cross-branch pay runs.  
4. A SUPER\_ADMIN viewing "All branches" can read aggregates but must switch to an active branch before editing any record.  
5. Fee structures are per-batch (and therefore per-branch) — the `monthly_fee` field lives on `Batch`.  
6. Flyway owns the schema in prod — never use `ddl-auto=update` in production.

---

*Academix Academix Management System — Full Design v2.0*  
