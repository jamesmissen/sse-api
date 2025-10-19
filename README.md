# Server-Sent Events API

A reactive **Kotlin** and **Spring Boot** application providing an API for showcasing **Server-Sent Events (SSE)**.

Built using **Spring WebFlux** and **Reactor** for non-blocking and asynchronous performance.

## 🚀 Features

- **Reactive stack** – powered by Spring WebFlux and Reactor
- **Kotlin-first** – concise, idiomatic Kotlin design utilising coroutines and suspense

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

## ⚙️ Configuration

You can configure the application using environment variables.

| Variable        | Description                    | Default |
|-----------------|--------------------------------|---------|
| `APP_BASE_PATH` | The base URL path for the API. | `/`     |

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
  -e APP_BASE_PATH=/api \
  sse-api
```

## 🧩 Dependencies

Key dependencies include:

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Reactor](https://projectreactor.io/)
- [Netty](https://netty.io/)

To view all dependencies and versions, see [`build.gradle.kts`](api/build.gradle.kts) and
[`libs.versions.toml`](gradle/libs.versions.toml).

## 📄 License

This software is released under the [MIT License](LICENSE).

You are free to use, modify, and distribute it under the same terms.

## 👤 Acknowledgements

#### Contributors

- James Missen ([@jamesmissen](https://github.com/jamesmissen))
