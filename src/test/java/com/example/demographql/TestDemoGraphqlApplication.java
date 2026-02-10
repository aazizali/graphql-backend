package com.example.demographql;

import org.springframework.boot.SpringApplication;
import org.testcontainers.utility.TestcontainersConfiguration;

public class TestDemoGraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.from(DemoGraphqlApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
