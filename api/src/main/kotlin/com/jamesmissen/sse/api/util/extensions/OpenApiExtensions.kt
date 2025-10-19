package com.jamesmissen.sse.api.util.extensions

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.responses.ApiResponse
import kotlin.reflect.KClass
import kotlin.reflect.full.functions

/**
 * Returns all path item elements defined in this API definition.
 *
 * Each item is a [PathItem] instance corresponding to a single API path.
 *
 * @receiver The [OpenAPI] instance to extract the [PathItem]s from.
 *
 * @return A [List] of all [PathItem]s, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see PathItem
 */
val OpenAPI.pathItems
    get() = paths
        ?.values
        ?.toList()
        ?: emptyList()

/**
 * Returns all operation elements defined in this API definition.
 *
 * Each item is an [Operation] instance corresponding to a single API endpoint.
 *
 * @receiver The [OpenAPI] instance to extract the [Operation]s from.
 *
 * @return A [List] of all [Operation]s from all paths, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see Operation
 */
val OpenAPI.operations
    get() = PathItem::class.functions
        .filter { it.returnType.classifier == Operation::class }
        .map { method -> pathItems.mapNotNull { method.call(it) as? Operation } }
        .flatten()

/**
 * Returns all response elements defined in this API definition.
 *
 * Each item is an [ApiResponse] instance corresponding to a single API endpoint response.
 *
 * @receiver The [OpenAPI] instance to extract the [ApiResponse]s from.
 *
 * @return A [List] of all [ApiResponse]s from all paths and operations, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see ApiResponse
 */
val OpenAPI.responses
    get() = operations
        .mapNotNull { it.responses }
        .flatMap { it.values }

fun Class<*>.toSchema() = ModelConverters.getInstance().read(this)
    .values
    .firstOrNull()

fun KClass<*>.toSchema() = this.java.toSchema()
