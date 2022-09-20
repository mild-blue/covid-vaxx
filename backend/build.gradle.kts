plugins {
    kotlin("jvm") version "1.7.10"
    application
    distribution
    id("net.nemerosa.versioning") version "2.14.0"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
}

group = "blue.mild.covid.vaxx"
version = versioning.info?.tag ?: versioning.info?.lastTag ?: "development"

val mClass = "blue.mild.covid.vaxx.MainKt"

buildscript {
    repositories {
        mavenCentral()
    }
}

detekt {
    config = files("detekt.yml")
    parallel = true
}

application {
    mainClass.set(mClass)
}

repositories {
    mavenCentral()
}

dependencies {
    // extension functions
    implementation("dev.forst", "katlib", "2.2.1")
    implementation(kotlin("reflect"))

    // Ktor server dependencies
    val ktorVersion = "2.1.1"
    implementation("io.ktor", "ktor-server-core", ktorVersion)
    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor", "ktor-server-content-negotiation", ktorVersion)
    implementation("io.ktor", "ktor-server-cors", ktorVersion)
    implementation("io.ktor", "ktor-server-default-headers", ktorVersion)
    implementation("io.ktor", "ktor-server-forwarded-header", ktorVersion)
    implementation("io.ktor", "ktor-server-status-pages", ktorVersion)
    implementation("io.ktor", "ktor-server-auth", ktorVersion)
    implementation("io.ktor", "ktor-server-auth-jwt", ktorVersion)
    implementation("io.ktor", "ktor-server-call-id", ktorVersion)
    implementation("io.ktor", "ktor-serialization-jackson", ktorVersion)

    // Ktor client dependencies
    implementation("io.ktor", "ktor-client-json", ktorVersion)
    implementation("io.ktor", "ktor-client-apache", ktorVersion)
    implementation("io.ktor", "ktor-client-logging-jvm", ktorVersion)
    implementation("io.ktor", "ktor-client-content-negotiation", ktorVersion)
    implementation("io.ktor", "ktor-client-jackson", ktorVersion)


    // ktor swagger
    implementation("dev.forst", "ktor-openapi-generator", "0.5.0")
    // rate limiting
    implementation("dev.forst", "ktor-rate-limiting", ktorVersion)
    implementation("dev.forst", "ktor-content-security-policy", "2.1.1-1")

    // Jackson JSON
    val jacksonVersion = "2.13.4"
    implementation("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)
    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", jacksonVersion)

    // logging
    implementation("io.github.microutils", "kotlin-logging-jvm", "3.0.0")
    implementation("ch.qos.logback", "logback-classic", "1.3.1")
    // if-else in logback.xml
    implementation("org.codehaus.janino", "janino", "3.1.3")

    // crypto
    implementation("com.lambdaworks", "scrypt", "1.4.0")

    // DI
    val kodeinVersion = "7.14.0"
    implementation("org.kodein.di", "kodein-di-jvm", kodeinVersion)
    implementation("org.kodein.di", "kodein-di-framework-ktor-server-jvm", kodeinVersion)

    // database
    implementation("org.postgresql", "postgresql", "42.3.1")

    val exposedVersion = "0.39.2"
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)

    // database migrations from the code
    implementation("org.flywaydb", "flyway-core", "8.2.2")

    // sending emails
    implementation("com.mailjet", "mailjet-client", "5.1.1")
    implementation("org.freemarker", "freemarker", "2.3.31")

    // validation
    implementation("com.googlecode.libphonenumber", "libphonenumber", "8.12.39")

    // tests
    testImplementation(kotlin("test"))
    testImplementation("io.mockk", "mockk", "1.12.1")

    val junitVersion = "5.9.0"
    testImplementation("org.junit.jupiter", "junit-jupiter-api", junitVersion) // junit testing framework
    testImplementation("org.junit.jupiter", "junit-jupiter-params", junitVersion) // generated parameters for tests
    testImplementation("io.ktor", "ktor-server-test-host", ktorVersion)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", junitVersion)
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    distTar {
        archiveFileName.set("app.tar")
    }

    withType<Test> {
        useJUnitPlatform()
    }

    register<Jar>("fatJar") {
        manifest {
            attributes["Main-Class"] = mClass
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("app.jar")
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        from(sourceSets.main.get().output)
    }

    register("resolveDependencies") {
        doLast {
            project.allprojects.forEach { subProject ->
                with(subProject) {
                    buildscript.configurations.forEach { if (it.isCanBeResolved) it.resolve() }
                    configurations.compileClasspath.get().resolve()
                    configurations.testCompileClasspath.get().resolve()
                }
            }
        }
    }
}
