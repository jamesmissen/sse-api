package io.jamesmissen.sse.api.configuration

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.databind.ObjectMapper
import io.jamesmissen.sse.api.util.extension.contact
import io.jamesmissen.sse.api.util.extension.isEmpty
import io.jamesmissen.sse.api.util.extension.license
import io.jamesmissen.sse.api.util.extension.objectMappers
import io.jamesmissen.sse.api.util.extension.text
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springdoc.core.conditions.SpecPropertiesCondition
import org.springdoc.core.configuration.SpringDocConfiguration
import org.springdoc.core.customizers.SpecPropertiesCustomizer
import org.springdoc.core.customizers.SpringDocCustomizers
import org.springdoc.core.models.GroupedOpenApi
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.properties.SwaggerUiConfigParameters
import org.springdoc.core.properties.SwaggerUiConfigParameters.CONFIG_URL_PROPERTY
import org.springdoc.core.properties.SwaggerUiConfigParameters.LAYOUT_PROPERTY
import org.springdoc.core.properties.SwaggerUiConfigParameters.URL_PROPERTY
import org.springdoc.core.properties.SwaggerUiConfigProperties
import org.springdoc.core.properties.SwaggerUiOAuthProperties
import org.springdoc.core.providers.ActuatorProvider
import org.springdoc.core.providers.ObjectMapperProvider
import org.springdoc.core.providers.SpringDocProviders
import org.springdoc.core.service.AbstractRequestService
import org.springdoc.core.service.GenericResponseService
import org.springdoc.core.service.OpenAPIService
import org.springdoc.core.service.OperationService
import org.springdoc.core.utils.Constants.CLASSPATH_RESOURCE_LOCATION
import org.springdoc.core.utils.Constants.DEFAULT_WEB_JARS_PREFIX_URL
import org.springdoc.core.utils.Constants.INDEX_PAGE
import org.springdoc.core.utils.Constants.OAUTH_REDIRECT_PAGE
import org.springdoc.core.utils.Constants.SPRINGDOC_ENABLED
import org.springdoc.core.utils.Constants.SPRINGDOC_SWAGGER_UI_ENABLED
import org.springdoc.core.utils.Constants.SPRINGDOC_USE_ROOT_PATH
import org.springdoc.core.utils.Constants.SWAGGER_INITIALIZER_JS
import org.springdoc.core.utils.Constants.SWAGGER_UI_OAUTH_REDIRECT_URL
import org.springdoc.core.utils.Constants.SWAGGER_UI_PATH
import org.springdoc.core.utils.Constants.SWAGGER_UI_PREFIX
import org.springdoc.webflux.api.MultipleOpenApiWebFluxResource
import org.springdoc.webflux.api.OpenApiWebfluxResource
import org.springdoc.webflux.ui.SwaggerConfigResource
import org.springdoc.webflux.ui.SwaggerIndexPageTransformer
import org.springdoc.webflux.ui.SwaggerIndexTransformer
import org.springdoc.webflux.ui.SwaggerResourceResolver
import org.springdoc.webflux.ui.SwaggerUiHome
import org.springdoc.webflux.ui.SwaggerWebFluxConfigurer
import org.springdoc.webflux.ui.SwaggerWelcomeCommon
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.util.AntPathMatcher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.PathMatchConfigurer
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.handler.AbstractHandlerMapping
import org.springframework.web.reactive.resource.ResourceTransformer
import org.springframework.web.reactive.resource.ResourceTransformerChain
import org.springframework.web.reactive.resource.ResourceUrlProvider
import org.springframework.web.reactive.resource.TransformedResource
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.Locale
import java.util.Optional
import kotlin.text.Charsets.UTF_8

/**
 * A custom Spring configuration for SpringDoc OpenAPI and Swagger UI.
 *
 * @author James Missen
 */
@Configuration(proxyBeanMethods = false)
class SpringDocConfiguration {

    companion object {

        /**
         * A constant for the path of the OAuth2 redirect page, relative to the Swagger UI path.
         *
         * @author James Missen
         */
        private const val OAUTH_REDIRECT_PATH = "/oauth2-redirect"

        /**
         * A constant for the URL of a hosted Swagger UI distribution.
         *
         * This is used for referencing Swagger UI distribution files using a CDN instead of loading local assets.
         *
         * @author James Missen
         */
        private const val SWAGGER_UI_DIST_URL = "https://cdn.jsdelivr.net/npm/swagger-ui-dist"

        /**
         * A constant for the resource folder containing WebJar resources.
         *
         * @author James Missen
         */
        private const val WEBJAR_RESOURCE_LOCATION = "$CLASSPATH_RESOURCE_LOCATION$DEFAULT_WEB_JARS_PREFIX_URL/"
    }

    /**
     * Configures the SpringDoc object mappers dynamically.
     *
     * This [Bean] adjusts the default configuration of the Jackson [ObjectMapper] instances used for serialisation.
     *
     * It sets threshold for the inclusion of properties, to ensure that serialised objects do not contain properties
     * with empty values.
     *
     * @param objectMapperProvider The SpringDoc object mapper provider.
     *
     * @return An updated [ObjectMapperProvider] instance.
     *
     * @author James Missen
     *
     * @see ObjectMapperProvider
     * @see ObjectMapper
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_ENABLED], matchIfMissing = true)
    fun defaultObjectMapperProvider(objectMapperProvider: ObjectMapperProvider) = objectMapperProvider.apply {
        for (objectMapper in objectMappers) {
            objectMapper.setDefaultPropertyInclusion(NON_EMPTY)
        }
    }

    /**
     * Configures the OpenAPI config properties dynamically.
     *
     * This [Bean] adjusts the default configuration settings for the OpenAPI definition.
     *
     * It removes any components that have not been set using `springdoc.open-api.*` configuration properties.
     *
     * @param springDocConfigProperties The SpringDoc config properties.
     *
     * @return A [SpecPropertiesCustomizer] instance.
     *
     * @author James Missen
     *
     * @see SpringDocConfigProperties.openApi
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_ENABLED], matchIfMissing = true)
    @Conditional(SpecPropertiesCondition::class)
    fun defaultOpenApiConfigProperties(springDocConfigProperties: SpringDocConfigProperties) =
        SpecPropertiesCustomizer(springDocConfigProperties.openApi?.apply {
            if (contact?.isEmpty() == true) contact = null
            if (license?.isEmpty() == true) license = null
            if (externalDocs?.isEmpty() == true) externalDocs = null
        })

    /**
     * Configures the Swagger UI config properties dynamically.
     *
     * This [Bean] adjusts the default configuration settings for the Swagger UI.
     *
     * @param config The initial default Swagger UI config properties.
     * @param swaggerUiPath The Swagger UI path.
     *
     * @return An updated [SwaggerUiConfigProperties] instance.
     *
     * @author James Missen
     *
     * @see SwaggerUiConfigProperties
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = [SPRINGDOC_SWAGGER_UI_ENABLED], matchIfMissing = true)
    fun defaultSwaggerUiConfigProperties(
        config: SwaggerUiConfigProperties,
        @Value(SWAGGER_UI_PATH) swaggerUiPath: String
    ) = config.also { config ->
        // Set the default OAuth2 redirect URL
        if (config.oauth2RedirectUrl == null || config.oauth2RedirectUrl == SWAGGER_UI_OAUTH_REDIRECT_URL) {
            config.oauth2RedirectUrl = "$swaggerUiPath$OAUTH_REDIRECT_PATH"
        }
    }

    /**
     * Modifies the Swagger UI WebFlux configurer.
     *
     * This [Bean] customises the default configurations relating to the Swagger UI behaviour and resources.
     *
     * @param swaggerUiConfigProperties The Swagger UI config properties.
     * @param springDocConfigProperties The SpringDoc config properties.
     * @param swaggerUiIndexTransformer The Swagger UI index page transformer.
     * @param springDocActuatorProvider The SpringDoc actuator provider.
     * @param swaggerUiResourceResolver The Swagger UI resource resolver.
     * @param swaggerUiPath The Swagger UI path.
     *
     * @return A [SwaggerWebFluxConfigurer] WebFlux configurer instance.
     *
     * @author James Missen
     *
     * @see WebFluxConfigurer
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_SWAGGER_UI_ENABLED], matchIfMissing = true)
    fun swaggerUiConfigurer(
        swaggerUiConfigProperties: SwaggerUiConfigProperties,
        springDocConfigProperties: SpringDocConfigProperties,
        swaggerUiIndexTransformer: SwaggerIndexTransformer,
        springDocActuatorProvider: Optional<ActuatorProvider>,
        swaggerUiResourceResolver: SwaggerResourceResolver,
        @Value(SWAGGER_UI_PATH) swaggerUiPath: String
    ) = object : SwaggerWebFluxConfigurer(
        swaggerUiConfigProperties,
        springDocConfigProperties,
        swaggerUiIndexTransformer,
        springDocActuatorProvider,
        swaggerUiResourceResolver
    ) {

        /**
         * Adds resource handlers for serving the static Swagger UI resources from a WebJar.
         *
         * This configures a resource handler at:
         * - the Swagger UI endpoint (e.g. `/docs`), serving the index page; and
         * - the Swagger UI OAuth2 redirect endpoint (e.g. `/docs/oauth2-redirect`), serving the redirect page.
         *
         * It also prioritises these resource endpoints over other [HandlerMapping]s, such as those defined using
         * `@`[RequestMapping] annotations in controllers.
         *
         * @param registry The resource handler registry.
         *
         * @author James Missen
         *
         * @see ResourceHandlerRegistry
         */
        override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
            // Register index page handler
            registry
                .addResourceHandler(swaggerUiPath)
                .addResourceLocations(WEBJAR_RESOURCE_LOCATION)
                .resourceChain(false)
                .addResolver(object : SwaggerResourceResolver(swaggerUiConfigProperties) {
                    override fun findWebJarResourcePath(pathStr: String) =
                        super.findWebJarResourcePath("$SWAGGER_UI_PREFIX$INDEX_PAGE")
                })
                .addTransformer(swaggerUiIndexTransformer)

            // Register OAuth2 redirect page handler
            registry
                .addResourceHandler("$swaggerUiPath$OAUTH_REDIRECT_PATH")
                .addResourceLocations(WEBJAR_RESOURCE_LOCATION)
                .resourceChain(true)
                .addResolver(object : SwaggerResourceResolver(swaggerUiConfigProperties) {
                    override fun findWebJarResourcePath(pathStr: String) =
                        super.findWebJarResourcePath("$SWAGGER_UI_PREFIX$OAUTH_REDIRECT_PAGE")
                })
        }
    }

    /**
     * Prioritises the resource handlers for serving the static Swagger UI resources.
     *
     * This gives precedence to the endpoints for the Swagger UI resources over other [HandlerMapping]s, such as those
     * defined using @`[RequestMapping] annotations in controllers.
     *
     * @param resourceLoader The resource loader.
     * @param resourceUrlProvider The resource URL provider.
     * @param swaggerWebFluxConfigurer The Swagger UI configurer.
     *
     * @return A [HandlerMapping] instance.
     *
     * @author James Missen
     *
     * @see HandlerMapping
     */
    @Bean
    fun swaggerUiHandlerMapping(
        resourceLoader: ResourceLoader,
        resourceUrlProvider: ResourceUrlProvider,
        swaggerWebFluxConfigurer: SwaggerWebFluxConfigurer
    ): HandlerMapping {
        // Create resource handler registry
        val resourceHandlerRegistry = object : ResourceHandlerRegistry(resourceLoader) {
            public override fun getHandlerMapping() = super.getHandlerMapping()
        }.apply { setResourceUrlProvider(resourceUrlProvider) }

        // Create CORS registry
        val corsRegistry = object : CorsRegistry() {
            public override fun getCorsConfigurations() = super.getCorsConfigurations()
        }

        // Create path match configurer
        val pathMatchConfigurer = object : PathMatchConfigurer() {
            val useCaseSensitiveMatch get() = super.isUseCaseSensitiveMatch()
        }

        // Apply Swagger UI configurer
        swaggerWebFluxConfigurer.apply {
            addResourceHandlers(resourceHandlerRegistry)
            addCorsMappings(corsRegistry)
            configurePathMatching(pathMatchConfigurer)
        }

        // Create prioritised handler mapping
        val handlerMapping = resourceHandlerRegistry.handlerMapping
            ?.apply {
                order = -1
                setCorsConfigurations(corsRegistry.corsConfigurations)
                pathMatchConfigurer.useCaseSensitiveMatch?.let { setUseCaseSensitiveMatch(it) }
            }
            ?: object : AbstractHandlerMapping() {
                override fun getHandlerInternal(exchange: ServerWebExchange) = mono {}
            }

        return handlerMapping
    }

    /**
     * Modifies the Swagger UI index resource transformer.
     *
     * This [Bean] transforms the Swagger UI index page resource into a standalone HTML file, without references to
     * other resources served by the same application.
     *
     * It extends the default transformer by:
     * - inlining all the Swagger UI config file properties into the initializer script;
     * - inlining the initializer script into the index page HTML file; and
     * - updating references to other CSS and JavaScript files to use a remote CDN-hosted Swagger distribution.
     *
     * @param swaggerUiConfigProperties The Swagger UI config properties.
     * @param swaggerUiOAuthProperties The Swagger UI OAuth properties.
     * @param swaggerUiWelcome The Swagger welcome.
     * @param springDocObjectMapperProvider The SpringDoc object mapper provider.
     *
     * @return A [SwaggerIndexTransformer] resource transformer instance.
     *
     * @author James Missen
     *
     * @see SwaggerIndexPageTransformer
     * @see ResourceTransformer
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_SWAGGER_UI_ENABLED], matchIfMissing = true)
    fun swaggerUiIndexTransformer(
        swaggerUiConfigProperties: SwaggerUiConfigProperties,
        swaggerUiOAuthProperties: SwaggerUiOAuthProperties,
        swaggerUiWelcome: SwaggerWelcomeCommon,
        springDocObjectMapperProvider: ObjectMapperProvider
    ): SwaggerIndexTransformer = object : SwaggerIndexPageTransformer(
        swaggerUiConfigProperties,
        swaggerUiOAuthProperties,
        swaggerUiWelcome,
        springDocObjectMapperProvider
    ) {

        /**
         * Transforms the Swagger UI index page resource.
         *
         * If the given resource is the Swagger UI index page, this:
         * - resolves the adjacent initializer script resource;
         * - applies the default transformations to the initializer based on the Swagger UI config
         * - merges the result into the index page; and
         * - updates the index page links to reference external distribution resources.
         *
         * @param exchange The current web exchange.
         * @param resource The resource to transform.
         * @param transformerChain The chain of remaining resource transformers.
         *
         * @return A [Mono] of the transformed [Resource].
         *
         * @author James Missen
         *
         * @see SwaggerIndexPageTransformer.transform
         */
        override fun transform(
            exchange: ServerWebExchange,
            resource: Resource,
            transformerChain: ResourceTransformerChain
        ) = mono {
            // Continue to next transformer if not Swagger UI index resource
            if (!AntPathMatcher().match("**$SWAGGER_UI_PREFIX/**$INDEX_PAGE", resource.url.toString())) {
                return@mono transformerChain.transform(exchange, resource).awaitSingle()
            }

            val html = resource.text(UTF_8)

            val indent =
                "(?<=\n)(?<indent>\\s*)<script\\b[^>]* src\\s*=\\s*\"[^>]*\\b$SWAGGER_INITIALIZER_JS\\s*\"".toRegex()
                    .find(html)?.groups?.get("indent")?.value ?: ""

            // Resolve and transform initializer script
            val initializer = transformerChain.resolverChain
                .resolveResource(exchange, SWAGGER_INITIALIZER_JS, listOf(resource))
                .flatMap { resource -> super.transform(exchange, resource, transformerChain) }
                .awaitSingle()
                .text(UTF_8)
                .replace("\n", "\n$indent").trim().let { "$indent$it" }

            // Inline initializer and update links
            val transformedHtml = html
                .replace("=\\s*\"\\s*\\./".toRegex(), "=\"")
                .replace(
                    "(?<=\\b(href|src))\\s*=\\s*\"\\s*(?=[^/])".toRegex(),
                    "=\"$SWAGGER_UI_DIST_URL${swaggerUiConfig.version?.let { "@$it" } ?: ""}/")
                .replace(
                    "(?<=\n)(?<indent>\\s*)(?<openTagStart><script\\b[^>]* )src\\s*=\\s*\"[^>]*\\b$SWAGGER_INITIALIZER_JS\\s*\"\\s*(?<openTagEnd>[^>]*>)[.\\s]*(?<closeTag><\\s*/\\s*script\\s*>\\s*)(?=[<\n])".toRegex(),
                    $$"${indent}${openTagStart}${openTagEnd}\n$$initializer\n${indent}${closeTag}"
                )

            TransformedResource(resource, transformedHtml.toByteArray(UTF_8))
        }

        /**
         * Adds the Swagger UI config properties into an initializer script.
         *
         * This stringifies all the set properties that are used to configure the Swagger UI, and inserts them into the
         * initializer script.
         *
         * This is the same as the default behaviour, but it does not exclude any properties, or use an additional
         * external config file reference.
         *
         * @param initializer The initializer script.
         * @param swaggerUiConfigParameters The Swagger UI config parameters.
         *
         * @return A modified initializer script [String].
         *
         * @author James Missen
         *
         * @see SwaggerIndexPageTransformer.addParameters
         * @see SwaggerUiConfigParameters
         */
        override fun addParameters(initializer: String, swaggerUiConfigParameters: SwaggerUiConfigParameters): String {
            // Extract all set properties
            val parameterMap = swaggerUiConfigParameters.configParameters
                .filterValues { it != null }
                .filterValues { it !is String || it.isNotBlank() }
                .filterKeys { it != CONFIG_URL_PROPERTY || !swaggerUiConfig.configUrl.isNullOrBlank() }

            val indent = "(?<=\n)(?<indent>\\s*)window\\.ui\\s*=\\s*".toRegex()
                .find(initializer)?.groups?.get("indent")?.value ?: ""

            // Stringify properties
            val parameters = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(parameterMap)
                .let { it.substring(1, it.length - 1) }
                .replace(
                    "(?<indent>\n\\s*)\"?\\s*\\b(?<name>[^\"]*)\\b\\s*\"?\\s*(?=:)".toRegex(),
                    $$"${indent}${name}"
                )
                .replace("\n", "\n$indent").trim()

            // Insert properties into initializer
            val transformedInitializer = initializer
                .replace(
                    "(?<indent>\n\\s*)\"?\\s*\\b$URL_PROPERTY\\b\\s*\"?\\s*:\\s*\"[^\n]*\"\\s*,?\\s*[,\n]\\s*".toRegex(),
                    $$"${indent}"
                )
                .replace(
                    "(?<indent>\n\\s*)\"?\\s*\\b$LAYOUT_PROPERTY\\b\\s*\"?\\s*:\\s*\"[^\n]*\"\\s*,?\\s*(?=[,\n])".toRegex(),
                    $$"${indent}$$parameters"
                )

            return transformedInitializer
        }
    }

    /**
     * Modifies the Swagger UI home redirection.
     *
     * This [Bean] adds another endpoint, without a trailing slash, for when the [SPRINGDOC_USE_ROOT_PATH] property is
     * enabled.
     *
     * @return A [SwaggerUiHome] controller instance.
     *
     * @author James Missen
     *
     * @see [SPRINGDOC_USE_ROOT_PATH]
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_SWAGGER_UI_ENABLED], matchIfMissing = true)
    @ConditionalOnProperty(name = [SPRINGDOC_USE_ROOT_PATH])
    fun swaggerUiHome(webFluxProperties: Optional<WebFluxProperties>) =
        object : SwaggerUiHome(webFluxProperties) {

            /**
             * Adds another mapping to the root endpoint, without a trailing slash.
             *
             * This creates an additional `@`[GetMapping] that is identical to the default mapping with the trailing
             * slash, redirecting the root path to the Swagger UI index page.
             *
             * @param response The server HTTP response.
             *
             * @return An empty [Mono] instance (due to the redirect).
             *
             * @author James Missen
             *
             * @see SwaggerUiHome.index
             */
            @GetMapping("")
            override fun index(response: ServerHttpResponse) = super.index(response)
        }

    /**
     * Modifies the default Swagger UI config docs.
     *
     * This [Bean] responds with a [NOT_FOUND] status code for requests to the Swagger UI config endpoint.
     *
     * @param swaggerUiWelcome The Swagger UI welcome.
     *
     * @return A [SwaggerConfigResource] controller instance.
     *
     * @author James Missen
     *
     * @see SwaggerUiConfigProperties
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_SWAGGER_UI_ENABLED], matchIfMissing = true)
    fun swaggerUiConfigResource(swaggerUiWelcome: SwaggerWelcomeCommon) =
        object : SwaggerConfigResource(swaggerUiWelcome) {

            /**
             * Disables the Swagger UI config docs endpoint.
             *
             * This endpoint produces a [NOT_FOUND] response instead of the default config docs.
             *
             * @param request The server HTTP request.
             *
             * @throws ResponseStatusException
             *
             * @author James Missen
             */
            override fun getSwaggerUiConfig(request: ServerHttpRequest) =
                throw ResponseStatusException(NOT_FOUND)
        }

    /**
     * Modifies the default OpenAPI docs.
     *
     * This [Bean] responds with a [NOT_FOUND] status code for requests to the OpenAPI YAML endpoint.
     *
     * @param openAPIBuilderObjectFactory The OpenAPI builder object factory.
     * @param requestBuilder The request builder.
     * @param responseBuilder The response builder.
     * @param operationParser The operation parser.
     * @param springDocConfigProperties The SpringDoc config properties.
     * @param springDocProviders The SpringDoc providers.
     * @param springDocCustomizers The SpringDoc customizers.
     *
     * @return An [OpenApiWebfluxResource] controller instance.
     *
     * @author James Missen
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_ENABLED], matchIfMissing = true)
    fun openApiResource(
        openAPIBuilderObjectFactory: ObjectFactory<OpenAPIService>,
        requestBuilder: AbstractRequestService,
        responseBuilder: GenericResponseService,
        operationParser: OperationService,
        springDocConfigProperties: SpringDocConfigProperties,
        springDocProviders: SpringDocProviders,
        springDocCustomizers: SpringDocCustomizers
    ) = object : OpenApiWebfluxResource(
        openAPIBuilderObjectFactory,
        requestBuilder,
        responseBuilder,
        operationParser,
        springDocConfigProperties,
        springDocProviders,
        springDocCustomizers
    ) {

        /**
         * Disables the YAML OpenAPI docs endpoint.
         *
         * This endpoint produces a [NOT_FOUND] response instead of the default YAML docs.
         *
         * @param request The server HTTP request.
         * @param apiDocsUrl The API docs URL.
         * @param locale The locale.
         *
         * @throws ResponseStatusException
         *
         * @author James Missen
         */
        override fun openapiYaml(request: ServerHttpRequest, apiDocsUrl: String, locale: Locale) =
            throw ResponseStatusException(NOT_FOUND)
    }

    /**
     * Modifies the default grouped OpenAPI docs.
     *
     * This [Bean] responds with a [NOT_FOUND] status code for requests to the grouped OpenAPI endpoints.
     *
     * @param groupedOpenApis The list of grouped OpenAPIs.
     * @param openAPIBuilderObjectFactory The OpenAPI builder object factory.
     * @param requestBuilder The request builder.
     * @param responseBuilder The response builder.
     * @param operationParser The operation parser.
     * @param springDocConfigProperties The SpringDoc config properties.
     * @param springDocProviders The SpringDoc providers.
     * @param springDocCustomizers The SpringDoc customizers.
     *
     * @return A [MultipleOpenApiWebFluxResource] controller instance.
     *
     * @author James Missen
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_ENABLED], matchIfMissing = true)
    fun multipleOpenApiResource(
        groupedOpenApis: List<GroupedOpenApi>,
        openAPIBuilderObjectFactory: ObjectFactory<OpenAPIService>,
        requestBuilder: AbstractRequestService,
        responseBuilder: GenericResponseService,
        operationParser: OperationService,
        springDocConfigProperties: SpringDocConfigProperties,
        springDocProviders: SpringDocProviders,
        springDocCustomizers: SpringDocCustomizers
    ) = object : MultipleOpenApiWebFluxResource(
        groupedOpenApis,
        openAPIBuilderObjectFactory,
        requestBuilder,
        responseBuilder,
        operationParser,
        springDocConfigProperties,
        springDocProviders,
        springDocCustomizers
    ) {

        /**
         * Disables the JSON grouped OpenAPI docs endpoint.
         *
         * This endpoint produces a [NOT_FOUND] response instead of the default JSON docs.
         *
         * @param request The server HTTP request.
         * @param apiDocsUrl The API docs URL.
         * @param group The group name.
         * @param locale The locale.
         *
         * @throws ResponseStatusException
         *
         * @author James Missen
         */
        override fun openapiJson(
            request: ServerHttpRequest,
            apiDocsUrl: String,
            group: String,
            locale: Locale
        ) = throw ResponseStatusException(NOT_FOUND)

        /**
         * Disables the YAML grouped OpenAPI docs endpoint.
         *
         * This endpoint produces a [NOT_FOUND] response instead of the default YAML docs.
         *
         * @param request The server HTTP request.
         * @param apiDocsUrl The API docs URL.
         * @param group The group name.
         * @param locale The locale.
         *
         * @throws ResponseStatusException
         *
         * @author James Missen
         */
        override fun openapiYaml(
            request: ServerHttpRequest,
            apiDocsUrl: String,
            group: String,
            locale: Locale
        ) = throw ResponseStatusException(NOT_FOUND)
    }

    /**
     * Enables type-safe property binding for the SpringDoc `open-api` property.
     *
     * This [Bean] instantiates an [OpenAPI] instance using the default constructor to allow property binding to work
     * correctly when configuring SpringDoc.
     *
     * @return An [OpenAPI] instance.
     *
     * @author James Missen
     */
    @Bean(autowireCandidate = false)
    @ConfigurationProperties("springdoc.open-api")
    fun openApiPropertyBinding() = OpenAPI()

    /**
     * Enables type-safe property binding for the SpringDoc `open-api.info` property.
     *
     * This [Bean] instantiates an [Info] instance using the default constructor to allow property binding to work
     * correctly when configuring SpringDoc.
     *
     * @return An [Info] instance.
     *
     * @author James Missen
     */
    @Bean(autowireCandidate = false)
    @ConfigurationProperties("springdoc.open-api.info")
    fun infoPropertyBinding() = Info()

    /**
     * Enables type-safe property binding for the SpringDoc `open-api.info.contact` property.
     *
     * This [Bean] instantiates a [Contact] instance using the default constructor to allow property binding to work
     * correctly when configuring SpringDoc.
     *
     * @return A [Contact] instance.
     *
     * @author James Missen
     */
    @Bean(autowireCandidate = false)
    @ConfigurationProperties("springdoc.open-api.info.contact")
    fun contactPropertyBinding() = Contact()

    /**
     * Enables type-safe property binding for the SpringDoc `open-api.info.license` property.
     *
     * This [Bean] instantiates a [License] instance using the default constructor to allow property binding to work
     * correctly when configuring SpringDoc.
     *
     * @return A [License] instance.
     *
     * @author James Missen
     */
    @Bean(autowireCandidate = false)
    @ConfigurationProperties("springdoc.open-api.info.license")
    fun licensePropertyBinding() = License()

    /**
     * Enables type-safe property binding for the SpringDoc `open-api.external-docs` property.
     *
     * This [Bean] instantiates an [ExternalDocumentation] instance using the default constructor to allow property
     * binding to work correctly when configuring SpringDoc.
     *
     * @return An [ExternalDocumentation] instance.
     *
     * @author James Missen
     */
    @Bean(autowireCandidate = false)
    @ConfigurationProperties("springdoc.open-api.external-docs")
    fun externalDocsPropertyBinding() = ExternalDocumentation()

    /**
     * Provides a minimal SpringDoc configuration.
     *
     * This [Bean] creates a fallback [SpringDocConfiguration] when the OpenAPI docs are not enabled.
     *
     * @return A [SpringDocConfiguration] instance.
     *
     * @author James Missen
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_ENABLED], havingValue = "false")
    fun fallbackSpringDocConfig() = SpringDocConfiguration()

    /**
     * Provides minimal SpringDoc config properties.
     *
     * This [Bean] creates a fallback [SpringDocConfigProperties] when the OpenAPI docs are not enabled.
     *
     * @return A [SpringDocConfigProperties] instance.
     *
     * @author James Missen
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_ENABLED], havingValue = "false")
    fun fallbackSpringDocConfigProperties() = SpringDocConfigProperties()

    /**
     * Provides a minimal SpringDoc object mapper provider.
     *
     * This [Bean] creates a fallback [ObjectMapperProvider] when the OpenAPI docs are not enabled.
     *
     * @param springDocConfigProperties The SpringDoc config properties.
     *
     * @return An [ObjectMapperProvider] instance.
     *
     * @author James Missen
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_ENABLED], havingValue = "false")
    fun fallbackObjectMapperProvider(springDocConfigProperties: SpringDocConfigProperties) =
        ObjectMapperProvider(springDocConfigProperties)

    /**
     * Provides minimal Swagger UI configuration properties.
     *
     * This [Bean] creates a fallback [SwaggerUiConfigProperties] when the Swagger UI is not enabled.
     *
     * @return A [SwaggerUiConfigProperties] instance.
     *
     * @author James Missen
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_SWAGGER_UI_ENABLED], havingValue = "false")
    fun fallbackSwaggerUiConfigProperties() = SwaggerUiConfigProperties()

    /**
     * Provides minimal Swagger UI OAuth properties.
     *
     * This [Bean] creates a fallback [SwaggerUiOAuthProperties] when the Swagger UI is not enabled.
     *
     * @return A [SwaggerUiOAuthProperties] instance.
     *
     * @author James Missen
     */
    @Bean
    @ConditionalOnProperty(name = [SPRINGDOC_SWAGGER_UI_ENABLED], havingValue = "false")
    fun fallbackSwaggerUiOAuthProperties() = SwaggerUiOAuthProperties()
}
