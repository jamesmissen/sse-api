package io.jamesmissen.sse.api.util.extensions

import kotlin.io.encoding.Base64
import kotlin.text.Charsets.UTF_8

/**
 * Returns a Base64 string encoded from a numeric ID.
 *
 * This adds an optional prefix in front of the numeric ID, and then encodes the result to a Base64 string.
 *
 * @receiver The [Long] ID to encode.
 *
 * @param prefix The optional prefix string.
 * @param delimiter The delimiter between the prefix and the numeric ID. The default value is `-`.
 *
 * @return A Base64-encoded ID [String].
 *
 * @author James Missen
 */
fun Base64.encodeId(id: Long, prefix: String = "", delimiter: String = "-") =
    "${prefix.takeIf { it.isNotEmpty() }?.let { prefix -> "$prefix$delimiter" } ?: ""}$id"
        .toByteArray(UTF_8)
        .let { encode(it) }

/**
 * Returns a numeric ID decoded from a Base64 string.
 *
 * This decodes a Base64 string, and then optionally removes an expected prefix before parsing the result to a long.
 *
 * @receiver The [String] ID to decode.
 *
 * @return A decoded [Long] numeric ID, or `null` if decoding failed.
 *
 * @param prefix The optional prefix string.
 * @param delimiter The delimiter between the prefix and the numeric ID. The default value is `-`.
 *
 * @author James Missen
 */
fun Base64.decodeId(id: String, prefix: String = "", delimiter: String = "-") =
    runCatching {
        decode(id)
            .toString(UTF_8)
            .removePrefix(prefix.takeIf { it.isNotEmpty() }.let { "$it$delimiter" })
            .toLong()
    }.getOrNull()
