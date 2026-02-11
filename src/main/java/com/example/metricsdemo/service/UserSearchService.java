package com.example.metricsdemo.service;

import com.example.metricsdemo.document.UserDocument;
import com.example.metricsdemo.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// @Service - Disabled for t2.micro deployment without Elasticsearch
public class UserSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSearchService.class);
    
    // Index a user in Elasticsearch
    public void indexUser(User user) {
        // Elasticsearch disabled for t2.micro deployment
        /*
        try {
            UserDocument doc = new UserDocument(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getDepartment() != null ? user.getDepartment().getName() : ""
            );
            userSearchRepository.save(doc);
            logger.info("Indexed user {} in Elasticsearch", user.getId());
        } catch (Exception e) {
            logger.error("Failed to index user {} in Elasticsearch: {}", user.getId(), e.getMessage());
        }
        */
    }
    
    // Fuzzy search with typo tolerance
    public Page<UserDocument> fuzzySearch(String searchTerm, Pageable pageable) {
        // Elasticsearch disabled for t2.micro deployment
        logger.info("Fuzzy search disabled - Elasticsearch not available");
        return null;
        /*
        logger.info("Performing fuzzy search for: {}", searchTerm);
        return userSearchRepository.fuzzySearch(searchTerm, pageable);
        */
    }
    
    // Delete user from index
    public void deleteUser(Long userId) {
        // Elasticsearch disabled for t2.micro deployment
        /*
        try {
            userSearchRepository.deleteById(userId);
            logger.info("Deleted user {} from Elasticsearch", userId);
        } catch (Exception e) {
            logger.error("Failed to delete user {} from Elasticsearch: {}", userId, e.getMessage());
        }
        */
    }
    
    // Reindex all users
    public void reindexAll(Iterable<User> users) {
        // Elasticsearch disabled for t2.micro deployment
        /*
        logger.info("Reindexing all users in Elasticsearch");
        users.forEach(this::indexUser);
        */
    }
}
