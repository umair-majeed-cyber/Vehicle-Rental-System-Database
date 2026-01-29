-- MySQL Database for Vehicle Rental System
CREATE DATABASE IF NOT EXISTS VehicleRentalDB;
USE VehicleRentalDB;

-- ========== DOMAIN TABLES ==========
CREATE TABLE UserRoles (
    RoleID INT PRIMARY KEY AUTO_INCREMENT,
    RoleName VARCHAR(20) NOT NULL UNIQUE,
    Description VARCHAR(100),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE VehicleStatuses (
    StatusID INT PRIMARY KEY AUTO_INCREMENT,
    StatusName VARCHAR(20) NOT NULL UNIQUE,
    Description VARCHAR(100),
    IsAvailable BOOLEAN DEFAULT FALSE
);

CREATE TABLE VehicleMakes (
    MakeID INT PRIMARY KEY AUTO_INCREMENT,
    MakeName VARCHAR(50) NOT NULL UNIQUE,
    Country VARCHAR(50),
    EstablishedYear INT
);

CREATE TABLE VehicleColors (
    ColorID INT PRIMARY KEY AUTO_INCREMENT,
    ColorName VARCHAR(30) NOT NULL UNIQUE,
    HexCode VARCHAR(7)
);

CREATE TABLE RentalStatuses (
    StatusID INT PRIMARY KEY AUTO_INCREMENT,
    StatusName VARCHAR(20) NOT NULL UNIQUE,
    Description VARCHAR(100)
);

CREATE TABLE PaymentStatuses (
    StatusID INT PRIMARY KEY AUTO_INCREMENT,
    StatusName VARCHAR(20) NOT NULL UNIQUE,
    Description VARCHAR(100)
);

CREATE TABLE TransactionTypes (
    TypeID INT PRIMARY KEY AUTO_INCREMENT,
    TypeName VARCHAR(30) NOT NULL UNIQUE,
    Description VARCHAR(100),
    CommissionRate DECIMAL(5,2) DEFAULT 0.00
);

-- ========== MAIN TABLES ==========
CREATE TABLE Users (
    UserID INT PRIMARY KEY AUTO_INCREMENT,
    Username VARCHAR(50) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    FullName VARCHAR(100) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Phone VARCHAR(20),
    RoleID INT NOT NULL DEFAULT 2,
    WalletBalance DECIMAL(10,2) DEFAULT 0.00,
    IsActive BOOLEAN DEFAULT TRUE,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LastLogin TIMESTAMP NULL,
    FOREIGN KEY (RoleID) REFERENCES UserRoles(RoleID),
    CHECK (WalletBalance >= 0),
    CHECK (Email LIKE '%_@__%.__%')
);

CREATE TABLE Vehicles (
    VehicleID INT PRIMARY KEY AUTO_INCREMENT,
    RegistrationNo VARCHAR(20) NOT NULL UNIQUE,
    MakeID INT NOT NULL,
    Model VARCHAR(50) NOT NULL,
    Year INT NOT NULL,
    ColorID INT NOT NULL,
    DailyRate DECIMAL(8,2) NOT NULL,
    StatusID INT NOT NULL DEFAULT 1,
    OwnerID INT NOT NULL,
    IsUserListed BOOLEAN DEFAULT FALSE,
    Location VARCHAR(200) NOT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (MakeID) REFERENCES VehicleMakes(MakeID),
    FOREIGN KEY (ColorID) REFERENCES VehicleColors(ColorID),
    FOREIGN KEY (StatusID) REFERENCES VehicleStatuses(StatusID),
    FOREIGN KEY (OwnerID) REFERENCES Users(UserID),
    CHECK (Year >= 1900 AND Year <= YEAR(CURDATE()) + 1),
    CHECK (DailyRate > 0)
);

CREATE TABLE Rentals (
    RentalID INT PRIMARY KEY AUTO_INCREMENT,
    UserID INT NOT NULL,
    VehicleID INT NOT NULL,
    RentalDate DATE NOT NULL,
    ReturnDate DATE NOT NULL,
    TotalAmount DECIMAL(10,2) NOT NULL,
    StatusID INT NOT NULL DEFAULT 1,
    PaymentStatusID INT NOT NULL DEFAULT 1,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ApprovedBy INT NULL,
    ApprovedAt TIMESTAMP NULL,
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (VehicleID) REFERENCES Vehicles(VehicleID),
    FOREIGN KEY (StatusID) REFERENCES RentalStatuses(StatusID),
    FOREIGN KEY (PaymentStatusID) REFERENCES PaymentStatuses(StatusID),
    FOREIGN KEY (ApprovedBy) REFERENCES Users(UserID),
    CHECK (ReturnDate > RentalDate),
    CHECK (TotalAmount > 0)
);

CREATE TABLE Transactions (
    TransactionID INT PRIMARY KEY AUTO_INCREMENT,
    RentalID INT NULL,
    UserID INT NOT NULL,
    TypeID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    AdminCommission DECIMAL(10,2) DEFAULT 0.00,
    OwnerEarnings DECIMAL(10,2) DEFAULT 0.00,
    TransactionDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Status VARCHAR(20) DEFAULT 'PENDING',
    Description VARCHAR(500),
    FOREIGN KEY (RentalID) REFERENCES Rentals(RentalID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (TypeID) REFERENCES TransactionTypes(TypeID),
    CHECK (Amount >= 0)
);

CREATE TABLE SystemLogs (
    LogID INT PRIMARY KEY AUTO_INCREMENT,
    LogType VARCHAR(50) NOT NULL,
    LogMessage TEXT NOT NULL,
    UserID INT NULL,
    IPAddress VARCHAR(45),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- ========== INSERT DEFAULT DATA ==========
INSERT INTO UserRoles (RoleName, Description) VALUES
('ADMIN', 'System Administrator'),
('CUSTOMER', 'Regular Customer'),
('OWNER', 'Vehicle Owner');

INSERT INTO VehicleStatuses (StatusName, Description, IsAvailable) VALUES
('AVAILABLE', 'Available for rent', TRUE),
('RENTED', 'Currently rented', FALSE),
('MAINTENANCE', 'Under maintenance', FALSE),
('UNAVAILABLE', 'Temporarily unavailable', FALSE);

INSERT INTO VehicleMakes (MakeName, Country, EstablishedYear) VALUES
('Toyota', 'Japan', 1937),
('Honda', 'Japan', 1948),
('Ford', 'USA', 1903),
('BMW', 'Germany', 1916),
('Tesla', 'USA', 2003),
('Mercedes', 'Germany', 1926),
('Hyundai', 'South Korea', 1967);

INSERT INTO VehicleColors (ColorName, HexCode) VALUES
('White', '#FFFFFF'),
('Black', '#000000'),
('Blue', '#0000FF'),
('Silver', '#C0C0C0'),
('Red', '#FF0000'),
('Gray', '#808080');

INSERT INTO RentalStatuses (StatusName, Description) VALUES
('PENDING', 'Waiting for approval'),
('ACTIVE', 'Currently active'),
('COMPLETED', 'Successfully completed'),
('CANCELLED', 'Cancelled by user'),
('REJECTED', 'Rejected by admin');

INSERT INTO PaymentStatuses (StatusName, Description) VALUES
('PENDING', 'Payment pending'),
('PAID', 'Payment completed'),
('REFUNDED', 'Payment refunded'),
('FAILED', 'Payment failed');

INSERT INTO TransactionTypes (TypeName, Description, CommissionRate) VALUES
('USER_RENTAL', 'Rental of user-listed vehicle', 20.00),
('COMPANY_RENTAL', 'Rental of company vehicle', 100.00),
('WALLET_ADD', 'Adding money to wallet', 0.00),
('WALLET_PAYMENT', 'Payment from wallet', 0.00),
('REFUND', 'Refund transaction', 0.00);

-- Insert default admin user (password: admin123)
INSERT INTO Users (Username, PasswordHash, FullName, Email, Phone, RoleID, WalletBalance) VALUES
('admin', 'admin123', 'System Admin', 'admin@rental.com', '1234567890', 1, 1000.00);

-- Insert sample customers
INSERT INTO Users (Username, PasswordHash, FullName, Email, Phone, RoleID, WalletBalance) VALUES
('john', 'john123', 'John Doe', 'john@email.com', '9876543210', 2, 500.00),
('jane', 'jane123', 'Jane Smith', 'jane@email.com', '9876543211', 2, 750.00);

-- Insert sample vehicles
INSERT INTO Vehicles (RegistrationNo, MakeID, Model, Year, ColorID, DailyRate, StatusID, OwnerID, IsUserListed, Location) VALUES
('ABC123', 1, 'Corolla', 2022, 1, 30.00, 1, 1, FALSE, 'Downtown'),
('XYZ789', 2, 'Civic', 2023, 2, 35.00, 1, 1, FALSE, 'Airport'),
('DEF456', 3, 'Explorer', 2021, 3, 50.00, 1, 1, FALSE, 'City Center');

-- ========== STORED PROCEDURES ==========
DELIMITER $$

CREATE PROCEDURE sp_CreateRental(
    IN p_UserID INT,
    IN p_VehicleID INT,
    IN p_RentalDate DATE,
    IN p_ReturnDate DATE
)
BEGIN
    DECLARE v_DailyRate DECIMAL(8,2);
    DECLARE v_Days INT;
    DECLARE v_TotalAmount DECIMAL(10,2);
    DECLARE v_IsUserListed BOOLEAN;
    DECLARE v_OwnerID INT;
    DECLARE v_TransactionTypeID INT;
    DECLARE v_AdminCommission DECIMAL(10,2);
    DECLARE v_OwnerEarnings DECIMAL(10,2);
    
    START TRANSACTION;
    
    -- Check vehicle availability
    IF NOT EXISTS (SELECT 1 FROM Vehicles v 
                   JOIN VehicleStatuses vs ON v.StatusID = vs.StatusID 
                   WHERE v.VehicleID = p_VehicleID AND vs.IsAvailable = TRUE) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Vehicle not available';
    END IF;
    
    -- Get vehicle details
    SELECT DailyRate, IsUserListed, OwnerID INTO v_DailyRate, v_IsUserListed, v_OwnerID
    FROM Vehicles WHERE VehicleID = p_VehicleID;
    
    -- Calculate rental cost
    SET v_Days = DATEDIFF(p_ReturnDate, p_RentalDate);
    IF v_Days <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid rental period';
    END IF;
    
    SET v_TotalAmount = v_DailyRate * v_Days;
    
    -- Check user wallet balance
    IF (SELECT WalletBalance FROM Users WHERE UserID = p_UserID) < v_TotalAmount THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient wallet balance';
    END IF;
    
    -- Create rental
    INSERT INTO Rentals (UserID, VehicleID, RentalDate, ReturnDate, TotalAmount, StatusID)
    VALUES (p_UserID, p_VehicleID, p_RentalDate, p_ReturnDate, v_TotalAmount, 1);
    
    SET @NewRentalID = LAST_INSERT_ID();
    
    -- Update vehicle status
    UPDATE Vehicles SET StatusID = 2 WHERE VehicleID = p_VehicleID;
    
    -- Deduct from user wallet
    UPDATE Users SET WalletBalance = WalletBalance - v_TotalAmount WHERE UserID = p_UserID;
    
    -- Determine transaction type and commissions
    IF v_IsUserListed = TRUE THEN
        SET v_TransactionTypeID = 1; -- USER_RENTAL
        SET v_AdminCommission = v_TotalAmount * 0.2;
        SET v_OwnerEarnings = v_TotalAmount * 0.8;
    ELSE
        SET v_TransactionTypeID = 2; -- COMPANY_RENTAL
        SET v_AdminCommission = v_TotalAmount;
        SET v_OwnerEarnings = 0;
    END IF;
    
    -- Create transaction record
    INSERT INTO Transactions (RentalID, UserID, TypeID, Amount, AdminCommission, OwnerEarnings, Status, Description)
    VALUES (@NewRentalID, p_UserID, v_TransactionTypeID, v_TotalAmount, v_AdminCommission, v_OwnerEarnings, 'PENDING', 
            CONCAT('Rental for vehicle #', p_VehicleID));
    
    COMMIT;
    
    SELECT @NewRentalID AS RentalID, 'SUCCESS' AS Status, 'Rental created successfully' AS Message;
END$$

CREATE PROCEDURE sp_ApproveRental(
    IN p_RentalID INT,
    IN p_AdminUserID INT
)
BEGIN
    DECLARE v_RentalStatus VARCHAR(20);
    DECLARE v_VehicleID INT;
    DECLARE v_OwnerID INT;
    DECLARE v_OwnerEarnings DECIMAL(10,2);
    
    START TRANSACTION;
    
    -- Check rental status
    SELECT rs.StatusName, r.VehicleID INTO v_RentalStatus, v_VehicleID
    FROM Rentals r
    JOIN RentalStatuses rs ON r.StatusID = rs.StatusID
    WHERE r.RentalID = p_RentalID;
    
    IF v_RentalStatus != 'PENDING' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rental is not pending approval';
    END IF;
    
    -- Update rental status
    UPDATE Rentals 
    SET StatusID = 2, -- ACTIVE
        PaymentStatusID = 2, -- PAID
        ApprovedBy = p_AdminUserID,
        ApprovedAt = CURRENT_TIMESTAMP
    WHERE RentalID = p_RentalID;
    
    -- Update transaction status
    UPDATE Transactions 
    SET Status = 'COMPLETED'
    WHERE RentalID = p_RentalID;
    
    -- Get owner earnings
    SELECT OwnerEarnings, v.OwnerID INTO v_OwnerEarnings, v_OwnerID
    FROM Transactions t
    JOIN Rentals r ON t.RentalID = r.RentalID
    JOIN Vehicles v ON r.VehicleID = v.VehicleID
    WHERE r.RentalID = p_RentalID;
    
    -- Update owner wallet if user-listed vehicle
    IF v_OwnerEarnings > 0 AND v_OwnerID IS NOT NULL THEN
        UPDATE Users SET WalletBalance = WalletBalance + v_OwnerEarnings WHERE UserID = v_OwnerID;
    END IF;
    
    COMMIT;
    
    SELECT 'SUCCESS' AS Status, 'Rental approved and payments processed' AS Message;
END$$

DELIMITER ;

-- ========== VIEWS ==========
CREATE VIEW vw_AvailableVehicles AS
SELECT 
    v.VehicleID,
    v.RegistrationNo,
    vm.MakeName,
    v.Model,
    v.Year,
    vc.ColorName,
    v.DailyRate,
    vs.StatusName,
    CASE WHEN v.IsUserListed = 1 THEN u.FullName ELSE 'Company' END AS OwnerName,
    v.Location
FROM Vehicles v
JOIN VehicleMakes vm ON v.MakeID = vm.MakeID
JOIN VehicleColors vc ON v.ColorID = vc.ColorID
JOIN VehicleStatuses vs ON v.StatusID = vs.StatusID
LEFT JOIN Users u ON v.OwnerID = u.UserID
WHERE vs.IsAvailable = TRUE;

-- ========== TRIGGERS ==========
DELIMITER $$

CREATE TRIGGER trg_VehicleStatusChange
AFTER UPDATE ON Vehicles
FOR EACH ROW
BEGIN
    IF OLD.StatusID != NEW.StatusID THEN
        INSERT INTO SystemLogs (LogType, LogMessage, UserID)
        VALUES ('VEHICLE_STATUS_CHANGE', 
                CONCAT('Vehicle ', OLD.RegistrationNo, ' status changed'),
                NULL);
    END IF;
END$$

DELIMITER ;

-- ========== INDEXES ==========
CREATE INDEX idx_Users_Username ON Users(Username);
CREATE INDEX idx_Users_Email ON Users(Email);
CREATE INDEX idx_Vehicles_StatusID ON Vehicles(StatusID);
CREATE INDEX idx_Vehicles_RegistrationNo ON Vehicles(RegistrationNo);
CREATE INDEX idx_Rentals_UserID ON Rentals(UserID);
CREATE INDEX idx_Rentals_VehicleID ON Rentals(VehicleID);
CREATE INDEX idx_Rentals_StatusID ON Rentals(StatusID);
CREATE INDEX idx_Transactions_RentalID ON Transactions(RentalID);
CREATE INDEX idx_Transactions_UserID ON Transactions(UserID);