package com.jamesmissen.sse.api.controller

import com.jamesmissen.sse.api.model.Country
import com.jamesmissen.sse.api.util.Constants.DATA_RESOURCE_LOCATION
import com.jamesmissen.sse.api.util.extensions.readValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.json.JsonMapper
import java.lang.System.currentTimeMillis

/**
 * A REST controller for country resources.
 *
 * @property jsonMapper The JSON object mapper.
 * @property resources The resource loader.
 *
 * @author James Missen
 */
@RestController
@RequestMapping("/countries")
class CountriesController(private final val jsonMapper: JsonMapper, private final val resources: ResourceLoader) {

    companion object {

        /**
         * A constant for the period of delay (in milliseconds) between subsequent events.
         *
         * @author James Missen
         */
        private const val DELAY = 4000L // milliseconds
    }

    /**
     * The list of all [Country] items.
     *
     * @author James Missen
     */
    private final val countryData: List<Country> by lazy {
        jsonMapper.readValue(resources.getResource("$DATA_RESOURCE_LOCATION/countries.json"))
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
     * This endpoint produces an event stream, with each event including a [Country] `data` payload.
     *
     * @return A [Flow] of [ServerSentEvent]s, each with a [Country] `data` payload.
     *
     * @author James Missen
     *
     * @see ServerSentEvent
     */
    @GetMapping("/stream", produces = [TEXT_EVENT_STREAM_VALUE])
    final fun getCountriesStream() = flow {
        val initialId = currentTimeMillis() / DELAY + 1

        generateSequence(initialId) { it + 1 }.forEach { id ->
            // Wait for next event time if necessary
            delay(DELAY * id - currentTimeMillis())

            val country = countryData[(id % countryData.size).toInt()]

            // Construct event
            val event = ServerSentEvent.builder<Country>()
                .data(country)
                .build()

            emit(event)
        }
    }
}
