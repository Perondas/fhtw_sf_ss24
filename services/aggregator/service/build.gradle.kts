val ktor_version: String by project
val kotlinx_serialization_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.google.protobuf") version "0.9.4"
    id("io.ktor.plugin") version "2.3.2"
    application
}

group = "at.fhtw"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("at.fhtw.MainKt")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.2"
    }
}

dependencies {
    implementation("org.apache.kafka:kafka-streams:3.7.0")

    implementation("com.google.protobuf:protobuf-java:3.22.2")
    implementation("io.confluent:kafka-protobuf-serializer:7.6.0")
    implementation("io.confluent:kafka-streams-protobuf-serde:7.3.0")

    implementation("ch.qos.logback:logback-classic:1.4.14")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}
