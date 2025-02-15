import com.autonomousapps.tasks.CodeSourceExploderTask
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.Confidence
import org.cthing.projectversion.BuildType
import org.cthing.projectversion.ProjectVersion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

repositories {
    mavenCentral()
}

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    `java-library`
    checkstyle
    jacoco
    `maven-publish`
    signing
    alias(libs.plugins.cthingPublishing)
    alias(libs.plugins.cthingVersioning)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.versions)
    id("org.cthing.locc4j.language")
}

version = ProjectVersion("2.0.1", BuildType.snapshot)
group = "org.cthing"
description = "A Java library for counting lines of source code."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get())
    }
}

dependencies {
    api(libs.jspecify)

    implementation(libs.filevisitor)
    implementation(libs.jacksonCore)
    implementation(libs.jacksonDatabind)

    compileOnly(libs.cthingAnnots)

    testImplementation(libs.assertJ)
    testImplementation(libs.equalsVerifier)
    testImplementation(libs.junitApi)
    testImplementation(libs.junitCommons)
    testImplementation(libs.junitParams)
    testImplementation(libs.mockito)

    testRuntimeOnly(libs.junitEngine)
    testRuntimeOnly(libs.junitLauncher)

    spotbugsPlugins(libs.spotbugsContrib)
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    isIgnoreFailures = false
    configFile = file("dev/checkstyle/checkstyle.xml")
    configDirectory = file("dev/checkstyle")
    isShowViolations = true
}

spotbugs {
    toolVersion = libs.versions.spotbugs
    ignoreFailures = false
    effort = Effort.MAX
    reportLevel = Confidence.MEDIUM
    excludeFilter = file("dev/spotbugs/suppressions.xml")
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
        }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

val generatedSrcDir = file("generated-src")

tasks {
    withType<JavaCompile> {
        options.release = libs.versions.java.get().toInt()
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-options", "-Werror"))
    }

    named("clean", Delete::class).configure {
        // Do not call composite build "clean" until https://github.com/gradle/gradle/issues/23585 is fixed.
        //dependsOn(gradle.includedBuild("languagePlugin").task(":clean"))
        delete(generatedSrcDir)
    }

    withType<Jar> {
        manifest.attributes(mapOf("Implementation-Title" to project.name,
                                  "Implementation-Vendor" to "C Thing Software",
                                  "Implementation-Version" to project.version))
    }

    withType<Javadoc> {
        val year = SimpleDateFormat("yyyy", Locale.ENGLISH).format(Date())
        with(options as StandardJavadocDocletOptions) {
            breakIterator(false)
            encoding("UTF-8")
            bottom("Copyright &copy; $year C Thing Software")
            addStringOption("Xdoclint:all,-missing", "-quiet")
            addStringOption("Werror", "-quiet")
            memberLevel = JavadocMemberLevel.PUBLIC
            outputLevel = JavadocOutputLevel.QUIET
        }
    }

    check {
        dependsOn(buildHealth, gradle.includedBuild("languagePlugin").task(":check"))
    }

    spotbugsMain {
        reports.create("html").required = true
    }

    spotbugsTest {
        isEnabled = false
    }

    withType<CodeSourceExploderTask> {
        dependsOn("generateLanguage")
    }

    withType<JacocoReport> {
        dependsOn("test")
        with(reports) {
            xml.required = false
            csv.required = false
            html.required = true
            html.outputLocation = layout.buildDirectory.dir("reports/jacoco")
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<GenerateModuleMetadata> {
        enabled = false
    }

    dependencyUpdates {
        revision = "release"
        gradleReleaseChannel = "current"
        outputFormatter = "plain,xml,html"
        outputDir = layout.buildDirectory.dir("reports/dependencyUpdates").get().asFile.absolutePath

        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
}

val mainSourceSet: SourceSet = extensions.getByType<JavaPluginExtension>().sourceSets[SourceSet.MAIN_SOURCE_SET_NAME]
val javaDirSet: SourceDirectorySet = mainSourceSet.java
javaDirSet.setSrcDirs(javaDirSet.srcDirs.plus(generatedSrcDir))

val sourceJar by tasks.registering(Jar::class) {
    dependsOn("generateLanguage")
    from(project.sourceSets["main"].allSource)
    archiveClassifier = "sources"
}

val javadocJar by tasks.registering(Jar::class) {
    from(tasks.getByName("javadoc"))
    archiveClassifier = "javadoc"
}

publishing {
    publications {
        register("jar", MavenPublication::class) {
            from(components["java"])

            artifact(sourceJar)
            artifact(javadocJar)

            pom(cthingPublishing.createPomAction())
        }
    }

    val repoUrl = cthingRepo.repoUrl
    if (repoUrl != null) {
        repositories {
            maven {
                name = "CThingMaven"
                setUrl(repoUrl)
                credentials {
                    username = cthingRepo.user
                    password = cthingRepo.password
                }
            }
        }
    }
}

if (cthingPublishing.canSign()) {
    signing {
        sign(publishing.publications["jar"])
    }
}
