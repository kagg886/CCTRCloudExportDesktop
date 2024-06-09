plugins {
    kotlin("jvm") version "1.9.22"
}

val ktor_version = extra["ktor.version"] as String

group = "top.kagg886.cctr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api("org.seleniumhq.selenium:selenium-edge-driver:4.20.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("io.ktor:ktor-client-core:$ktor_version")

    implementation(project(":modules:util"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}