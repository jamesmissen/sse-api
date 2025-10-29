package io.jamesmissen.sse.api.configuration

import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX

/**
 * A custom Spring configuration for adding a property source containing build-related information.
 *
 * @author James Missen
 *
 * @see BuildProperties
 */
@Configuration(proxyBeanMethods = false)
@PropertySource(
    $$"${spring.info.build.location:$$CLASSPATH_URL_PREFIX/META-INF/build-info.properties}",
    ignoreResourceNotFound = true
)
class BuildPropertiesConfiguration
