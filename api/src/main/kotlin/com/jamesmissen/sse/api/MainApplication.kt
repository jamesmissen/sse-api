package com.jamesmissen.sse.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * The main entry point for the Spring Boot application.
 *
 * This class bootstraps the service using Spring Boot's autoconfiguration and component scanning features.
 *
 * @author James Missen
 *
 * @see SpringBootApplication
 */
@SpringBootApplication
class MainApplication

/**
 * Starts the application.
 *
 * This invokes [runApplication] with the provided command-line arguments, in order to start the Spring Boot context.
 *
 * @param args The command-line arguments passed to the application.
 *
 * @author James Missen
 */
fun main(args: Array<String>) {
    runApplication<MainApplication>(*args)
}
