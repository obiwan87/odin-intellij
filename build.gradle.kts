import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij.platform") version "2.0.0"
    id("io.freefair.lombok") version "8.6"
}

group = "com.lasagnerd"
version = "0.5.3"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellijPlatform  {

    buildSearchableOptions = true
    instrumentCode = true
    projectName = project.name

    pluginConfiguration {
        version = "0.5.3"

        ideaVersion {
            sinceBuild = "242"
            untilBuild = "242.*"
        }
    }

    publishing {
        val tokenFile = File("certificate/token")
        if (tokenFile.exists()) {
            val myToken = tokenFile.readText()
            token.set(myToken)
        }
    }

    signing {
        certificateChainFile.set(File("certificate/chain.crt"))
        privateKeyFile.set(File("certificate/private.pem"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
}

val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Drobot-server.port=8082",
                "-Dide.mac.message.dialogs.as.sheets=false",
                "-Djb.privacy.policy.text=<!--999.999-->",
                "-Djb.consents.confirmation.enabled=false",
            )
        }
    }

    plugins {
        robotServerPlugin()
    }
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

        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

}
