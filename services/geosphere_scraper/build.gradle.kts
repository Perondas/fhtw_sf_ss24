import io.swagger.codegen.v3.DefaultGenerator
import io.swagger.codegen.v3.config.CodegenConfigurator
import io.swagger.v3.parser.OpenAPIV3Parser

val ktor_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    application
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.swagger.codegen.v3:swagger-codegen-maven-plugin:3.0.54")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "21"
    }
}

group = "at.fhtw"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("at.fhtw.MainKt")
}

val apiPackage   = "com.message.api"
val modelPackage = "com.message.model"
val ignoreFile = "${rootDir}/.swagger-codegen-ignore"
val swaggerFile  = "${rootDir}/src/main/resources/messenger-service.yml"
val generatedOutput = "$projectDir/build/"

tasks.create("generateServer") {
    doLast {
        val config = CodegenConfigurator()
        config.lang = "kotlin-client"
        config.apiPackage = "at.fhwt.api"
        config.modelPackage = "at.fhwt.model"
        config.inputSpecURL = "https://dataset.api.hub.geosphere.at/v1/openapi.json"
        config.outputDir = "${layout.buildDirectory.get()}/generated"
        config.invokerPackage = "at.fhwt.invoker"

        val something = config.toClientOptInput()
        something.openAPI(OpenAPIV3Parser().read("https://dataset.api.hub.geosphere.at/v1/openapi.json"))

        DefaultGenerator().opts(something).generate()
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.7.0")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}