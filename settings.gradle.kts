rootProject.name = "locc4j"

pluginManagement {
    includeBuild("languagePlugin")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}
