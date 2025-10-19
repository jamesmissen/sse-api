package com.jamesmissen.sse.api.controller

import com.jamesmissen.sse.api.model.Country
import com.jamesmissen.sse.api.util.Constants.DATA_RESOURCE_LOCATION
import com.jamesmissen.sse.api.util.extensions.decodeId
import com.jamesmissen.sse.api.util.extensions.encodeId
import com.jamesmissen.sse.api.util.extensions.readValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.json.JsonMapper
import java.lang.System.currentTimeMillis
import java.time.Duration.ofMillis
import java.time.Instant.ofEpochMilli
import java.time.format.DateTimeFormatter
import kotlin.io.encoding.Base64
import kotlin.math.max

/**
 * A REST controller for country resources.
 *
 * @property base64 The Base64 encoder and decoder.
 * @property dateTimeFormatter The formatter for date-times.
 * @property jsonMapper The JSON object mapper.
 * @property resourceLoader The resource loader.
 *
 * @author James Missen
 */
@RestController
@RequestMapping("/countries")
class CountriesController(
    private final val base64: Base64,
    private final val dateTimeFormatter: DateTimeFormatter,
    private final val jsonMapper: JsonMapper,
    private final val resourceLoader: ResourceLoader
) {

    companion object {

        /**
         * A constant for the period of delay (in milliseconds) between subsequent events.
         *
         * @author James Missen
         */
        private const val DELAY = 4000L // milliseconds

        /**
         * A constant for the name used as the Server-Sent Event `event` field value.
         *
         * @author James Missen
         */
        private const val EVENT = "country"

        /**
         * A constant for the reconnection time (in milliseconds) used as the Server-Sent Event `retry` field value.
         *
         * @author James Missen
         */
        private const val RETRY = 10000L // milliseconds

        /**
         * A constant for the maximum number of events to be re-emitted when resuming a stream using the `Last-Event-ID`
         * header.
         *
         * @author James Missen
         */
        private const val MAX_LATEST_EVENTS = 1000L
    }

    /**
     * The list of all [Country] items.
     *
     * @author James Missen
     */
    private final val countryData: List<Country> by lazy {
        jsonMapper.readValue(resourceLoader.getResource("$DATA_RESOURCE_LOCATION/countries.json"))
    }

    /**
     * Gets all country data.
     *
     * This endpoint produces a JSON array of all [Country] items.
     *
     * @return A [List] of [Country] items.
     *
     * @author James Missen
     */
    @GetMapping("", produces = [APPLICATION_JSON_VALUE])
    final fun getCountries() = countryData

    /**
     * Streams country data as a continuous flow of Server-Sent Events.
     *
     * This endpoint produces an event stream, with each event including a [Country] `data` payload and a [String] `id`.
     *
     * @param lastEventId The optional `Last-Event-ID` header used for resuming a stream.
     *
     * @return A [Flow] of [ServerSentEvent]s, each with a [Country] `data` payload.
     *
     * @author James Missen
     *
     * @see ServerSentEvent
     */
    @GetMapping("/stream", produces = [TEXT_EVENT_STREAM_VALUE])
    final fun getCountriesStream(@RequestHeader("Last-Event-ID") lastEventId: String?) = flow {
        val currentId = currentTimeMillis() / DELAY + 1
        val initialId = lastEventId
            ?.let { id -> base64.decodeId(id, EVENT)?.plus(1) }
            ?.let { id -> max(id, currentId - MAX_LATEST_EVENTS) }
            ?: currentId

        generateSequence(initialId) { it + 1 }.forEach { id ->
            // Wait for next event time if necessary
            delay(DELAY * id - currentTimeMillis())

            val country = countryData[(id % countryData.size).toInt()]
            val timestamp = ofEpochMilli(DELAY * id)

            // Construct event
            val event = ServerSentEvent.builder<Country>()
                .data(country)
                .id(base64.encodeId(id, EVENT))
                .event(EVENT)
                .retry(ofMillis(RETRY))
                .comment(dateTimeFormatter.format(timestamp))
                .build()

            emit(event)
        }
    }
}
