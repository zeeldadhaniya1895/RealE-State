import java.sql.*;
import java.util.Scanner;

public class TestConnection {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = createConnection()) {
            System.out.println("Connected to PostgreSQL successfully!");

            while (true) {
                displayMenu();
                int choice = getUserChoice(scanner);

                if (choice == 28) {
                    System.out.println("Exiting the program.");
                    break;
                }

                executeQueryBasedOnChoice(connection, choice);
            }

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static Connection createConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/RealE-State", // Your database URL
                "postgres", // Your database username
                "zeel@5127@" // Your database password
        );
    }

    private static void displayMenu() {
        System.out.println("Choose an option:");
        System.out.println("1. Get a list of all users.");
        System.out.println("2. Get a list of properties sorted by price.");
        System.out.println("3. Find all properties priced above 100,000.");
        System.out.println("4. Get a list of all usernames and email IDs.");
        System.out.println("5. Get all properties located in Mumbai.");
        System.out.println("6. Count the number of properties that are either on rent or on sale.");
        System.out.println("7. Get a list of properties that are available for rent.");
        System.out.println("8. List all properties along with the seller details.");
        System.out.println("9. List all users who have wishlisted properties.");
        System.out.println("10. List sellers who have no properties listed.");
        System.out.println("11. Find the average price of properties for each property type.");
        System.out.println("12. Find sellers who have more than 2 properties available for rent.");
        System.out.println("13. Find the total number of properties listed by sellers who have a linked bank account.");
        System.out.println("14. List the number of properties per city (cities with more than 5 properties).");
        System.out.println("15. List users who have wishlisted properties that are not available for rent.");
        System.out.println("16. List properties priced above the average price in the same city.");
        System.out.println("17. Find sellers who have at least one property with a rating lower than 3.");
        System.out.println("18. List properties with a rating higher than the average rating in the same city.");
        System.out.println("19. Find sellers who have a higher average property price than the overall average.");
        System.out.println("20. List property types with an average rating above 4.");
        System.out.println("21. Find properties in cities where the average price is below 500,000.");
        System.out.println("22. Find users who have wishlisted properties priced higher than any property in Mumbai.");
        System.out.println("23. List properties with a higher rating than all other properties by the same seller.");
        System.out.println("24. List sellers who have listed properties in all cities with average prices above 1 million.");
        System.out.println("25. Find users who have wishlisted all properties in Bengaluru.");
        System.out.println("26. List sellers who have properties in every state with more than 10 properties available.");
        System.out.println("27. List all properties available for sale that have not received any reviews.");
        System.out.println("28. Exit");
        System.out.print("Enter your choice: ");
    }

    private static int getUserChoice(Scanner scanner) {
        return scanner.nextInt();
    }

    private static void executeQueryBasedOnChoice(Connection connection, int choice) {
        String query = getQueryByChoice(choice);
        if (query != null) {
            executeQuery(connection, query);
        } else {
            System.out.println("Invalid choice. Please select a valid option.");
        }
    }

    private static String getQueryByChoice(int choice) {
        switch (choice) {
            case 1: return "SELECT * FROM realestate.user;";
            case 2: return "SELECT * FROM property ORDER BY price DESC;";
            case 3: return "SELECT * FROM property WHERE price > 100000;";
            case 4: return "SELECT u.username, u.emailId FROM realestate.user as u;";
            case 5: return "SELECT * FROM propertyAddress WHERE city = 'Mumbai';";
            case 6: return "SELECT COUNT(*) FROM propertyDetails WHERE propertyAvailability IN ('On rent', 'On sell');";
            case 7: return "SELECT * FROM property WHERE propertyId IN (SELECT propertyId FROM propertyDetails WHERE propertyAvailability = 'On rent');";
            case 8: return "SELECT p.propertyId, p.price, s.sellerId, s.mobileNumber, b.bankName, b.ifscCode FROM property p JOIN seller s ON p.sellerId = s.sellerId LEFT JOIN bankDetails b ON s.accountNumber = b.accountNumber;";
            case 9: return "SELECT u.username, p.propertyId, pd.propertyType, p.price FROM realestate.user u JOIN realestate.wishlist w ON u.userId = w.userId JOIN realestate.property p ON w.wishlistId = p.propertyId JOIN realestate.propertyDetails pd ON p.propertyId = pd.propertyId;";
            case 10: return "SELECT s.sellerId, s.mobileNumber FROM seller s LEFT JOIN property p ON s.sellerId = p.sellerId WHERE p.propertyId IS NULL;";
            case 11: return "SELECT pd.propertyType, AVG(p.price) AS avgPrice FROM property p JOIN propertyDetails pd ON p.propertyId = pd.propertyId GROUP BY pd.propertyType;";
            case 12: return "SELECT s.sellerId, COUNT(p.propertyId) AS rentProperties FROM seller s JOIN property p ON s.sellerId = p.sellerId JOIN propertyDetails pd ON p.propertyId = pd.propertyId WHERE pd.propertyAvailability = 'On rent' GROUP BY s.sellerId HAVING COUNT(p.propertyId) >= 2;";
            case 13: return "SELECT s.sellerId, COUNT(p.propertyId) AS totalProperties FROM seller s JOIN property p ON s.sellerId = p.sellerId WHERE s.accountNumber IS NOT NULL GROUP BY s.sellerId;";
            case 14: return "SELECT pa.city, COUNT(p.propertyId) AS propertyCount FROM property p JOIN propertyAddress pa ON p.propertyId = pa.propertyId GROUP BY pa.city HAVING COUNT(p.propertyId) > 5;";
            case 15: return "SELECT u.username FROM realestate.user u WHERE u.userId IN (SELECT w.userId FROM realestate.wishlist w JOIN realestate.propertyDetails pd ON w.wishlistId = pd.propertyId WHERE pd.propertyAvailability != 'On rent');";
            case 16: return "SELECT p.propertyId, p.price FROM property p JOIN propertyAddress pa ON p.propertyId = pa.propertyId WHERE p.price > (SELECT AVG(p2.price) FROM property p2 JOIN propertyAddress pa2 ON p2.propertyId = pa2.propertyId WHERE pa2.city = pa.city);";
            case 17: return "SELECT s.sellerId FROM seller s WHERE EXISTS (SELECT 1 FROM property p JOIN review r ON p.propertyId = r.propertyId WHERE p.sellerId = s.sellerId AND r.rating < 3);";
            case 18: return "SELECT p.propertyId, r.rating FROM property p JOIN review r ON p.propertyId = r.propertyId JOIN propertyAddress pa ON p.propertyId = pa.propertyId WHERE r.rating > (SELECT AVG(r2.rating) FROM property p2 JOIN review r2 ON p2.propertyId = r2.propertyId JOIN propertyAddress pa2 ON p2.propertyId = pa2.propertyId WHERE pa2.city = pa.city);";
            case 19: return "SELECT s.sellerId FROM seller s JOIN property p ON s.sellerId = p.sellerId GROUP BY s.sellerId HAVING AVG(p.price) > (SELECT AVG(price) FROM property);";
            case 20: return "SELECT pd.propertyType, AVG(r.rating) AS avgRating FROM propertyDetails pd JOIN property p ON pd.propertyId = p.propertyId JOIN review r ON p.propertyId = r.propertyId GROUP BY pd.propertyType HAVING AVG(r.rating) > 4;";
            case 21: return "SELECT p.propertyId, p.price FROM property p JOIN propertyAddress pa ON p.propertyId = pa.propertyId WHERE (SELECT AVG(p2.price) FROM property p2 JOIN propertyAddress pa2 ON p2.propertyId = pa2.propertyId WHERE pa2.city = pa.city) < 500000;";
            case 22: return "SELECT u.username FROM realestate.user u JOIN realestate.wishlist w ON u.userId = w.userId JOIN realestate.property p ON w.wishlistId = p.propertyId WHERE p.price > (SELECT MAX(p2.price) FROM realestate.property p2 JOIN realestate.propertyAddress pa2 ON p2.propertyId = pa2.propertyId WHERE pa2.city = 'Mumbai');";
            case 23: return "SELECT p.propertyId, r.rating FROM property p JOIN review r ON p.propertyId = r.propertyId JOIN propertyAddress pa ON p.propertyId = pa.propertyId WHERE r.rating > ALL (SELECT r2.rating FROM property p2 JOIN review r2 ON p2.propertyId = r2.propertyId WHERE p2.sellerId = p.sellerId);";
            case 24: return "SELECT s.sellerId FROM seller s JOIN property p ON s.sellerId = p.sellerId JOIN propertyAddress pa ON p.propertyId = pa.propertyId GROUP BY s.sellerId HAVING COUNT(DISTINCT pa.city) = (SELECT COUNT(DISTINCT pa2.city) FROM property p2 JOIN propertyAddress pa2 ON p2.propertyId = pa2.propertyId WHERE p2.sellerId = s.sellerId) AND (SELECT AVG(p2.price) FROM property p2 JOIN propertyAddress pa2 ON p2.propertyId = pa2.propertyId WHERE pa2.city IN (SELECT pa3.city FROM property p3 JOIN propertyAddress pa3 ON p3.propertyId = pa3.propertyId GROUP BY pa3.city HAVING AVG(p3.price) > 1000000)) > 0;";
            case 25: return "SELECT u.username FROM realestate.user u WHERE NOT EXISTS (SELECT p.propertyId FROM realestate.property p JOIN realestate.propertyAddress pa ON p.propertyId = pa.propertyId WHERE pa.city = 'Bengaluru' AND NOT EXISTS (SELECT w.wishlistId FROM realestate.wishlist w WHERE w.userId = u.userId AND w.wishlistId = p.propertyId));";
            case 26: return "SELECT s.sellerId FROM seller s JOIN property p ON s.sellerId = p.sellerId JOIN propertyAddress pa ON p.propertyId = pa.propertyId GROUP BY s.sellerId HAVING COUNT(DISTINCT pa.state) >= 10;";
            case 27: return "SELECT p.propertyId FROM property p LEFT JOIN review r ON p.propertyId = r.propertyId WHERE r.propertyId IS NULL AND p.propertyAvailability = 'On sale';";
            default: return null;
        }
    }

    private static void executeQuery(Connection connection, String query) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO realestate;");
            boolean isResultSet = stmt.execute(query);

            if (isResultSet) {
                processResultSet(stmt);
            } else {
                System.out.println("Rows affected: " + stmt.getUpdateCount());
            }

        } catch (SQLException e) {
            System.out.println("Query execution failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processResultSet(Statement stmt) throws SQLException {
        try (ResultSet rs = stmt.getResultSet()) {
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();

            printHeader(rsMetaData, columnCount);
            printRows(rs, columnCount);
        }
    }

    private static void printHeader(ResultSetMetaData rsMetaData, int columnCount) throws SQLException {
        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rsMetaData.getColumnName(i);
            header.append(String.format("| %-20s ", columnName));
        }
        System.out.println(header.toString());
        System.out.println(new String(new char[21 * columnCount]).replace("\0", "-"));
    }

    private static void printRows(ResultSet rs, int columnCount) throws SQLException {
        while (rs.next()) {
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = rs.getString(i);
                row.append(String.format("| %-20s ", columnValue != null ? columnValue : "NULL"));
            }
            System.out.println(row.toString());
            
        }
        System.out.println();
    }
}

// javac -cp ".;S:\study\dev\project\dbms\dbms\lib\postgresql-42.7.4.jar" TestConnection.java
// java -cp ".;S:\study\dev\project\dbms\dbms\lib\postgresql-42.7.4.jar" TestConnection