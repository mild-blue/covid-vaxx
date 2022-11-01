FROM node:lts-slim as frontend-build

# see https://github.com/webpack/webpack/issues/14532
ENV NODE_OPTIONS=--openssl-legacy-provider

COPY frontend/ ./frontend
WORKDIR ./frontend
RUN npm i
RUN npm run build-prod

FROM eclipse-temurin:17-jdk AS backend-build

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
FROM eclipse-temurin:17-jre as runtime
LABEL description="Mild Blue Covid Vaxx"
LABEL project="mild-blue:covid-vaxx"

# prepare logging
ENV FILE_LOG_PATH=/var/logs/mildblue
RUN mkdir -p $FILE_LOG_PATH
ENV PRODUCTION_LOGGING=true
ENV ENABLE_FILE_LOG=true

ENV APP_ROOT /app
WORKDIR $APP_ROOT

# Copy frontend
ENV FRONTEND_PATH=/app/frontend
COPY --from=frontend-build ./frontend/dist/frontend $FRONTEND_PATH

# Copy backend
COPY --from=backend-build /src/build/distributions/app.tar $APP_ROOT/

# Extract executables
RUN mkdir $APP_ROOT/run
RUN tar -xvf app.tar --strip-components=1 -C $APP_ROOT/run

# create version file
ARG release_version=development
ENV RELEASE_FILE_PATH=$APP_ROOT/run/release.txt
RUN echo $release_version > $RELEASE_FILE_PATH

# setup env
ENV PORT=8080

EXPOSE $PORT
ENTRYPOINT ["/bin/sh", "-c", "/app/run/bin/covid-vaxx"]
