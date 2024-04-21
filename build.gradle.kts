plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.16.0"
    id("io.freefair.lombok") version "8.6"
}

group = "com.lasagnerd"
version = "0.3.1"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
}


sourceSets.main.get().java.srcDirs("src/main/gen")
dependencies {
    implementation("org.projectlombok:lombok:1.18.32")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("241.*")
    }

    signPlugin {
        certificateChainFile.set(File("certificate/chain.crt"))
        privateKeyFile.set(File("certificate/private.pem"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        val myToken = File("certificate/token").readText()
        token.set(myToken)
    }
}