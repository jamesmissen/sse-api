@file:Suppress("unused")

package io.jamesmissen.sse.api.util.extension

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.links.Link
import io.swagger.v3.oas.models.media.Discriminator
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.XML
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.servers.ServerVariable
import io.swagger.v3.oas.models.tags.Tag
import kotlin.reflect.KClass
import kotlin.reflect.full.functions

/**
 * The contact information for this API definition, or `null` if no contact information is defined.
 *
 * @receiver The [OpenAPI] instance with which the [Contact] element is associated.
 *
 * @author James Missen
 *
 * @see Contact
 * @see Info.contact
 */
var OpenAPI.contact
    get() = info?.contact
    set(contact) {
        if (info == null && contact != null) info = Info()
        info.contact = contact
    }

/**
 * The license information for this API definition, or `null` if no license information is defined.
 *
 * @receiver The [OpenAPI] instance with which the [License] element is associated.
 *
 * @author James Missen
 *
 * @see License
 * @see Info.license
 */
var OpenAPI.license
    get() = info?.license
    set(license) {
        if (info == null && license != null) info = Info()
        info.license = license
    }

/**
 * Returns all discriminator elements defined in this API definition.
 *
 * Each item is a [Discriminator] instance corresponding to a single API discriminator property.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Discriminator] elements.
 *
 * @return A [List] of all [Discriminator] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Discriminator
 */
val OpenAPI.allDiscriminators
    get() = allSchemas.mapNotNull { schema -> schema.discriminator }

/**
 * Returns all example elements defined in this API definition.
 *
 * Each item is an [Example] instance corresponding to a single API example value.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Example] elements.
 *
 * @return A [List] of all [Example] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Example
 */
val OpenAPI.allExamples
    get() = buildList {
        components?.examples?.values?.forEach(::addIfNotNull)
        allParameters.forEach { parameter -> parameter.examples?.values?.forEach(::addIfNotNull) }
        allMediaTypes.forEach { mediaType -> mediaType.examples?.values?.forEach(::addIfNotNull) }
        allHeaders.forEach { header -> header.examples?.values?.forEach(::addIfNotNull) }
    }

/**
 * Returns all external documentation elements defined in this API definition.
 *
 * Each item is an [ExternalDocumentation] instance corresponding to a single API external documentation reference.
 *
 * @receiver The [OpenAPI] instance from which to extract the [ExternalDocumentation] elements.
 *
 * @return A [List] of all [ExternalDocumentation] instances from all paths and operations, or an empty list if none are
 * defined.
 *
 * @author James Missen
 *
 * @see ExternalDocumentation
 */
val OpenAPI.allExternalDocs
    get() = buildList {
        addIfNotNull(externalDocs)
        allOperations.forEach { operation -> addIfNotNull(operation.externalDocs) }
        allTags.forEach { tag -> addIfNotNull(tag.externalDocs) }
        allSchemas.forEach { schema -> addIfNotNull(schema.externalDocs) }
    }

/**
 * Returns all header elements defined in this API definition.
 *
 * Each item is a [Header] instance corresponding to a single API header.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Header] elements.
 *
 * @return A [List] of all [Header] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Header
 */
val OpenAPI.allHeaders
    get() = buildList {
        components?.headers?.values?.forEach(::addIfNotNull)
        allResponses.forEach { response -> response.headers?.values?.forEach(::addIfNotNull) }
    }

/**
 * Returns all link elements defined in this API definition.
 *
 * Each item is a [Link] instance corresponding to a single API design-time link.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Link] elements.
 *
 * @return A [List] of all [Link] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Link
 */
val OpenAPI.allLinks
    get() = buildList {
        components?.links?.values?.forEach(::addIfNotNull)
        allResponses.forEach { response -> response.links?.values?.forEach(::addIfNotNull) }
    }

/**
 * Returns all media type elements defined in this API definition.
 *
 * Each item is a [MediaType] instance corresponding to a single API content type.
 *
 * @receiver The [OpenAPI] instance from which to extract the [MediaType] elements.
 *
 * @return A [List] of all [MediaType] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see MediaType
 */
val OpenAPI.allMediaTypes
    get() = buildList {
        allParameters.forEach { parameter -> parameter.content?.values?.forEach(::addIfNotNull) }
        allRequestBodies.forEach { requestBody -> requestBody.content?.values?.forEach(::addIfNotNull) }
        allResponses.forEach { response -> response.content?.values?.forEach(::addIfNotNull) }
        allHeaders.forEach { header -> header.content?.values?.forEach(::addIfNotNull) }
    }

/**
 * Returns all OAuth Flow elements defined in this API definition.
 *
 * Each item is an [OAuthFlow] instance corresponding to a single API OAuth Flow.
 *
 * @receiver The [OpenAPI] instance from which to extract the [OAuthFlow] elements.
 *
 * @return A [List] of all [OAuthFlow] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see OAuthFlow
 */
val OpenAPI.allOAuthFlows
    get() = buildList {
        OAuthFlows::class.functions.forEach { method ->
            if (method.returnType.classifier == OAuthFlow::class) {
                allSecuritySchemes.forEach { securityScheme ->
                    if (securityScheme.flows != null) addIfNotNull(method.call(securityScheme.flows) as? OAuthFlow)
                }
            }
        }
    }

/**
 * Returns all operation elements defined in this API definition.
 *
 * Each item is an [Operation] instance corresponding to a single API endpoint operation.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Operation] elements.
 *
 * @return A [List] of all [Operation] instances from all paths, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Operation
 */
val OpenAPI.allOperations
    get() = buildList {
        PathItem::class.functions.forEach { method ->
            if (method.returnType.classifier == Operation::class) {
                allPathItems.forEach { pathItem -> addIfNotNull(method.call(pathItem) as? Operation) }
            }
        }
    }

/**
 * Returns all parameter elements defined in this API definition.
 *
 * Each item is a [Parameter] instance corresponding to a single API parameter.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Parameter] elements.
 *
 * @return A [List] of all [Parameter] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Parameter
 */
val OpenAPI.allParameters
    get() = buildList {
        components?.parameters?.values?.forEach(::addIfNotNull)
        allPathItems.forEach { pathItem -> pathItem.parameters?.forEach(::addIfNotNull) }
        allOperations.forEach { operation -> operation.parameters?.forEach(::addIfNotNull) }
    }

/**
 * Returns all path item elements defined in this API definition.
 *
 * Each item is a [PathItem] instance corresponding to a single API path.
 *
 * @receiver The [OpenAPI] instance from which to extract the [PathItem] elements.
 *
 * @return A [List] of all [PathItem] instances, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see PathItem
 */
val OpenAPI.allPathItems
    get() = buildList {
        components?.pathItems?.values?.forEach(::addIfNotNull)
        paths?.values?.forEach(::addIfNotNull)
    }

/**
 * Returns all request body elements defined in this API definition.
 *
 * Each item is a [RequestBody] instance corresponding to a single API endpoint response.
 *
 * @receiver The [OpenAPI] instance from which to extract the [RequestBody] elements.
 *
 * @return A [List] of all [RequestBody] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see RequestBody
 */
val OpenAPI.allRequestBodies
    get() = buildList {
        components?.requestBodies?.values?.forEach(::addIfNotNull)
        allOperations.forEach { operation -> addIfNotNull(operation.requestBody) }
    }

/**
 * Returns all response elements defined in this API definition.
 *
 * Each item is an [ApiResponse] instance corresponding to a single API endpoint response.
 *
 * @receiver The [OpenAPI] instance from which to extract the [ApiResponse] elements.
 *
 * @return A [List] of all [ApiResponse] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see ApiResponse
 */
val OpenAPI.allResponses
    get() = buildList {
        components?.responses?.values?.forEach(::addIfNotNull)
        allOperations.forEach { operation -> operation.responses?.values?.forEach(::addIfNotNull) }
    }

/**
 * Returns all schema elements defined in this API definition.
 *
 * Each item is a [Schema] instance corresponding to a single API schema for an input or output.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Schema] elements.
 *
 * @return A [List] of all [Schema] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Schema
 */
val OpenAPI.allSchemas
    get() = buildList {
        components?.schemas?.values?.forEach(::addIfNotNull)
        allParameters.forEach { parameter -> addIfNotNull(parameter.schema) }
        allMediaTypes.forEach { mediaType -> addIfNotNull(mediaType.schema) }
        allMediaTypes.forEach { mediaType -> mediaType.extensions?.values?.forEach { value -> addIfNotNull(value as? Schema<*>) } }
        allHeaders.forEach { header -> addIfNotNull(header.schema) }
    }

/**
 * Returns all security requirement elements defined in this API definition.
 *
 * Each item is an [SecurityRequirement] instance corresponding to a single API security requirement.
 *
 * @receiver The [OpenAPI] instance from which to extract the [SecurityRequirement] elements.
 *
 * @return A [List] of all [SecurityRequirement] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see SecurityRequirement
 */
val OpenAPI.allSecurityRequirements
    get() = buildList {
        security?.forEach(::addIfNotNull)
        allOperations.forEach { operation -> operation.security?.forEach(::addIfNotNull) }
    }

/**
 * Returns all security scheme elements defined in this API definition.
 *
 * Each item is a [SecurityScheme] instance corresponding to a single API security scheme.
 *
 * @receiver The [OpenAPI] instance from which to extract the [SecurityScheme] elements.
 *
 * @return A [List] of all [SecurityScheme] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see SecurityScheme
 */
val OpenAPI.allSecuritySchemes
    get() = components?.securitySchemes?.values?.filterNotNull() ?: emptyList()

/**
 * Returns all server elements defined in this API definition.
 *
 * Each item is a [Server] instance corresponding to a single API server.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Server] elements.
 *
 * @return A [List] of all [Server] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Server
 */
val OpenAPI.allServers
    get() = buildList {
        servers?.forEach(::addIfNotNull)
        allPathItems.forEach { pathItem -> pathItem.servers?.forEach(::addIfNotNull) }
        allOperations.forEach { operation -> operation.servers?.forEach(::addIfNotNull) }
        allLinks.forEach { link -> addIfNotNull(link.server) }
    }

/**
 * Returns all server variable elements defined in this API definition.
 *
 * Each item is a [ServerVariable] instance corresponding to a single API server URL variable.
 *
 * @receiver The [OpenAPI] instance from which to extract the [ServerVariable] elements.
 *
 * @return A [List] of all [ServerVariable] instances from all paths and operations, or an empty list if none are
 * defined.
 *
 * @author James Missen
 *
 * @see ServerVariable
 */
val OpenAPI.allServerVariables
    get() = buildList {
        allServers.forEach { server -> server.variables?.values?.forEach(::addIfNotNull) }
    }

/**
 * Returns all tag elements defined in this API definition.
 *
 * Each item is a [Tag] instance corresponding to a single API tag.
 *
 * @receiver The [OpenAPI] instance from which to extract the [Tag] elements.
 *
 * @return A [List] of all [Tag] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Tag
 */
val OpenAPI.allTags
    get() = tags?.filterNotNull() ?: emptyList()

/**
 * Returns all XML elements defined in this API definition.
 *
 * Each item is an [XML] instance corresponding to a single API XML model definition.
 *
 * @receiver The [OpenAPI] instance from which to extract the [XML] elements.
 *
 * @return A [List] of all [XML] instances from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see XML
 */
val OpenAPI.allXmls
    get() = allSchemas.mapNotNull { schema -> schema.xml }

/**
 * Returns `true` if this contact does not have any defined properties.
 *
 * @receiver The [Contact] instance to check for emptiness.
 *
 * @return A [Boolean] value indicating whether this [Contact] is empty.
 *
 * @author James Missen
 */
fun Contact.isEmpty() =
    name.isNullOrEmpty() && email.isNullOrEmpty() && url.isNullOrEmpty() && extensions.isNullOrEmpty()

/**
 * Returns `true` if this contact does not have any defined properties.
 *
 * @receiver The [License] instance to check for emptiness.
 *
 * @return A [Boolean] value indicating whether this [License] is empty.
 *
 * @author James Missen
 */
fun License.isEmpty() =
    name.isNullOrEmpty() && identifier.isNullOrEmpty() && url.isNullOrEmpty() && extensions.isNullOrEmpty()

/**
 * Returns `true` if this contact does not have any defined properties.
 *
 * @receiver The [ExternalDocumentation] instance to check for emptiness.
 *
 * @return A [Boolean] value indicating whether this [ExternalDocumentation] is empty.
 *
 * @author James Missen
 */
fun ExternalDocumentation.isEmpty() =
    description.isNullOrEmpty() && url.isNullOrEmpty() && extensions.isNullOrEmpty()

/**
 * Converts this class to an OpenAPI schema.
 *
 * @receiver The [Class] to transform into a schema.
 *
 * @return An OpenAPI [Schema] representative of instances of the [Class].
 *
 * @author James Missen
 *
 * @see Schema
 */
fun Class<*>.toSchema() = ModelConverters.getInstance().read(this)
    .values
    .firstOrNull()

/**
 * Converts this class to an OpenAPI schema.
 *
 * @receiver The [KClass] to transform into a schema.
 *
 * @return An OpenAPI [Schema] representative of instances of the [KClass].
 *
 * @author James Missen
 *
 * @see Schema
 */
fun KClass<*>.toSchema() = this.java.toSchema()

/**
 * Adds the specified element to the collection, if it is not `null`.
 *
 * @receiver The [MutableCollection] to which to add the element.
 *
 * @return A [Boolean] value indicating whether the element has been added to the collection.
 *
 * @author James Missen
 *
 * @see MutableCollection.add
 */
private fun <T> MutableCollection<T>.addIfNotNull(element: T?) = if (element != null) add(element) else false
