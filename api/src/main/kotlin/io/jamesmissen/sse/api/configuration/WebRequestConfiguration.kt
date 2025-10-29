package io.jamesmissen.sse.api.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus.MOVED_PERMANENTLY
import org.springframework.web.server.WebFilter
import java.net.URI

/**
 * A custom Spring configuration for Spring Web application requests.
 *
 * @author James Missen
 */
@Configuration
class WebRequestConfiguration {

    /**
     * Intercepts requests to redirect paths ending with a slash.
     *
     * This [Bean] responds to requests with a [MOVED_PERMANENTLY] status if the request path ends with one or more slash
     * characters. The redirect location is the original path with any trailing slashes removed.
     *
     * @return A [WebFilter] interceptor instance.
     *
     * @author James Missen
     *
     * @see WebFilter
     */
    @Bean
    fun trailingSlashPathInterceptor() = WebFilter { exchange, chain ->
        val request = exchange.request

        // Continue to next interceptor if not a path with trailing slash characters
        if (!request.uri.path.endsWith("/")) return@WebFilter chain.filter(exchange)

        val path = request.uri.path.trimEnd('/')
        val query = request.uri.rawQuery?.let { "?$it" } ?: ""

        exchange.response.headers.location = URI.create("$path$query")
        exchange.response.statusCode = MOVED_PERMANENTLY

        return@WebFilter exchange.response.setComplete()
    }
}
