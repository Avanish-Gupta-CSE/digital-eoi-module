package com.uco.eoi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Entry point. Extends SpringBootServletInitializer so the same artifact can be
 * started with `java -jar` locally and deployed as a WAR to Tomcat 11 in UAT.
 */
@SpringBootApplication
public class DigitalEoiApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(DigitalEoiApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(DigitalEoiApplication.class, args);
    }
}
