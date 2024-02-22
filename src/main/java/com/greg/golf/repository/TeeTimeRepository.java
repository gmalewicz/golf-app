package com.greg.golf.repository;

import com.greg.golf.entity.TeeTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeeTimeRepository extends JpaRepository<TeeTime, Long> {
}
