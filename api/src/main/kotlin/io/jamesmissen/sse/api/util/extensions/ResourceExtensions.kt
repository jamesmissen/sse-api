package io.jamesmissen.sse.api.util.extensions

import org.springframework.core.io.Resource
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_8

/**
 * Reads the resource entirely as a string.
 *
 * @receiver The [Resource] to read from.
 *
 * @param charset The charset to use. The default value is [UTF_8].
 *
 * @return A resource text [String].
 *
 * @throws FileNotFoundException The underlying resource does not exist.
 * @throws IOException The resource content stream could not be opened.
 *
 * @author James Missen
 */
fun Resource.text(charset: Charset = UTF_8) = inputStream.bufferedReader(charset).use { it.readText() }
