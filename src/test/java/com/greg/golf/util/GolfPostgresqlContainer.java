package com.greg.golf.util;

import org.testcontainers.containers.PostgreSQLContainer;

public class GolfPostgresqlContainer extends PostgreSQLContainer<GolfPostgresqlContainer>{

    private static final String IMAGE_VERSION = "postgres:13.1";

    private static GolfPostgresqlContainer container;


    private GolfPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    public static GolfPostgresqlContainer getInstance() {
        if (container == null) {
            container = new GolfPostgresqlContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
