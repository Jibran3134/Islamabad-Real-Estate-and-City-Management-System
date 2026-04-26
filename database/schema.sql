CREATE DATABASE islamabad_real_estate;
USE islamabad_real_estate;
CREATE TABLE Role (
                      role_id INT PRIMARY KEY,
                      role_name VARCHAR(50)
);

CREATE TABLE Users (
                      user_id INT PRIMARY KEY IDENTITY(1,1),
                      role_id INT,
                      full_name VARCHAR(100),
                      email VARCHAR(100),
                      password_hash VARCHAR(255),
                      status VARCHAR(20),
                      phone_number VARCHAR(20),
                      FOREIGN KEY (role_id) REFERENCES Role(role_id)
);
CREATE TABLE Sector (
                        sector_id INT PRIMARY KEY IDENTITY(1,1),
                        sector_name VARCHAR(100),
                        capacity_limit INT,
                        current_count INT,
                        status VARCHAR(20)
);

CREATE TABLE Property (
                          property_id INT PRIMARY KEY IDENTITY(1,1),
                          sector_id INT,
                          agent_id INT,
                          title VARCHAR(150),
                          description TEXT,
                          price DECIMAL(12,2),
                          property_type VARCHAR(30),
                          location VARCHAR(200),
                          listing_status VARCHAR(20),
                          selling_method VARCHAR(20),
                          created_at DATETIME,
                          FOREIGN KEY (sector_id) REFERENCES Sector(sector_id),
                          FOREIGN KEY (agent_id) REFERENCES Users(user_id)
);

CREATE TABLE Bidding_Session (
                                 session_id INT PRIMARY KEY IDENTITY(1,1),
                                 property_id INT,
                                 base_price DECIMAL(12,2),
                                 deadline DATETIME,
                                 status VARCHAR(20),
                                 winner_id INT,
                                 winning_bid_amount DECIMAL(12,2),
                                 current_highest_bid DECIMAL(12,2),
                                 FOREIGN KEY (property_id) REFERENCES Property(property_id),
                                 FOREIGN KEY (winner_id) REFERENCES Users(user_id)
);

CREATE TABLE Bid (
                     bid_id INT PRIMARY KEY IDENTITY(1,1),
                     session_id INT,
                     buyer_id INT,
                     bid_amount DECIMAL(12,2),
                     bid_time DATETIME,
                     FOREIGN KEY (session_id) REFERENCES Bidding_Session(session_id),
                     FOREIGN KEY (buyer_id) REFERENCES Users(user_id)
);

CREATE TABLE Activity_Log (
                              log_id INT PRIMARY KEY IDENTITY(1,1),
                              user_id INT,
                              action VARCHAR(100),
                              entity_type VARCHAR(50),
                              entity_id INT,
                              timestamp DATETIME DEFAULT GETDATE(),
                              details TEXT,
                              FOREIGN KEY (user_id) REFERENCES Users(user_id)
);


-- INSERT STATEMENTS

-- Role (no IDENTITY — manual IDs)
INSERT INTO Role (role_id, role_name) VALUES
    (1, 'Authority'),
    (2, 'Agent'),
    (3, 'Buyer'),
    (4, 'Admin');

-- Sector (IDENTITY — omit sector_id, auto-generates 1 through 40)
INSERT INTO Sector (sector_name, capacity_limit, current_count, status) VALUES
    ('Sector A-11', 100, 12,  'Active'),
    ('Sector A-12', 120, 45,  'Active'),
    ('Sector B-17', 200, 198, 'Frozen'),
    ('Sector C-11', 150, 60,  'Active'),
    ('Sector D-12', 180, 90,  'Active'),
    ('Sector E-11', 130, 130, 'Frozen'),
    ('Sector F-6',  160, 20,  'Active'),
    ('Sector F-7',  160, 55,  'Active'),
    ('Sector F-8',  160, 75,  'Active'),
    ('Sector F-10', 140, 30,  'Active'),
    ('Sector F-11', 140, 140, 'Frozen'),
    ('Sector G-6',  170, 85,  'Active'),
    ('Sector G-7',  170, 110, 'Active'),
    ('Sector G-8',  170, 50,  'Active'),
    ('Sector G-9',  170, 40,  'Active'),
    ('Sector G-10', 170, 65,  'Active'),
    ('Sector G-11', 170, 170, 'Frozen'),
    ('Sector G-13', 150, 80,  'Active'),
    ('Sector H-8',  200, 100, 'Active'),
    ('Sector H-9',  200, 120, 'Active'),
    ('Sector H-10', 200, 145, 'Active'),
    ('Sector H-11', 200, 200, 'Frozen'),
    ('Sector H-12', 200, 90,  'Active'),
    ('Sector H-13', 180, 60,  'Active'),
    ('Sector I-8',  160, 35,  'Active'),
    ('Sector I-9',  160, 70,  'Active'),
    ('Sector I-10', 160, 95,  'Active'),
    ('Sector I-11', 160, 110, 'Active'),
    ('Sector I-14', 130, 25,  'Active'),
    ('Sector I-15', 130, 15,  'Active'),
    ('Sector I-16', 130, 10,  'Active'),
    ('Blue Area',   50,  48,  'Active'),
    ('DHA Phase 1', 300, 280, 'Active'),
    ('DHA Phase 2', 300, 150, 'Active'),
    ('Bahria Town Phase 1', 400, 390, 'Active'),
    ('Bahria Town Phase 2', 400, 200, 'Active'),
    ('PWD Housing Society', 250, 180, 'Active'),
    ('CBR Town',    220, 100, 'Active'),
    ('Naval Anchorage', 180, 90, 'Active'),
    ('Gulberg Islamabad', 200, 170, 'Active');

-- Users (IDENTITY — omit user_id, auto-generates 1, 2)
INSERT INTO Users (role_id, full_name, email, password_hash, status, phone_number) VALUES
    (4, 'Jibran Admin',  'jibran@irms.pk',  'adminpass', 'Active', '0300-0000001'),
    (4, 'Alishba Admin', 'alishba@irms.pk', 'adminpass', 'Active', '0300-0000001');

-- Property (IDENTITY — omit property_id, auto-generates 1, 2)
INSERT INTO Property (sector_id, agent_id, title, description, price, property_type, listing_status, selling_method, created_at) VALUES
    (6, 1, 'House in F-6', '5 Marla house with 3 beds',  15000000.00, 'Residential', 'For Sale', 'Fixed Price', '2026-01-10 09:00:00'),
    (5, 2, 'Plot in F-7',  '10 Marla residential plot',  22000000.00, 'Residential', 'For Sale', 'Bidding',     '2026-01-15 10:00:00');
