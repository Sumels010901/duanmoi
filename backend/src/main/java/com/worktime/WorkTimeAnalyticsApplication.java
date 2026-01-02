package com.worktime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for Work Time Analytics Backend.
 *
 * This application provides REST APIs for ingesting, processing, and analyzing
 * physical activity data from Samsung Galaxy Watch via Health Connect.
 *
 * @author Thang
 * @version 1.0.0
 * @since 2026-01-02
 */
@SpringBootApplication
public class WorkTimeAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkTimeAnalyticsApplication.class, args);
    }
}