package io.jamesmissen.sse.api

import io.jamesmissen.sse.api.util.constant.APPLICATION_NAME
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * The main entry point for the Spring Boot application.
 *
 * This class bootstraps the service using Spring Boot's autoconfiguration and component scanning features.
 *
 * The `@`[OpenAPIDefinition] annotation provides top-level metadata for the generated OpenAPI docs.
 *
 * @author James Missen
 *
 * @see SpringBootApplication
 * @see OpenAPIDefinition
 */
@SpringBootApplication(proxyBeanMethods = false)
@OpenAPIDefinition(info = Info(APPLICATION_NAME))
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
