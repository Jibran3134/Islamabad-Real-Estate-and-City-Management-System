-- ============================================================
-- IRMS SEED DATA - Run this in SSMS after schema.sql
-- Adds: Users, Properties, Bidding Sessions, Bids
-- ============================================================
USE islamabad_real_estate;

-- ============================================================
-- 1. ADD MORE USERS (Agents, Buyers, Authority)
-- Existing: user_id 1 (Jibran Admin), 2 (Alishba Admin)
-- ============================================================
INSERT INTO Users (role_id, full_name, email, password_hash, status, phone_number) VALUES
    (1, 'Authority Officer',   'authority@irms.pk', 'pass123',  'Active', '0300-1111111'),
    (2, 'Ahmed Agent',         'ahmed@irms.pk',     'pass123',  'Active', '0301-2222222'),
    (2, 'Sara Agent',          'sara@irms.pk',      'pass123',  'Active', '0302-3333333'),
    (3, 'Ali Buyer',           'ali@irms.pk',       'pass123',  'Active', '0303-4444444'),
    (3, 'Fatima Buyer',        'fatima@irms.pk',    'pass123',  'Active', '0304-5555555'),
    (3, 'Hassan Buyer',        'hassan@irms.pk',    'pass123',  'Active', '0305-6666666'),
    (3, 'Ayesha Buyer',        'ayesha@irms.pk',    'pass123',  'Active', '0306-7777777'),
    (3, 'Omar Buyer',          'omar@irms.pk',      'pass123',  'Active', '0307-8888888');

-- ============================================================
-- 2. ADD MORE PROPERTIES (with location filled)
-- Existing: property_id 1,2
-- Agent IDs: 4 (Ahmed), 5 (Sara)
-- ============================================================
INSERT INTO Property (sector_id, agent_id, title, description, price, property_type, location, listing_status, selling_method, created_at) VALUES
    (7,  4, '5 Marla House F-6',        '3 bed, 2 bath, modern kitchen, garage',        15500000.00, 'Residential', 'F-6, Islamabad',       'For Sale', 'Fixed Price', '2026-02-01 09:00:00'),
    (8,  4, '10 Marla Plot F-7',         'Prime location corner plot near market',        28000000.00, 'Residential', 'F-7, Islamabad',       'For Sale', 'Auction',     '2026-02-05 10:00:00'),
    (8,  5, '1 Kanal House F-7',         'Luxury 5 bed house with pool and garden',       65000000.00, 'Residential', 'F-7/2, Islamabad',     'For Sale', 'Auction',     '2026-02-10 11:00:00'),
    (9,  4, 'Commercial Plaza F-8',      '4 floor commercial building, parking',          120000000.00,'Commercial',  'F-8 Markaz, Islamabad','For Sale', 'Auction',     '2026-02-15 09:30:00'),
    (12, 5, '8 Marla House G-6',         'Newly constructed, 4 bed, double storey',       22000000.00, 'Residential', 'G-6/1, Islamabad',     'For Sale', 'Fixed Price', '2026-02-20 14:00:00'),
    (13, 4, 'Shop G-7 Markaz',           'Ground floor shop in main market',              35000000.00, 'Commercial',  'G-7 Markaz, Islamabad','For Sale', 'Fixed Price', '2026-03-01 10:00:00'),
    (32, 5, 'Blue Area Office',          '2000 sqft office space, furnished',             45000000.00, 'Commercial',  'Blue Area, Islamabad', 'For Sale', 'Auction',     '2026-03-05 09:00:00'),
    (33, 4, 'DHA 1 Kanal Plot',          'Prime location in DHA Phase 1, sector A',       42000000.00, 'Residential', 'DHA Phase 1, Islamabad','For Sale','Auction',     '2026-03-10 11:00:00'),
    (34, 5, 'DHA Phase 2 House',         '7 bed luxury villa, swimming pool, lawn',       85000000.00, 'Residential', 'DHA Phase 2, Islamabad','For Sale','Auction',     '2026-03-12 10:00:00'),
    (35, 4, 'Bahria Town Villa',         'Luxury 6 bed villa with home theater',          55000000.00, 'Residential', 'Bahria Town Ph1, Islamabad','For Sale','Fixed Price','2026-03-15 09:00:00'),
    (37, 5, 'PWD Society Apartment',     '3 bed apartment, 5th floor, lift access',       12000000.00, 'Residential', 'PWD Housing, Islamabad','For Sale','Fixed Price', '2026-03-18 14:00:00'),
    (25, 4, 'Industrial Unit I-8',       'Warehouse + factory space, 10000 sqft',         75000000.00, 'Industrial',  'I-8 Industrial, Islamabad','For Sale','Auction',  '2026-03-20 09:00:00');

-- Also fix existing properties (add missing location)
UPDATE Property SET location = 'E-11, Islamabad' WHERE property_id = 1 AND location IS NULL;
UPDATE Property SET location = 'D-12, Islamabad' WHERE property_id = 2 AND location IS NULL;

-- ============================================================
-- 3. BIDDING SESSIONS (for Auction properties)
-- Properties with selling_method='Auction': 4,5,6,9,10,11,14
-- ============================================================
INSERT INTO Bidding_Session (property_id, base_price, deadline, status, winner_id, winning_bid_amount, current_highest_bid) VALUES
    (4,  28000000.00,  '2026-06-01 18:00:00', 'Active', NULL, NULL, 30000000.00),
    (5,  65000000.00,  '2026-06-05 18:00:00', 'Active', NULL, NULL, 72000000.00),
    (6,  120000000.00, '2026-06-10 18:00:00', 'Active', NULL, NULL, 125000000.00),
    (9,  45000000.00,  '2026-06-15 18:00:00', 'Active', NULL, NULL, 48000000.00),
    (10, 42000000.00,  '2026-06-20 18:00:00', 'Active', NULL, NULL, NULL),
    (11, 85000000.00,  '2026-05-25 18:00:00', 'Active', NULL, NULL, 90000000.00),
    (14, 75000000.00,  '2026-06-30 18:00:00', 'Active', NULL, NULL, NULL);
    SELECT * FROM Bidding_Session
-- ============================================================
-- 4. BIDS (sample bids for active sessions)
-- Buyer IDs: 6 (Ali), 7 (Fatima), 8 (Hassan), 9 (Ayesha), 10 (Omar)
-- ============================================================
INSERT INTO Bid (session_id, buyer_id, bid_amount, bid_time) VALUES
    -- Session 5 (10 Marla Plot F-7)
    (5, 6,  29000000.00, '2026-04-20 10:15:00'),
    (5, 7,  29500000.00, '2026-04-20 11:30:00'),
    (5, 8,  30000000.00, '2026-04-21 09:00:00'),

    -- Session 8 (1 Kanal House F-7)
    (8, 7,  68000000.00, '2026-04-22 10:00:00'),
    (8, 9,  70000000.00, '2026-04-22 14:00:00'),
    (8, 6,  72000000.00, '2026-04-23 09:30:00'),

    -- Session 3 (Commercial Plaza F-8)
    (3, 8,  122000000.00,'2026-04-24 11:00:00'),
    (3, 10, 125000000.00,'2026-04-25 10:00:00'),

    -- Session 4 (Blue Area Office)
    (4, 6,  46000000.00, '2026-04-26 09:00:00'),
    (4, 9,  47000000.00, '2026-04-26 14:00:00'),
    (4, 7,  48000000.00, '2026-04-27 10:00:00'),

    -- Session 6 (DHA Phase 2 House)
    (6, 10, 87000000.00, '2026-04-28 10:00:00'),
    (6, 8,  89000000.00, '2026-04-28 15:00:00'),
    (6, 6,  90000000.00, '2026-04-29 09:00:00');

-- ============================================================
-- 5. ACTIVITY LOG (sample entries)
-- ============================================================
INSERT INTO Activity_Log (user_id, action, entity_type, entity_id, details) VALUES
    (1, 'LOGIN',           'User',    1,  'Admin login successful'),
    (3, 'CAPACITY_UPDATE', 'Sector',  7,  'Sector F-6 capacity updated from 150 to 160'),
    (4, 'PROPERTY_LISTED', 'Property',3,  'New property listed: 5 Marla House F-6'),
    (6, 'BID_PLACED',      'Bid',     1,  'Bid of 29000000 placed on session 1'),
    (7, 'BID_PLACED',      'Bid',     2,  'Bid of 29500000 placed on session 1');

PRINT ' SEED DATA INSERTED SUCCESSFULLY!';
PRINT 'Users: 10 total (2 Admin, 1 Authority, 2 Agent, 5 Buyer)';
PRINT 'Properties: 14 total';
PRINT 'Bidding Sessions: 7 active';
PRINT 'Bids: 15 total';
