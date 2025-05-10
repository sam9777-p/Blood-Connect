import java.util.Properties
import java.io.FileInputStream

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Load GitHub credentials
        val githubProperties = Properties().apply {
            load(FileInputStream(file("github.properties")))
        }

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Cuberto/liquid-swipe-android")
            credentials {
                username = githubProperties["gpr.usr"] as String? ?: System.getenv("GPR_USER")
                password = githubProperties["gpr.key"] as String? ?: System.getenv("GPR_API_KEY")
            }
        }
    }
}

rootProject.name = "blood"
include(":app")
