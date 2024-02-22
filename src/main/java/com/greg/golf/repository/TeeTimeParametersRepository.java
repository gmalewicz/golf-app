package com.greg.golf.repository;

import com.greg.golf.entity.TeeTimeParameters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeeTimeParametersRepository extends JpaRepository<TeeTimeParameters, Long> {
}
