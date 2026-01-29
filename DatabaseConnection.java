// Vehicle Rental System - Complete Database Version
import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

// DATABASE CONNECTION MANAGER
class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/VehicleRentalDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "jerrome_maximof1";
    private static Connection connection = null;
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("[✗] Database connection error: " + e.getMessage());
            return null;
        }
    }
    
    private static void initializeConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("[✓] MySQL Database connected successfully!");
            
            // Test the connection and check if tables exist
            testDatabaseSetup();
            
        } catch (ClassNotFoundException e) {
            System.err.println("[✗] MySQL JDBC Driver not found!");
            System.err.println("[✗] Please add mysql-connector-java-8.0.xx.jar to your classpath");
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("[✗] Database connection failed!");
            System.err.println("[✗] Error: " + e.getMessage());
            System.err.println("[✗] Check if:");
            System.err.println("    1. MySQL server is running");
            System.err.println("    2. Database 'VehicleRentalDB' exists");
            System.err.println("    3. Username/password is correct");
            System.exit(1);
        }
    }
    
    private static void testDatabaseSetup() {
        try {
            // Check if UserRoles table has data
            String checkSql = "SELECT COUNT(*) as count FROM UserRoles";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt("count") == 0) {
                    System.out.println("[!] Database tables are empty. Initializing default data...");
                    initializeDefaultData();
                }
            }
        } catch (SQLException e) {
            System.err.println("[!] Database tables might not exist. Make sure you ran the SQL script.");
        }
    }
    
    private static void initializeDefaultData() {
        try (Statement stmt = connection.createStatement()) {
            // Insert default roles
            stmt.execute("INSERT IGNORE INTO UserRoles (RoleName, Description) VALUES " +
                        "('ADMIN', 'System Administrator'), " +
                        "('CUSTOMER', 'Regular Customer'), " +
                        "('OWNER', 'Vehicle Owner')");
            
            // Insert default statuses
            stmt.execute("INSERT IGNORE INTO VehicleStatuses (StatusName, Description, IsAvailable) VALUES " +
                        "('AVAILABLE', 'Available for rent', TRUE), " +
                        "('RENTED', 'Currently rented', FALSE), " +
                        "('MAINTENANCE', 'Under maintenance', FALSE)");
            
            // Insert default makes
            stmt.execute("INSERT IGNORE INTO VehicleMakes (MakeName, Country) VALUES " +
                        "('Toyota', 'Japan'), ('Honda', 'Japan'), ('Ford', 'USA'), " +
                        "('BMW', 'Germany'), ('Tesla', 'USA'), ('Mercedes', 'Germany')");
            
            // Insert default colors
            stmt.execute("INSERT IGNORE INTO VehicleColors (ColorName) VALUES " +
                        "('White'), ('Black'), ('Blue'), ('Silver'), ('Red'), ('Gray')");
            
            // Insert default admin user (password: admin123)
            stmt.execute("INSERT IGNORE INTO Users (Username, PasswordHash, FullName, Email, Phone, RoleID, WalletBalance) VALUES " +
                        "('admin', 'admin123', 'System Admin', 'admin@rental.com', '1234567890', 1, 1000.00)");
            
            System.out.println("[✓] Default data initialized successfully!");
            
        } catch (SQLException e) {
            System.err.println("[✗] Error initializing default data: " + e.getMessage());
        }
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[✓] Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error closing connection: " + e.getMessage());
        }
    }
    
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn == null) return false;
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }
}

// MODELS
class User {
    private int userId; private String username; private String password; 
    private String fullName; private String email; private String phone; 
    private String role; private double walletBalance;
    
    public User(int userId, String username, String password, String fullName, 
               String email, String phone, String role) {
        this.userId = userId; this.username = username; this.password = password;
        this.fullName = fullName; this.email = email; this.phone = phone;
        this.role = role; this.walletBalance = 0.0;
    }
    
    // Getters
    public int getUserId() { return userId; } public String getUsername() { return username; }
    public String getPassword() { return password; } public String getFullName() { return fullName; }
    public String getEmail() { return email; } public String getPhone() { return phone; }
    public String getRole() { return role; } public double getWalletBalance() { return walletBalance; }
    
    // Setters
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }
    public void addToWallet(double amount) { this.walletBalance += amount; }
    public boolean deductFromWallet(double amount) {
        if (this.walletBalance >= amount) { this.walletBalance -= amount; return true; }
        return false;
    }
    @Override public String toString() {
        return fullName + " (" + role + ") - Balance: $" + String.format("%.2f", walletBalance);
    }
}

class Vehicle {
    private int vehicleId; private String registrationNo; private String make; private String model;
    private int year; private String color; private double dailyRate; private String status;
    private int ownerId; private boolean isUserListed; private String location;
    
    public Vehicle(int vehicleId, String registrationNo, String make, String model, 
                  int year, String color, double dailyRate, String status, 
                  int ownerId, boolean isUserListed, String location) {
        this.vehicleId = vehicleId; this.registrationNo = registrationNo; this.make = make;
        this.model = model; this.year = year; this.color = color; this.dailyRate = dailyRate;
        this.status = status; this.ownerId = ownerId; this.isUserListed = isUserListed;
        this.location = location;
    }
    
    public int getVehicleId() { return vehicleId; } public String getRegistrationNo() { return registrationNo; }
    public String getMake() { return make; } public String getModel() { return model; }
    public int getYear() { return year; } public String getColor() { return color; }
    public double getDailyRate() { return dailyRate; } public String getStatus() { return status; }
    public int getOwnerId() { return ownerId; } public boolean isUserListed() { return isUserListed; }
    public String getLocation() { return location; }
    
    public void setStatus(String status) { this.status = status; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }
    @Override public String toString() {
        return String.format("%d. %s %s (%d) - $%.2f/day - %s - %s - %s", 
            vehicleId, make, model, year, dailyRate, status, 
            isUserListed ? "User" : "Company", location);
    }
}

class Rental {
    private int rentalId; private int userId; private int vehicleId; 
    private LocalDate rentalDate; private LocalDate returnDate; private double totalAmount;
    private String status; private String paymentStatus; private LocalDateTime createdAt;
    
    public Rental(int rentalId, int userId, int vehicleId, LocalDate rentalDate, 
                 LocalDate returnDate, double totalAmount, String status, String paymentStatus) {
        this.rentalId = rentalId; this.userId = userId; this.vehicleId = vehicleId;
        this.rentalDate = rentalDate; this.returnDate = returnDate; this.totalAmount = totalAmount;
        this.status = status; this.paymentStatus = paymentStatus; this.createdAt = LocalDateTime.now();
    }
    
    public int getRentalId() { return rentalId; } public int getUserId() { return userId; }
    public int getVehicleId() { return vehicleId; } public LocalDate getRentalDate() { return rentalDate; }
    public LocalDate getReturnDate() { return returnDate; } public double getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; } public String getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    @Override public String toString() {
        return String.format("Rental #%d: Vehicle %d from %s to %s - $%.2f - %s - Payment: %s", 
            rentalId, vehicleId, rentalDate, returnDate, totalAmount, status, paymentStatus);
    }
}

// DATA STORE
class DataStore {
    
    // Helper method to get or create lookup values
    private static int getOrCreateLookup(String table, String nameColumn, String idColumn, String value) {
        String checkSql = "SELECT " + idColumn + " FROM " + table + " WHERE " + nameColumn + " = ?";
        String insertSql = "INSERT INTO " + table + " (" + nameColumn + ") VALUES (?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, value);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            // Insert new value
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, value);
                insertStmt.executeUpdate();
                
                ResultSet keys = insertStmt.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error in getOrCreateLookup for " + value + ": " + e.getMessage());
        }
        return -1;
    }
    
    // USER METHODS
    public static User getUserByUsername(String username) {
        String sql = "SELECT u.*, ur.RoleName FROM Users u " +
                    "JOIN UserRoles ur ON u.RoleID = ur.RoleID " +
                    "WHERE u.Username = ? AND u.IsActive = 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("PasswordHash"),
                    rs.getString("FullName"),
                    rs.getString("Email"),
                    rs.getString("Phone"),
                    rs.getString("RoleName")
                );
                user.setWalletBalance(rs.getDouble("WalletBalance"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting user: " + e.getMessage());
        }
        return null;
    }
    
    public static User getUserById(int userId) {
        String sql = "SELECT u.*, ur.RoleName FROM Users u " +
                    "JOIN UserRoles ur ON u.RoleID = ur.RoleID " +
                    "WHERE u.UserID = ? AND u.IsActive = 1";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("PasswordHash"),
                    rs.getString("FullName"),
                    rs.getString("Email"),
                    rs.getString("Phone"),
                    rs.getString("RoleName")
                );
                user.setWalletBalance(rs.getDouble("WalletBalance"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting user: " + e.getMessage());
        }
        return null;
    }
    
    public static boolean addUser(User user) {
        // Check if username exists
        if (getUserByUsername(user.getUsername()) != null) {
            System.err.println("[✗] Username already exists!");
            return false;
        }
        
        // Determine RoleID
        int roleId = 2; // Default to CUSTOMER
        if (user.getRole().equalsIgnoreCase("ADMIN")) {
            roleId = 1;
        } else if (user.getRole().equalsIgnoreCase("OWNER")) {
            roleId = 3;
        }
        
        String sql = "INSERT INTO Users (Username, PasswordHash, FullName, Email, Phone, RoleID, WalletBalance) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getPhone());
            pstmt.setInt(6, roleId);
            pstmt.setDouble(7, user.getWalletBalance());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Log the event
                logEvent("USER_REGISTERED", "New user: " + user.getUsername(), 0);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error adding user: " + e.getMessage());
        }
        return false;
    }
    
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, ur.RoleName FROM Users u " +
                    "JOIN UserRoles ur ON u.RoleID = ur.RoleID " +
                    "WHERE u.IsActive = 1 ORDER BY u.UserID";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("PasswordHash"),
                    rs.getString("FullName"),
                    rs.getString("Email"),
                    rs.getString("Phone"),
                    rs.getString("RoleName")
                );
                user.setWalletBalance(rs.getDouble("WalletBalance"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting users: " + e.getMessage());
        }
        return users;
    }
    
    public static boolean updateUserWallet(int userId, double amount) {
        String sql = "UPDATE Users SET WalletBalance = ? WHERE UserID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                logEvent("WALLET_UPDATED", "User " + userId + " wallet: $" + amount, userId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error updating wallet: " + e.getMessage());
        }
        return false;
    }
    
    // VEHICLE METHODS
    public static List<Vehicle> getAvailableVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.*, vm.MakeName, vc.ColorName, vs.StatusName " +
                    "FROM Vehicles v " +
                    "JOIN VehicleMakes vm ON v.MakeID = vm.MakeID " +
                    "JOIN VehicleColors vc ON v.ColorID = vc.ColorID " +
                    "JOIN VehicleStatuses vs ON v.StatusID = vs.StatusID " +
                    "WHERE vs.StatusName = 'AVAILABLE' " +
                    "ORDER BY v.DailyRate";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehicles.add(new Vehicle(
                    rs.getInt("VehicleID"),
                    rs.getString("RegistrationNo"),
                    rs.getString("MakeName"),
                    rs.getString("Model"),
                    rs.getInt("Year"),
                    rs.getString("ColorName"),
                    rs.getDouble("DailyRate"),
                    rs.getString("StatusName"),
                    rs.getInt("OwnerID"),
                    rs.getBoolean("IsUserListed"),
                    rs.getString("Location")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting vehicles: " + e.getMessage());
        }
        return vehicles;
    }
    
    public static List<Vehicle> getUserListedVehicles(int ownerId) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.*, vm.MakeName, vc.ColorName, vs.StatusName " +
                    "FROM Vehicles v " +
                    "JOIN VehicleMakes vm ON v.MakeID = vm.MakeID " +
                    "JOIN VehicleColors vc ON v.ColorID = vc.ColorID " +
                    "JOIN VehicleStatuses vs ON v.StatusID = vs.StatusID " +
                    "WHERE v.OwnerID = ? AND v.IsUserListed = TRUE";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ownerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                vehicles.add(new Vehicle(
                    rs.getInt("VehicleID"),
                    rs.getString("RegistrationNo"),
                    rs.getString("MakeName"),
                    rs.getString("Model"),
                    rs.getInt("Year"),
                    rs.getString("ColorName"),
                    rs.getDouble("DailyRate"),
                    rs.getString("StatusName"),
                    rs.getInt("OwnerID"),
                    rs.getBoolean("IsUserListed"),
                    rs.getString("Location")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting user vehicles: " + e.getMessage());
        }
        return vehicles;
    }
    
    public static Vehicle getVehicleById(int id) {
        String sql = "SELECT v.*, vm.MakeName, vc.ColorName, vs.StatusName " +
                    "FROM Vehicles v " +
                    "JOIN VehicleMakes vm ON v.MakeID = vm.MakeID " +
                    "JOIN VehicleColors vc ON v.ColorID = vc.ColorID " +
                    "JOIN VehicleStatuses vs ON v.StatusID = vs.StatusID " +
                    "WHERE v.VehicleID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Vehicle(
                    rs.getInt("VehicleID"),
                    rs.getString("RegistrationNo"),
                    rs.getString("MakeName"),
                    rs.getString("Model"),
                    rs.getInt("Year"),
                    rs.getString("ColorName"),
                    rs.getDouble("DailyRate"),
                    rs.getString("StatusName"),
                    rs.getInt("OwnerID"),
                    rs.getBoolean("IsUserListed"),
                    rs.getString("Location")
                );
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting vehicle: " + e.getMessage());
        }
        return null;
    }
    
    public static boolean updateVehicleStatus(int vehicleId, String status) {
        String sql = "UPDATE Vehicles SET StatusID = (SELECT StatusID FROM VehicleStatuses WHERE StatusName = ?) " +
                    "WHERE VehicleID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, vehicleId);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                logEvent("VEHICLE_STATUS", "Vehicle " + vehicleId + " -> " + status, 0);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error updating vehicle status: " + e.getMessage());
        }
        return false;
    }
    
    public static boolean addVehicle(Vehicle vehicle) {
        // Get or create MakeID
        int makeId = getOrCreateLookup("VehicleMakes", "MakeName", "MakeID", vehicle.getMake());
        if (makeId == -1) return false;
        
        // Get or create ColorID
        int colorId = getOrCreateLookup("VehicleColors", "ColorName", "ColorID", vehicle.getColor());
        if (colorId == -1) return false;
        
        // Get StatusID for AVAILABLE
        int statusId = 1; // Default to AVAILABLE
        
        String sql = "INSERT INTO Vehicles (RegistrationNo, MakeID, Model, Year, ColorID, " +
                    "DailyRate, StatusID, OwnerID, IsUserListed, Location) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, vehicle.getRegistrationNo());
            pstmt.setInt(2, makeId);
            pstmt.setString(3, vehicle.getModel());
            pstmt.setInt(4, vehicle.getYear());
            pstmt.setInt(5, colorId);
            pstmt.setDouble(6, vehicle.getDailyRate());
            pstmt.setInt(7, statusId);
            pstmt.setInt(8, vehicle.getOwnerId());
            pstmt.setBoolean(9, vehicle.isUserListed());
            pstmt.setString(10, vehicle.getLocation());
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                logEvent("VEHICLE_ADDED", vehicle.getRegistrationNo() + " added", vehicle.getOwnerId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error adding vehicle: " + e.getMessage());
        }
        return false;
    }
    
    public static boolean updateVehicleRate(int vehicleId, double newRate) {
        String sql = "UPDATE Vehicles SET DailyRate = ? WHERE VehicleID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newRate);
            pstmt.setInt(2, vehicleId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[✗] Error updating vehicle rate: " + e.getMessage());
            return false;
        }
    }
    
    // RENTAL METHODS
    public static List<Rental> getRentalsByUserId(int userId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT r.*, rs.StatusName, ps.StatusName as PaymentStatus " +
                    "FROM Rentals r " +
                    "JOIN RentalStatuses rs ON r.StatusID = rs.StatusID " +
                    "JOIN PaymentStatuses ps ON r.PaymentStatusID = ps.StatusID " +
                    "WHERE r.UserID = ? ORDER BY r.RentalID DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rentals.add(new Rental(
                    rs.getInt("RentalID"),
                    rs.getInt("UserID"),
                    rs.getInt("VehicleID"),
                    rs.getDate("RentalDate").toLocalDate(),
                    rs.getDate("ReturnDate").toLocalDate(),
                    rs.getDouble("TotalAmount"),
                    rs.getString("StatusName"),
                    rs.getString("PaymentStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting user rentals: " + e.getMessage());
        }
        return rentals;
    }
    
    public static List<Rental> getPendingRentals() {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT r.*, rs.StatusName, ps.StatusName as PaymentStatus " +
                    "FROM Rentals r " +
                    "JOIN RentalStatuses rs ON r.StatusID = rs.StatusID " +
                    "JOIN PaymentStatuses ps ON r.PaymentStatusID = ps.StatusID " +
                    "WHERE rs.StatusName = 'PENDING' ORDER BY r.CreatedAt";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                rentals.add(new Rental(
                    rs.getInt("RentalID"),
                    rs.getInt("UserID"),
                    rs.getInt("VehicleID"),
                    rs.getDate("RentalDate").toLocalDate(),
                    rs.getDate("ReturnDate").toLocalDate(),
                    rs.getDouble("TotalAmount"),
                    rs.getString("StatusName"),
                    rs.getString("PaymentStatus")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting pending rentals: " + e.getMessage());
        }
        return rentals;
    }
    
    public static boolean createRentalWithProcedure(int userId, int vehicleId, LocalDate rentalDate, LocalDate returnDate) {
        String sql = "{call sp_CreateRental(?, ?, ?, ?)}";
        
        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setInt(1, userId);
            cstmt.setInt(2, vehicleId);
            cstmt.setDate(3, Date.valueOf(rentalDate));
            cstmt.setDate(4, Date.valueOf(returnDate));
            
            ResultSet rs = cstmt.executeQuery();
            if (rs.next()) {
                boolean success = rs.getString("Status").equals("SUCCESS");
                if (success) {
                    logEvent("RENTAL_CREATED", "User " + userId + " rented vehicle " + vehicleId, userId);
                }
                return success;
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error creating rental: " + e.getMessage());
        }
        return false;
    }
    
    public static boolean approveRentalWithProcedure(int rentalId, int adminUserId) {
        String sql = "{call sp_ApproveRental(?, ?)}";
        
        try (Connection conn = DatabaseManager.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setInt(1, rentalId);
            cstmt.setInt(2, adminUserId);
            
            ResultSet rs = cstmt.executeQuery();
            if (rs.next()) {
                boolean success = rs.getString("Status").equals("SUCCESS");
                if (success) {
                    logEvent("RENTAL_APPROVED", "Rental " + rentalId + " approved", adminUserId);
                }
                return success;
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error approving rental: " + e.getMessage());
        }
        return false;
    }
    
    // TRANSACTION METHODS
    public static double getTotalAdminProfit() {
        String sql = "SELECT SUM(AdminCommission) as TotalProfit FROM Transactions WHERE Status = 'COMPLETED'";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("TotalProfit");
            }
        } catch (SQLException e) {
            System.err.println("[✗] Error getting admin profit: " + e.getMessage());
        }
        return 0.0;
    }
    
    // LOGGING
    static void logEvent(String logType, String message, int userId) {
        String sql = "INSERT INTO SystemLogs (LogType, LogMessage, UserID) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, logType);
            pstmt.setString(2, message);
            if (userId > 0) {
                pstmt.setInt(3, userId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[✗] Failed to log event: " + e.getMessage());
        }
    }
    
    // DEBUG METHODS
    public static void printDatabaseStats() {
        System.out.println("\n=== DATABASE STATISTICS ===");
        String[] tables = {"Users", "Vehicles", "Rentals", "Transactions", "SystemLogs"};
        
        for (String table : tables) {
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + table)) {
                
                if (rs.next()) {
                    System.out.println(table + ": " + rs.getInt("count") + " rows");
                }
            } catch (SQLException e) {
                System.out.println(table + ": Error - " + e.getMessage());
            }
        }
        System.out.println("===========================\n");
    }
    
    public static void viewSystemLogs() {
        String sql = "SELECT * FROM SystemLogs ORDER BY CreatedAt DESC LIMIT 20";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== RECENT SYSTEM LOGS ===");
            System.out.printf("%-5s %-20s %-40s %-10s %-20s\n", 
                "ID", "Type", "Message", "UserID", "Timestamp");
            System.out.println("----------------------------------------------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-5d %-20s %-40s %-10d %-20s\n",
                    rs.getInt("LogID"),
                    rs.getString("LogType"),
                    rs.getString("LogMessage").length() > 40 ? 
                        rs.getString("LogMessage").substring(0, 37) + "..." : 
                        rs.getString("LogMessage"),
                    rs.getInt("UserID"),
                    rs.getTimestamp("CreatedAt").toString().substring(0, 19)
                );
            }
            System.out.println("----------------------------------------------------------------------------------------\n");
        } catch (SQLException e) {
            System.err.println("[✗] Error getting logs: " + e.getMessage());
        }
    }
}

// CONTROLLERS
class AuthController {
    private User currentUser;
    
    public User login(String username, String password) {
        User user = DataStore.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            System.out.println("[✓] Welcome, " + user.getFullName() + "!");
            System.out.println("   Role: " + user.getRole());
            System.out.println("   Wallet Balance: $" + user.getWalletBalance());
            DataStore.logEvent("USER_LOGIN", user.getUsername() + " logged in", user.getUserId());
        } else {
            System.out.println("[-] Invalid username or password!");
            DataStore.logEvent("LOGIN_FAILED", "Failed login: " + username, 0);
        }
        return currentUser;
    }
    
    public boolean register(String username, String password, String fullName, String email, String phone) {
        // Validate input
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
            System.out.println("[-] All fields are required!");
            return false;
        }
        
        // Check if username exists
        if (DataStore.getUserByUsername(username) != null) {
            System.out.println("[-] Username already exists!");
            return false;
        }
        
        // Create and add user
        User newUser = new User(0, username, password, fullName, email, phone, "CUSTOMER");
        newUser.setWalletBalance(0.0);
        
        if (DataStore.addUser(newUser)) {
            System.out.println("[✓] Registration successful!");
            System.out.println("[✓] User '" + username + "' added to database.");
            return true;
        } else {
            System.out.println("[-] Registration failed!");
            return false;
        }
    }
    
    public User getCurrentUser() { return currentUser; }
    
    public void logout() {
        if (currentUser != null) {
            System.out.println("[✓] Goodbye, " + currentUser.getFullName() + "!");
            DataStore.logEvent("USER_LOGOUT", currentUser.getUsername() + " logged out", currentUser.getUserId());
            currentUser = null;
        }
    }
}

class VehicleController {
    public List<Vehicle> getAvailableVehicles() {
        return DataStore.getAvailableVehicles();
    }
    
    public Vehicle getVehicleById(int id) {
        return DataStore.getVehicleById(id);
    }
    
    public boolean addCompanyVehicle(String regNo, String make, String model, int year, 
                                     String color, double dailyRate, String location) {
        return DataStore.addVehicle(new Vehicle(0, regNo, make, model, year, color, dailyRate, "AVAILABLE", 0, false, location));
    }
    
    public boolean addUserVehicle(int ownerId, String regNo, String make, String model, int year, 
                                  String color, double dailyRate, String location) {
        return DataStore.addVehicle(new Vehicle(0, regNo, make, model, year, color, dailyRate, "AVAILABLE", ownerId, true, location));
    }
    
    public List<Vehicle> getUserVehicles(int ownerId) {
        return DataStore.getUserListedVehicles(ownerId);
    }
    
    public boolean updateVehicleRate(int vehicleId, double newRate) {
        return DataStore.updateVehicleRate(vehicleId, newRate);
    }
}

class RentalController {
    public double calculateRentalCost(int vehicleId, LocalDate rentalDate, LocalDate returnDate) {
        Vehicle v = DataStore.getVehicleById(vehicleId);
        if (v == null) return 0;
        return ChronoUnit.DAYS.between(rentalDate, returnDate) * v.getDailyRate();
    }
    
    public List<Rental> getUserRentals(int userId) {
        return DataStore.getRentalsByUserId(userId);
    }
    
    public List<Rental> getPendingRentals() {
        return DataStore.getPendingRentals();
    }
    
    public boolean createRental(int userId, int vehicleId, LocalDate rentalDate, LocalDate returnDate) {
        return DataStore.createRentalWithProcedure(userId, vehicleId, rentalDate, returnDate);
    }
    
    public boolean approveRental(int rentalId, int adminUserId) {
        return DataStore.approveRentalWithProcedure(rentalId, adminUserId);
    }
}

class PaymentController {
    public boolean processPayment(User user, double amount, String method) {
        if (user.deductFromWallet(amount)) {
            if (DataStore.updateUserWallet(user.getUserId(), user.getWalletBalance())) {
                System.out.println("[✓] Payment of $" + amount + " processed via " + method);
                DataStore.logEvent("PAYMENT_PROCESSED", user.getUserId() + " paid $" + amount, user.getUserId());
                return true;
            } else {
                user.addToWallet(amount); // Rollback
            }
        }
        System.out.println("[-] Insufficient funds!");
        return false;
    }
    
    public boolean addToWallet(User user, double amount) {
        user.addToWallet(amount);
        if (DataStore.updateUserWallet(user.getUserId(), user.getWalletBalance())) {
            System.out.println("[✓] $" + amount + " added to wallet.");
            System.out.println("[✓] New balance: $" + user.getWalletBalance());
            DataStore.logEvent("WALLET_ADDED", user.getUserId() + " added $" + amount, user.getUserId());
            return true;
        }
        return false;
    }
}

// MAIN APPLICATION
public class DatabaseConnection {
    private static Scanner scanner = new Scanner(System.in);
    private static AuthController auth = new AuthController();
    private static VehicleController vehicleCtrl = new VehicleController();
    private static RentalController rentalCtrl = new RentalController();
    private static PaymentController paymentCtrl = new PaymentController();
    
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   VEHICLE RENTAL SYSTEM (MySQL)");
        System.out.println("=========================================\n");
        
        // Initialize database connection
        if (!DatabaseManager.testConnection()) {
            System.err.println("[✗] Failed to connect to database. Exiting...");
            return;
        }
        
        System.out.println("[✓] System ready!\n");
        
        boolean running = true;
        while (running) {
            if (auth.getCurrentUser() == null) {
                running = showLoginMenu();
            } else {
                running = auth.getCurrentUser().getRole().equals("ADMIN") ? showAdminMenu() : showCustomerMenu();
            }
        }
        
        DatabaseManager.closeConnection();
        System.out.println("\nThank you for using Vehicle Rental System!");
        scanner.close();
    }
    
    private static boolean showLoginMenu() {
        System.out.println("\n=== LOGIN / REGISTER ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. View Database Status");
        System.out.println("4. Exit");
        System.out.print("Choose: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1: login(); break;
            case 2: register(); break;
            case 3: 
                DataStore.printDatabaseStats();
                DataStore.viewSystemLogs();
                break;
            case 4: return false;
            default: System.out.println("[-] Invalid choice!");
        }
        return true;
    }
    
    private static void login() {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Username: "); String username = scanner.nextLine();
        System.out.print("Password: "); String password = scanner.nextLine();
        auth.login(username, password);
    }
    
    private static void register() {
        System.out.println("\n=== REGISTER ===");
        System.out.print("Full Name: "); String fullName = scanner.nextLine();
        System.out.print("Email: "); String email = scanner.nextLine();
        System.out.print("Phone: "); String phone = scanner.nextLine();
        System.out.print("Username: "); String username = scanner.nextLine();
        System.out.print("Password: "); String password = scanner.nextLine();
        auth.register(username, password, fullName, email, phone);
    }
    
    private static boolean showCustomerMenu() {
        User u = auth.getCurrentUser();
        System.out.println("\n=== CUSTOMER DASHBOARD ===");
        System.out.println("User: " + u.getFullName());
        System.out.println("Balance: $" + u.getWalletBalance());
        System.out.println("\n1. Rent Vehicle");
        System.out.println("2. List Vehicle for Rent");
        System.out.println("3. My Listed Vehicles");
        System.out.println("4. My Rentals");
        System.out.println("5. Add Wallet Money");
        System.out.println("6. View Available Vehicles");
        System.out.println("7. Update Vehicle Rates");
        System.out.println("8. Logout");
        System.out.print("Choose: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1: rentVehicle(); break;
            case 2: listVehicle(); break;
            case 3: viewMyVehicles(); break;
            case 4: viewMyRentals(); break;
            case 5: addWalletMoney(); break;
            case 6: viewAvailableVehicles(); break;
            case 7: updateVehicleRates(); break;
            case 8: auth.logout(); break;
            default: System.out.println("[-] Invalid choice!");
        }
        return true;
    }
    
    private static boolean showAdminMenu() {
        System.out.println("\n=== ADMIN DASHBOARD ===");
        System.out.println("Admin Profit: $" + DataStore.getTotalAdminProfit());
        System.out.println("\n1. Approve Pending Rentals");
        System.out.println("2. View All Vehicles");
        System.out.println("3. Add Company Vehicle");
        System.out.println("4. View All Users");
        System.out.println("5. View System Logs");
        System.out.println("6. View Database Stats");
        System.out.println("7. Logout");
        System.out.print("Choose: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1: approveRentals(); break;
            case 2: viewAllVehicles(); break;
            case 3: addCompanyVehicle(); break;
            case 4: viewAllUsers(); break;
            case 5: DataStore.viewSystemLogs(); break;
            case 6: DataStore.printDatabaseStats(); break;
            case 7: auth.logout(); break;
            default: System.out.println("[-] Invalid choice!");
        }
        return true;
    }
    
    // CUSTOMER METHODS
    private static void rentVehicle() {
        System.out.println("\n=== RENT VEHICLE ===");
        List<Vehicle> vehicles = vehicleCtrl.getAvailableVehicles();
        
        if (vehicles.isEmpty()) {
            System.out.println("[-] No vehicles available!");
            return;
        }
        
        System.out.println("Available Vehicles:");
        for (Vehicle v : vehicles) {
            System.out.println(v);
        }
        
        System.out.print("\nVehicle ID: ");
        int vehicleId = getIntInput();
        scanner.nextLine();
        
        Vehicle v = vehicleCtrl.getVehicleById(vehicleId);
        if (v == null || !v.getStatus().equals("AVAILABLE")) {
            System.out.println("[-] Invalid vehicle!");
            return;
        }
        
        System.out.print("Rental Date (YYYY-MM-DD): ");
        LocalDate rentalDate = getDateInput();
        System.out.print("Return Date (YYYY-MM-DD): ");
        LocalDate returnDate = getDateInput();
        
        if (returnDate.isBefore(rentalDate)) {
            System.out.println("[-] Invalid dates!");
            return;
        }
        
        long days = ChronoUnit.DAYS.between(rentalDate, returnDate);
        double cost = rentalCtrl.calculateRentalCost(vehicleId, rentalDate, returnDate);
        System.out.printf("\nCost: $%.2f for %d days\n", cost, days);
        
        User u = auth.getCurrentUser();
        if (u.getWalletBalance() < cost) {
            System.out.println("[-] Insufficient funds! Add money first.");
            return;
        }
        
        System.out.print("Confirm? (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            if (rentalCtrl.createRental(u.getUserId(), vehicleId, rentalDate, returnDate)) {
                System.out.println("[✓] Rental submitted! Awaiting approval.");
                u.deductFromWallet(cost);
            } else {
                System.out.println("[-] Rental failed!");
            }
        }
    }
    
    private static void listVehicle() {
        System.out.println("\n=== LIST VEHICLE ===");
        User u = auth.getCurrentUser();
        
        System.out.print("Registration No: "); String regNo = scanner.nextLine();
        System.out.print("Make: "); String make = scanner.nextLine();
        System.out.print("Model: "); String model = scanner.nextLine();
        System.out.print("Year: "); int year = getIntInput(); scanner.nextLine();
        System.out.print("Color: "); String color = scanner.nextLine();
        System.out.print("Daily Rate: $"); double rate = getDoubleInput(); scanner.nextLine();
        System.out.print("Location: "); String location = scanner.nextLine();
        
        if (vehicleCtrl.addUserVehicle(u.getUserId(), regNo, make, model, year, color, rate, location)) {
            System.out.println("[✓] Vehicle listed! You earn 80% of rentals.");
        } else {
            System.out.println("[-] Failed to list vehicle!");
        }
    }
    
    private static void viewMyVehicles() {
        System.out.println("\n=== MY VEHICLES ===");
        User u = auth.getCurrentUser();
        List<Vehicle> vehicles = vehicleCtrl.getUserVehicles(u.getUserId());
        
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles listed.");
        } else {
            vehicles.forEach(System.out::println);
        }
    }
    
    private static void viewMyRentals() {
        System.out.println("\n=== MY RENTALS ===");
        User u = auth.getCurrentUser();
        List<Rental> rentals = rentalCtrl.getUserRentals(u.getUserId());
        
        if (rentals.isEmpty()) {
            System.out.println("No rentals found.");
        } else {
            rentals.forEach(System.out::println);
        }
    }
    
    private static void addWalletMoney() {
        User u = auth.getCurrentUser();
        System.out.println("\n=== ADD WALLET MONEY ===");
        System.out.println("Current Balance: $" + u.getWalletBalance());
        System.out.print("Amount: $");
        double amount = getDoubleInput();
        
        if (amount > 0) {
            paymentCtrl.addToWallet(u, amount);
        } else {
            System.out.println("[-] Invalid amount!");
        }
    }
    
    private static void viewAvailableVehicles() {
        System.out.println("\n=== AVAILABLE VEHICLES ===");
        List<Vehicle> vehicles = vehicleCtrl.getAvailableVehicles();
        
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles available.");
        } else {
            vehicles.forEach(System.out::println);
        }
    }
    
    private static void updateVehicleRates() {
        System.out.println("\n=== UPDATE VEHICLE RATES ===");
        User u = auth.getCurrentUser();
        List<Vehicle> vehicles = vehicleCtrl.getUserVehicles(u.getUserId());
        
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles to update.");
            return;
        }
        
        System.out.println("Your Vehicles:");
        vehicles.forEach(System.out::println);
        
        System.out.print("\nVehicle ID to update: ");
        int vehicleId = getIntInput();
        
        boolean owns = vehicles.stream().anyMatch(v -> v.getVehicleId() == vehicleId);
        if (!owns) {
            System.out.println("[-] Not your vehicle!");
            return;
        }
        
        System.out.print("New daily rate: $");
        double newRate = getDoubleInput();
        
        if (vehicleCtrl.updateVehicleRate(vehicleId, newRate)) {
            System.out.println("[✓] Rate updated!");
        } else {
            System.out.println("[-] Update failed!");
        }
    }
    
    // ADMIN METHODS
    private static void approveRentals() {
        System.out.println("\n=== APPROVE RENTALS ===");
        List<Rental> pending = rentalCtrl.getPendingRentals();
        
        if (pending.isEmpty()) {
            System.out.println("No pending rentals.");
            return;
        }
        
        for (Rental r : pending) {
            System.out.println("\n" + r);
            Vehicle v = vehicleCtrl.getVehicleById(r.getVehicleId());
            if (v != null) {
                System.out.println("Vehicle: " + v.getMake() + " " + v.getModel());
            }
            
            System.out.print("Approve? (yes/no): ");
            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                if (rentalCtrl.approveRental(r.getRentalId(), auth.getCurrentUser().getUserId())) {
                    System.out.println("[✓] Approved!");
                } else {
                    System.out.println("[-] Failed to approve!");
                }
            }
        }
    }
    
    private static void viewAllVehicles() {
        System.out.println("\n=== ALL VEHICLES ===");
        List<Vehicle> vehicles = vehicleCtrl.getAvailableVehicles();
        
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles.");
        } else {
            vehicles.forEach(System.out::println);
        }
    }
    
    private static void addCompanyVehicle() {
        System.out.println("\n=== ADD COMPANY VEHICLE ===");
        System.out.print("Registration No: "); String regNo = scanner.nextLine();
        System.out.print("Make: "); String make = scanner.nextLine();
        System.out.print("Model: "); String model = scanner.nextLine();
        System.out.print("Year: "); int year = getIntInput(); scanner.nextLine();
        System.out.print("Color: "); String color = scanner.nextLine();
        System.out.print("Daily Rate: $"); double rate = getDoubleInput(); scanner.nextLine();
        System.out.print("Location: "); String location = scanner.nextLine();
        
        if (vehicleCtrl.addCompanyVehicle(regNo, make, model, year, color, rate, location)) {
            System.out.println("[✓] Company vehicle added!");
        } else {
            System.out.println("[-] Failed to add vehicle!");
        }
    }
    
    private static void viewAllUsers() {
        System.out.println("\n=== ALL USERS ===");
        List<User> users = DataStore.getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("No users.");
        } else {
            users.forEach(System.out::println);
        }
    }
    
    // HELPER METHODS
    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Enter valid number: ");
            }
        }
    }
    
    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Enter valid number: ");
            }
        }
    }
    
    private static LocalDate getDateInput() {
        while (true) {
            try {
                return LocalDate.parse(scanner.nextLine());
            } catch (Exception e) {
                System.out.print("Enter date (YYYY-MM-DD): ");
            }
        }
    }
}