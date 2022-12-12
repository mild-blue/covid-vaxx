plugins {
    kotlin("jvm") version "1.7.20"
    application
    distribution
    id("net.nemerosa.versioning") version "3.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
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
    // compile only detekt plugin
    detektPlugins("io.gitlab.arturbosch.detekt", "detekt-formatting", "1.21.0")

    // extension functions
    implementation("dev.forst:katlib:2.2.2")
    implementation(kotlin("reflect"))

    // Ktor server dependencies
    implementation("io.ktor:ktor-server-core:2.1.3")
    implementation("io.ktor:ktor-server-netty:2.1.3")
    implementation("io.ktor:ktor-server-content-negotiation:2.1.3")
    implementation("io.ktor:ktor-server-cors:2.1.3")
    implementation("io.ktor:ktor-server-default-headers:2.1.3")
    implementation("io.ktor:ktor-server-forwarded-header:2.1.3")
    implementation("io.ktor:ktor-server-status-pages:2.1.3")
    implementation("io.ktor:ktor-server-auth:2.1.3")
    implementation("io.ktor:ktor-server-auth-jwt:2.1.3")
    implementation("io.ktor:ktor-server-call-id:2.1.3")
    implementation("io.ktor:ktor-serialization-jackson:2.1.3")

    // Ktor client dependencies
    implementation("io.ktor:ktor-client-json:2.1.3")
    implementation("io.ktor:ktor-client-apache:2.1.3")
    implementation("io.ktor:ktor-client-logging-jvm:2.1.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.1.3")
    implementation("io.ktor:ktor-client-jackson:2.1.3")


    // ktor swagger
    implementation("dev.forst:ktor-openapi-generator:0.5.2")
    // rate limiting
    implementation("dev.forst:ktor-rate-limiting:2.1.3")
    implementation("dev.forst:ktor-content-security-policy:2.1.3")

    // Jackson JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.0")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.3")
    implementation("ch.qos.logback:logback-classic:1.4.4")
    // if-else in logback.xml
    implementation("org.codehaus.janino:janino:3.1.8")

    // crypto
    implementation("com.lambdaworks:scrypt:1.4.0")

    // DI
    implementation("org.kodein.di:kodein-di-jvm:7.15.0")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:7.15.1")

    // database
    implementation("org.postgresql:postgresql:42.5.0")

    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.40.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.40.1")

    // database migrations from the code
    implementation("org.flywaydb:flyway-core:9.10.0")

    // sending emails
    implementation("com.mailjet:mailjet-client:5.2.1")
    implementation("org.freemarker:freemarker:2.3.31")

    // validation
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.0")

    // tests
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1") // junit testing framework
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1") // generated parameters for tests
    testImplementation("io.ktor:ktor-server-test-host:2.1.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
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
