import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import java.text.SimpleDateFormat
import java.util.*

repositories {
    mavenCentral()
}

plugins {
    java
    checkstyle
    `java-gradle-plugin`
    alias(libs.plugins.spotbugs)
}

gradlePlugin {
    plugins {
        create("languagePlugin") {
            id = "org.cthing.locc4j.language"
            implementationClass = "org.cthing.locc4j.plugins.LanguagePlugin"
        }
    }
}

dependencies {
    implementation(libs.commonsText)
    implementation(libs.cthingAnnots)
    implementation(libs.jsr305)
    implementation(libs.freemarker)
    implementation(libs.jacksonDatabind)

    spotbugsPlugins(libs.spotbugsContrib)

    modules {
        module("com.google.collections:google-collections") {
            replacedBy("com.google.guava:guava", "google-collections is now part of Guava")
        }
    }
}

val checkstyleDevDir: File = file("../dev/checkstyle")
val spotbugsDevDir: File = file("../dev/spotbugs")

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    isIgnoreFailures = false
    configFile = File(checkstyleDevDir, "checkstyle.xml")
    configDirectory = checkstyleDevDir.absoluteFile
    isShowViolations = true
}

spotbugs {
    toolVersion = libs.versions.spotbugs
    ignoreFailures = false
    effort = Effort.MAX
    reportLevel = Confidence.MEDIUM
    excludeFilter = File(spotbugsDevDir, "suppressions.xml")
}

tasks {
    withType<JavaCompile> {
        options.release = libs.versions.java.get().toInt()
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-options", "-Werror"))
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
            addStringOption("Werror", "-quiet")
            memberLevel = JavadocMemberLevel.PUBLIC
            outputLevel = JavadocOutputLevel.QUIET
        }
    }

    spotbugsMain {
        reports.create("html").required = true
    }

    spotbugsTest {
        isEnabled = false
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
}
