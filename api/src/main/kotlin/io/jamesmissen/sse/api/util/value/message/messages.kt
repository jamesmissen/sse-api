@file:Suppress("unused")

package io.jamesmissen.sse.api.util.value.message

import org.springframework.context.MessageSource
import java.util.Locale

const val API_TITLE_CODE = "api-title"
const val API_DESCRIPTION_CODE = "api-description"

/**
 * Resolves the message associated with [API_TITLE_CODE].
 *
 * @receiver The [MessageSource] from which to resolve the message.
 *
 * @param locale The locale in which to perform the lookup.
 *
 * @return A resolved message [String].
 */
fun MessageSource.apiTitle(locale: Locale? = null) = getMessage(API_TITLE_CODE, null, locale)

/**
 * Resolves the message associated with [API_DESCRIPTION_CODE].
 *
 * @receiver The [MessageSource] from which to resolve the message.
 *
 * @param locale The locale in which to perform the lookup.
 *
 * @return A resolved message [String].
 */
fun MessageSource.apiDescription(locale: Locale? = null) = getMessage(API_DESCRIPTION_CODE, null, locale)
