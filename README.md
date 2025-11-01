# Server-Sent Events API

A reactive **Kotlin** and **Spring Boot** application providing an API for showcasing **Server-Sent Events (SSE)**, with
**OpenAPI** and **Swagger UI** documentation included.

Built using **Spring WebFlux** and **Reactor** for non-blocking and asynchronous performance, and **SpringDoc** for API
documentation.

## 🚀 Features

- **Reactive stack** – powered by Spring WebFlux and Reactor
- **Kotlin-first** – concise, idiomatic Kotlin design utilising coroutines and suspense
- **OpenAPI documentation** – auto-generated using SpringDoc
- **Swagger UI** – interactive API docs with data schemas

## 📘 OpenAPI Documentation

The OpenAPI documentation generated for this application is fully compatible with **version 3.2** of OpenAPI
Specification (released in [September 2025](https://github.com/OAI/OpenAPI-Specification/releases/tag/3.2.0)).

This version of the specification adds support for the
[`text/event-stream`](https://html.spec.whatwg.org/multipage/iana.html#text/event-stream) media type (used for SSE) and
other sequential media types. In particular, it uses a new
[`itemSchema`](https://spec.openapis.org/oas/v3.2.0.html#fixed-fields-11) field to model the schema of each event's
[`data`](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#data) field.

_**Note:** Although the docs are compatible with version 3.2 of the specification, the JSON doc still lists the version
as 3.1. This is for compatability purposes with other software, such as Swagger UI._

## 🏗️ Build and Run

#### Option 1: Using Gradle

_Prerequisites:_

- Java 17+

_Commands:_

```
./gradlew bootRun
```

#### Option 2: Using Docker

_Prerequisites:_

- Docker

_Commands:_

```
docker build -t sse-api .
docker run -p 8080:8080 sse-api
```

#### Application

The application will be available at `localhost` on port `8080`.

- To explore the Swagger UI – <http://localhost:8080/docs>
- To view the OpenAPI doc – <http://localhost:8080/openapi.json>

## ⚙️ Configuration

#### Runtime Configuration

You can configure how the application runs using environment variables.

| Variable                 | Description                        | Default         |
|--------------------------|------------------------------------|-----------------|
| `APP_BASE_PATH`          | The base URL path for the API.     | `/`             |
| `APP_API_DOCS_ENABLED`   | Whether the API docs are enabled.  | `true`          |
| `APP_API_DOCS_PATH`      | The URL path for the API docs.     | `/openapi.json` |
| `APP_SWAGGER_UI_ENABLED` | Whether the Swagger UI is enabled. | `true`          |
| `APP_SWAGGER_UI_PATH`    | The URL path for the Swagger UI.   | `/docs`         |

You can set environment variables before running the application commands. For example:

```
export APP_BASE_PATH=/api

./gradlew bootRun
```

If using Docker, you can use flags with the `run` command. For example:

```
docker build -t sse-api .
docker run \
  -p 8080:8080 \
  -e APP_API_DOCS_PATH=/docs/api.json \
  -e APP_SWAGGER_UI_ENABLED=false \
  sse-api
```

#### Build Configuration

You can configure how the application builds using Gradle properties.

| Property                        | Description                                                                                 |
|---------------------------------|---------------------------------------------------------------------------------------------|
| `api.version`                   | The version of the API docs*.                                                               |
| `api.contact.name`              | The name of the contact person or organisation for the API.                                 |
| `api.contact.email`             | The email address of the contact person or organisation for the API.                        |
| `api.contact.url`               | The URL for the contact information of the API.                                             |
| `api.license.name`              | The name of the license used for the API.                                                   |
| `api.license.identifier`        | The [SPDX expression](https://spdx.org/licenses) representing the license used for the API. |
| `api.license.url`               | The URL for the license information of the API.                                             |
| `api.terms-of-service.url`      | The URL for the terms of service of the API.                                                |
| `api.external-docs.description` | The description of the target documentation of the API.                                     |
| `api.external-docs.url`         | The URL for the target documentation of the API.                                            |

_*This is the version of the OpenAPI document. It is not (or does not need to be) the same as `version` property for the
Gradle project, nor the version of the API itself, nor the version of the OpenAPI specification._

You can set Gradle properties by adding to [`gradle.properties`](gradle.properties). For example:

```
# other properties...

api.license.name=MIT License
api.license.identifier=MIT
```

You can also set Gradle properties using flags with the `gradlew` command. For example:

```
./gradlew bootRun -Papi.version=0.1.0
```

You can also set Gradle properties using environment variables prefixed with `ORG_GRADLE_PROJECT_`. In this case,
property names must be specified using camel case. For example:

```
export ORG_GRADLE_PROJECT_apiVersion=0.1.0
export ORG_GRADLE_PROJECT_apiTermsOfServiceUrl=https://example.com/terms

./gradlew bootRun
```

## 🧩 Dependencies

Key dependencies include:

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Reactor](https://projectreactor.io/)
- [Netty](https://netty.io/)
- [SpringDoc](https://springdoc.org/)
- [Jackson](https://github.com/FasterXML/jackson)

To view all dependencies and versions, see [`build.gradle.kts`](api/build.gradle.kts) and
[`libs.versions.toml`](gradle/libs.versions.toml).

## 📄 License

This software is released under the [MIT License](LICENSE).

You are free to use, modify, and distribute it under the same terms.

## 👤 Acknowledgements

#### Contributors

- James Missen ([@jamesmissen](https://github.com/jamesmissen))
