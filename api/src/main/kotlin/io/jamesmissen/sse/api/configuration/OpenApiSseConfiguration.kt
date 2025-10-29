package io.jamesmissen.sse.api.configuration

import io.jamesmissen.sse.api.util.extensions.responses
import io.jamesmissen.sse.api.util.extensions.toSchema
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED
import org.springdoc.core.utils.SpringDocUtils.getConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE
import org.springframework.http.codec.ServerSentEvent

/**
 * A custom Spring configuration for OpenAPI docs with Server-Sent Event (SSE) endpoints.
 *
 * @author James Missen
 */
@Configuration
@ConditionalOnProperty(name = [SPRINGDOC_ENABLED], matchIfMissing = true)
class OpenApiSseConfiguration {

    companion object {

        /**
         * A constant for the name of the OpenAPI schema representing a Server-Sent Event
         *
         * @author James Missen
         */
        private const val EVENT_SCHEMA_NAME = "Event"
    }

    init {
        // Adjust SpringDoc configuration to ignore ServerSentEvent wrappers during OpenAPI docs generation.
        getConfig().let { config ->
            config.addFluxWrapperToIgnore(ServerSentEvent::class.java)
            config.addResponseWrapperToIgnore(ServerSentEvent::class.java)
        }
    }

    /**
     * Customises the generated `schemas` field in the OpenAPI docs.
     *
     * This [Bean] adds a reusable `Event` schema to the generated docs if the API contains any SSE endpoints.
     *
     * @return An [OpenApiCustomizer] instance.
     *
     * @author James Missen
     *
     * @see Components.schemas
     */
    @Bean
    fun sseEventSchema() = OpenApiCustomizer { openApi ->
        val names = openApi.responses.mapNotNull { it.content?.keys }.flatten()

        // Return if no SSE endpoints present
        if (names.none { it.contains("\\b$TEXT_EVENT_STREAM_VALUE\\b".toRegex()) }) return@OpenApiCustomizer

        openApi.components.addSchemas(EVENT_SCHEMA_NAME, Event::class.toSchema()!!)
    }

    /**
     * Customises the generated `content` field items for each operation in the OpenAPI docs.
     *
     * This [Bean] modifies any SSE endpoint docs to use the `itemSchema` field (introduced in OpenAPI 3.2) instead of
     * the `schema` field.
     *
     * @return An [OpenApiCustomizer] instance.
     *
     * @author James Missen
     *
     * @see ApiResponse.content
     */
    @Bean
    fun sseMediaTypes() = OpenApiCustomizer { openApi ->
        openApi.responses.forEach { response ->
            response.content?.replaceAll { name, mediaType ->
                // Skip if not SSE endpoint
                if (mediaType.schema?.items == null || !name.contains("\\b$TEXT_EVENT_STREAM_VALUE\\b".toRegex())) {
                    return@replaceAll mediaType
                }

                val arrayItemSchema = mediaType.schema.items

                val itemSchema = Schema<Any>()
                    .`$ref`("#/components/schemas/$EVENT_SCHEMA_NAME")

                if (arrayItemSchema.types?.size != 1 || arrayItemSchema.types.iterator().next() != "string") {
                    val dataSchema = Schema<Any>()
                        .contentMediaType(APPLICATION_JSON_VALUE)
                        .contentSchema(arrayItemSchema)

                    itemSchema.oneOf = listOf(Schema<Any>().addProperty("data", dataSchema))
                }

                MediaType()
                    .examples(mediaType.examples?.toMutableMap())
                    .encoding(mediaType.encoding)
                    .extensions((mediaType.extensions?.toMutableMap() ?: mutableMapOf()).apply {
                        put("itemSchema", itemSchema)
                    })
            }
        }
    }

    /**
     * A model for a Server-Sent Event (as defined by the HTML standard).
     *
     * @property data The event payload.
     * @property event The event type.
     * @property id The event ID.
     * @property retry The reconnection time (in milliseconds).
     *
     * @author James Missen
     */
    private data class Event(val data: String?, val event: String?, val id: String?, val retry: Long?)
}
