pluginManagement {

    val toolsVersion = "0.5.0"

    plugins {
        id("scientifik.mpp") version toolsVersion
        id("scientifik.jvm") version toolsVersion
        id("scientifik.atomic") version toolsVersion
        id("scientifik.publish") version toolsVersion
    }

    repositories {
        mavenLocal()
        jcenter()
        gradlePluginPortal()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        maven("https://dl.bintray.com/mipt-npm/scientifik")
        maven("https://dl.bintray.com/mipt-npm/dev")
        maven("https://dl.bintray.com/kotlin/kotlinx")
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "scientifik.mpp", "scientifik.jvm", "scientifik.publish" -> useModule("scientifik:gradle-tools:$toolsVersion")
            }
        }
    }
}

rootProject.name = "kmath"
include(
    ":kmath-memory",
    ":kmath-core",
    ":kmath-functions",
//    ":kmath-io",
    ":kmath-coroutines",
    ":kmath-histograms",
    ":kmath-commons",
    ":kmath-viktor",
    ":kmath-koma",
    ":kmath-prob",
    ":kmath-io",
    ":kmath-dimensions",
    ":kmath-for-real",
    ":kmath-geometry",
    ":examples"
)
