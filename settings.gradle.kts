pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "CCTRCloudExportDesktop"
include("modules:cctr-api")
include("modules:web-driver-api")
findProject("modules:web-driver-api")?.name = "web-driver-api"
include("modules:util")
findProject("modules:util")?.name = "util"
