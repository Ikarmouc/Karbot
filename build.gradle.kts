plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.kord:kord-core:0.11.1")
    implementation("dev.kord:kord-voice:0.11.1")
    implementation("dev.kord:kord-core-voice:0.11.1")
    implementation("dev.schlaubi.lavakord:kord:5.1.7")
    implementation("org.slf4j:slf4j-simple:2.0.9")

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