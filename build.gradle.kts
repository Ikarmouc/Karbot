plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "org.ikarmouc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven{
        url = uri("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("dev.kord:kord-core:0.13.0")
    implementation("dev.kord:kord-voice:0.13.0")
    implementation("dev.kord:kord-core-voice:0.13.0")
    implementation("dev.schlaubi.lavakord:kord:6.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("com.sedmelluq:lavaplayer:1.3.78")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("com.google.code.gson:gson:2.10.1")
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