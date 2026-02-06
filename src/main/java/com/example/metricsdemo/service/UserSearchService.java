package com.example.metricsdemo.service;

import com.example.metricsdemo.document.UserDocument;
import com.example.metricsdemo.model.User;
import com.example.metricsdemo.repository.UserSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSearchService.class);
    
    @Autowired
    private UserSearchRepository userSearchRepository;
    
    // Index a user in Elasticsearch
    public void indexUser(User user) {
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
    }
    
    // Fuzzy search with typo tolerance
    public Page<UserDocument> fuzzySearch(String searchTerm, Pageable pageable) {
        logger.info("Performing fuzzy search for: {}", searchTerm);
        return userSearchRepository.fuzzySearch(searchTerm, pageable);
    }
    
    // Delete user from index
    public void deleteUser(Long userId) {
        try {
            userSearchRepository.deleteById(userId);
            logger.info("Deleted user {} from Elasticsearch", userId);
        } catch (Exception e) {
            logger.error("Failed to delete user {} from Elasticsearch: {}", userId, e.getMessage());
        }
    }
    
    // Reindex all users
    public void reindexAll(Iterable<User> users) {
        logger.info("Reindexing all users in Elasticsearch");
        users.forEach(this::indexUser);
    }
}
