package com.example.metricsdemo.repository;

import com.example.metricsdemo.document.UserDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, Long> {
    
    // Fuzzy search across name, email, and department
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^2\", \"email\", \"departmentName\"], \"fuzziness\": \"AUTO\"}}")
    Page<UserDocument> fuzzySearch(String searchTerm, Pageable pageable);
}
