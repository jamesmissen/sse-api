package com.jamesmissen.sse.api.controller

import com.jamesmissen.sse.api.model.Country
import com.jamesmissen.sse.api.util.Constants.DATA_RESOURCE_LOCATION
import com.jamesmissen.sse.api.util.extensions.readValue
import org.springframework.core.io.ResourceLoader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.json.JsonMapper

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

    /**
     * The list of all [Country] items.
     *
     * @author James Missen
     */
    private final val countryData: List<Country> by lazy {
        jsonMapper.readValue(resources.getResource("$DATA_RESOURCE_LOCATION/countries.json"))
    }
}
