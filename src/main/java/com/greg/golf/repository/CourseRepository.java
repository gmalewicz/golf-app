package com.greg.golf.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greg.golf.entity.Course;


@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
	
	 List<Course> findByOrderByNameAsc();
	 
	 List<Course> findByNameContainingIgnoreCase(String courseName);
	 
	 List<Course> findByOrderByNameAsc(Pageable pageable);
}
