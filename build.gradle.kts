
plugins {
    kotlin("jvm") version "2.1.21"
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
    implementation("dev.kord:kord-core:0.15.0")
    implementation("dev.kord:kord-voice:0.15.0")
    implementation("dev.kord:kord-core-voice:0.15.0")
    implementation("dev.schlaubi.lavakord:kord:9.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    implementation("com.google.code.gson:gson:2.13.1")
}
tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)

}

application {
    mainClass.set("MainKt")

}