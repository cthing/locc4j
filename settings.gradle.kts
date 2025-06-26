rootProject.name = "locc4j"

pluginManagement {
    includeBuild("languagePlugin")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}
