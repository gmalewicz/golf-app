package com.greg.golf.security;

import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.greg.golf.repository.PlayerRepository;
import com.greg.golf.util.GolfPostgresqlContainer;

import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
class JwtTokenUtilTest {

	@ClassRule
	public static PostgreSQLContainer<GolfPostgresqlContainer> postgreSQLContainer = GolfPostgresqlContainer
			.getInstance();

	@BeforeAll
	public static void setup(@Autowired PlayerRepository playerRepository) {

		log.info("Set up completed");
	}

	@AfterAll
	public static void done() {

		// favouriteCourseRepository.deleteAll();

		log.info("Clean up completed");

	}

}
