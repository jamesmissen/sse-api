package com.jamesmissen.sse.api.util

import org.springframework.core.io.ResourceLoader.CLASSPATH_URL_PREFIX

/**
 * An object defining constants used by the application.
 *
 * @author James Missen
 */
object Constants {

    /**
     * A constant for the name of the application.
     *
     * @author James Missen
     */
    const val APPLICATION_NAME = "SSE API"

    /**
     * A constant for the resource folder containing static data files.
     *
     * @author James Missen
     */
    const val DATA_RESOURCE_LOCATION = "${CLASSPATH_URL_PREFIX}data"
}
