plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven{
        url = uri("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.kord:kord-core:0.11.1")
    implementation("dev.kord:kord-voice:0.11.1")
    implementation("dev.kord:kord-core-voice:0.11.1")
    implementation("dev.schlaubi.lavakord:kord:6.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("com.sedmelluq:lavaplayer:1.3.78")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}