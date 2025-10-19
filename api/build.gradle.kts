plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.jackson.module)
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
