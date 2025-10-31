package io.jamesmissen.sse.api.util.extension

import com.fasterxml.jackson.databind.ObjectMapper
import org.springdoc.core.providers.ObjectMapperProvider
import kotlin.reflect.full.declaredMemberFunctions

/**
 * A collection of all object mappers contained within this provider.
 *
 * Each item is an [ObjectMapper] instance.
 *
 * @receiver The [ObjectMapperProvider] instance within which the [ObjectMapper] elements are contained.
 *
 * @author James Missen
 *
 * @see ObjectMapper
 */
val ObjectMapperProvider.objectMappers
    get() = objectMapperMethods.mapNotNull { method -> method(this) }

/**
 * A collection of functions that extracts a specific object mapper from a given object mapper provider.
 *
 * Each function receives an [ObjectMapperProvider] as an argument, and returns a [List] of all [ObjectMapper]
 * instances, or an empty list if none are defined.
 *
 * @author James Missen
 *
 * @see ObjectMapper
 */
private val objectMapperMethods by lazy {
    ObjectMapperProvider::class.declaredMemberFunctions.mapNotNull { method ->
        method.takeIf { method -> method.returnType.classifier == ObjectMapper::class }
            ?.let { method ->
                { objectMapperProvider: ObjectMapperProvider ->
                    method.call(objectMapperProvider) as? ObjectMapper?
                }
            }
    }
}
