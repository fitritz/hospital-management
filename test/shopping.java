import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

class shopping {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    Map<String, String> users = new HashMap<>();
    users.put("admin", "1234");

    System.out.println("1. New User");
    System.out.println("2. Login");
    System.out.print("Choose option: ");
    int option = scanner.nextInt();
    scanner.nextLine();

    if (option == 1) {
      System.out.print("Create username: ");
      String newUser = scanner.nextLine();

      System.out.print("Create password: ");
      String newPass = scanner.nextLine();

      users.put(newUser, newPass);
      System.out.println("Account created. Please login.");
    }

    System.out.print("Enter username: ");
    String username = scanner.nextLine();

    System.out.print("Enter password: ");
    String password = scanner.nextLine();

    if (users.containsKey(username) && users.get(username).equals(password)) {
      System.out.println("Login Successful");

      System.out.print("Enter product name: ");
      String productName = scanner.nextLine();

      System.out.print("Enter quantity: ");
      int quantity = scanner.nextInt();

      System.out.print("Enter price per item: ");
      double price = scanner.nextDouble();

      double total = quantity * price;
      double discount = 0;

      if (total > 5000) {
        discount = total * 0.10;
      }

      double finalAmount = total - discount;

      System.out.println("Product Name: " + productName);
      System.out.println("Total Amount: " + total);
      System.out.println("Discount: " + discount);
      System.out.println("Final Amount: " + finalAmount);
    } else {
      System.out.println("Invalid Credentials");
    }

    scanner.close();
  }
}
