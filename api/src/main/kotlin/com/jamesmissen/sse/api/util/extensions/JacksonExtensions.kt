package com.jamesmissen.sse.api.util.extensions

import org.springframework.core.io.Resource
import tools.jackson.databind.DatabindException
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue

/**
 * Deserializes JSON content from a resource.
 *
 * @receiver The [ObjectMapper] to read the resource with.
 *
 * @param src The JSON-deserializable resource.
 *
 * @return An object of type [T].
 *
 * @throws DatabindException [T] is non-`null` but the value read is `null`.
 *
 * @author James Missen
 *
 * @see ObjectMapper.readValue
 */
inline fun <reified T> ObjectMapper.readValue(src: Resource): T = readValue(src.inputStream)
