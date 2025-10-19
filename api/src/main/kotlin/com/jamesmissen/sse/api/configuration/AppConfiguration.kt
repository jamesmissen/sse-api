package com.jamesmissen.sse.api.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jsonMapper

/**
 * A custom Spring configuration for application dependencies.
 *
 * @author James Missen
 */
@Configuration
class AppConfiguration {

    /**
     * Provides a Jackson JSON mapper.
     *
     * @return A [JsonMapper] instance.
     *
     * @author James Missen
     */
    @Bean
    fun jacksonJsonMapper() = jsonMapper()
}
