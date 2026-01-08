import groovy.lang.Closure
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType


fun properties(key: String): Provider<String> = providers.gradleProperty(key)
fun environment(key: String): Provider<String> = providers.environmentVariable(key)

plugins {
    java
    `maven-publish`
    `java-library`

    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    id("de.undercouch.download") version ("5.6.0")
}

val gitVersion: Closure<String> by extra

val grammarKitGenDir = "src/main/gen"
val rootPackage = "com.lasagnerd.odin"

val rootPackagePath = rootPackage.replace('.', '/')

// Keep these in sync with whatever the oldest IDE version we're targeting in gradle.properties needs
val javaLangVersion: JavaLanguageVersion = JavaLanguageVersion.of(21)
val javaVersion = JavaVersion.VERSION_21

val baseIDE: String = properties("baseIDE").get()
val ideaVersion: String = properties("ideaVersion").get()
val clionVersion: String = properties("clionVersion").get()
val riderVersion: String = properties("riderVersion").get()

val debuggerPlugins = listOf(
    "com.intellij.clion",
    "com.intellij.nativeDebug",
    "com.jetbrains.plugins.webDeployment",
)
val indexViewer = "com.jetbrains.hackathon.indices.viewer:1.30"
val idePerf = "com.google.ide-perf:1.3.2"
val nativeDebuggerSupportPlugin = "com.intellij.nativeDebug:253.29346.138"
val riderPlugins = emptyList<String>()
val coreModules = listOf("intellij.platform.langInjection")

val lsp4jVersion = "0.23.0"

val lsp4ijDep: DependencyHandler.() -> Unit = {
    compileOnlyApi("org.eclipse.lsp4j:org.eclipse.lsp4j:$lsp4jVersion")
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

}

fun pluginVersion(): Provider<String> {
    return provider {
        System.getenv("RELEASE_VERSION")
    }.orElse(properties("pluginVersion"))
}

allprojects {
    apply {
        plugin("org.jetbrains.intellij.platform")
        plugin("org.jetbrains.kotlin.jvm")
    }

    kotlin {
        jvmToolchain(21)
    }

    repositories {
        mavenCentral()
        intellijPlatform {
            localPlatformArtifacts {
                content {
                    includeGroup("bundledPlugin")
                }
            }
            marketplace {
                content {
                    includeGroup("com.jetbrains.plugins")
                    includeGroup("nightly.com.jetbrains.plugins")
                }
            }
            defaultRepositories()
            snapshots()
        }
    }
    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
        if (path !in listOf(":", ":plugin", ":debugger", ":rider")) {
            intellijPlatform {
                intellijIdea(ideaVersion, { useInstaller = false })
            }
        }

    }
    tasks {
        runIde { enabled = false }
        prepareSandbox { enabled = false }
        buildSearchableOptions { enabled = false }
        verifyPlugin { enabled = false }
        buildPlugin { enabled = false }
        signPlugin { enabled = false }
        publishPlugin { enabled = false }
        verifyPluginProjectConfiguration { enabled = false }

        patchPluginXml {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }
    intellijPlatform {
        instrumentCode = false
    }
    if (path in listOf(":core")) {
        sourceSets {
            main {
                java {
                    srcDirs(
                        grammarKitGenDir,
                    )
                }
            }
        }
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(javaLangVersion)
            @Suppress("UnstableApiUsage")
            vendor = JvmVendorSpec.JETBRAINS
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    tasks.withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }

    group = properties("pluginGroup").get()
    version = properties("pluginVersion").get()
}

project(":debugger") {
    dependencies {
        implementation(project(":core"))
        implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.debug:$lsp4jVersion") {
            exclude("org.eclipse.lsp4j", "org.eclipse.lsp4j")
            exclude("com.google.code.gson", "gson")
        }
        intellijPlatform {
            clion(clionVersion, { useInstaller = false })
            for (p in debuggerPlugins) {
                bundledPlugin(p)
            }
        }
    }
}

project(":rider") {
    dependencies {
        implementation(project(":core"))
        implementation(project(":debugger"))
        intellijPlatform {
            rider(riderVersion, { useInstaller = false })
            bundledModule("intellij.rider")
            jetbrainsRuntime()
            for (p in riderPlugins) {
                bundledPlugin(p)
            }
        }
    }
}

project(":core") {
    apply {
        plugin("java-library")
    }
    dependencies {
        lsp4ijDep()
        intellijPlatform {
            testFramework(TestFrameworkType.Platform)
            testImplementation("junit:junit:4.13.2")
            testCompileOnly("org.projectlombok:lombok:1.18.34")
            testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.4.2")
            for (p in coreModules) {
                bundledModule(p)
            }
        }

    }
}

project(":plugin") {
    dependencies {
        implementation(project(":core"))
        implementation(project(":debugger"))
        implementation(project(":rider"))

        intellijPlatform {
            zipSigner()
            pluginVerifier()
            when (baseIDE) {
                "ideaC" -> create(IntelliJPlatformType.IntellijIdeaCommunity, ideaVersion) { useInstaller = false }
                "ideaU" -> create(IntelliJPlatformType.IntellijIdeaUltimate, ideaVersion) { useInstaller = false }
                "clion" -> clion(clionVersion, { useInstaller = false })
                "rider" -> rider(riderVersion, { useInstaller = false })
            }
        }
    }

    intellijPlatform {
        projectName = "odin-intellij"
        pluginConfiguration {
            name = properties("pluginName")
            version = pluginVersion()
        }
        signing {
            certificateChainFile = rootProject.file("secrets/chain.crt")
            privateKeyFile = rootProject.file("secrets/private.pem")
            password = environment("PRIVATE_KEY_PASSWORD")
        }

        publishing {


        }

        pluginVerification {
            ides {
                create(IntelliJPlatformType.IntellijIdea, ideaVersion)
                create(IntelliJPlatformType.CLion, clionVersion)
                create(IntelliJPlatformType.Rider, riderVersion)

            }
        }
    }

    tasks {
        runIde {
            enabled = true
            dependencies {
                intellijPlatform {
                    plugin(idePerf)
                    plugin(indexViewer)
                    if (baseIDE == "ideaU") {
                        plugin(nativeDebuggerSupportPlugin)
                    }
                }
            }
        }

        prepareSandbox {
            enabled = true
        }

        verifyPlugin {
            enabled = true
        }

        verifyPluginProjectConfiguration {
            enabled = true
        }

        signPlugin {
            enabled = true
        }

        verifyPluginSignature {
            dependsOn(signPlugin)
        }

        buildPlugin {
            enabled = true
        }

        publishPlugin {
            enabled = true
            token = providers.gradleProperty("TOKEN")
        }
    }
}

dependencies {
    intellijPlatform {
        when (baseIDE) {
            "ideaC" -> intellijIdea(ideaVersion, configure = {
                useInstaller = false
            })

            "ideaU" -> intellijIdea(ideaVersion, configure = {
                useInstaller = false
            })

            "clion" -> clion(clionVersion, configure = {
                useInstaller = false
            })

            "rider" -> rider(riderVersion, configure = {
                useInstaller = false
            })
        }
    }
}
