plugins {
    kotlin("jvm") version "2.2.10"
    kotlin("plugin.serialization") version "2.3.0"
}

group = "io.github.catizard"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    testImplementation("com.github.Catizard:jbmstable-parser:b03daae20c")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}