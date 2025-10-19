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

You can configure the application using environment variables.

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
