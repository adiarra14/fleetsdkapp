package com.maxvision.fleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.maxvision.edge.gateway", "com.maxvision.fleet"})
public class FleetGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(FleetGatewayApplication.class, args);
    }
}
