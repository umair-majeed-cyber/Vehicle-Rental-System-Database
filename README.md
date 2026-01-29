
Vehicle Rental System Database
Overview
A MySQL database for a peer-to-peer vehicle rental platform supporting user roles, vehicle listings, rental bookings, and payment processing with commission tracking.

Key Features
User Management: Admin, Customer, and Owner roles

Vehicle Listings: Company-owned and user-listed vehicles

Rental System: Complete booking workflow with approval

Wallet System: Internal payment processing

Commission Tracking: 20% commission for user-listed vehicles

Audit Logs: System activity tracking

Core Tables
Users - User accounts with wallet balance

Vehicles - Vehicle details and availability

Rentals - Booking records

Transactions - Financial records

SystemLogs - Activity audit trail

Technical Components
Stored Procedures: sp_CreateRental, sp_ApproveRental

Views: vw_AvailableVehicles

Triggers: trg_VehicleStatusChange

Constraints: PK, FK, Check, Unique, Not Null

Indexes: Optimized query performance

Quick Start
Execute the SQL script in MySQL Workbench

Connect to VehicleRentalDB

Use test accounts:

Admin: admin / admin123

Customer: john / john123.
