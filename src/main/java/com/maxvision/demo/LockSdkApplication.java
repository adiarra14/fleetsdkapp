package com.maxvision.demo;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
    basePackages = {"com.maxvision.demo", "com.maxvision.edge.gateway"},
    nameGenerator = LockSdkApplication.CustomGenerator.class
)
public class LockSdkApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockSdkApplication.class, args);
    }

    public static class CustomGenerator implements BeanNameGenerator {
        @Override
        public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
            return definition.getBeanClassName();
        }
    }
} 