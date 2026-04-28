package com.myapp.adapter;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GoF DESIGN PATTERN: ADAPTER
 * GRASP: PURE FABRICATION - No domain equivalent, fabricated for image storage
 * 
 * This class adapts the local file system to work as a media storage system.
 * It wraps external storage operations and makes them compatible with PropertyController.
 */
public class MediaStorageAdapter {
    
    private static final String STORAGE_DIR = "uploads/properties/";
    private static final List<String> ALLOWED_FORMATS = List.of("png", "jpg", "jpeg");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    public MediaStorageAdapter() {
        // Create storage directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
        } catch (IOException e) {
            System.err.println("Could not create storage directory: " + e.getMessage());
        }
    }
    
    /**
     * SD3 step 2: validateFileFormat()
     * Checks if file format is allowed (PNG, JPG, JPEG)
     */
    public boolean validateFileFormat(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return ALLOWED_FORMATS.contains(extension);
    }
    
    /**
     * SD3 step 2: validateFileSize()
     * Checks if file size is within limit (10MB)
     */
    public boolean validateFileSize(byte[] fileData) {
        return fileData.length <= MAX_FILE_SIZE;
    }
    
    /**
     * SD3 step 3: saveToStorage()
     * Stores images to local file system
     * Returns list of stored file paths
     */
    public List<String> storeImages(List<byte[]> imageFiles, List<String> originalFileNames) {
        List<String> storedPaths = new ArrayList<>();
        
        if (imageFiles == null || imageFiles.isEmpty()) {
            return storedPaths;
        }
        
        for (int i = 0; i < imageFiles.size(); i++) {
            byte[] imageData = imageFiles.get(i);
            String originalName = originalFileNames.get(i);
            
            // Validate before storing
            if (!validateFileFormat(originalName)) {
                System.err.println("Invalid file format: " + originalName);
                continue;
            }
            
            if (!validateFileSize(imageData)) {
                System.err.println("File too large: " + originalName);
                continue;
            }
            
            // Generate unique filename
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalName;
            String fullPath = STORAGE_DIR + uniqueFileName;
            
            // Save to disk
            try {
                Files.write(Paths.get(fullPath), imageData);
                storedPaths.add(fullPath);
                System.out.println("Image stored: " + fullPath);
            } catch (IOException e) {
                System.err.println("Failed to store image: " + e.getMessage());
            }
        }
        
        return storedPaths;
    }
    
    /**
     * Helper method to get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }
}
