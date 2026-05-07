package com.example.slackscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlackSchedulerApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(SlackSchedulerApplication.class, args);
    }
}
