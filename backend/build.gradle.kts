import java.net.URI


plugins {
    kotlin("jvm") version "1.5.10"
    application
    distribution
    id("net.nemerosa.versioning") version "2.14.0"
    id("io.gitlab.arturbosch.detekt") version "1.17.1"
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
    maven {
        // for swagger
        url = URI.create("https://jitpack.io")
    }
}

dependencies {
    // extension functions
    implementation("pw.forst", "katlib", "2.0.1")
    implementation("org.jetbrains.kotlin", "kotlin-reflect", "1.5.10")

    // Ktor server dependencies
    val ktorVersion = "1.6.0"
    implementation("io.ktor", "ktor-server-core", ktorVersion)
    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor", "ktor-jackson", ktorVersion)
    implementation("io.ktor", "ktor-auth", ktorVersion)
    implementation("io.ktor", "ktor-auth-jwt", ktorVersion)

    // these two are here because OpenAPI generator needs then
    // however, we don't use them directly
    implementation("io.ktor", "ktor-metrics", ktorVersion)
    implementation("io.ktor", "ktor-server-sessions", ktorVersion)

    // ktor swagger
    implementation("com.github.papsign", "Ktor-OpenAPI-Generator", "0.2-beta.18") {
        // exclude obsolete libraries from the generator
        exclude("io.ktor", "ktor-metrics")
        exclude("io.ktor", "ktor-server-sessions")
    }

    // Ktor client dependencies
    implementation("io.ktor", "ktor-client-json", ktorVersion)
    implementation("io.ktor", "ktor-client-jackson", ktorVersion)
    implementation("io.ktor", "ktor-client-apache", ktorVersion)
    implementation("io.ktor", "ktor-client-logging-jvm", ktorVersion)

    // Jackson JSON
    val jacksonVersion = "2.12.3"
    implementation("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", jacksonVersion)
    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", jacksonVersion)

    // logging
    implementation("io.github.microutils", "kotlin-logging", "2.0.8")
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    // if-else in logback.xml
    implementation("org.codehaus.janino", "janino", "3.1.3")

    // crypto
    implementation("com.lambdaworks", "scrypt", "1.4.0")

    // DI
    val kodeinVersion = "7.6.0"
    implementation("org.kodein.di", "kodein-di-jvm", kodeinVersion)
    implementation("org.kodein.di", "kodein-di-framework-ktor-server-jvm", kodeinVersion)

    // database
    implementation("org.postgresql", "postgresql", "42.2.20")

    val exposedVersion = "0.32.1"
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)

    // database migrations from the code
    implementation("org.flywaydb", "flyway-core", "7.10.0")

    // sending emails
    implementation("com.mailjet", "mailjet-client", "5.1.1")
    implementation("org.freemarker", "freemarker", "2.3.31")

    // validation
    implementation("com.googlecode.libphonenumber", "libphonenumber", "8.12.25")

    // tests
    testImplementation("io.mockk", "mockk", "1.11.0")

    val junitVersion = "5.7.2"
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
