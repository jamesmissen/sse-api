ARG JAVA_VERSION=17

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS dev-builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle

COPY settings.gradle.kts ./
COPY api/build.gradle.kts ./api/

RUN --mount=type=cache,id=gradle,target=/root/.gradle \
    ./gradlew dependencies --no-daemon

COPY api/src ./api/src

RUN --mount=type=cache,id=gradle,target=/root/.gradle \
    ./gradlew clean build -x test --no-daemon

FROM dev-builder AS dev

EXPOSE 8080

ENTRYPOINT ["./gradlew", "bootRun", "--no-daemon"]

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS final-builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle

COPY --from=dev /app/settings.gradle.kts ./
COPY --from=dev /app/api/build.gradle.kts ./api/

COPY api/src ./api/src

RUN --mount=type=cache,id=gradle,target=/root/.gradle \
    ./gradlew clean bootJar -x test --no-daemon
RUN mkdir -p api/build/dependency && (cd api/build/dependency; jar -xf ../libs/api.jar)

FROM eclipse-temurin:${JAVA_VERSION}-jre AS final

COPY --from=final-builder /app/api/build/dependency/BOOT-INF/lib /app/lib
COPY --from=final-builder /app/api/build/dependency/BOOT-INF/classes /app/classes
COPY --from=final-builder /app/api/build/dependency/META-INF /app/META-INF

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app/classes:app/lib/*", "com.jamesmissen.sse.api.MainApplicationKt"]
