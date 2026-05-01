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

        int currentCount  = sector.getCurrentPropertyCount();
        int currentLimit  = sector.getCapacityLimit();

        // ── Step 1: Current usage % ──────────────────────────
        double currentUsagePct = currentLimit > 0
                ? (currentCount * 100.0) / currentLimit : 0;

        // ── Step 2: Proposed usage % after change ────────────
        double proposedUsagePct = proposedCapacity > 0
                ? (currentCount * 100.0) / proposedCapacity : 100;

        // ── Step 3: Risk Score (0=Safe, 100=Critical) ────────
        // Formula: weighted combo of usage%, proximity to overload,
        // and direction of change (decrease = more risky)
        int riskScore;
        if (proposedCapacity < currentCount) {
            riskScore = 100; // Immediate overload — critical
        } else if (proposedUsagePct >= 90) {
            riskScore = 85;
        } else if (proposedUsagePct >= 75) {
            riskScore = 60;
        } else if (proposedUsagePct >= 50) {
            riskScore = 35;
        } else {
            riskScore = 15;
        }

        // If reducing capacity, add 20 penalty points
        if (proposedCapacity < currentLimit) {
            riskScore = Math.min(100, riskScore + 20);
        }

        // ── Step 4: Risk Level Label ──────────────────────────
        String riskLevel;
        if      (riskScore >= 85) riskLevel = "🔴 CRITICAL";
        else if (riskScore >= 60) riskLevel = "🟠 HIGH";
        else if (riskScore >= 35) riskLevel = "🟡 MEDIUM";
        else                      riskLevel = "🟢 SAFE";

        // ── Step 5: Smart Recommendation ─────────────────────
        // Optimal capacity = current properties + 25% growth buffer
        int recommended = (int) Math.ceil(currentCount * 1.25);
        if (recommended < 10) recommended = 10; // minimum floor

        // ── Step 6: Advisory message ──────────────────────────
        StringBuilder advice = new StringBuilder();
        advice.append(String.format(
            "📊 Current: %d/%d (%.1f%% full)\n", currentCount, currentLimit, currentUsagePct));
        advice.append(String.format(
            "📈 After change: %d/%d (%.1f%% full)\n", currentCount, proposedCapacity, proposedUsagePct));
        advice.append(String.format(
            "⚠️  Risk Score: %d/100 — %s\n", riskScore, riskLevel));
        advice.append(String.format(
            "💡 Recommended safe capacity: %d (25%% growth buffer)\n", recommended));

        if (proposedCapacity < currentCount) {
            advice.append("❌ WARNING: Proposed capacity is BELOW current property count! Sector will be immediately overloaded.\n");
        } else if (proposedUsagePct >= 90) {
            advice.append("⚠️  WARNING: Sector will be nearly full. Consider setting higher capacity.\n");
        } else if (proposedCapacity > currentLimit * 2) {
            advice.append("ℹ️  NOTE: Large capacity increase detected. Verify this is intentional.\n");
        } else {
            advice.append("✅ Proposed capacity is within safe range.\n");
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
        int[] listingData = propertyRepository.countAndValueActiveListingsBySector(sectorId);
        int blockedListings   = listingData[0];
        BigDecimal blockedValue = new BigDecimal(listingData[1]);

        // Step 3: Count active bidding sessions on this sector's properties (NON-CRUD)
        int affectedBidSessions = propertyRepository.countActiveBiddingSessionsBySector(sectorId);

        // Step 4: Severity scoring formula
        // Factors: number of blocked listings, value tier, active auctions
        int severityScore = 0;
        if (blockedListings > 20)  severityScore += 40;
        else if (blockedListings > 10) severityScore += 25;
        else if (blockedListings > 5)  severityScore += 15;
        else if (blockedListings > 0)  severityScore += 5;

        // Value scoring (PKR tiers)
        double valueMillion = blockedValue.doubleValue() / 1_000_000.0;
        if (valueMillion > 500)       severityScore += 40;
        else if (valueMillion > 100)  severityScore += 25;
        else if (valueMillion > 50)   severityScore += 15;
        else if (valueMillion > 0)    severityScore += 5;

        // Active auctions are most critical
        severityScore += affectedBidSessions * 10;
        severityScore = Math.min(100, severityScore);

        // Step 5: Severity label
        String severity;
        if      (severityScore >= 75) severity = "🔴 SEVERE";
        else if (severityScore >= 50) severity = "🟠 HIGH";
        else if (severityScore >= 25) severity = "🟡 MEDIUM";
        else                          severity = "🟢 LOW";

        // Step 6: Impact report text
        boolean isOverloaded = sector.isOverloaded();
        double usagePct = sector.getCapacityLimit() > 0
            ? (sector.getCurrentPropertyCount() * 100.0) / sector.getCapacityLimit() : 0;

        StringBuilder report = new StringBuilder();
        report.append(String.format("🏘️  Sector: %s\n", sector.getSectorName()));
        report.append(String.format("📊 Current Usage: %d/%d (%.1f%%)\n",
            sector.getCurrentPropertyCount(), sector.getCapacityLimit(), usagePct));
        report.append(String.format("⚠️  Overloaded: %s\n", isOverloaded ? "YES" : "NO"));
        report.append("\n── FREEZE IMPACT ──\n");
        report.append(String.format("🚫 Listings to be BLOCKED: %d\n", blockedListings));
        report.append(String.format("💰 Total Value at Risk: PKR %.1fM\n", valueMillion));
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
