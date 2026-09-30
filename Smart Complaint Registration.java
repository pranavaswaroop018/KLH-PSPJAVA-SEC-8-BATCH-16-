import java.util.Scanner;

public class SmartComplaintSystem {

    // ==========================================
    // 1. DATA STORAGE (PARALLEL 1D ARRAYS)
    // ==========================================
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

    // Constants
    static final double DUPLICATE_RADIUS_METERS = 50.0;
    static final int SLA_HOURS_THRESHOLD = 48;

    // ==========================================
    // 2. HELPER METHODS (ALGORITHMS)
    // ==========================================

    // Calculates distance in meters using Haversine formula
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

    // Generates a random 8-character ID
    public static String generateTrackingId() {
        String chars = "0123456789ABCDEF";
        String id = "TRK-";
        for (int i = 0; i < 8; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            id += chars.charAt(randomIndex);
        }
        return id;
    }

    // ==========================================
    // 3. CORE SERVICES
    // ==========================================

    public static void registerComplaint(String name, String desc, String type, String dept, double lat, double lon) {
        // Step 1: Check for duplicates using a for-loop (Control Flow & Arrays)
        for (int i = 0; i < totalComplaints; i++) {
            if (!statuses[i].equals("RESOLVED") && issueTypes[i].equals(type)) {
                double distance = calculateDistance(lat, lon, latitudes[i], longitudes[i]);
                
                if (distance <= DUPLICATE_RADIUS_METERS) {
                    System.out.printf("\nDUPLICATE REJECTED: Similar %s reported nearby under Ticket ID: %s%n", type, trackingIds[i]);
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

        // Formatted Output (Module 1)
        System.out.printf("\nCOMPLAINT REGISTERED: [%s] Issue: %s | Dept: %s | Status: %s | Escalated: %b | Description: '%s'%n",
                trackingIds[index], issueTypes[index], departments[index], statuses[index], isEscalated[index], descriptions[index]);

        totalComplaints++;
    }

    public static void trackComplaint(String trackingId) {
        boolean found = false;
        for (int i = 0; i < totalComplaints; i++) {
            if (trackingIds[i].equalsIgnoreCase(trackingId.trim())) {
                System.out.printf("\nTRACKING UPDATE: [%s] Issue: %s | Dept: %s | Status: %s | Escalated: %b | Description: '%s'%n",
                        trackingIds[i], issueTypes[i], departments[i], statuses[i], isEscalated[i], descriptions[i]);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("\nComplaint ID not found.");
        }
    }

    public static void runSLAEngine() {
        boolean foundEscalations = false;
        for (int i = 0; i < totalComplaints; i++) {
            if (!statuses[i].equals("RESOLVED") && hoursPending[i] >= SLA_HOURS_THRESHOLD && !isEscalated[i]) {
                isEscalated[i] = true;
                statuses[i] = "ESCALATED";
                System.out.printf("\nESCALATION ALERT: Complaint %s has been pending for %d hours! Escalated to Zonal Head.%n", 
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
            System.out.println("\nPlease register a complaint first before simulating an aging ticket.");
            return;
        }
        hoursPending[0] = 72; // Force the first ticket to be 72 hours old
        System.out.println("\nSimulated aging ticket for ID: " + trackingIds[0] + " (Set to 72 hours old)");
    }

    // ==========================================
    // 4. MAIN METHOD & INTERACTIVE CLI
    // ==========================================

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("Welcome to the Smart Complaint System!");

        // Infinite loop for menu (Module 2)
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
                // Read input (Module 1 Scanner)
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            // Switch statement for menu processing (Module 2)
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

                    // Automated Routing using switch-case (Module 2)
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
                            continue; // Skip the rest of the loop and show menu again
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
        scanner.close();
    }
}
