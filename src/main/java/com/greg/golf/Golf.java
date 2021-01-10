package com.greg.golf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication //(scanBasePackages = { "com.greg.golf.bin", "com.greg.golf.dao"})
@EnableScheduling
public class Golf {

	public static void main(String[] args) {

		SpringApplication.run(Golf.class, args);
	}

}
