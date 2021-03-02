FROM adoptopenjdk/openjdk11:alpine AS build
LABEL description="Mild Blue Covid Vaxx"
LABEL project="mild-blue:covid-vaxx"

ENV PROJECT_ROOT /src
WORKDIR $PROJECT_ROOT

# Copy gradle settings
COPY backend/build.gradle.kts backend/settings.gradle.kts backend/gradle.properties backend/gradlew $PROJECT_ROOT/
# Make sure gradlew is executable
RUN chmod +x gradlew
# Copy gradle specification
COPY backend/gradle $PROJECT_ROOT/gradle
# Download gradle
RUN ./gradlew --version
# download and cache dependencies
RUN ./gradlew resolveDependencies --no-daemon

# Copy project and build
COPY backend/ $PROJECT_ROOT
RUN ./gradlew distTar --no-daemon

# Runtime
FROM adoptopenjdk/openjdk11:alpine-jre

ENV APP_ROOT /app
WORKDIR $APP_ROOT

# Obtain built from the base
COPY --from=build /src/build/distributions/app.tar $APP_ROOT/

# Extract executables
RUN mkdir $APP_ROOT/run
RUN tar -xvf app.tar --strip-components=1 -C $APP_ROOT/run

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=$APP_ROOT/run/release.txt
RUN echo $release_version > $RELEASE_FILE_PATH

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "/app/run/bin/covid-vaxx"]
