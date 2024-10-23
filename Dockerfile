#
# Download dependencies
#

FROM eclipse-temurin:23-jdk-alpine AS deps

WORKDIR /build

COPY --chmod=0755 gradlew gradlew
COPY settings.gradle /build/settings.gradle
COPY app/build.gradle /build/app/build.gradle
COPY gradle /build/gradle

RUN ./gradlew build --no-daemon

#
# Build fat jar
#

FROM deps AS package

WORKDIR /build

COPY ./app/src /build/app/src/
RUN ./gradlew fatJar

#
# Final container
#

FROM eclipse-temurin:23-jre-alpine AS final

WORKDIR /app

# Create a non-privileged user that the app will run under
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

COPY --from=package /build/app/build/libs/fat-app.jar /app/app.jar

ENTRYPOINT [ "java", "-cp", "app.jar", "com.nicolauscg.reminder.discord.bot.App"]