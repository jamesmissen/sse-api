package com.jamesmissen.sse.api.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jsonMapper
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.format.DateTimeFormatter
import kotlin.io.encoding.Base64
import kotlin.io.encoding.Base64.PaddingOption.ABSENT_OPTIONAL

/**
 * A custom Spring configuration for application dependencies.
 *
 * @author James Missen
 */
@Configuration
class AppConfiguration {

    /**
     * Provides a URL-safe Base64 encoder and decoder without padding.
     *
     * @return A [Base64] instance.
     *
     * @author James Missen
     */
    @Bean
    fun base64() = Base64.UrlSafe.withPadding(ABSENT_OPTIONAL)

    /**
     * Provides a UTC formatter for date-times.
     *
     * @return A [DateTimeFormatter] instance.
     *
     * @author James Missen
     */
    @Bean
    fun dateTimeFormatter() = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(UTC))!!

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
