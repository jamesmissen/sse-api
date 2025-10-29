package io.jamesmissen.sse.api.configuration

import io.jamesmissen.sse.api.util.extensions.operations
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.service.OpenAPIService.splitCamelCase
import org.springdoc.core.utils.Constants.DEFAULT_SERVER_DESCRIPTION
import org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

/**
 * A custom Spring configuration for OpenAPI docs.
 *
 * @author James Missen
 */
@Configuration
@ConditionalOnProperty(name = [SPRINGDOC_ENABLED], matchIfMissing = true)
class OpenApiConfiguration {

    /**
     * Customises the generated `version` field in the OpenAPI docs.
     *
     * @return An [OpenApiCustomizer] instance.
     *
     * @author James Missen
     *
     * @see Info.version
     */
    @Bean
    fun apiVersion() = OpenApiCustomizer { openApi ->
        if (openApi.info == null) openApi.info = Info()

        openApi.info.version = null
    }

    /**
     * Customises the generated `servers` field in the OpenAPI docs.
     *
     * This [Bean] adds a default server path to use if no servers are specified.
     *
     * @return An [OpenApiCustomizer] instance.
     *
     * @author James Missen
     *
     * @see OpenAPI.servers
     */
    @Bean
    fun apiServers() = OpenApiCustomizer { openApi ->
        openApi.servers = openApi.servers.map { server ->
            server
                .takeIf { it.description != DEFAULT_SERVER_DESCRIPTION }
                ?: server.url(URI.create(server.url).path).description(null)
        }
    }

    /**
     * Customises the generated `tags` field in the OpenAPI docs.
     *
     * This [Bean] orders the `tags` alphabetically.
     *
     * @return An [OpenApiCustomizer] instance.
     *
     * @author James Missen
     *
     * @see OpenAPI.tags
     */
    @Bean
    fun apiTags() = OpenApiCustomizer { openApi ->
        openApi.tags?.sortWith { tag1, tag2 -> tag1.name.compareTo(tag2.name) }
    }

    /**
     * Customises the generated `paths` field in the OpenAPI docs.
     *
     * This [Bean] orders the `paths` alphabetically.
     *
     * @return An [OpenApiCustomizer] instance.
     *
     * @author James Missen
     *
     * @see OpenAPI.paths
     */
    @Bean
    fun apiPaths() = OpenApiCustomizer { openApi ->
        openApi.paths = openApi.paths?.let { paths ->
            Paths()
                .apply { putAll(paths.toSortedMap()) }
                .extensions(paths.extensions)
        }
    }

    /**
     * Customises the generated `summary` field for each operation in the OpenAPI docs.
     *
     * This [Bean] generates a `summary` field for each API endpoint based on the endpoint's `operationId`.
     *
     * @return An [OpenApiCustomizer] instance.
     *
     * @author James Missen
     *
     * @see Operation.summary
     */
    @Bean
    fun apiOperationSummaries() = OpenApiCustomizer { openApi ->
        openApi.operations.forEach { operation ->
            if (!operation.summary.isNullOrBlank() || operation.operationId.isEmpty()) return@forEach

            operation.summary = splitCamelCase(operation.operationId)
                .split("-")
                .joinToString(" ") { word -> word.replaceFirstChar { it.titlecase() } }
        }
    }

    /**
     * Customises the generated `tags` field for each operation in the OpenAPI docs.
     *
     * This [Bean] removes the default suffix added to each tag of an operation's `tags`, and orders the `tags`
     * alphabetically.
     *
     * @return An [OpenApiCustomizer] instance.
     *
     * @author James Missen
     *
     * @see Operation.tags
     */
    @Bean
    fun apiOperationTags() = OpenApiCustomizer { openApi ->
        openApi.operations.forEach { operation ->
            if (operation.tags.isNullOrEmpty()) return@forEach

            operation.tags = operation.tags.map { it.removeSuffix("-controller") }
            operation.tags.sort()
        }
    }
}
