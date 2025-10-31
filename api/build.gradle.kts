plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
}

kotlin {
    jvmToolchain(17)
}

springBoot {
    buildInfo {
        properties {
            if (project.version == "unspecified") excludes.add("version")

            listOf(
                "api.version",
                "api.contact.name",
                "api.contact.email",
                "api.contact.url",
                "api.license.name",
                "api.license.identifier",
                "api.license.url",
                "api.terms-of-service.url",
                "api.external-docs.description",
                "api.external-docs.url"
            ).associateWith { propertyName ->
                providers.gradleProperty(propertyName).orElse(providers.gradleProperty(propertyName.toCamelCase()))
            }.forEach { (propertyName, provider) ->
                if (provider.isPresent) additional.put(propertyName, provider)
            }
        }
    }
}

dependencies {
    implementation(libs.jackson2.module)
    implementation(libs.jackson3.module)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.qos.logback.classic)
    implementation(libs.qos.slf4j.bridge)
    implementation(libs.reactor.kotlin)
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.spring.boot.jackson)
    implementation(libs.spring.boot.reactor)
    implementation(libs.spring.boot.reactor.netty)
    implementation(libs.spring.boot.webflux)
    implementation(libs.springdoc.webflux.api)
    implementation(libs.springdoc.webflux.ui)
    implementation(libs.yaml.snakeyaml)

    developmentOnly(libs.spring.boot.devtools)
}

private fun String.toCamelCase() = this
    .lowercase()
    .split('.', '_', '-')
    .joinToString("") { part -> part.replaceFirstChar(Char::titlecase) }
    .replaceFirstChar(Char::lowercase)
