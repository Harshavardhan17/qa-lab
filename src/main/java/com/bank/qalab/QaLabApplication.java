package com.bank.qalab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bank QA Lab.
 *
 * A small but realistic banking backend used to practise:
 *   - REST API testing with Postman (all HTTP methods, auth flows, validation)
 *   - verifying results in a PostgreSQL database
 *   - Selenium UI automation against the bundled web pages
 */
@SpringBootApplication
public class QaLabApplication {
    public static void main(String[] args) {
        SpringApplication.run(QaLabApplication.class, args);
    }
}
