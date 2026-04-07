import java.net.URL

plugins {
    java
    idea
    application
    id("com.gradleup.shadow") version "8.3.0"
    id("org.graalvm.buildtools.native") version "0.11.1"
    id("com.google.osdetector") version "1.7.3"
}

println("Gradle ${gradle.gradleVersion} with Java ${org.gradle.internal.jvm.Jvm.current()}")
group = "cx.ctt.skom"
val skomMainClass = "cx.ctt.skom.Main"

dependencies {
    // https://repo1.maven.org/maven2/org/reflections/reflections/maven-metadata.xml
    implementation("org.reflections:reflections:0.10.2")
    // https://repo1.maven.org/maven2/org/slf4j/slf4j-api/maven-metadata.xml
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    // https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/maven-metadata.xml
    implementation("ch.qos.logback:logback-classic:1.5.32")

    // https://repo1.maven.org/maven2/io/valkey/valkey-glide/maven-metadata.xml
    implementation("io.valkey:valkey-glide:2.3.1+:${osdetector.classifier}")
    // https://repo1.maven.org/maven2/redis/clients/jedis/maven-metadata.xml
    implementation("redis.clients:jedis:7.4.0")

    // https://repo1.maven.org/maven2/net/minestom/minestom/maven-metadata.xml
    implementation("net.minestom:minestom:2026.03.25-1.21.11")
    // https://repo.smolder.fr/#/public/fr/ghostrider584/axiom-minestom
    implementation("fr.ghostrider584:axiom-minestom:0.0.4")

    // TODO: get them merged
    // https://github.com/couleurm/MinestomMechanics -> https://github.com/Term4/MinestomMechanics
    implementation("com.github.couleurm:MinestomMechanics:-SNAPSHOT")
    // https://github.com/couleurm/Minestom173 -> https://github.com/emortaldev/Minestom173
	implementation("com.github.couleurm:Minestom173:-SNAPSHOT")
}



java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}


tasks {
    jar {
        manifest {
            attributes["Main-Class"] = skomMainClass
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // prevent the -all suffix on the shadowjar file.
    }

    named("generateResourcesConfigFile") {
        dependsOn(shadowJar)
    }
}
graalvmNative {
    binaries {
        named("main") {
            imageName.set("marathon")
            mainClass.set(mainClass)

//            buildArgs.add("-march=native")
            buildArgs.add("-Os")
            buildArgs.add("-march=x86-64-v3")
            quickBuild.set(true)
            buildArgs.add("--enable-url-protocols=https")
            buildArgs.add("--gc=G1")
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(25))
            })

            verbose.set(true)
            fallback.set(false)
            quickBuild.set(true)
        }

        all {
            resources.autodetect()
        }
    }
}

tasks.register("printClasspath") {
    println(configurations.runtimeClasspath)
}

val version = "1.0.1"
val assetName = "ViaProxyAuthHook-Agent-$version.jar"
val downloadUrl = "https://github.com/ViaVersionAddons/ViaProxyAuthHook/releases/download/v$version/$assetName"
val agentJarFile = File(projectDir, assetName)

tasks.register<Copy>("downloadViaProxyAuthHookAgent") {
    if (!agentJarFile.exists()) {
        logger.lifecycle("Downloading $downloadUrl")
        URL(downloadUrl).openStream().use { input ->
            agentJarFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        logger.lifecycle("Downloaded Java agent to $agentJarFile")
    }
}
tasks.named<JavaExec>("run") {
    mainClass = skomMainClass
    dependsOn("downloadViaProxyAuthHookAgent")
    jvmArgs("-javaagent:${agentJarFile.absolutePath}")
}

application {
    applicationDefaultJvmArgs = listOf("-javaagent:${agentJarFile.absolutePath}")
    mainClass = skomMainClass
}
