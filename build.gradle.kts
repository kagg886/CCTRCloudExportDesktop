import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.cli.jvm.main

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}
val ktorVersion = extra["ktor.version"] as String
val exposedVersion = extra["exposed.version"] as String

group = "top.kagg886.cctr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}
dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation(kotlin("test"))
    implementation("com.itextpdf:itext7-core:8.0.4")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("moe.tlaster:precompose:1.6.0")
    implementation("moe.tlaster:precompose-viewmodel:1.6.0")

    implementation(project("modules:cctr-api"))
    implementation(project("modules:web-driver-api"))
    implementation(project("modules:util"))

    implementation("io.ktor:ktor-client-core:$ktorVersion")

    implementation("org.xerial:sqlite-jdbc:3.45.3.0")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
}

kotlin {
    jvmToolchain(11)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "top.kagg886.cctr.desktop.MainKt"
        )
    }
}

compose.desktop {
    application {
        mainClass = "top.kagg886.cctr.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.AppImage)
            packageName = "CCTRCloudExportDesktop"
            packageVersion = "1.0.0"
            includeAllModules = true
            buildTypes {
                release {
                    proguard {
                        isEnabled = false
                    }
                }
            }
        }
    }
}

