package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Course;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
	
	 List<Course> findByHistoricalOrderByNameAsc(Boolean historical);
	 
	 List<Course> findByHistoricalAndNameContainingIgnoreCase(Boolean historical, String courseName);
	 
	 List<Course> findByHistoricalOrderByNameAsc(Boolean historical, Pageable pageable);
}
