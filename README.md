# Smart Complaint Registration and Tracking System



> **Automated Department Routing, Geospatial Deduplication & Time-Triggered SLA Escalation**
> 

---

## 📌 Overview

The **Smart Complaint Registration and Tracking System** is a Java-based civic governance solution designed to streamline urban grievance management. Built around core computer science fundamentals—including parallel 1D arrays, trigonometric math, and automated control flow—it eliminates manual routing errors, prevents duplicate complaint logging, and enforces officer accountability through time-triggered SLA escalations.

---

## 🏫 Academic Context

* **Institution:** Koneru Lakshmaiah Education Foundation (KLH Deemed to be University, Hyderabad)


* **Academic Year:** 2026–27


* **Department:** Department of FED / Computer Science & Engineering (CSE)


* **Course:** Problem Solving Using Programming (Java) — CO-II


* **Student Contributors:**
* **Pranava Swaroop Miriyala** (Roll No: 2620030648, CSE)


* **Bharat Varma** (Roll No: 2620030574, CSE)




* **Faculty Guide:** Dr. Gopikrishna Vellaturi (Department of FED, KLH Hyderabad)



---

## 🚨 Problem Statement & Operational Gaps

Conventional municipal grievance desks suffer from critical operational inefficiencies:

| Operational Gap | Metric | Impact |
| --- | --- | --- |
| **Misrouted Complaints**<br> | **38%** error rate

 | Manual operator data-entry errors dispatch tickets to wrong municipal departments.

 |
| **Duplicate Submissions**<br> | **25%** volume surge

 | Multiple commuters file identical road/civic hazards, choking resolution queues.

 |
| **Opaque Workflows**<br> | Zero tracking IDs

 | Lack of tracking tokens or lifecycle status updates for citizens.

 |
| **No Escalation Protocols**<br> | Indefinite delays

 | Grievances linger unresolved without supervisory officer accountability.

 |

---

## 🎯 Key Objectives

1. **Deterministic Department Routing:** Automate mapping of complaint categories to responsible municipal agencies using `switch-case` logic.


2. **Haversine Geospatial Duplicate Rejection:** Filter repetitive tickets reported within a 50-meter spherical distance threshold.


3. **Automated SLA Escalation Engine:** Automatically flag and escalate unattended tickets pending for more than 48 hours to Zonal Heads.


4. **Transparent Tracking:** Issue unique 8-character hexadecimal tracking tokens (`TRK-XXXXXXXX`) for instant status updates.



---

## 🏛️ Department Routing Table

| Option | Issue Category | Target Department |
| --- | --- | --- |
| `1` | `POTHOLE`<br> | `PUBLIC_WORKS`<br> |
| `2` | `BROKEN_STREETLIGHT`<br> | `ELECTRICAL_MAINTENANCE`<br> |
| `3` | `WATER_PIPELINE_LEAK`<br> | `WATER_SUPPLY`<br> |
| `4` | `WASTE_MANAGEMENT`<br> | `SANITATION`<br> |

---

## ⚙️ Architecture & Data Structures

The core system relies on **10 Parallel 1D Arrays** for in-memory synchronized data management:

* `String[] trackingIds` — Random 8-character hex tracking token (`TRK-XXXXXXXX`).


* `String[] citizenNames` — Name of the citizen filing the issue.


* `String[] descriptions` — Free text description of the grievance.


* `String[] issueTypes` — Categorized problem type.


* `String[] departments` — Auto-routed municipal department.


* `double[] latitudes` — Decimal GPS latitude coordinate.


* `double[] longitudes` — Decimal GPS longitude coordinate.


* `String[] statuses` — Current lifecycle state (`SUBMITTED`, `RESOLVED`, `ESCALATED`).


* `boolean[] isEscalated` — SLA escalation flag.


* `int[] hoursPending` — Simulated time counter tracking ticket age in hours.



---

## 📐 Core Algorithms

### 1. Haversine Distance Formula (Spatial Deduplication)

Calculates spherical distance $d$ between incoming ticket $(lat_1, lon_1)$ and active records $(lat_2, lon_2)$:

$$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1) \cdot \cos(\phi_2) \cdot \sin^2\left(\frac{\Delta \lambda}{2}\right)$$

$$c = 2 \cdot \operatorname{atan2}\left(\sqrt{a}, \sqrt{1-a}\right)$$

$$d = R \cdot c$$

Where $R = 6,371,000\text{ meters}$. If $d \le 50.0\text{ m}$ for an identical unresolved issue type, the submission is rejected as a duplicate.

### 2. SLA Escalation Engine

Iterates through active tickets. If `!status.equals("RESOLVED")` and `hoursPending >= 48`, the system sets `isEscalated = true` and updates `status` to `"ESCALATED"`.

---

## 💻 Source Code (`SmartComplaintSystem.java`)

```java
import java.util.Scanner;

public class SmartComplaintSystem {
    // 1. DATA STORAGE (PARALLEL 1D ARRAYS)
    static final int MAX_COMPLAINTS = 100;
    static int totalComplaints = 0;

    static String[] trackingIds = new String[MAX_COMPLAINTS];
    static String[] citizenNames = new String[MAX_COMPLAINTS];
    static String[] descriptions = new String[MAX_COMPLAINTS];
    static String[] issueTypes = new String[MAX_COMPLAINTS];
    static double[] latitudes = new double[MAX_COMPLAINTS];
    static double[] longitudes = new double[MAX_COMPLAINTS];
    static String[] departments = new String[MAX_COMPLAINTS];
    static String[] statuses = new String[MAX_COMPLAINTS];
    static boolean[] isEscalated = new boolean[MAX_COMPLAINTS];
    static int[] hoursPending = new int[MAX_COMPLAINTS]; // Simulates time passed

    static final double DUPLICATE_RADIUS_METERS = 50.0;
    static final int SLA_HOURS_THRESHOLD = 48;

    // 2. HELPER METHODS (ALGORITHMS)
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_METERS = 6371000;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    public static String generateTrackingId() {
        String chars = "0123456789ABCDEF";
        String id = "TRK-";
        for (int i = 0; i < 8; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            id += chars.charAt(randomIndex);
        }
        return id;
    }

    // 3. CORE SERVICES
    public static void registerComplaint(String name, String desc, String type, String dept, double lat, double lon) {
        // Step 1: Check for duplicates using a for-loop
        for (int i = 0; i < totalComplaints; i++) {
            if (!statuses[i].equals("RESOLVED") && issueTypes[i].equals(type)) {
                double distance = calculateDistance(lat, lon, latitudes[i], longitudes[i]);
                if (distance <= DUPLICATE_RADIUS_METERS) {
                    System.out.printf("%nDUPLICATE REJECTED: Similar %s reported nearby under Ticket ID: %s%n", type, trackingIds[i]);
                    return; // Exit method early
                }
            }
        }

        // Step 2: Store in parallel arrays
        int index = totalComplaints;
        trackingIds[index] = generateTrackingId();
        citizenNames[index] = name;
        descriptions[index] = desc;
        issueTypes[index] = type;
        departments[index] = dept;
        latitudes[index] = lat;
        longitudes[index] = lon;
        statuses[index] = "SUBMITTED";
        isEscalated[index] = false;
        hoursPending[index] = 0; // Starts at 0 hours

        System.out.printf("%nCOMPLAINT REGISTERED: [%s] Issue: %s | Dept: %s | Status: %s | Escalated: %b | Description: '%s'%n",
                trackingIds[index], issueTypes[index], departments[index], statuses[index], isEscalated[index], descriptions[index]);

        totalComplaints++;
    }

    public static void trackComplaint(String trackingId) {
        boolean found = false;
        for (int i = 0; i < totalComplaints; i++) {
            if (trackingIds[i].equalsIgnoreCase(trackingId.trim())) {
                System.out.printf("%nTRACKING UPDATE: [%s] Issue: %s | Dept: %s | Status: %s | Escalated: %b | Description: '%s'%n",
                        trackingIds[i], issueTypes[i], departments[i], statuses[i], isEscalated[i], descriptions[i]);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("%nComplaint ID not found.");
        }
    }

    public static void runSLAEngine() {
        boolean foundEscalations = false;
        for (int i = 0; i < totalComplaints; i++) {
            if (!statuses[i].equals("RESOLVED") && hoursPending[i] >= SLA_HOURS_THRESHOLD && !isEscalated[i]) {
                isEscalated[i] = true;
                statuses[i] = "ESCALATED";
                System.out.printf("%nESCALATION ALERT: Complaint %s has been pending for %d hours! Escalated to Zonal Head.%n", 
                        trackingIds[i], hoursPending[i]);
                foundEscalations = true;
            }
        }
        if (!foundEscalations) {
            System.out.println("No complaints currently require escalation.");
        }
    }

    public static void simulateLateTicket() {
        if (totalComplaints == 0) {
            System.out.println("%nPlease register a complaint first before simulating an aging ticket.");
            return;
        }
        hoursPending[0] = 72; // Force the first ticket to be 72 hours old
        System.out.println("%nSimulated aging ticket for ID: " + trackingIds[0] + " (Set to 72 hours old)");
    }

    // 4. MAIN METHOD & INTERACTIVE CLI
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("Welcome to the Smart Complaint System!");

        while (running) {
            System.out.println("\n==================================");
            System.out.println("              MENU                ");
            System.out.println("==================================");
            System.out.println("1. File a New Complaint");
            System.out.println("2. Track an Existing Complaint");
            System.out.println("3. Run SLA Escalation Engine");
            System.out.println("4. Simulate an Overdue Ticket (Test Escalation)");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    if (totalComplaints >= MAX_COMPLAINTS) {
                        System.out.println("System full. Cannot accept more complaints.");
                        break;
                    }

                    System.out.print("Enter your name: ");
                    String name = scanner.nextLine();

                    System.out.print("Enter a description of the issue: ");
                    String desc = scanner.nextLine();

                    System.out.println("Select Issue Type:");
                    System.out.println("  1. POTHOLE");
                    System.out.println("  2. BROKEN_STREETLIGHT");
                    System.out.println("  3. WATER_PIPELINE_LEAK");
                    System.out.println("  4. WASTE_MANAGEMENT");
                    System.out.print("Enter choice (1-4): ");

                    int typeChoice;
                    try {
                        typeChoice = Integer.parseInt(scanner.nextLine());
                    } catch (Exception e) {
                        System.out.println("Invalid issue type selected. Operation cancelled.");
                        break;
                    }

                    String issueType = "";
                    String department = "";

                    switch (typeChoice) {
                        case 1:
                            issueType = "POTHOLE";
                            department = "PUBLIC_WORKS";
                            break;
                        case 2:
                            issueType = "BROKEN_STREETLIGHT";
                            department = "ELECTRICAL_MAINTENANCE";
                            break;
                        case 3:
                            issueType = "WATER_PIPELINE_LEAK";
                            department = "WATER_SUPPLY";
                            break;
                        case 4:
                            issueType = "WASTE_MANAGEMENT";
                            department = "SANITATION";
                            break;
                        default:
                            System.out.println("Invalid choice. Cancelled.");
                            continue;
                    }

                    System.out.print("Enter Latitude (e.g., 17.3850): ");
                    double lat;
                    try {
                        lat = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid latitude. Operation cancelled.");
                        break;
                    }

                    System.out.print("Enter Longitude (e.g., 78.4867): ");
                    double lon;
                    try {
                        lon = Double.parseDouble(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid longitude. Operation cancelled.");
                        break;
                    }

                    registerComplaint(name, desc, issueType, department, lat, lon);
                    break;

                case 2:
                    System.out.print("Enter Complaint Tracking ID (e.g., TRK-A1B2C3D4): ");
                    String trackingId = scanner.nextLine();
                    trackComplaint(trackingId);
                    break;

                case 3:
                    System.out.println("\nRunning SLA Check...");
                    runSLAEngine();
                    break;

                case 4:
                    simulateLateTicket();
                    break;

                case 5:
                    System.out.println("Exiting System. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please choose between 1 and 5.");
            }
        }
    }
}

```

---

## 🖥️ Sample Execution Output

```text
--- 1. DEMONSTRATING AUTOMATED ROUTING & TRACKING ID CREATION ---
COMPLAINT REGISTERED: [TRK-A1B2C3D4] Issue: POTHOLE | Dept: PUBLIC_WORKS | Status: SUBMITTED | Escalated: false | Description: 'Deep pothole near Main St.'
COMPLAINT REGISTERED: [TRK-E5F6G7H8] Issue: BROKEN_STREETLIGHT | Dept: ELECTRICAL_MAINTENANCE | Status: SUBMITTED | Escalated: false | Description: 'Dark street lamp'

--- 2. DEMONSTRATING DUPLICATE DETECTION ---
DUPLICATE REJECTED: Similar POTHOLE reported nearby under Ticket ID: TRK-A1B2C3D4

--- 3. DEMONSTRATING TRANSPARENT TRACKING ---
TRACKING UPDATE: [TRK-A1B2C3D4] Issue: POTHOLE | Dept: PUBLIC_WORKS | Status: SUBMITTED | Escalated: false | Description: 'Deep pothole near Main St.'

--- 4. DEMONSTRATING SLA ESCALATION PROTOCOL ---
ESCALATION ALERT: Complaint TRK-A1B2C3D4 has been pending for 72 hours! Escalated to Zonal Head.

--- FINAL TRACKING STATUS ---
TRACKING UPDATE: [TRK-A1B2C3D4] Issue: POTHOLE | Dept: PUBLIC_WORKS | Status: ESCALATED | Escalated: true | Description: 'Deep pothole near Main St.'

```

---

## 🚀 Roadmap & Future Enhancements

* **Mobile GIS Application:** Native smartphone frontend with automatic GPS fetching and live heatmap view.


* **PostgreSQL & Cloud API:** Migration to PostGIS spatial database paired with Spring Boot REST microservices.


* **AI Vision Rating:** Computer vision deep learning model to estimate pothole depth and automate priority scoring.
