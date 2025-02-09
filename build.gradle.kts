import groovy.xml.XmlParser
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PatchPluginXmlTask

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    java
    `maven-publish`
    `java-library`

    id("org.jetbrains.kotlin.jvm") version ("2.0.21")
    id("org.jetbrains.intellij.platform") version ("2.1.0")
    id("org.jetbrains.changelog") version ("2.2.1")
    id("org.jetbrains.grammarkit") version ("2022.3.2.2")
    id("de.undercouch.download") version ("5.6.0")
}

val gitVersion: groovy.lang.Closure<String> by extra

val grammarKitGenDir = "src/main/gen"
val rootPackage = "com.lasagnerd.odin"

val rootPackagePath = rootPackage.replace('.', '/')

// Keep these in sync with whatever the oldest IDE version we're targeting in gradle.properties needs
val javaLangVersion: JavaLanguageVersion = JavaLanguageVersion.of(21)
val javaVersion = JavaVersion.VERSION_21

val baseIDE = properties("baseIDE").get()
val ideaVersion = properties("ideaVersion").get()
val clionVersion = properties("clionVersion").get()
val riderVersion = properties("riderVersion").get()

val debuggerPlugins = listOf("com.intellij.cidr.lang", "com.intellij.cidr.base", "com.intellij.nativeDebug", "com.jetbrains.plugins.webDeployment")
val indexViewer = "com.jetbrains.hackathon.indices.viewer:1.29"
val idePerf = "com.google.ide-perf:1.3.2"
val nativeDebuggerSupportPlugin = "com.intellij.nativeDebug:243.21565.129"
val riderPlugins = emptyList<String>()
val corePlugins = listOf("org.intellij.intelliLang")

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

fun pluginVersionFull(): Provider<String> {
    return pluginVersion().map { it + "-" + properties("pluginSinceBuild").get() }
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
            snapshots {
                content {
                    includeModule("com.jetbrains.intellij.clion", "clion")
                    includeModule("com.jetbrains.intellij.idea", "ideaIC")
                    includeModule("com.jetbrains.intellij.idea", "ideaIU")
                    includeModule("com.jetbrains.intellij.rider", "riderRD")
                }
            }
            releases {
                content {
                    includeModule("com.jetbrains.intellij.rider", "riderRD")
                    includeModule("com.jetbrains.intellij.clion", "clion")
                    includeModule("com.jetbrains.intellij.idea", "ideaIC")
                    includeModule("com.jetbrains.intellij.idea", "ideaIU")
                }
            }
        }
    }
    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.34")
        annotationProcessor("org.projectlombok:lombok:1.18.34")
        if (path !in listOf(":", ":plugin", ":debugger", ":rider")) {
            intellijPlatform {
                intellijIdeaCommunity(ideaVersion, useInstaller = false)
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
        verifyPluginProjectConfiguration { enabled = false }

        withType<PatchPluginXmlTask> {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = properties("pluginUntilBuild")
        }
    }
    intellijPlatform {
        instrumentCode = false
    }
    if (path in listOf(":core")) {
        apply {
            plugin("org.jetbrains.grammarkit")
        }
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
            clion(clionVersion, useInstaller = false)
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
            rider(riderVersion, useInstaller = false)
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
            testFramework(TestFrameworkType.Platform, version = "243.23654.166")
            testImplementation("junit:junit:4.13.2")
            testCompileOnly("org.projectlombok:lombok:1.18.34")
            testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.4.2")
            for (p in corePlugins) {
                bundledPlugin(p)
            }
        }
    }
    tasks {
        generateLexer {
            sourceFile = file("src/main/grammar/Odin.flex")
            targetOutputDir = file("${grammarKitGenDir}/lexer/${rootPackagePath}/odin/lexer")
        }

        generateParser {
            sourceFile = file("src/main/grammar/Odin.bnf")
            pathToParser = "${rootPackagePath}/odin/psi/OdinParser.java"
            pathToPsiRoot = "${rootPackagePath}/odin/psi"
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
                "ideaC" -> intellijIdeaCommunity(ideaVersion, useInstaller = false)
                "ideaU" -> intellijIdeaUltimate(ideaVersion, useInstaller = false)
                "clion" -> clion(clionVersion, useInstaller = false)
                "rider" -> rider(riderVersion, useInstaller = false)
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
                ide(IntelliJPlatformType.IntellijIdeaCommunity, ideaVersion)
                ide(IntelliJPlatformType.IntellijIdeaUltimate, ideaVersion)
                ide(IntelliJPlatformType.CLion, clionVersion)
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
            val tokenFile = File("../../secrets/token")
            if (tokenFile.exists()) {
                val myToken = tokenFile.readText()
                token.set(myToken)
                enabled = true
            } else {
                enabled = false
            }
        }
    }
}

dependencies {
    intellijPlatform {
        when (baseIDE) {
            "ideaC" -> intellijIdeaCommunity(ideaVersion, useInstaller = false)
            "ideaU" -> intellijIdeaUltimate(ideaVersion, useInstaller = false)
            "clion" -> clion(clionVersion, useInstaller = false)
            "rider" -> rider(riderVersion, useInstaller = false)
        }
    }
}

tasks {
    generateLexer {
        enabled = false
    }
    generateParser {
        enabled = false
    }
}


fun File.isPluginJar(): Boolean {
    if (!isFile) return false
    if (extension != "jar") return false
    return zipTree(this).files.any { it.isManifestFile() }
}

fun File.isManifestFile(): Boolean {
    if (extension != "xml") return false
    val rootNode = try {
        val parser = XmlParser()
        parser.parse(this)
    } catch (e: Exception) {
        logger.error("Failed to parse $path", e)
        return false
    }
    return rootNode.name() == "idea-plugin"
}
