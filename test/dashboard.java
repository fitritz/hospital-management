import java.util.Scanner;

class dashboard {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    String employeeName = "Not set";
    String attendanceStatus = "Not marked";
    int workingDays = 0;
    int hoursPerDay = 0;

    while (true) {
      System.out.println("\nEmployee Attendance Dashboard");
      System.out.println("1. Mark Attendance");
      System.out.println("2. View Employee Details");
      System.out.println("3. Calculate Working Hours");
      System.out.println("4. Exit");
      System.out.print("Choose an option: ");

      int choice = scanner.nextInt();
      scanner.nextLine();

      switch (choice) {
        case 1:
          System.out.print("Enter employee name: ");
          employeeName = scanner.nextLine();

          System.out.print("Enter attendance status (Present/Absent): ");
          attendanceStatus = scanner.nextLine();

          System.out.print("Enter working days: ");
          workingDays = scanner.nextInt();

          System.out.print("Enter hours per day: ");
          hoursPerDay = scanner.nextInt();
          scanner.nextLine();

          System.out.println("Attendance marked successfully.");
          break;

        case 2:
          System.out.println("Employee Name: " + employeeName);
          System.out.println("Attendance Status: " + attendanceStatus);
          System.out.println("Working Days: " + workingDays);
          System.out.println("Hours Per Day: " + hoursPerDay);
          break;

        case 3:
          int totalWorkingHours = workingDays * hoursPerDay;
          System.out.println("Total Working Hours: " + totalWorkingHours);
          break;

        case 4:
          System.out.println("Exiting system...");
          scanner.close();
          return;

        default:
          System.out.println("Invalid option. Try again.");
      }
    }
  }
}
