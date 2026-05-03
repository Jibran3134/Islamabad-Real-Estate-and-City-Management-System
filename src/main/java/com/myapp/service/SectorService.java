package com.myapp.service;

import com.myapp.model.Sector;
import com.myapp.repository.SectorRepository;
import com.myapp.repository.AuditLogRepository;
import com.myapp.repository.PropertyRepository;
import com.myapp.command.UpdateCapacityCommand;
import com.myapp.service.SectorCapacityValidator.ValidationResult;
import com.myapp.service.PermissionValidator.PermissionResult;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * BUSINESS LOGIC LAYER (SERVICE)
 * GRASP: HIGH COHESION - Only handles sector-related business logic
 * GRASP: LOW COUPLING - Talks only to repositories, never directly to database
 * OOP Principle: ABSTRACTION - Hides implementation details from controllers
 * 
 * Handles both UC1 (Define Sector Capacity) and UC2 (Freeze Overloaded Sector)
 */
public class SectorService {
    
    private final SectorRepository sectorRepository;
    private final AuditLogRepository auditLogRepository;
    private final PropertyRepository propertyRepository;
    private final SectorCapacityValidator validator;
    private final PermissionValidator permissionValidator;  // UC2: Singleton + Pure Fabrication
    
    public SectorService() {
        this.sectorRepository = new SectorRepository();
        this.auditLogRepository = new AuditLogRepository();
        this.propertyRepository = new PropertyRepository();
        this.validator = new SectorCapacityValidator();          // GoF STRATEGY (UC1)
        this.permissionValidator = PermissionValidator.getInstance(); // GoF SINGLETON (UC2)
    }
    
    // Returns all sectors - Pass-through to repository
    public List<Sector> getAllSectors() throws SQLException {
        return sectorRepository.findAllSectors();
    }
    
    // Returns single sector by ID - With input validation
    public Sector getSectorById(int sectorId) throws SQLException {
        if (sectorId <= 0) {
            throw new IllegalArgumentException("Invalid sector ID");
        }
        return sectorRepository.findSectorById(sectorId);
    }
    
    /**
     * UC2 SD1: Get all sectors with usage statistics for dashboard
     * Returns list of SectorStatistics with usage percentage
     */
    public List<SectorRepository.SectorStatistics> getSectorStatistics() throws SQLException {
        return sectorRepository.getAllSectorsWithUsage();
    }
    
    // ========== UC1: NON-CRUD — SMART CAPACITY ADVISOR ==========

    /**
     * NON-CRUD: analyzeCapacity()
     *
     * This method does NOT just read/write data.
     * It performs BUSINESS INTELLIGENCE calculations:
     *   1. Calculates current usage percentage
     *   (Guard: proposedCapacity <= 0 returns safe advisory with recommended value)
     *   2. Calculates risk score (0–100)
     *   3. Auto-recommends the optimal safe capacity
     *   4. Predicts new usage % after the proposed change
     *   5. Issues warnings if proposed capacity is unsafe
     *
     * This is NON-CRUD because:
     *  - It computes derived values not stored in DB
     *  - It applies a domain-specific scoring formula
     *  - It generates recommendations based on business rules
     *  - It predicts future state (not just current state)
     *
     * GRASP: INFORMATION EXPERT — SectorService has all sector data
     * GRASP: PURE FABRICATION — advisory logic has no DB counterpart
     */
    public CapacityAdvisory analyzeCapacity(int sectorId, int proposedCapacity) throws SQLException {
        Sector sector = sectorRepository.findSectorById(sectorId);
        if (sector == null) {
            return new CapacityAdvisory(false, "Sector not found.", 0, 0, 0, "UNKNOWN", "");
        }

        int currentCount = sector.getCurrentPropertyCount();
        int currentLimit = sector.getCapacityLimit();

        // ── Step 1: Current usage % ────────────────────────────────────────────
        double currentUsagePct = currentLimit > 0
                ? (currentCount * 100.0) / currentLimit : 0;

        // ── Step 2: Proposed usage % after change ─────────────────────────────
        // "If we set the limit to proposedCapacity, what % full will this sector be?"
        double proposedUsagePct = proposedCapacity > 0
                ? (currentCount * 100.0) / proposedCapacity : 100.0;

        // ── Step 3: Risk Score (real-estate-aware thresholds) ─────────────────
        //
        // Example: sector has limit=150, count=100 (currently 66.7% full)
        //
        //  Proposed → 120  : proposed usage = 100/120 = 83.3%  → base=85 (≥80%)
        //                    + reducing penalty (+25)            → 100  → 🔴 CRITICAL
        //
        //  Proposed → 130  : proposed usage = 100/130 = 76.9%  → base=65 (≥70%)
        //                    + reducing penalty (+25)            → 90   → 🔴 CRITICAL
        //
        //  Proposed → 150  : proposed usage = 100/150 = 66.7%  → base=50 (≥60%)
        //                    same capacity, no penalty           → 50   → 🟠 HIGH
        //
        //  Proposed → 175  : proposed usage = 100/175 = 57.1%  → base=30 (≥45%)
        //                    increasing, no penalty              → 30   → 🟡 MEDIUM
        //
        //  Proposed → 250  : proposed usage = 100/250 = 40.0%  → base=10 (<45%)
        //                    large increase, no penalty          → 10   → 🟢 SAFE
        //
        // Thresholds (tighter than generic — reflects real estate scarcity):
        //   ≥ 80% proposed full  →  base 85  (only 20% slots left — near overload)
        //   ≥ 70% proposed full  →  base 65  (only 30% slots left — very tight)
        //   ≥ 60% proposed full  →  base 50  (still workable but watch out)
        //   ≥ 45% proposed full  →  base 30  (healthy buffer)
        //   <  45% proposed full →  base 10  (very comfortable)

        int riskScore;
        if (proposedCapacity < currentCount) {
            riskScore = 100; // Sector is ALREADY overloaded by this limit
        } else if (proposedUsagePct >= 80) {
            riskScore = 85;
        } else if (proposedUsagePct >= 70) {
            riskScore = 65;
        } else if (proposedUsagePct >= 60) {
            riskScore = 50;
        } else if (proposedUsagePct >= 45) {
            riskScore = 30;
        } else {
            riskScore = 10;
        }

        // Reduction penalty: lowering the limit shrinks the safety buffer.
        // +25 points if reducing capacity (on top of base score).
        // This ensures: limit=150→120 with count=100 → 83.3% + penalty → CRITICAL.
        if (proposedCapacity < currentLimit) {
            riskScore = Math.min(100, riskScore + 25);
        }

        // ── Step 4: Risk Level Label ───────────────────────────────────────────
        String riskLevel;
        if      (riskScore >= 85) riskLevel = "🔴 CRITICAL";
        else if (riskScore >= 65) riskLevel = "🟠 HIGH";
        else if (riskScore >= 40) riskLevel = "🟡 MEDIUM";
        else                      riskLevel = "🟢 SAFE";

        // ── Step 5: Smart Recommendation ──────────────────────────────────────
        // Rule 1: Always keep at least a 35% growth buffer above current count.
        // Rule 2: NEVER recommend going below the current limit (don't suggest
        //         shrinking a sector that is already functioning).
        //
        // Example: count=100, limit=150 → ceil(100×1.35)=135, max(150,135)=150
        //   → recommends keeping at 150, not reducing to 135.
        //
        // Example: count=140, limit=150 → ceil(140×1.35)=189, max(150,189)=189
        //   → recommends increasing to 189 because sector is getting full.
        int recommended = (int) Math.ceil(currentCount * 1.35);
        if (recommended < 10) recommended = 10; // absolute minimum floor
        // Do not recommend reducing below current limit
        if (recommended < currentLimit) recommended = currentLimit;

        // ── Step 6: Advisory text ──────────────────────────────────────────────
        boolean isReducing  = proposedCapacity < currentLimit;
        int     slotsLeft   = proposedCapacity - currentCount;
        double  headroomPct = proposedCapacity > 0
                ? (slotsLeft * 100.0) / proposedCapacity : 0;

        StringBuilder advice = new StringBuilder();
        advice.append(String.format(
            "📊 Current:       %d / %d slots used (%.1f%% full)\n",
            currentCount, currentLimit, currentUsagePct));
        advice.append(String.format(
            "📦 Proposed:      %d / %d slots used (%.1f%% full)\n",
            currentCount, proposedCapacity, proposedUsagePct));
        advice.append(String.format(
            "🪟 Headroom left: %d slots (%.1f%% free)\n",
            Math.max(0, slotsLeft), Math.max(0, headroomPct)));
        advice.append(String.format(
            "⚠️  Risk Score:    %d / 100 — %s\n", riskScore, riskLevel));
        advice.append(String.format(
            "💡 Recommended:   %d slots (35%% growth buffer, never below current)\n\n",
            recommended));

        // Context-specific warnings
        if (proposedCapacity < currentCount) {
            advice.append("❌ OVERLOAD: Proposed limit is BELOW current property count!\n");
            advice.append("   The sector will be immediately over-capacity.\n");
        } else if (isReducing && proposedUsagePct >= 80) {
            advice.append("🔴 CRITICAL: You are REDUCING capacity on a sector that is already\n");
            advice.append("   " + String.format("%.0f%%", currentUsagePct) + " full. After this change it will be ");
            advice.append(String.format("%.0f%%", proposedUsagePct) + "% full, leaving only " + slotsLeft + " slot(s).\n");
            advice.append("   Apply the recommended capacity of " + recommended + " instead.\n");
        } else if (isReducing && proposedUsagePct >= 60) {
            advice.append("🟠 HIGH: Reducing capacity will leave very little headroom.\n");
            advice.append("   Consider keeping at " + currentLimit + " or using recommended: " + recommended + ".\n");
        } else if (isReducing) {
            advice.append("🟡 Reducing capacity. Headroom remains acceptable (" + String.format("%.0f%%", headroomPct) + " free).\n");
        } else if (proposedUsagePct >= 80) {
            advice.append("🔴 WARNING: Even without reducing, this limit leaves only\n");
            advice.append("   " + slotsLeft + " slot(s). Consider the recommended capacity of " + recommended + ".\n");
        } else if (proposedCapacity > currentLimit * 2) {
            advice.append("ℹ️  Large increase detected (more than double current limit).\n");
            advice.append("   Verify this is intentional.\n");
        } else {
            advice.append("✅ Proposed capacity provides " + String.format("%.0f%%", headroomPct) + "% headroom. Safe to proceed.\n");
        }

        return new CapacityAdvisory(
            proposedCapacity >= currentCount,
            advice.toString(),
            riskScore,
            recommended,
            (int) Math.round(proposedUsagePct),
            riskLevel,
            sector.getSectorName()
        );
    }


    /**
     * Result class for Smart Capacity Advisor (NON-CRUD output)
     */
    public static class CapacityAdvisory {
        private final boolean safe;
        private final String advisoryText;
        private final int riskScore;
        private final int recommendedCapacity;
        private final int projectedUsagePct;
        private final String riskLevel;
        private final String sectorName;

        public CapacityAdvisory(boolean safe, String advisoryText, int riskScore,
                                int recommendedCapacity, int projectedUsagePct,
                                String riskLevel, String sectorName) {
            this.safe = safe;
            this.advisoryText = advisoryText;
            this.riskScore = riskScore;
            this.recommendedCapacity = recommendedCapacity;
            this.projectedUsagePct = projectedUsagePct;
            this.riskLevel = riskLevel;
            this.sectorName = sectorName;
        }

        public boolean isSafe()               { return safe; }
        public String getAdvisoryText()        { return advisoryText; }
        public int getRiskScore()              { return riskScore; }
        public int getRecommendedCapacity()    { return recommendedCapacity; }
        public int getProjectedUsagePct()      { return projectedUsagePct; }
        public String getRiskLevel()           { return riskLevel; }
        public String getSectorName()          { return sectorName; }
    }

    // ========== UC2: NON-CRUD — FREEZE IMPACT ANALYSIS ==========

    /**
     * NON-CRUD: analyzeFreezeImpact(sectorId)
     *
     * Calculates the FULL IMPACT of freezing a sector BEFORE it happens.
     * Does NOT modify any data — pure analysis only.
     *
     * Computes:
     *   1. Number of active "For Sale" listings that will be blocked
     *   2. Total monetary value of those blocked listings (PKR)
     *   3. Number of active bidding sessions that will be affected
     *   4. Severity score: LOW / MEDIUM / HIGH / SEVERE
     *   5. Human-readable impact report
     *
     * NON-CRUD because:
     *   - Computes derived aggregates (COUNT, SUM) not stored in DB
     *   - Applies domain-specific severity scoring formula
     *   - Generates a predictive report of future state
     *   - Reads from multiple tables (Sector, Property, Bidding_Session)
     *
     * GRASP: INFORMATION EXPERT — SectorService coordinates multi-table read
     * GRASP: PURE FABRICATION — impact scoring has no direct DB equivalent
     */
    public FreezeImpact analyzeFreezeImpact(int sectorId) throws SQLException {
        // Step 1: Get sector info
        Sector sector = sectorRepository.findSectorById(sectorId);
        if (sector == null) {
            return new FreezeImpact(false, "Sector not found.", 0, BigDecimal.ZERO, 0, "UNKNOWN", "");
        }

        // Step 2: Count & value active listings in this sector (NON-CRUD query)
        double[] listingData = propertyRepository.countAndValueActiveListingsBySector(sectorId);
        int blockedListings   = (int) listingData[0];

        // ── NEW LOGIC: Impact is based on WASTED SPACE (Lost Potential Revenue) ──
        // "more empty space = more amount lost"
        int capacity = sector.getCapacityLimit();
        int used = sector.getCurrentPropertyCount();
        int wastedSlots = Math.max(0, capacity - used);
        
        // Calculate lost potential revenue based on wasted slots.
        // Assuming average 2.5 Crore (25,000,000 PKR) potential per empty slot.
        double lostPotentialValue = wastedSlots * 25_000_000.0;
        BigDecimal blockedValue = BigDecimal.valueOf(lostPotentialValue);

        // Step 3: Count active bidding sessions on this sector's properties (NON-CRUD)
        int affectedBidSessions = propertyRepository.countActiveBiddingSessionsBySector(sectorId);

        // Step 4: Severity scoring formula
        int severityScore = 0;
        if (blockedListings > 20)  severityScore += 40;
        else if (blockedListings > 10) severityScore += 25;
        else if (blockedListings > 5)  severityScore += 15;
        else if (blockedListings > 0)  severityScore += 5;

        // Value scoring (PKR tiers - based on Millions of lost potential)
        double valueMillion = blockedValue.doubleValue() / 1_000_000.0;
        if (valueMillion > 500)       severityScore += 40;
        else if (valueMillion > 100)  severityScore += 25;
        else if (valueMillion > 50)   severityScore += 15;
        else if (valueMillion > 0)    severityScore += 5;

        // Active auctions are most critical
        severityScore += affectedBidSessions * 10;
        severityScore = Math.min(100, severityScore);

        // Formatting PKR logically into Crores/Lakhs
        String formattedValue;
        if (lostPotentialValue >= 10_000_000) {
            formattedValue = String.format("PKR %.2f Crore", lostPotentialValue / 10_000_000.0);
        } else if (lostPotentialValue >= 100_000) {
            formattedValue = String.format("PKR %.2f Lakh", lostPotentialValue / 100_000.0);
        } else {
            formattedValue = String.format("PKR %,.0f", lostPotentialValue);
        }

        // Step 5: Severity label
        String severity;
        if      (severityScore >= 75) severity = "🔴 SEVERE";
        else if (severityScore >= 50) severity = "🟠 HIGH";
        else if (severityScore >= 25) severity = "🟡 MEDIUM";
        else                          severity = "🟢 LOW";

        // Step 6: Impact report text
        boolean isOverloaded = sector.isOverloaded();
        double usagePct = capacity > 0 ? (used * 100.0) / capacity : 0;

        StringBuilder report = new StringBuilder();
        report.append(String.format("🏘️  Sector: %s\n", sector.getSectorName()));
        report.append(String.format("📊 Current Usage: %d/%d (%.1f%%)\n", used, capacity, usagePct));
        report.append(String.format("⚠️  Overloaded: %s\n", isOverloaded ? "YES" : "NO"));
        report.append("\n── FREEZE IMPACT ──\n");
        report.append(String.format("🚫 Listings to be BLOCKED: %d\n", blockedListings));
        
        // Replaced "Total Value at Risk" with "Lost Potential Revenue"
        report.append(String.format("💰 Lost Potential Revenue: %s\n", formattedValue));
        report.append(String.format("   (Calculated based on %d empty slots × 2.5 Crore avg market value)\n", wastedSlots));
        
        report.append(String.format("🔨 Active Auctions Affected: %d\n", affectedBidSessions));
        report.append(String.format("📈 Impact Severity: %d/100 — %s\n", severityScore, severity));

        if (severityScore >= 75) {
            report.append("\n❌ CRITICAL: Freezing will block major active listings and auctions.\n");
            report.append("   Recommend resolving overload before freezing.\n");
        } else if (severityScore >= 50) {
            report.append("\n⚠️  HIGH IMPACT: Multiple listings will be blocked.\n");
            report.append("   Notify affected agents and buyers before proceeding.\n");
        } else if (blockedListings == 0 && affectedBidSessions == 0) {
            report.append("\n✅ No active listings or auctions. Safe to freeze.\n");
        } else {
            report.append("\n⚠️  Some listings will be affected. Proceed with caution.\n");
        }

        return new FreezeImpact(
            true, report.toString(), blockedListings, blockedValue,
            affectedBidSessions, severity, sector.getSectorName()
        );
    }

    /**
     * Result class for Freeze Impact Analysis (NON-CRUD output)
     */
    public static class FreezeImpact {
        private final boolean found;
        private final String reportText;
        private final int blockedListings;
        private final BigDecimal blockedValue;
        private final int affectedBidSessions;
        private final String severity;
        private final String sectorName;

        public FreezeImpact(boolean found, String reportText, int blockedListings,
                            BigDecimal blockedValue, int affectedBidSessions,
                            String severity, String sectorName) {
            this.found = found;
            this.reportText = reportText;
            this.blockedListings = blockedListings;
            this.blockedValue = blockedValue;
            this.affectedBidSessions = affectedBidSessions;
            this.severity = severity;
            this.sectorName = sectorName;
        }

        public boolean isFound()               { return found; }
        public String getReportText()           { return reportText; }
        public int getBlockedListings()         { return blockedListings; }
        public BigDecimal getBlockedValue()     { return blockedValue; }
        public int getAffectedBidSessions()     { return affectedBidSessions; }
        public String getSeverity()             { return severity; }
        public String getSectorName()           { return sectorName; }
    }

    // ========== UC1: DEFINE SECTOR CAPACITY LIMITS (CRUD part) ==========
    
    /**
     * Main business logic for UC1 - Define Sector Capacity Limits
     * GRASP: PROTECTED VARIATIONS - Changes to validation won't affect this method
     * 
     * SD4 FIX: After DB update, sector.setCapacityLimit() is called to keep
     *          the Java object in sync with the database (SD4 step 6-7).
     * ALT SCENARIO 4 FIX: handleSystemCrash() wraps the entire operation
     *          and calls restoreLastSavedData() on unexpected failures.
     */
    public UpdateResult updateSectorCapacity(int sectorId, int newCapacity, int authorityId) throws SQLException {
        
        // NFR - Robustness: validate inputs and preserve old capacity for recovery messages.
        // STEP 1: Verify sector exists (INFORMATION EXPERT delegates to repository)
        Sector sector = sectorRepository.findSectorById(sectorId);
        if (sector == null) {
            return new UpdateResult(false, "Sector not found", null);
        }
        
        // Save old capacity for rollback/logging (ALT SCENARIO 4: restoreLastSavedData)
        int oldCapacity = sector.getCapacityLimit();
        
        // STEP 2: Validate new capacity (GoF STRATEGY pattern in action)
        ValidationResult validation = validator.validate(newCapacity);
        if (!validation.isValid()) {
            return new UpdateResult(false, validation.getErrorMessage(), null);
        }
        
        // STEP 3: Create and execute command (GRASP CREATOR pattern)
        // Wrapped in crash handler (ALT SCENARIO 4)
        try {
            UpdateCapacityCommand command = new UpdateCapacityCommand(sectorId, newCapacity, sectorRepository);
            boolean success = command.execute();
            
            if (success) {
                // SD4 FIX (STEP 6): Update the in-memory Sector object
                // This matches SD4: SectorRepository → Sector.setCapacityLimit()
                sector.setCapacityLimit(newCapacity);
                
                // NFR - Auditability: log the capacity change for accountability.
                // STEP 5: Log the update (PURE FABRICATION)
                String logMessage = "CAPACITY_UPDATED from " + oldCapacity + " to " + newCapacity;
                auditLogRepository.logUpdate(sectorId, authorityId, logMessage);
                
                return new UpdateResult(true, "Capacity updated successfully", newCapacity);
            } else {
                return new UpdateResult(false, "Database update failed, please try again", null);
            }
            
        } catch (SQLException e) {
            // ALT SCENARIO 4: handleSystemCrash() — catch unexpected DB failures
            return handleSystemCrash(sectorId, oldCapacity, e);
        } catch (Exception e) {
            // ALT SCENARIO 4: Catch any unexpected runtime crash
            return handleSystemCrash(sectorId, oldCapacity, e);
        }
    }
    
    // ========== UC2: FREEZE OVERLOADED SECTOR ==========
    
    /**
     * UC2 Main business logic - Freeze Overloaded Sector
     * 
     * SD Flow:
     *   SD1: Authority opens dashboard → getSectorStatistics()
     *   SD2: Authority selects overloaded sector → getSectorById()
     *   SD3: System validates permissions → PermissionValidator.checkPermission()
     *   SD4: System freezes sector → SectorRepository.freezeSector() with transaction
     *   SD5: System logs action → AuditLogRepository.logUpdate()
     * 
     * Extensions:
     *   1. Sector not overloaded → warning + override option
     *   2. Permission denied → reject request
     *   3. DB failure → rollback transaction (handled in repository)
     *   4. System crash → handleSystemCrash() + restoreLastSavedData()
     * 
     * @param sectorId - ID of sector to freeze
     * @param authorityId - ID of logged-in authority
     * @param overrideWarning - true if authority wants to freeze non-overloaded sector
     * @return FreezeResult with success/failure and message
     */
    public FreezeResult freezeSector(int sectorId, int authorityId, boolean overrideWarning) throws SQLException {
        
        // STEP 1: Get sector details (INFORMATION EXPERT delegates to repository)
        Sector sector = sectorRepository.findSectorById(sectorId);
        if (sector == null) {
            return new FreezeResult(false, "Sector not found", false);
        }
        
        // Check if already frozen
        if (sector.isFrozen()) {
            return new FreezeResult(false, "Sector is already FROZEN!", false);
        }
        
        // STEP 2: Check if sector is overloaded (UC2 Extension 1 - warning)
        boolean isOverloaded = sectorRepository.isSectorOverloaded(sectorId);
        if (!isOverloaded && !overrideWarning) {
            // UC2 Extension 1: Sector not overloaded - show warning, allow override
            return new FreezeResult(false, 
                "WARNING: Sector '" + sector.getSectorName() + "' is not overloaded (" + 
                sector.getCurrentPropertyCount() + "/" + sector.getCapacityLimit() + 
                "). Use override to freeze anyway.", true);
        }
        
        // NFR - Security/Access Control: only allowed authority/admin users can freeze sectors.
        // STEP 3: Validate authority permissions (SD3 - Pure Fabrication + Singleton)
        PermissionResult permission = permissionValidator.checkPermission(authorityId, "FREEZE_SECTOR");
        if (!permission.isGranted()) {
            // UC2 Extension 2: Permission invalid - deny request
            return new FreezeResult(false, permission.getMessage(), false);
        }
        
        // STEP 4: Freeze sector in database with transaction (SD4 step 4.1 + 4.2)
        // Wrapped in crash handler (ALT SCENARIO 4)
        try {
            boolean success = sectorRepository.freezeSector(sectorId);
            
            if (success) {
                // SD4 FIX: Update Java object too (same fix as UC1)
                sector.freeze();
                
                // NFR - Auditability: record the regulatory freeze action.
                // STEP 5: Log the regulatory action (PURE FABRICATION)
                String logMessage = "SECTOR_FROZEN - Sector '" + sector.getSectorName() + 
                    "' status changed to FROZEN. Property listings blocked.";
                auditLogRepository.logUpdate(sectorId, authorityId, logMessage);
                
                return new FreezeResult(true, 
                    "Sector '" + sector.getSectorName() + "' frozen successfully. Property listings are now blocked.", false);
            } else {
                // UC2 Extension 3: Database update fails (rollback handled in repository)
                return new FreezeResult(false, "Database update failed. Operation cancelled.", false);
            }
            
        } catch (SQLException e) {
            // ALT SCENARIO 4: handleSystemCrash() for freeze operation
            System.err.println("[CRASH HANDLER] System error during freeze: " + e.getMessage());
            Sector restored = restoreLastSavedData(sectorId);
            if (restored != null) {
                return new FreezeResult(false, 
                    "System error occurred. Data restored. Current status: " + restored.getStatus() + 
                    ". Please try again.", false);
            }
            return new FreezeResult(false, 
                "System error occurred. Could not verify data state. Please contact administrator.", false);
        }
    }
    
    // ========== SHARED: CRASH HANDLING (UC1 + UC2) ==========
    
    /**
     * ALT SCENARIO 4: handleSystemCrash()
     * Called when an unexpected error occurs during any operation.
     * Attempts to restore the last known good state from the database.
     * Maps to Extended UC1/UC2 Extension 4: "system shows error and restores last saved data"
     */
    private UpdateResult handleSystemCrash(int sectorId, int previousCapacity, Exception e) {
        System.err.println("[CRASH HANDLER] System error during capacity update: " + e.getMessage());
        
        // Attempt to restore last saved data from database
        Sector restoredSector = restoreLastSavedData(sectorId);
        
        if (restoredSector != null) {
            System.err.println("[CRASH HANDLER] Restored sector data from database. " +
                "Current capacity in DB: " + restoredSector.getCapacityLimit());
            return new UpdateResult(false, 
                "System error occurred. Data restored to last saved state (capacity: " + 
                restoredSector.getCapacityLimit() + "). Please try again.", null);
        } else {
            System.err.println("[CRASH HANDLER] Could not restore sector data. Previous capacity was: " + previousCapacity);
            return new UpdateResult(false, 
                "System error occurred. Could not verify data state. " +
                "Previous capacity was: " + previousCapacity + ". Please contact administrator.", null);
        }
    }
    
    /**
     * ALT SCENARIO 4: restoreLastSavedData()
     * Re-fetches sector from database to verify/restore consistent state.
     * Since SectorRepository uses transaction rollback (Alt Scenario 3),
     * the DB state is always consistent — this method confirms it.
     */
    private Sector restoreLastSavedData(int sectorId) {
        try {
            // Re-read from database — this is the "last saved data"
            // Since transactions roll back on failure, DB is always in a valid state
            return sectorRepository.findSectorById(sectorId);
        } catch (SQLException ex) {
            System.err.println("[RESTORE FAILED] Cannot read from database: " + ex.getMessage());
            return null;
        }
    }
    
    // ========== RESULT WRAPPER CLASSES ==========
    
    /**
     * UC1 Result wrapper class
     * HIGH COHESION - Groups success status with message and data
     * OOP ENCAPSULATION: callers receive a clear result object instead of multiple loose values.
     */
    public static class UpdateResult {
        private final boolean success;
        private final String message;
        private final Integer newCapacity;
        
        public UpdateResult(boolean success, String message, Integer newCapacity) {
            this.success = success;
            this.message = message;
            this.newCapacity = newCapacity;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Integer getNewCapacity() { return newCapacity; }
    }
    
    /**
     * UC2 Result wrapper class
     * HIGH COHESION - Groups freeze result with override flag
     * OOP ENCAPSULATION: success, message, and override warning are kept together.
     */
    public static class FreezeResult {
        private final boolean success;
        private final String message;
        private final boolean needsOverride;  // warning for non-overloaded sector
        
        public FreezeResult(boolean success, String message, boolean needsOverride) {
            this.success = success;
            this.message = message;
            this.needsOverride = needsOverride;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public boolean needsOverride() { return needsOverride; }
    }
}
