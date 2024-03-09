import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.openapi.generator") version "7.3.0"
    application
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

val apiRootName = "at.fhtw.geosphere.api.client"
val generatedSourcesPath = "${layout.buildDirectory.get()}/generated"


openApiGenerate {
    generatorName.set("kotlin")
    remoteInputSpec.set("https://validator.swagger.io/validator/openapi.json")
    outputDir.set(generatedSourcesPath)
    apiPackage.set("$apiRootName.api")
    invokerPackage.set("$apiRootName.invoker")
    modelPackage.set("$apiRootName.model")
    skipValidateSpec.set(true)
}

kotlin.sourceSets["main"].kotlin.srcDir("$generatedSourcesPath/src/main/kotlin")

tasks.withType<KotlinCompile>().configureEach {
    dependsOn("openApiGenerate")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.7.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

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