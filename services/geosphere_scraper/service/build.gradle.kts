import io.swagger.codegen.v3.DefaultGenerator
import io.swagger.codegen.v3.config.CodegenConfigurator
import io.swagger.v3.parser.OpenAPIV3Parser
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

val ktor_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("com.google.protobuf") version "0.8.+"
    id("io.ktor.plugin") version "2.3.2"
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

kotlin {
    jvmToolchain(21)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.+"
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
    maven(url = "https://packages.confluent.io/maven/")
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

    implementation("com.google.protobuf:protobuf-java:3.19.+")
    implementation("io.confluent:kafka-protobuf-serializer:7.6.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}