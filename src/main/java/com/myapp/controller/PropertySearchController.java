package com.myapp.controller;

import com.myapp.model.SearchCriteria;
import com.myapp.service.PropertySearchService;
import com.myapp.service.FilterValidator;
import com.myapp.service.FilterValidator.ValidationResult;
import com.myapp.service.PropertySearchService.SearchResult;

/**
 * UC4 - Property Search Controller
 * GRASP: CONTROLLER PATTERN - Handles all UC4 system events
 * GRASP: LOW COUPLING - Only calls service, never database directly
 * 
 * Note: SEPARATE from PropertyController (UC3/UC5).
 *       PropertyController handles CRUD, this handles SEARCH with ranking.
 */
public class PropertySearchController {
    
    private final PropertySearchService searchService;
    private final FilterValidator validator;
    
    public PropertySearchController() {
        this.searchService = new PropertySearchService();
        this.validator = new FilterValidator();
    }
    
    /**
     * SD1 step: validateSearchParameters(filters)
     * Validates filter values before search
     */
    public ValidationResult validateFilters(SearchCriteria criteria) {
        if (criteria == null) {
            return new ValidationResult(false, "No search criteria provided");
        }
        return validator.validate(criteria);
    }
    
    /**
     * UC4 Main Method: Submit search request with ranking
     * SD2 step 1: submitSearchRequest(filters)
     * Returns ranked results (NON-CRUD operation)
     */
    public SearchResult submitSearchRequest(SearchCriteria criteria) {
        if (criteria == null) {
            return new SearchResult(false, null, "No search criteria provided", 0);
        }
        
        return searchService.searchProperties(criteria);
    }
    
    /**
     * Quick search with no filters (show all active properties, ranked)
     */
    public SearchResult getAllProperties() {
        SearchCriteria emptyCriteria = new SearchCriteria();
        return searchService.searchProperties(emptyCriteria);
    }
}
