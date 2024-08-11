import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij.platform") version "2.0.0"
//    id("org.jetbrains.intellij.platform.migration") version "2.0.0"
    id("io.freefair.lombok") version "8.6"
}

group = "com.lasagnerd"
version = "0.5.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellijPlatform  {

    buildSearchableOptions = true
    instrumentCode = true
    projectName = project.name
//    sandboxContainer = "..."

    pluginConfiguration {
        version = "2024.2"

        ideaVersion {
            sinceBuild = "242"
            untilBuild = "242.*"
        }
    }

    publishing {
        val myToken = File("certificate/token").readText()
        token.set(myToken)
    }

    signing {
        certificateChainFile.set(File("certificate/chain.crt"))
        privateKeyFile.set(File("certificate/private.pem"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
//    type.set("IC") // Target IDE Platform

}


sourceSets.main.get().java.srcDirs("src/main/gen")
dependencies {
    implementation("org.projectlombok:lombok:1.18.34")
    intellijPlatform {
        intellijIdeaCommunity("2024.2")
        pluginVerifier()
        zipSigner()
        instrumentationTools()

        testFramework(TestFrameworkType.Platform)

        testImplementation("junit:junit:4.13.2")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.4.0")
        testCompileOnly ("org.junit.jupiter:junit-jupiter-api:5.4.2")
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
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

//    signPlugin {
//        certificateChainFile.set(File("certificate/chain.crt"))
//        privateKeyFile.set(File("certificate/private.pem"))
//        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
//    }
//
//    publishPlugin {
//        val myToken = File("certificate/token").readText()
//        token.set(myToken)
//    }
}