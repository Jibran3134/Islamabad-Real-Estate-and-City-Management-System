package com.myapp.service;

/**
 * GRASP: Pure Fabrication - Is class ka koi domain equivalent nahi hai
 * GoF: Singleton Pattern - Sirf ek instance poori app mein
 * 
 * UC2 SD3: validateAuthorityPermissions()
 * Saari permission-checking logic yahan centralised hai (High Cohesion)
 */
public class PermissionValidator {
    
    // Singleton instance
    private static PermissionValidator instance;
    
    // Private constructor for Singleton
    private PermissionValidator() {}
    
    /**
     * GoF SINGLETON: getInstance()
     * Thread-safe double-checked locking
     */
    public static PermissionValidator getInstance() {
        if (instance == null) {
            synchronized (PermissionValidator.class) {
                if (instance == null) {
                    instance = new PermissionValidator();
                }
            }
        }
        return instance;
    }
    
    /**
     * UC2 SD3: validateAuthorityPermissions()
     * Checks if authority has permission to perform the given action
     * 
     * SD3 step 3.1: lookupRole(authorityId)
     * SD3 step 3.2: PermissionResult(granted: true/false)
     * 
     * @param authorityId - ID of the logged-in user
     * @param action - Action being performed (e.g. "FREEZE_SECTOR")
     * @return PermissionResult with granted status and message
     */
    public PermissionResult checkPermission(int authorityId, String action) {
        // SD3 step 3.1: lookupRole(authorityId)
        String userRole = getUserRole(authorityId);
        
        // SD3 step 3.2: PermissionResult(granted: true/false)
        boolean granted = false;
        String message = "";
        
        switch (action.toUpperCase()) {
            case "FREEZE_SECTOR":
                // Sirf City Real Estate Authority freeze kar sakta hai
                if ("AUTHORITY".equals(userRole) || "ADMIN".equals(userRole)) {
                    granted = true;
                    message = "Permission granted for FREEZE_SECTOR";
                } else {
                    // SD3 alt [permission denied] step
                    message = "Access Denied. Insufficient permissions. Only Authority can freeze sectors.";
                }
                break;
                
            case "UPDATE_CAPACITY":
                // Authority and Admin can update capacity
                if ("AUTHORITY".equals(userRole) || "ADMIN".equals(userRole)) {
                    granted = true;
                    message = "Permission granted for UPDATE_CAPACITY";
                } else {
                    message = "Access Denied. Only Authority can update sector capacity.";
                }
                break;
                
            default:
                message = "Unknown action: " + action;
                break;
        }
        
        return new PermissionResult(granted, message);
    }
    
    /**
     * Internal method - user ki role fetch karta hai
     * INFORMATION EXPERT - PermissionValidator knows about user roles
     * 
     * Note: Future implementation mein database se UserRepository ke through
     * role fetch karni chahiye. Abhi hardcoded hai for demonstration.
     */
    private String getUserRole(int authorityId) {
        // authorityId 1 = City Real Estate Authority (from Users table)
        // authorityId 2 = Admin
        // Others = Agent/Buyer (no freeze permission)
        if (authorityId == 1 || authorityId == 2) {
            return "AUTHORITY";
        }
        return "AGENT"; // default - no freeze permission
    }
    
    /**
     * Result wrapper class - High Cohesion
     * Bundles permission status with message
     */
    public static class PermissionResult {
        private final boolean granted;
        private final String message;
        
        public PermissionResult(boolean granted, String message) {
            this.granted = granted;
            this.message = message;
        }
        
        public boolean isGranted() { return granted; }
        public String getMessage() { return message; }
    }
}
