import org.jetbrains.intellij.platform.gradle.TestFrameworkType

val pluginVersion = "0.5.4"

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.0.1"
    id("io.freefair.lombok") version "8.6"
}

group = "com.lasagnerd"
version = pluginVersion

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellijPlatform  {

    buildSearchableOptions = true
    instrumentCode = true
    projectName = project.name

    pluginConfiguration {
        version = pluginVersion

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
        clion("2024.2")
        pluginVerifier()
        zipSigner()
        instrumentationTools()

        plugin("com.intellij.nativeDebug:242.21829.3")

        testFramework(TestFrameworkType.Platform)

        testImplementation("junit:junit:4.13.2")
        testCompileOnly ("org.junit.jupiter:junit-jupiter-api:5.4.2")
    }
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.debug:0.21.1") {
        exclude("org.eclipse.lsp4j", "org.eclipse.lsp4j")
        exclude("org.eclipse.lsp4j", "org.eclipse.lsp4j.jsonrpc")
        exclude("com.google.code.gson", "gson")
    }
}

kotlin {
    jvmToolchain(21)
}