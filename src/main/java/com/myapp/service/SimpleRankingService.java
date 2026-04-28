package com.myapp.service;

import com.myapp.model.Property;
import com.myapp.model.SearchCriteria;
import java.math.BigDecimal;
import java.util.List;

/**
 * UC4 - Simple Ranking Service (NON-CRUD Operation)
 * 
 * This is a NON-CRUD operation because:
 * 1. It calculates relevance scores (business logic, not just DB fetch)
 * 2. It ranks properties based on filter matches (decision making)
 * 3. It sorts by custom business rule (not database order)
 * 
 * GRASP: INFORMATION EXPERT - Knows all ranking rules
 * GRASP: HIGH COHESION - Only handles ranking logic
 */
public class SimpleRankingService {
    
    /**
     * SIMPLE RANKING FORMULA:
     * Score = (Number of matched filters) x 10
     * 
     * Scoring breakdown:
     * - If location matches  -> +10
     * - If price in range    -> +10
     * - If type matches      -> +10
     * - If sector matches    -> +10
     * 
     * Maximum possible score = 40 (all 4 filters match)
     * 
     * Uses BigDecimal for price comparison (matches existing Property model)
     */
    public int calculateScore(Property property, SearchCriteria criteria) {
        int matchedFilters = 0;
        
        // Filter 1: Location match? (+10)
        if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
            if (property.getLocation() != null &&
                property.getLocation().toLowerCase().contains(criteria.getLocation().toLowerCase())) {
                matchedFilters++;
            }
        }
        
        // Filter 2: Price range match? (+10)
        // Uses BigDecimal.compareTo() to match existing Property model
        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            BigDecimal price = property.getPrice();
            if (price != null) {
                boolean inRange = true;
                
                if (criteria.getMinPrice() != null && price.compareTo(criteria.getMinPrice()) < 0) {
                    inRange = false;
                }
                if (criteria.getMaxPrice() != null && price.compareTo(criteria.getMaxPrice()) > 0) {
                    inRange = false;
                }
                
                if (inRange) {
                    matchedFilters++;
                }
            }
        }
        
        // Filter 3: Property type match? (+10)
        if (criteria.getPropertyType() != null && !criteria.getPropertyType().trim().isEmpty()) {
            if (property.getPropertyType() != null &&
                property.getPropertyType().equalsIgnoreCase(criteria.getPropertyType())) {
                matchedFilters++;
            }
        }
        
        // Filter 4: Sector match? (+10)
        if (criteria.getSectorId() != null) {
            if (property.getSectorId() == criteria.getSectorId()) {
                matchedFilters++;
            }
        }
        
        // Score = matched filters x 10 (possible values: 0, 10, 20, 30, or 40)
        return matchedFilters * 10;
    }
    
    /**
     * Rank properties by relevance score (highest to lowest)
     * This is a NON-CRUD operation - sorting with business logic
     * 
     * @param properties List of properties to rank
     * @param criteria Search criteria used for scoring
     * @return Same list, sorted by relevance score descending
     */
    public List<Property> rankProperties(List<Property> properties, SearchCriteria criteria) {
        // Step 1: Calculate score for each property (BUSINESS LOGIC)
        for (Property property : properties) {
            int score = calculateScore(property, criteria);
            property.setRelevanceScore(score);
        }
        
        // Step 2: Sort by score (highest to lowest) - DECISION MAKING
        properties.sort((p1, p2) -> Integer.compare(p2.getRelevanceScore(), p1.getRelevanceScore()));
        
        // Step 3: Assign rank positions (1, 2, 3...)
        for (int i = 0; i < properties.size(); i++) {
            properties.get(i).setRankPosition(i + 1);
        }
        
        return properties;
    }
    
    /**
     * Generate human-readable ranking explanation for a property
     * Shows which filters matched and which did not
     */
    public String getRankingExplanation(Property property, SearchCriteria criteria) {
        StringBuilder explanation = new StringBuilder();
        explanation.append("Rank #").append(property.getRankPosition());
        explanation.append(" (Score: ").append(property.getRelevanceScore()).append("/40)");
        explanation.append(" | Matched filters: ");
        
        int matched = 0;
        int total = 0;
        
        if (criteria.getLocation() != null && !criteria.getLocation().trim().isEmpty()) {
            total++;
            boolean match = property.getLocation() != null &&
                property.getLocation().toLowerCase().contains(criteria.getLocation().toLowerCase());
            if (match) matched++;
            explanation.append(match ? "[Location: YES] " : "[Location: NO] ");
        }
        
        if (criteria.getMinPrice() != null || criteria.getMaxPrice() != null) {
            total++;
            BigDecimal price = property.getPrice();
            boolean inRange = price != null;
            if (inRange && criteria.getMinPrice() != null && price.compareTo(criteria.getMinPrice()) < 0) inRange = false;
            if (inRange && criteria.getMaxPrice() != null && price.compareTo(criteria.getMaxPrice()) > 0) inRange = false;
            if (inRange) matched++;
            explanation.append(inRange ? "[Price: YES] " : "[Price: NO] ");
        }
        
        if (criteria.getPropertyType() != null && !criteria.getPropertyType().trim().isEmpty()) {
            total++;
            boolean match = property.getPropertyType() != null &&
                property.getPropertyType().equalsIgnoreCase(criteria.getPropertyType());
            if (match) matched++;
            explanation.append(match ? "[Type: YES] " : "[Type: NO] ");
        }
        
        if (criteria.getSectorId() != null) {
            total++;
            boolean match = property.getSectorId() == criteria.getSectorId();
            if (match) matched++;
            explanation.append(match ? "[Sector: YES] " : "[Sector: NO] ");
        }
        
        explanation.append("| Total: ").append(matched).append("/").append(total).append(" filters matched");
        return explanation.toString();
    }
}
